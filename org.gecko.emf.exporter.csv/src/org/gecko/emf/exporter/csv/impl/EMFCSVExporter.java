/**
 * Copyright (c) 2012 - 2023 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.exporter.csv.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.emf.ecore.EObject;
import org.gecko.emf.exporter.AbstractEMFExporter;
import org.gecko.emf.exporter.EMFExportException;
import org.gecko.emf.exporter.EMFExportOptions;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.exporter.annotation.ProvideEMFExporter;
import org.gecko.emf.exporter.cells.EMFExportEObjectIDValueCell;
import org.gecko.emf.exporter.cells.EMFExportEObjectManyReferencesValueCell;
import org.gecko.emf.exporter.cells.EMFExportEObjectOneReferenceValueCell;
import org.gecko.emf.exporter.cells.EMFExportEObjectReferenceValueCell;
import org.gecko.emf.exporter.csv.api.EMFCSVExportMode;
import org.gecko.emf.exporter.csv.api.EMFCSVExportOptions;
import org.gecko.emf.exporter.csv.api.EMFCSVExporterConstants;
import org.gecko.emf.exporter.headers.EMFExportEObjectColumnHeader;
import org.gecko.emf.exporter.headers.EMFExportEObjectManyReferencesColumnHeader;
import org.gecko.emf.exporter.headers.EMFExportEObjectOneReferenceColumnHeader;
import org.gecko.emf.exporter.headers.EMFExportEObjectReferenceColumnHeader;
import org.gecko.emf.exporter.keys.EMFExportColumnHasValueCompositeKey;
import org.gecko.emf.exporter.keys.EMFExportColumnRefsMaxValueCountCompositeKey;
import org.gecko.emf.exporter.keys.EMFExportRefHasValueCompositeKey;
import org.gecko.emf.exporter.keys.EMFExportRefMatrixNameIDCompositeKey;
import org.gecko.emf.exporter.keys.EMFExportRefsMaxValueCountCompositeKey;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Stopwatch;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.siegmar.fastcsv.writer.CsvWriter;

/**
 * Implementation of the {@link EMFExporter} to provide support for exporting
 * EMF resources and lists of EMF objects to CSV format.
 * 
 * @author Michal H. Siemaszko
 */
@Component(name = EMFCSVExporterConstants.EMF_EXPORTER_NAME, scope = ServiceScope.PROTOTYPE)
@ProvideEMFExporter(name = EMFCSVExporterConstants.EMF_EXPORTER_NAME)
public class EMFCSVExporter extends AbstractEMFExporter implements EMFExporter {
	private static final Logger LOG = LoggerFactory.getLogger(EMFCSVExporter.class);

	private static final String CSV_FILE_EXTENSION = "csv";

	private static final String REF_COLUMN_SUFFIX_PART = REF_COLUMN_SUFFIX.replaceFirst(".", "");

	public EMFCSVExporter() {
		super(LOG, Stopwatch.createStarted());
	}

	/*
	 * Depending on export mode used (FLAT, ZIP), 'java.io.OutputStream' passed is
	 * either a file or a ZIP archive to which files are collected.
	 * 
	 * (non-Javadoc)
	 * @see org.gecko.emf.exporter.EMFExporter#exportEObjectsTo(java.util.List, java.io.OutputStream, java.util.Map)
	 */
	@Override
	public void exportEObjectsTo(List<EObject> eObjects, OutputStream outputStream, Map<?, ?> options)
			throws EMFExportException {
		Objects.requireNonNull(eObjects, "At least one EObject is required for export!");
		Objects.requireNonNull(outputStream, "Output stream is required for export!");

		if (!eObjects.isEmpty()) {

			try {

				final Map<Object, Object> exportOptions = validateExportOptions(options);

				LookupIndexesDTO lookupIndexesDTO = new LookupIndexesDTO();

				LOG.info("Starting export of {} EObject(s) to CSV format"
						+ (!exportOptions.isEmpty() ? " with options" : ""), eObjects.size());
				LOG.info("  Export mode: " + (flatExportMode(exportOptions) ? "flat"
						: (zipExportMode(exportOptions) ? "zip" : "unknown")));
				LOG.info("  Locale to use: {}", locale(exportOptions)); // TODO: remove if not needed
				LOG.info("  Export non-containment references: {}", exportNonContainmentEnabled(exportOptions));
				LOG.info("  Export metadata: {}", exportMetadataEnabled(exportOptions));
				LOG.info("  Add mapping table: {}", addMappingTableEnabled(exportOptions));
				LOG.info("  Show URIs instead of IDs (where applicable): {}", showURIsEnabled(exportOptions));
				LOG.info("  Show columns containing references: {}", showREFsEnabled(exportOptions));

				exportMatricesToCSV(outputStream, eObjects, lookupIndexesDTO, exportOptions);

			} catch (Exception e) {
				throw new EMFExportException(e);
			}
		}
	}

	private void exportMatricesToCSV(OutputStream outputStream, List<EObject> eObjects,
			LookupIndexesDTO lookupIndexesDTO, Map<Object, Object> exportOptions)
			throws IOException, EMFExportException {

		if (flatExportMode(exportOptions)) {
			exportMatricesToCSVInFlatMode(outputStream, eObjects, lookupIndexesDTO, exportOptions);

		} else if (zipExportMode(exportOptions)) {
			exportMatricesToCSVInZipMode(outputStream, eObjects, lookupIndexesDTO, exportOptions);
		}
	}

	private void exportMatricesToCSVInZipMode(OutputStream outputStream, List<EObject> eObjects,
			LookupIndexesDTO lookupIndexesDTO, Map<Object, Object> exportOptions) throws EMFExportException {

		ProcessedEObjectsDTO processedEObjectsDTO = exportEObjectsToMatrices(eObjects, exportOptions);

		resetStopwatch();

		LOG.info("Starting generation of CSV files in ZIP mode");

		try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
			for (String matrixName : processedEObjectsDTO.matrixNameToMatrixMap.keySet()) {
				LOG.debug("Generating CSV file for matrix named '{}'", matrixName);

				Table<Integer, Integer, Object> matrix = processedEObjectsDTO.matrixNameToMatrixMap.get(matrixName);

				exportMatrixToCSVInZipMode(zipOutputStream, matrixName, matrix, exportOptions);
			}
		} catch (IOException e) {
			throw new EMFExportException(e);
		}

		LOG.info("Finished generation of CSV files in ZIP mode in {} second(s)", elapsedTimeInSeconds());
	}

	private void exportMatrixToCSVInZipMode(ZipOutputStream zipOutputStream, String matrixName,
			Table<Integer, Integer, Object> matrix, Map<Object, Object> exportOptions) throws IOException {
		final StringWriter csvStringWriter = new StringWriter();

		try (CsvWriter csvWriter = CsvWriter.builder().build(csvStringWriter)) {

			writeCSVHeader(matrix, csvWriter);

			writeCSVData(matrix, csvWriter, exportOptions);

			writeZipEntry(zipOutputStream, matrixName, csvStringWriter);
		}
	}

	private void exportMatricesToCSVInFlatMode(OutputStream outputStream, List<EObject> eObjects,
			LookupIndexesDTO lookupIndexesDTO, Map<Object, Object> exportOptions)
			throws IOException, EMFExportException {

		// validate if objects from list passed share the same hierarchy - this is only
		// necessary for flat export mode, there are no such restrictions for "regular"
		// mode
		validateClassHierarchyForRootObjects(eObjects);

		ProcessedEObjectsDTO processedEObjectsDTO = exportEObjectsToMatrices(eObjects, exportOptions);

		resetStopwatch();

		LOG.info("Starting generation of CSV files in flat mode");

		Map<String, Table<Integer, Integer, Object>> eObjectMatrixNameToMatrixMap = eObjectMatricesOnly(
				processedEObjectsDTO.matrixNameToMatrixMap);

		Table<Integer, Integer, Object> flatMatrix = HashBasedTable.create();

		// skip matrices whose structural features are unpacked when constructing one
		// reference / many references column headers (e.g. "Filter_Id" vs
		// "filtering.Id") to avoid duplication of information presented
		Set<String> nonRefMatrixNames = nonRefMatrixNames(eObjectMatrixNameToMatrixMap);

		constructFlatMatrixColumnHeaders(eObjectMatrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				exportOptions, nonRefMatrixNames, flatMatrix);

		validateMatrixColumnsSize(flatMatrix, "flat matrix");

		populateFlatMatrixWithData(eObjectMatrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				nonRefMatrixNames, exportOptions, flatMatrix);

		try (PrintWriter printWriterOutputStream = new PrintWriter(outputStream)) {

			try (CsvWriter csvWriter = CsvWriter.builder().build(printWriterOutputStream)) {

				writeCSVHeader(flatMatrix, csvWriter);

				writeCSVData(flatMatrix, csvWriter, exportOptions);
			}
		}

		LOG.info("Finished generation of CSV files in flat mode in {} second(s)", elapsedTimeInSeconds());
	}

	private void constructFlatMatrixColumnHeaders(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Set<String> nonRefMatrixNames,
			Table<Integer, Integer, Object> flatMatrix) throws EMFExportException {
		AtomicInteger flatMatrixColumnKey = new AtomicInteger(1);

		for (String nonRefMatrixName : nonRefMatrixNames) {
			constructFlatMatrixColumnHeaders(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
					exportOptions, flatMatrix, flatMatrixColumnKey, nonRefMatrixName);
		}
	}

	private void constructFlatMatrixColumnHeaders(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, String nonRefMatrixName) throws EMFExportException {
		Table<Integer, Integer, Object> nonRefMatrix = matrixNameToMatrixMap.get(nonRefMatrixName);

		Map<Integer, Object> firstNonRefMatrixRow = nonRefMatrix.row(getMatrixRowKey(1));

		for (Map.Entry<Integer, Object> firstNonRefMatrixRowColumn : firstNonRefMatrixRow.entrySet()) {

			// @formatter:off
			constructFlatMatrixColumnHeaders(
					matrixNameToMatrixMap,
					processedEObjectsDTO, 
					lookupIndexesDTO,
					exportOptions,
					flatMatrix, 
					flatMatrixColumnKey,
					nonRefMatrix,
					nonRefMatrixName,
					(EMFExportEObjectColumnHeader) firstNonRefMatrixRowColumn.getValue(),
					firstNonRefMatrixRowColumn.getKey(),
					firstNonRefMatrixRowColumn.getValue());
			// @formatter:on
		}
	}

	private void constructFlatMatrixColumnHeaders(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, Table<Integer, Integer, Object> matrix, String matrixName,
			EMFExportEObjectColumnHeader nonRefMatrixColumnHeader, Integer matrixRowColumnKey, Object rawColumnHeader,
			String... columnHeaderNameParts) throws EMFExportException {

		if ((rawColumnHeader instanceof EMFExportEObjectOneReferenceColumnHeader)
				&& matrixNameToMatrixMap
						.containsKey(((EMFExportEObjectOneReferenceColumnHeader) rawColumnHeader).getRefMatrixName())
				&& !matrixName.equalsIgnoreCase(
						((EMFExportEObjectOneReferenceColumnHeader) rawColumnHeader).getRefMatrixName())) {

			// @formatter:off
			constructFlatMatrixOneReferenceColumnHeaders(
					matrixNameToMatrixMap,
					processedEObjectsDTO, 
					lookupIndexesDTO,
					exportOptions,
					flatMatrix, 
					flatMatrixColumnKey,
					nonRefMatrixColumnHeader, 
					rawColumnHeader, 
					columnHeaderNameParts);
			// @formatter:on

		} else if ((rawColumnHeader instanceof EMFExportEObjectManyReferencesColumnHeader)
				&& matrixNameToMatrixMap
						.containsKey(((EMFExportEObjectManyReferencesColumnHeader) rawColumnHeader).getRefMatrixName())
				&& !matrixName.equalsIgnoreCase(
						((EMFExportEObjectManyReferencesColumnHeader) rawColumnHeader).getRefMatrixName())) {

			// @formatter:off
			constructFlatMatrixManyReferencesColumnHeaders(
					matrixNameToMatrixMap,
					processedEObjectsDTO, 
					lookupIndexesDTO,
					exportOptions,
					flatMatrix, 
					flatMatrixColumnKey,
					matrix, 
					nonRefMatrixColumnHeader, 
					matrixRowColumnKey, 
					rawColumnHeader, 
					columnHeaderNameParts);
			// @formatter:on

		} else if ((rawColumnHeader instanceof EMFExportEObjectManyReferencesColumnHeader)
				&& ((!exportNonContainmentEnabled(exportOptions) && !matrixNameToMatrixMap
						.containsKey(((EMFExportEObjectManyReferencesColumnHeader) rawColumnHeader).getRefMatrixName()))
						|| matrixName.equalsIgnoreCase(
								((EMFExportEObjectManyReferencesColumnHeader) rawColumnHeader).getRefMatrixName()))) {

			// @formatter:off
			constructNonContainmentDisabledOrSelfReferencingModelFlatMatrixColumnHeader(
					matrixNameToMatrixMap,
					processedEObjectsDTO,
					lookupIndexesDTO,
					exportOptions,
					flatMatrix, 
					flatMatrixColumnKey,
					rawColumnHeader);
			// @formatter:on

		} else {

			// @formatter:off
			constructFlatMatrixColumnHeader(
					matrixNameToMatrixMap,
					processedEObjectsDTO, 
					lookupIndexesDTO,
					flatMatrix, 
					flatMatrixColumnKey,
					nonRefMatrixColumnHeader, 
					rawColumnHeader);
			// @formatter:on
		}
	}

	private void constructNonContainmentDisabledOrSelfReferencingModelFlatMatrixColumnHeader(
			Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, Object rawColumnHeader, String... columnHeaderNameParts)
			throws EMFExportException {

		int columnRefsMaxValueCount = findColumnRefsMaxValueCount(matrixNameToMatrixMap, processedEObjectsDTO,
				lookupIndexesDTO, (EMFExportEObjectColumnHeader) rawColumnHeader);

		if (columnRefsMaxValueCount > 0) {

			EMFExportEObjectManyReferencesColumnHeader manyReferencesColumnHeader = (EMFExportEObjectManyReferencesColumnHeader) rawColumnHeader;

			String columnHeaderName = manyReferencesColumnHeader.getColumnHeaderName();

			for (int colIndex = 0; colIndex < columnRefsMaxValueCount; colIndex++) {

				// @formatter:off
				constructFlatMatrixColumnHeader(
						flatMatrix,
						constructFlatMatrixReferenceColumnHeaderName(
								constructFlatMatrixReferenceColumnHeaderNameParts(
										Arrays.asList(columnHeaderNameParts),
										columnHeaderName,
										String.valueOf(colIndex))
								), 	
						flatMatrixColumnKey);
				// @formatter:on
			}
		}
	}

	private void constructFlatMatrixColumnHeader(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Table<Integer, Integer, Object> flatMatrix, AtomicInteger flatMatrixColumnKey,
			EMFExportEObjectColumnHeader nonRefMatrixColumnHeader, Object rawColumnHeader) throws EMFExportException {

		boolean hasValue = hasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				nonRefMatrixColumnHeader, (EMFExportEObjectColumnHeader) rawColumnHeader);

		if (hasValue) {
			// @formatter:off
			constructFlatMatrixColumnHeader(
					flatMatrix,
					constructFlatMatrixColumnHeaderName( (EMFExportEObjectColumnHeader) rawColumnHeader ), 
					flatMatrixColumnKey);
			// @formatter:on
		}
	}

	private void constructFlatMatrixOneReferenceColumnHeaders(
			Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, EMFExportEObjectColumnHeader nonRefMatrixColumnHeader,
			Object rawColumnHeader, String... columnHeaderNameParts) throws EMFExportException {

		boolean refHasValue = refHasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				nonRefMatrixColumnHeader, rawColumnHeader);

		if (refHasValue) {

			EMFExportEObjectOneReferenceColumnHeader oneReferenceColumnHeader = (EMFExportEObjectOneReferenceColumnHeader) rawColumnHeader;

			String columnHeaderName = oneReferenceColumnHeader.getColumnHeaderName().replaceFirst(REF_COLUMN_SUFFIX,
					"");

			String refMatrixName = oneReferenceColumnHeader.getRefMatrixName();

			Table<Integer, Integer, Object> refMatrix = matrixNameToMatrixMap.get(refMatrixName);

			Map<Integer, Object> firstRefMatrixRow = refMatrix.row(getMatrixRowKey(1));

			for (Map.Entry<Integer, Object> firstRefMatrixRowColumn : firstRefMatrixRow.entrySet()) {

				Object firstRefMatrixRowColumnValue = firstRefMatrixRowColumn.getValue();

				// this check is needed to avoid infinite loops in case of self-referencing
				// models
				if (firstRefMatrixRowColumnValue instanceof EMFExportEObjectReferenceColumnHeader && !refMatrixName
						.equalsIgnoreCase(((EMFExportEObjectReferenceColumnHeader) firstRefMatrixRowColumnValue)
								.getRefMatrixName())) {

					// @formatter:off
					constructFlatMatrixColumnHeaders(
							matrixNameToMatrixMap,
							processedEObjectsDTO, 
							lookupIndexesDTO,
							exportOptions,
							flatMatrix, 
							flatMatrixColumnKey, 
							refMatrix,
							refMatrixName,
							nonRefMatrixColumnHeader,
							firstRefMatrixRowColumn.getKey(),
							firstRefMatrixRowColumn.getValue(), 
							columnHeaderName);
					// @formatter:on

				} else if ((firstRefMatrixRowColumnValue instanceof EMFExportEObjectManyReferencesColumnHeader)
						&& ((!exportNonContainmentEnabled(exportOptions) && !matrixNameToMatrixMap
								.containsKey(((EMFExportEObjectManyReferencesColumnHeader) firstRefMatrixRowColumnValue)
										.getRefMatrixName()))
								|| refMatrixName.equalsIgnoreCase(
										((EMFExportEObjectManyReferencesColumnHeader) firstRefMatrixRowColumnValue)
												.getRefMatrixName()))) {

					// @formatter:off
					constructNonContainmentDisabledOrSelfReferencingModelFlatMatrixColumnHeader(
							matrixNameToMatrixMap,
							processedEObjectsDTO,
							lookupIndexesDTO,
							exportOptions,
							flatMatrix, 
							flatMatrixColumnKey,
							firstRefMatrixRowColumnValue, 
							normalizeFlatMatrixReferenceColumnHeaderName(
									(EMFExportEObjectColumnHeader) rawColumnHeader));
					// @formatter:on

				} else {

					boolean refFeatureHasValue = refHasValue(matrixNameToMatrixMap, processedEObjectsDTO,
							lookupIndexesDTO, oneReferenceColumnHeader,
							(EMFExportEObjectColumnHeader) firstRefMatrixRowColumnValue);

					if (refFeatureHasValue) {

						// @formatter:off
						constructFlatMatrixColumnHeader(flatMatrix,
								constructFlatMatrixReferenceColumnHeaderName(
										constructFlatMatrixReferenceColumnHeaderNameParts(Arrays.asList(columnHeaderNameParts), 
												columnHeaderName, ((EMFExportEObjectColumnHeader) firstRefMatrixRowColumnValue).getColumnHeaderName())),
								flatMatrixColumnKey);
						// @formatter:on
					}
				}
			}
		}
	}

	private void constructFlatMatrixManyReferencesColumnHeaders(
			Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, Table<Integer, Integer, Object> matrix,
			EMFExportEObjectColumnHeader nonRefMatrixColumnHeader, Integer matrixRowColumnKey, Object rawColumnHeader,
			String... columnHeaderNameParts) throws EMFExportException {

		int refsMaxValueCount = findRefsMaxValueCount(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				matrix, nonRefMatrixColumnHeader, matrixRowColumnKey, rawColumnHeader);

		if (refsMaxValueCount > 0) {

			EMFExportEObjectManyReferencesColumnHeader manyReferencesColumnHeader = (EMFExportEObjectManyReferencesColumnHeader) rawColumnHeader;

			String columnHeaderName = manyReferencesColumnHeader.getColumnHeaderName().replaceFirst(REF_COLUMN_SUFFIX,
					"");

			String refMatrixName = manyReferencesColumnHeader.getRefMatrixName();

			Table<Integer, Integer, Object> refMatrix = matrixNameToMatrixMap.get(refMatrixName);

			for (int colIndex = 0; colIndex < refsMaxValueCount; colIndex++) {

				Map<Integer, Object> firstRefMatrixRow = refMatrix.row(getMatrixRowKey(1));

				for (Map.Entry<Integer, Object> firstRefMatrixRowColumn : firstRefMatrixRow.entrySet()) {

					Object firstRefMatrixRowColumnValue = firstRefMatrixRowColumn.getValue();

					// this check is needed to avoid infinite loops in case of self-referencing
					// models
					if (firstRefMatrixRowColumnValue instanceof EMFExportEObjectReferenceColumnHeader && !refMatrixName
							.equalsIgnoreCase(((EMFExportEObjectReferenceColumnHeader) firstRefMatrixRowColumnValue)
									.getRefMatrixName())) {

						// @formatter:off
						constructFlatMatrixColumnHeaders(
								matrixNameToMatrixMap,
								processedEObjectsDTO, 
								lookupIndexesDTO,
								exportOptions,
								flatMatrix, 
								flatMatrixColumnKey, 
								refMatrix,
								refMatrixName,
								nonRefMatrixColumnHeader,
								firstRefMatrixRowColumn.getKey(),
								firstRefMatrixRowColumn.getValue(), 
								constructFlatMatrixReferenceColumnHeaderName(
										constructFlatMatrixReferenceColumnHeaderNameParts(
												Arrays.asList(columnHeaderNameParts), columnHeaderName,
												String.valueOf(colIndex))));
						// @formatter:on

					} else if ((firstRefMatrixRowColumnValue instanceof EMFExportEObjectManyReferencesColumnHeader)
							&& ((!exportNonContainmentEnabled(exportOptions) && !matrixNameToMatrixMap.containsKey(
									((EMFExportEObjectManyReferencesColumnHeader) firstRefMatrixRowColumnValue)
											.getRefMatrixName()))
									|| refMatrixName.equalsIgnoreCase(
											((EMFExportEObjectManyReferencesColumnHeader) firstRefMatrixRowColumnValue)
													.getRefMatrixName()))) {

						// @formatter:off
						constructNonContainmentDisabledOrSelfReferencingModelFlatMatrixColumnHeader(
								matrixNameToMatrixMap,
								processedEObjectsDTO,
								lookupIndexesDTO,
								exportOptions,
								flatMatrix, 
								flatMatrixColumnKey,
								firstRefMatrixRowColumnValue, 
								constructFlatMatrixReferenceColumnHeaderName(
										constructFlatMatrixReferenceColumnHeaderNameParts(
												Arrays.asList(columnHeaderNameParts), columnHeaderName,
												String.valueOf(colIndex))));
						// @formatter:on						

					} else {

						boolean refFeatureHasValue = refHasValue(matrixNameToMatrixMap, processedEObjectsDTO,
								lookupIndexesDTO, manyReferencesColumnHeader,
								(EMFExportEObjectColumnHeader) firstRefMatrixRowColumnValue);

						if (refFeatureHasValue) {

							// @formatter:off
							constructFlatMatrixColumnHeader(
									flatMatrix,
									constructFlatMatrixReferenceColumnHeaderName(
											constructFlatMatrixReferenceColumnHeaderNameParts(
													Arrays.asList(columnHeaderNameParts), columnHeaderName,
													String.valueOf(colIndex),
													normalizeFlatMatrixReferenceColumnHeaderName(
															(EMFExportEObjectColumnHeader) firstRefMatrixRowColumnValue))
											), 	
									flatMatrixColumnKey);
							// @formatter:on
						}
					}
				}
			}
		}
	}

	private void constructFlatMatrixColumnHeader(Table<Integer, Integer, Object> flatMatrix, String columnHeaderName,
			AtomicInteger flatMatrixColumnKey) {

		LOG.debug("Constructing column header name '{}'", columnHeaderName);

		flatMatrix.put(getMatrixRowKey(1), getMatrixColumnKey(flatMatrixColumnKey.getAndIncrement()), columnHeaderName);
	}

	private String constructFlatMatrixColumnHeaderName(EMFExportEObjectColumnHeader columnHeader) {
		return columnHeader.getColumnHeaderName();
	}

	private String constructFlatMatrixReferenceColumnHeaderName(String... columnHeaderNameParts) {
		boolean hasRefColumnHeaderNamePart = hasRefColumnHeaderNamePart(columnHeaderNameParts);
		if (hasRefColumnHeaderNamePart) {
			columnHeaderNameParts = swapRefColumnHeaderNamePart(columnHeaderNameParts);
		}

		return String.join(".", columnHeaderNameParts);
	}

	private String[] swapRefColumnHeaderNamePart(String... columnHeaderNameParts) {
		List<String> parts = Arrays.stream(columnHeaderNameParts)
				.map(columnHeaderNamePart -> columnHeaderNamePart.replace(REF_COLUMN_SUFFIX, ""))
				.collect(Collectors.toList());
		parts.add(REF_COLUMN_SUFFIX_PART);
		return parts.toArray(String[]::new);
	}

	private boolean hasRefColumnHeaderNamePart(String... columnHeaderNameParts) {
		return Arrays.stream(columnHeaderNameParts)
				.anyMatch(columnHeaderNamePart -> columnHeaderNamePart.endsWith(REF_COLUMN_SUFFIX));
	}

	private String[] constructFlatMatrixReferenceColumnHeaderNameParts(List<String> existingParts, String... newParts) {
		List<String> parts = new ArrayList<>();
		parts.addAll(existingParts);
		parts.addAll(Arrays.asList(newParts));
		return parts.toArray(new String[parts.size()]);
	}

	private String normalizeFlatMatrixReferenceColumnHeaderName(EMFExportEObjectColumnHeader columnHeader) {
		// for self-referencing models, we leave the 'ref_' suffix
		if (columnHeader instanceof EMFExportEObjectReferenceColumnHeader
				&& ((EMFExportEObjectReferenceColumnHeader) columnHeader).getMatrixName()
						.equalsIgnoreCase(((EMFExportEObjectReferenceColumnHeader) columnHeader).getRefMatrixName())) {
			return ((EMFExportEObjectColumnHeader) columnHeader).getColumnHeaderName();
		} else {
			return ((EMFExportEObjectColumnHeader) columnHeader).getColumnHeaderName().replaceFirst(REF_COLUMN_SUFFIX,
					"");
		}
	}

	private void populateFlatMatrixWithData(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO, Set<String> nonRefMatrixNames,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix) throws EMFExportException {

		// in flat mode, we only process non-ref matrices
		for (String nonRefMatrixName : nonRefMatrixNames) {
			populateFlatMatrixWithData(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO, exportOptions,
					flatMatrix, nonRefMatrixName);
		}
	}

	private void populateFlatMatrixWithData(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix, String nonRefMatrixName)
			throws EMFExportException {

		Table<Integer, Integer, Object> nonRefMatrix = matrixNameToMatrixMap.get(nonRefMatrixName);

		Map<Integer, Map<Integer, Object>> nonRefMatrixRowMap = nonRefMatrix.rowMap();

		// @formatter:off
		List<Integer> remainingNonRefMatrixRowNumbers = nonRefMatrixRowMap.keySet()
				.stream()
				.skip(1)
				.collect(Collectors.toList());
		// @formatter:on

		for (Integer nonRefMatrixRowNumber : remainingNonRefMatrixRowNumbers) {
			Map<Integer, Object> nonRefMatrixRow = nonRefMatrixRowMap.get(nonRefMatrixRowNumber);

			AtomicInteger flatMatrixColumnKey = new AtomicInteger(1);

			for (Map.Entry<Integer, Object> nonRefMatrixRowColumn : nonRefMatrixRow.entrySet()) {

				Object rawNonRefMatrixColumnHeader = nonRefMatrix.get(getMatrixRowKey(1),
						getMatrixColumnKey(nonRefMatrixRowColumn.getKey()));

				// @formatter:off
				populateFlatMatrixWithData(
						matrixNameToMatrixMap,
						processedEObjectsDTO, 
						lookupIndexesDTO,
						exportOptions,
						flatMatrix, 
						flatMatrixColumnKey, 
						nonRefMatrix, 
						nonRefMatrixName,
						nonRefMatrixRowNumber, 
						(EMFExportEObjectColumnHeader) rawNonRefMatrixColumnHeader,
						null,
						nonRefMatrixRowColumn.getKey(),
						nonRefMatrixRowColumn.getValue());
				// @formatter:on
			}
		}
	}

	private void populateFlatMatrixWithData(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, Table<Integer, Integer, Object> matrix, String matrixName,
			Integer matrixRowNumber, EMFExportEObjectColumnHeader matrixColumnHeader,
			EMFExportEObjectColumnHeader refMatrixColumnHeader, Integer matrixRowColumnKey,
			Object rawMatrixRowColumnValue) throws EMFExportException {

		if ((rawMatrixRowColumnValue instanceof EMFExportEObjectOneReferenceValueCell)
				&& matrixNameToMatrixMap.containsKey(
						((EMFExportEObjectOneReferenceValueCell) rawMatrixRowColumnValue).getRefMatrixName())
				&& !matrixName.equalsIgnoreCase(
						((EMFExportEObjectOneReferenceValueCell) rawMatrixRowColumnValue).getRefMatrixName())) {

			// @formatter:off
			populateFlatMatrixOneReferenceColumnWithData(
					matrixNameToMatrixMap,
					processedEObjectsDTO, 
					lookupIndexesDTO,
					exportOptions,
					flatMatrix, 
					flatMatrixColumnKey, 
					matrix,
					matrixRowNumber, 
					matrixColumnHeader, 
					refMatrixColumnHeader,
					matrixRowColumnKey, 
					rawMatrixRowColumnValue);
			// @formatter:on

		} else if ((rawMatrixRowColumnValue instanceof EMFExportEObjectManyReferencesValueCell)
				&& matrixNameToMatrixMap.containsKey(
						((EMFExportEObjectManyReferencesValueCell) rawMatrixRowColumnValue).getRefMatrixName())
				&& !matrixName.equalsIgnoreCase(
						((EMFExportEObjectManyReferencesValueCell) rawMatrixRowColumnValue).getRefMatrixName())) {

			// @formatter:off
			populateFlatMatrixManyReferencesColumnWithData(
					matrixNameToMatrixMap,
					processedEObjectsDTO, 
					lookupIndexesDTO,
					exportOptions,
					flatMatrix, 
					flatMatrixColumnKey,
					matrix, 
					matrixRowNumber, 
					matrixColumnHeader, 
					refMatrixColumnHeader,
					matrixRowColumnKey, 
					rawMatrixRowColumnValue);
			// @formatter:on

		} else if ((rawMatrixRowColumnValue instanceof EMFExportEObjectManyReferencesValueCell)
				&& ((!exportNonContainmentEnabled(exportOptions) && !matrixNameToMatrixMap.containsKey(
						((EMFExportEObjectManyReferencesValueCell) rawMatrixRowColumnValue).getRefMatrixName()))
						|| matrixName
								.equalsIgnoreCase(((EMFExportEObjectManyReferencesValueCell) rawMatrixRowColumnValue)
										.getRefMatrixName()))) {

			// @formatter:off
			populateNonContainmentDisabledOrSelfReferencingModelFlatMatrixColumnWithData(
					matrixNameToMatrixMap,
					processedEObjectsDTO, 
					lookupIndexesDTO,
					exportOptions,
					flatMatrix,
					flatMatrixColumnKey, 
					matrixRowNumber, 
					(refMatrixColumnHeader != null) ? refMatrixColumnHeader: matrixColumnHeader,
					rawMatrixRowColumnValue);
			// @formatter:on			

		} else {

			// @formatter:off
			populateFlatMatrixColumnWithData(
					matrixNameToMatrixMap,
					processedEObjectsDTO, 
					lookupIndexesDTO,
					exportOptions,
					flatMatrix, 
					flatMatrixColumnKey,
					matrixRowNumber, 
					matrixColumnHeader, 
					refMatrixColumnHeader, 
					rawMatrixRowColumnValue);
			// @formatter:on
		}
	}

	private void populateNonContainmentDisabledOrSelfReferencingModelFlatMatrixColumnWithData(
			Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, Integer matrixRowNumber, EMFExportEObjectColumnHeader matrixColumnHeader,
			Object rawMatrixRowColumnValue) throws EMFExportException {

		int columnRefsMaxValueCount = findColumnRefsMaxValueCount(matrixNameToMatrixMap, processedEObjectsDTO,
				lookupIndexesDTO, matrixColumnHeader);

		if (columnRefsMaxValueCount > 0) {

			EMFExportEObjectManyReferencesValueCell manyReferencesValueCell = (EMFExportEObjectManyReferencesValueCell) rawMatrixRowColumnValue;

			List<String> manyReferencesValueCellValues = (showURIsEnabled(exportOptions)
					&& !manyReferencesValueCell.isSelfReferencingModel() && manyReferencesValueCell.hasURIs())
							? manyReferencesValueCell.getURIs()
							: manyReferencesValueCell.hasRefIDs() ? manyReferencesValueCell.getRefIDs()
									: Collections.emptyList();

			for (int colIndex = 0; colIndex < columnRefsMaxValueCount; colIndex++) {

				if (manyReferencesValueCellValues.size() > colIndex) {
					populateFlatMatrixColumnWithData(exportOptions, flatMatrix, flatMatrixColumnKey, matrixRowNumber,
							manyReferencesValueCellValues.get(colIndex));
				} else {
					populateFlatMatrixColumnWithData(exportOptions, flatMatrix, flatMatrixColumnKey, matrixRowNumber,
							Optional.empty());
				}
			}
		}
	}

	private void populateFlatMatrixColumnWithData(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, Integer matrixRowNumber, EMFExportEObjectColumnHeader matrixColumnHeader,
			EMFExportEObjectColumnHeader refMatrixColumnHeader, Object rawMatrixRowColumnValue)
			throws EMFExportException {

		boolean hasValue = hasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO, matrixColumnHeader,
				refMatrixColumnHeader);

		if (hasValue) {
			populateFlatMatrixColumnWithData(exportOptions, flatMatrix, flatMatrixColumnKey, matrixRowNumber,
					rawMatrixRowColumnValue);
		}
	}

	private void populateFlatMatrixColumnWithData(Map<Object, Object> exportOptions,
			Table<Integer, Integer, Object> flatMatrix, AtomicInteger flatMatrixColumnKey, Integer rowKey,
			Object value) {

		if ((value == null) || (value instanceof Optional)) {
			LOG.debug("Inserting EMPTY value cell at row '{}' and column '{}'", rowKey,
					flatMatrix.get(getMatrixRowKey(1), getMatrixColumnKey(flatMatrixColumnKey.get())));

			flatMatrix.put(rowKey, getMatrixColumnKey(flatMatrixColumnKey.getAndIncrement()), Optional.empty());

		} else {

			LOG.debug("Inserting value cell at row '{}' and column '{}'", rowKey,
					flatMatrix.get(getMatrixRowKey(1), getMatrixColumnKey(flatMatrixColumnKey.get())));

			flatMatrix.put(rowKey, getMatrixColumnKey(flatMatrixColumnKey.getAndIncrement()), value);
		}
	}

	private void populateFlatMatrixOneReferenceColumnWithData(
			Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, Table<Integer, Integer, Object> matrix, Integer matrixRowNumber,
			EMFExportEObjectColumnHeader matrixColumnHeader, EMFExportEObjectColumnHeader refMatrixColumnHeader,
			Integer matrixRowColumnKey, Object rawMatrixRowColumnValue) throws EMFExportException {

		String refMatrixName = ((EMFExportEObjectOneReferenceValueCell) rawMatrixRowColumnValue).getRefMatrixName();

		Table<Integer, Integer, Object> refMatrix = matrixNameToMatrixMap.get(refMatrixName);

		if (((EMFExportEObjectOneReferenceValueCell) rawMatrixRowColumnValue).hasRefID()) {

			String refID = ((EMFExportEObjectOneReferenceValueCell) rawMatrixRowColumnValue).getRefID();

			int refMatrixRowKey = findRefMatrixRowKey(processedEObjectsDTO, refMatrixName, refID);

			if (refMatrixRowKey != -1) {

				Map<Integer, Object> refMatrixRow = refMatrix.row(getMatrixRowKey(refMatrixRowKey));

				for (Map.Entry<Integer, Object> refMatrixRowColumn : refMatrixRow.entrySet()) {

					// @formatter:off
					populateFlatMatrixWithData(
							matrixNameToMatrixMap,
							processedEObjectsDTO,
							lookupIndexesDTO,
							exportOptions,
							flatMatrix,
							flatMatrixColumnKey, 
							refMatrix,
							refMatrixName,
							matrixRowNumber, 
							(refMatrixColumnHeader != null) ? refMatrixColumnHeader : matrixColumnHeader,
							(EMFExportEObjectColumnHeader) refMatrix.get(getMatrixRowKey(1), getMatrixColumnKey(refMatrixRowColumn.getKey())),
							refMatrixRowColumn.getKey(),
							refMatrixRowColumn.getValue());
					// @formatter:on
				}
			}

			// even if there is no value for that particular ref, column may already be
			// present if other rows have value in that column, so at least empty cell(s)
			// must be created
		} else {

			boolean refHasValue = refHasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
					matrixColumnHeader, refMatrixColumnHeader);

			if (refHasValue) {

				for (Map.Entry<Integer, Object> firstRefMatrixRowColumn : refMatrix.row(getMatrixRowKey(1))
						.entrySet()) {

					// @formatter:off
					boolean refFeatureHasValue = refHasValue(
							matrixNameToMatrixMap,
							processedEObjectsDTO,
							lookupIndexesDTO,
							matrix,
							matrixRowColumnKey,
							refMatrix,
							firstRefMatrixRowColumn.getKey());
					// @formatter:on

					if (refFeatureHasValue) {
						populateFlatMatrixColumnWithData(exportOptions, flatMatrix, flatMatrixColumnKey,
								matrixRowNumber, Optional.empty());
					}
				}
			}
		}
	}

	private void populateFlatMatrixManyReferencesColumnWithData(
			Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Map<Object, Object> exportOptions, Table<Integer, Integer, Object> flatMatrix,
			AtomicInteger flatMatrixColumnKey, Table<Integer, Integer, Object> matrix, Integer matrixRowNumber,
			EMFExportEObjectColumnHeader matrixColumnHeader, EMFExportEObjectColumnHeader refMatrixColumnHeader,
			Integer matrixRowColumnKey, Object rawMatrixRowColumnValue) throws EMFExportException {

		String refMatrixName = ((EMFExportEObjectManyReferencesValueCell) rawMatrixRowColumnValue).getRefMatrixName();

		Table<Integer, Integer, Object> refMatrix = matrixNameToMatrixMap.get(refMatrixName);

		if (((EMFExportEObjectManyReferencesValueCell) rawMatrixRowColumnValue).hasRefIDs()) {

			List<String> refIDs = ((EMFExportEObjectManyReferencesValueCell) rawMatrixRowColumnValue).getRefIDs();

			for (String refID : refIDs) {

				int refMatrixRowKey = findRefMatrixRowKey(processedEObjectsDTO, refMatrixName, refID);

				if (refMatrixRowKey != -1) {

					Map<Integer, Object> refMatrixRow = refMatrix.row(getMatrixRowKey(refMatrixRowKey));

					for (Map.Entry<Integer, Object> refMatrixRowColumn : refMatrixRow.entrySet()) {

						// @formatter:off
						populateFlatMatrixWithData(
								matrixNameToMatrixMap,
								processedEObjectsDTO,
								lookupIndexesDTO,
								exportOptions,
								flatMatrix,
								flatMatrixColumnKey, 
								refMatrix,
								refMatrixName,
								matrixRowNumber, 
								(refMatrixColumnHeader != null) ? refMatrixColumnHeader : matrixColumnHeader,
								(EMFExportEObjectColumnHeader) refMatrix.get(getMatrixRowKey(1), getMatrixColumnKey(refMatrixRowColumn.getKey())),
								refMatrixRowColumn.getKey(),
								refMatrixRowColumn.getValue());
						// @formatter:on
					}
				}
			}

			// even if there is no value for that particular ref, column may already be
			// present if other rows have value in that column, so at least empty cell(s)
			// must be created
		} else {

			int refsMaxValueCount = (refMatrixColumnHeader != null && (matrixColumnHeader != refMatrixColumnHeader))
					? findRefsMaxValueCount(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
							matrixColumnHeader, refMatrixColumnHeader)
					: findColumnRefsMaxValueCount(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
							matrixColumnHeader);

			if (refsMaxValueCount > 0) {

				for (int emptyRefId = 0; emptyRefId < refsMaxValueCount; emptyRefId++) {

					for (Map.Entry<Integer, Object> firstRefMatrixRowColumn : refMatrix.row(getMatrixRowKey(1))
							.entrySet()) {

						// @formatter:off
						boolean refFeatureHasValue = refHasValue(
								matrixNameToMatrixMap,
								processedEObjectsDTO,
								lookupIndexesDTO,
								matrix,
								matrixRowColumnKey,
								refMatrix,
								firstRefMatrixRowColumn.getKey());
						// @formatter:on

						if (refFeatureHasValue) {
							populateFlatMatrixColumnWithData(exportOptions, flatMatrix, flatMatrixColumnKey,
									matrixRowNumber, Optional.empty());
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private int findRefMatrixRowKey(ProcessedEObjectsDTO processedEObjectsDTO, String refID) {
		if (processedEObjectsDTO.eObjectIDToMatrixNameMap.containsKey(refID)) {
			return findRefMatrixRowKey(processedEObjectsDTO, processedEObjectsDTO.eObjectIDToMatrixNameMap.get(refID),
					refID);
		} else {
			return -1;
		}
	}

	private int findRefMatrixRowKey(ProcessedEObjectsDTO processedEObjectsDTO, String refMatrixName, String refID) {
		EMFExportRefMatrixNameIDCompositeKey refMatrixNameIDCompositeKey = new EMFExportRefMatrixNameIDCompositeKey(
				refMatrixName, refID);

		Integer refMatrixRowKey = processedEObjectsDTO.refMatrixRowKeyIndex.get(refMatrixNameIDCompositeKey);
		if (refMatrixRowKey != null) {
			return refMatrixRowKey;
		} else {
			return -1;
		}
	}

	private int findRefsMaxValueCount(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Table<Integer, Integer, Object> matrix, EMFExportEObjectColumnHeader nonRefMatrixColumnHeader,
			Integer matrixRowColumnKey, Object refMatrixColumnHeader) throws EMFExportException {

		int refsMaxValueCount = 0;

		if (refMatrixColumnHeader != nonRefMatrixColumnHeader
				&& (nonRefMatrixColumnHeader instanceof EMFExportEObjectReferenceColumnHeader)
				&& (refMatrixColumnHeader instanceof EMFExportEObjectReferenceColumnHeader)
				&& (((EMFExportEObjectReferenceColumnHeader) nonRefMatrixColumnHeader).getRefMatrixName())
						.equalsIgnoreCase(
								((EMFExportEObjectReferenceColumnHeader) refMatrixColumnHeader).getMatrixName())) {

			refsMaxValueCount = findRefsMaxValueCount(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
					((EMFExportEObjectReferenceColumnHeader) nonRefMatrixColumnHeader).getMatrixName(),
					((EMFExportEObjectReferenceColumnHeader) nonRefMatrixColumnHeader).getColumnHeaderName(),
					((EMFExportEObjectReferenceColumnHeader) refMatrixColumnHeader).getMatrixName(),
					((EMFExportEObjectReferenceColumnHeader) refMatrixColumnHeader).getColumnHeaderName());

		} else {
			refsMaxValueCount = findRefsMaxValueCount(matrix, matrixRowColumnKey);

		}

		return refsMaxValueCount;
	}

	private int findRefsMaxValueCount(Table<Integer, Integer, Object> matrix, Integer matrixColIndex) {
		int refsMaxValueCount = 0;

		Map<Integer, Object> matrixColumn = matrix.column(matrixColIndex);
		for (Map.Entry<Integer, Object> matrixColumnRow : matrixColumn.entrySet()) {
			if (matrixColumnRow.getValue() instanceof EMFExportEObjectManyReferencesValueCell) {
				refsMaxValueCount = Math.max(refsMaxValueCount,
						((EMFExportEObjectManyReferencesValueCell) matrixColumnRow.getValue()).getRefIDsCount());
			}
		}
		return refsMaxValueCount;
	}

	private int findRefsMaxValueCount(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			EMFExportEObjectColumnHeader matrixColumnHeader, EMFExportEObjectColumnHeader refMatrixColumnHeader)
			throws EMFExportException {

		if (matrixColumnHeader == null) {
			throw new EMFExportException(String.format("Matrix column header is required!"));
		}

		if (refMatrixColumnHeader == null) {
			throw new EMFExportException(String.format("RefMatrix column header is required!"));
		}

		return findRefsMaxValueCount(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				((EMFExportEObjectColumnHeader) matrixColumnHeader).getMatrixName(),
				((EMFExportEObjectColumnHeader) matrixColumnHeader).getColumnHeaderName(),
				((EMFExportEObjectColumnHeader) refMatrixColumnHeader).getMatrixName(),
				((EMFExportEObjectColumnHeader) refMatrixColumnHeader).getColumnHeaderName());
	}

	private int findRefsMaxValueCount(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO, String matrixName,
			String columnHeaderName, String refMatrixName, String refColumnHeaderName) throws EMFExportException {

		EMFExportRefsMaxValueCountCompositeKey refsMaxValueCountCompositeKey = new EMFExportRefsMaxValueCountCompositeKey(
				matrixName, columnHeaderName, refMatrixName, refColumnHeaderName);
		if (lookupIndexesDTO.refsMaxValueCountIndex.containsKey(refsMaxValueCountCompositeKey)) {
			return lookupIndexesDTO.refsMaxValueCountIndex.get(refsMaxValueCountCompositeKey);
		}

		int refsMaxValueCount = 0;

		if (matrixNameToMatrixMap.containsKey(matrixName) && matrixNameToMatrixMap.containsKey(refMatrixName)) {

			Table<Integer, Integer, Object> matrix = matrixNameToMatrixMap.get(matrixName);

			Map<Integer, Object> firstMatrixRow = matrix.row(getMatrixRowKey(1));

			Integer matrixColumnKey = findMatrixColumnKey(columnHeaderName, firstMatrixRow);
			if (matrixColumnKey == null) {
				throw new EMFExportException(String.format("Column named %s does not exist in matrix named %s!",
						columnHeaderName, matrixName));
			}

			Table<Integer, Integer, Object> refMatrix = matrixNameToMatrixMap.get(refMatrixName);

			Map<Integer, Object> firstRefMatrixRow = refMatrix.row(getMatrixRowKey(1));

			Integer refMatrixColumnKey = findMatrixColumnKey(refColumnHeaderName, firstRefMatrixRow);
			if (refMatrixColumnKey == null) {
				throw new EMFExportException(String.format("Column named %s does not exist in matrix named %s!",
						refColumnHeaderName, refMatrixName));
			}

			List<String> refIDs = findRefIDs(matrix, matrixColumnKey);

			if (!refIDs.isEmpty()) {

				for (String refID : refIDs) {

					int refMatrixRowKey = findRefMatrixRowKey(processedEObjectsDTO, refMatrixName, refID);

					if (refMatrixRowKey != -1) {

						Object refMatrixColumnRowValue = refMatrix.get(getMatrixRowKey(refMatrixRowKey),
								refMatrixColumnKey);

						if (refMatrixColumnRowValue instanceof EMFExportEObjectManyReferencesValueCell) {
							refsMaxValueCount = Math.max(refsMaxValueCount,
									((EMFExportEObjectManyReferencesValueCell) refMatrixColumnRowValue)
											.getRefIDsCount());
						}
					}
				}
			}

			lookupIndexesDTO.refsMaxValueCountIndex.put(refsMaxValueCountCompositeKey,
					Integer.valueOf(refsMaxValueCount));
		}

		return refsMaxValueCount;
	}

	private boolean refHasValue(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			Table<Integer, Integer, Object> matrix, Integer matrixRowColumnKey,
			Table<Integer, Integer, Object> refMatrix, Integer refMatrixRowColumnKey) throws EMFExportException {

		Object matrixColumnHeader = matrix.get(getMatrixRowKey(1), matrixRowColumnKey);
		if (matrixColumnHeader == null) {
			throw new EMFExportException(String.format("Column header does not exist at column %s in matrix named %s!",
					matrixRowColumnKey, matrix));
		}

		Object refMatrixColumnHeader = refMatrix.get(getMatrixRowKey(1), refMatrixRowColumnKey);
		if (refMatrixColumnHeader == null) {
			throw new EMFExportException(String.format("Column header does not exist at column %s in matrix named %s!",
					refMatrixRowColumnKey, refMatrix));
		}

		return refHasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				(EMFExportEObjectColumnHeader) matrixColumnHeader,
				(EMFExportEObjectColumnHeader) refMatrixColumnHeader);
	}

	private boolean refHasValue(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			EMFExportEObjectColumnHeader matrixColumnHeader, Object refMatrixColumnHeader) throws EMFExportException {

		boolean refHasValue = true;

		if (refMatrixColumnHeader != matrixColumnHeader
				&& (matrixColumnHeader instanceof EMFExportEObjectReferenceColumnHeader)
				&& (refMatrixColumnHeader instanceof EMFExportEObjectReferenceColumnHeader)
				&& (((EMFExportEObjectReferenceColumnHeader) matrixColumnHeader).getRefMatrixName()).equalsIgnoreCase(
						((EMFExportEObjectReferenceColumnHeader) refMatrixColumnHeader).getMatrixName())) {

			refHasValue = refHasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO, matrixColumnHeader,
					(EMFExportEObjectColumnHeader) refMatrixColumnHeader);
		}

		return refHasValue;
	}

	private boolean refHasValue(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			EMFExportEObjectColumnHeader matrixColumnHeader, EMFExportEObjectColumnHeader refMatrixColumnHeader)
			throws EMFExportException {

		if (matrixColumnHeader == null) {
			throw new EMFExportException(String.format("Matrix column header is required!"));
		}

		if (refMatrixColumnHeader == null) {
			throw new EMFExportException(String.format("RefMatrix column header is required!"));
		}

		return refHasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				((EMFExportEObjectColumnHeader) matrixColumnHeader).getMatrixName(),
				((EMFExportEObjectColumnHeader) matrixColumnHeader).getColumnHeaderName(),
				((EMFExportEObjectColumnHeader) refMatrixColumnHeader).getMatrixName(),
				((EMFExportEObjectColumnHeader) refMatrixColumnHeader).getColumnHeaderName());
	}

	private boolean refHasValue(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO, String matrixName,
			String columnHeaderName, String refMatrixName, String refColumnHeaderName) throws EMFExportException {

		boolean refHasValue = true;

		if (!matrixName.equalsIgnoreCase(refMatrixName)) {

			EMFExportRefHasValueCompositeKey refHasValueCompositeKey = new EMFExportRefHasValueCompositeKey(matrixName,
					columnHeaderName, refMatrixName, refColumnHeaderName);
			if (lookupIndexesDTO.refHasValueIndex.containsKey(refHasValueCompositeKey)) {
				return lookupIndexesDTO.refHasValueIndex.get(refHasValueCompositeKey);
			}

			if (matrixNameToMatrixMap.containsKey(matrixName) && matrixNameToMatrixMap.containsKey(refMatrixName)) {

				Table<Integer, Integer, Object> matrix = matrixNameToMatrixMap.get(matrixName);

				Map<Integer, Object> firstMatrixRow = matrix.row(getMatrixRowKey(1));

				Integer matrixColumnKey = findMatrixColumnKey(columnHeaderName, firstMatrixRow);
				if (matrixColumnKey == null) {
					throw new EMFExportException(String.format("Column named %s does not exist in matrix named %s!",
							columnHeaderName, matrixName));
				}

				Table<Integer, Integer, Object> refMatrix = matrixNameToMatrixMap.get(refMatrixName);

				Map<Integer, Object> firstRefMatrixRow = refMatrix.row(getMatrixRowKey(1));

				Integer refMatrixColumnKey = findMatrixColumnKey(refColumnHeaderName, firstRefMatrixRow);
				if (refMatrixColumnKey == null) {
					throw new EMFExportException(String.format("Column named %s does not exist in matrix named %s!",
							refColumnHeaderName, refMatrixName));
				}

				List<String> refIDs = findRefIDs(matrix, matrixColumnKey);

				if (!refIDs.isEmpty()) {

					boolean refIDsHaveValue = false;

					for (String refID : refIDs) {

						int refMatrixRowKey = findRefMatrixRowKey(processedEObjectsDTO, refMatrixName, refID);

						if (refMatrixRowKey != -1) {

							Object refMatrixColumnRowValue = refMatrix.get(getMatrixRowKey(refMatrixRowKey),
									refMatrixColumnKey);

							refIDsHaveValue = refHasValue(refIDsHaveValue, refMatrixColumnRowValue);
						}
					}

					refHasValue = refIDsHaveValue;
				}

				lookupIndexesDTO.refHasValueIndex.put(refHasValueCompositeKey, Boolean.valueOf(refHasValue));
			}
		}

		return refHasValue;
	}

	private boolean refHasValue(boolean refHasValue, Object refMatrixColumnRowValue) {
		if ((refMatrixColumnRowValue != null)
				&& !(refMatrixColumnRowValue instanceof Optional && ((Optional<?>) refMatrixColumnRowValue).isEmpty())
				&& (((refMatrixColumnRowValue instanceof EMFExportEObjectOneReferenceValueCell)
						&& ((EMFExportEObjectOneReferenceValueCell) refMatrixColumnRowValue).hasRefID())
						|| ((refMatrixColumnRowValue instanceof EMFExportEObjectManyReferencesValueCell)
								&& ((EMFExportEObjectManyReferencesValueCell) refMatrixColumnRowValue).hasRefIDs())
						|| ((refMatrixColumnRowValue instanceof EMFExportEObjectIDValueCell)
								&& ((EMFExportEObjectIDValueCell) refMatrixColumnRowValue).hasValue())
						|| (String.valueOf(refMatrixColumnRowValue) != null
								&& !String.valueOf(refMatrixColumnRowValue).isEmpty()))) {
			refHasValue = true;
		}

		return refHasValue;
	}

	private List<String> findRefIDs(Table<Integer, Integer, Object> matrix, Integer matrixColumnKey) {
		List<String> refIDs = new ArrayList<>();

		Map<Integer, Object> matrixColumn = matrix.column(matrixColumnKey);
		for (Map.Entry<Integer, Object> matrixColumnRow : matrixColumn.entrySet()) {
			Object matrixColumnRowValue = matrixColumnRow.getValue();

			if (matrixColumnRowValue instanceof EMFExportEObjectColumnHeader) {
				continue;
			}

			if (matrixColumnRowValue instanceof EMFExportEObjectIDValueCell) {
				if (((EMFExportEObjectIDValueCell) matrixColumnRowValue).hasValue()) {
					refIDs.add(((EMFExportEObjectIDValueCell) matrixColumnRowValue).getValue());
				}
			} else if (matrixColumnRowValue instanceof EMFExportEObjectOneReferenceValueCell) {
				if (((EMFExportEObjectOneReferenceValueCell) matrixColumnRowValue).hasRefID()) {
					refIDs.add(((EMFExportEObjectOneReferenceValueCell) matrixColumnRowValue).getRefID());
				}

			} else if (matrixColumnRowValue instanceof EMFExportEObjectManyReferencesValueCell) {
				if (((EMFExportEObjectManyReferencesValueCell) matrixColumnRowValue).hasRefIDs()) {
					refIDs.addAll(((EMFExportEObjectManyReferencesValueCell) matrixColumnRowValue).getRefIDs());
				}
			}
		}

		return refIDs;
	}

	private Integer findMatrixColumnKey(String columnHeaderName, Map<Integer, Object> matrixRow) {
		Integer matrixColumnKey = null;

		for (Map.Entry<Integer, Object> matrixRowColumn : matrixRow.entrySet()) {
			Object value = matrixRowColumn.getValue();

			if (value instanceof EMFExportEObjectColumnHeader && columnHeaderName
					.equalsIgnoreCase(((EMFExportEObjectColumnHeader) value).getColumnHeaderName())) {
				matrixColumnKey = matrixRowColumn.getKey();
			}
		}

		return matrixColumnKey;
	}

	private boolean columnHasValue(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			EMFExportEObjectColumnHeader matrixColumnHeader) throws EMFExportException {

		if (matrixColumnHeader == null) {
			throw new EMFExportException(String.format("Matrix column header is required!"));
		}

		return columnHasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				((EMFExportEObjectColumnHeader) matrixColumnHeader).getMatrixName(),
				((EMFExportEObjectColumnHeader) matrixColumnHeader).getColumnHeaderName());
	}

	private boolean columnHasValue(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO, String matrixName,
			String columnHeaderName) throws EMFExportException {

		boolean columnHasValue = false;

		EMFExportColumnHasValueCompositeKey columnHasValueCompositeKey = new EMFExportColumnHasValueCompositeKey(
				matrixName, columnHeaderName);
		if (lookupIndexesDTO.columnHasValueIndex.containsKey(columnHasValueCompositeKey)) {
			return lookupIndexesDTO.columnHasValueIndex.get(columnHasValueCompositeKey);
		}

		if (matrixNameToMatrixMap.containsKey(matrixName)) {

			Table<Integer, Integer, Object> matrix = matrixNameToMatrixMap.get(matrixName);

			Map<Integer, Object> firstMatrixRow = matrix.row(getMatrixRowKey(1));

			Integer matrixColumnKey = findMatrixColumnKey(columnHeaderName, firstMatrixRow);
			if (matrixColumnKey == null) {
				throw new EMFExportException(String.format("Column named %s does not exist in matrix named %s!",
						columnHeaderName, matrixName));
			}

			columnHasValue = columnHasValue(matrix, matrixColumnKey);

			lookupIndexesDTO.columnHasValueIndex.put(columnHasValueCompositeKey, Boolean.valueOf(columnHasValue));
		}

		return columnHasValue;
	}

	private boolean columnHasValue(Table<Integer, Integer, Object> matrix, Integer matrixColumnKey) {
		boolean columnHasValue = false;

		Map<Integer, Object> matrixColumn = matrix.column(matrixColumnKey);
		for (Map.Entry<Integer, Object> matrixColumnRow : matrixColumn.entrySet()) {
			Object matrixColumnRowValue = matrixColumnRow.getValue();

			if (matrixColumnRowValue instanceof EMFExportEObjectColumnHeader) {
				continue;
			}

			if (columnHasValue(matrixColumnRowValue)) {
				columnHasValue = true;
				break;
			}
		}

		return columnHasValue;
	}

	private boolean columnHasValue(Object matrixColumnRowValue) {
		return ((matrixColumnRowValue != null)
				&& !(matrixColumnRowValue instanceof Optional && ((Optional<?>) matrixColumnRowValue).isEmpty())
				&& (((matrixColumnRowValue instanceof EMFExportEObjectOneReferenceValueCell)
						&& ((EMFExportEObjectOneReferenceValueCell) matrixColumnRowValue).hasRefID())
						|| ((matrixColumnRowValue instanceof EMFExportEObjectManyReferencesValueCell)
								&& ((EMFExportEObjectManyReferencesValueCell) matrixColumnRowValue).hasRefIDs())
						|| ((matrixColumnRowValue instanceof EMFExportEObjectIDValueCell)
								&& ((EMFExportEObjectIDValueCell) matrixColumnRowValue).hasValue())
						|| (String.valueOf(matrixColumnRowValue) != null
								&& !String.valueOf(matrixColumnRowValue).isEmpty())));
	}

	private int findColumnRefsMaxValueCount(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			EMFExportEObjectColumnHeader matrixColumnHeader) throws EMFExportException {

		if (matrixColumnHeader == null) {
			throw new EMFExportException(String.format("Matrix column header is required!"));
		}

		return findColumnRefsMaxValueCount(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO,
				((EMFExportEObjectColumnHeader) matrixColumnHeader).getMatrixName(),
				((EMFExportEObjectColumnHeader) matrixColumnHeader).getColumnHeaderName());
	}

	private int findColumnRefsMaxValueCount(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO, String matrixName,
			String columnHeaderName) throws EMFExportException {

		EMFExportColumnRefsMaxValueCountCompositeKey columnRefsMaxValueCountCompositeKey = new EMFExportColumnRefsMaxValueCountCompositeKey(
				matrixName, columnHeaderName);
		if (lookupIndexesDTO.columnRefsMaxValueCountIndex.containsKey(columnRefsMaxValueCountCompositeKey)) {
			return lookupIndexesDTO.columnRefsMaxValueCountIndex.get(columnRefsMaxValueCountCompositeKey);
		}

		int columnRefsMaxValueCount = 0;

		if (matrixNameToMatrixMap.containsKey(matrixName)) {

			Table<Integer, Integer, Object> matrix = matrixNameToMatrixMap.get(matrixName);

			Map<Integer, Object> firstMatrixRow = matrix.row(getMatrixRowKey(1));

			Integer matrixColumnKey = findMatrixColumnKey(columnHeaderName, firstMatrixRow);
			if (matrixColumnKey == null) {
				throw new EMFExportException(String.format("Column named %s does not exist in matrix named %s!",
						columnHeaderName, matrixName));
			}

			columnRefsMaxValueCount = findRefsMaxValueCount(matrix, matrixColumnKey);

			lookupIndexesDTO.columnRefsMaxValueCountIndex.put(columnRefsMaxValueCountCompositeKey,
					Integer.valueOf(columnRefsMaxValueCount));
		}

		return columnRefsMaxValueCount;
	}

	private boolean hasValue(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap,
			ProcessedEObjectsDTO processedEObjectsDTO, LookupIndexesDTO lookupIndexesDTO,
			EMFExportEObjectColumnHeader matrixColumnHeader, EMFExportEObjectColumnHeader refMatrixColumnHeader)
			throws EMFExportException {

		if (refMatrixColumnHeader != null && matrixColumnHeader != refMatrixColumnHeader) {
			return refHasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO, matrixColumnHeader,
					refMatrixColumnHeader);
		} else {
			return columnHasValue(matrixNameToMatrixMap, processedEObjectsDTO, lookupIndexesDTO, matrixColumnHeader);
		}
	}

	private Set<String> nonRefMatrixNames(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap) {
		Set<String> refMatrixNames = refMatrixNames(matrixNameToMatrixMap);

		// @formatter:off
		return matrixNameToMatrixMap.keySet().stream()
				.filter(matrixName -> !refMatrixNames.contains(matrixName))
				.collect(Collectors.toSet());
		// @formatter:on
	}

	private Set<String> refMatrixNames(Map<String, Table<Integer, Integer, Object>> matrixNameToMatrixMap) {
		Set<String> filterMatrixNames = new HashSet<>();

		for (String matrixName : matrixNameToMatrixMap.keySet()) {
			Table<Integer, Integer, Object> matrix = matrixNameToMatrixMap.get(matrixName);

			Map<Integer, Object> firstRow = matrix.row(getMatrixRowKey(1));

			for (Map.Entry<Integer, Object> rowColumn : firstRow.entrySet()) {
				Object value = rowColumn.getValue();

				if ((value != null) && (value instanceof EMFExportEObjectReferenceColumnHeader)) {
					String refMatrixName = ((EMFExportEObjectReferenceColumnHeader) value).getRefMatrixName();

					// only non self-referencing models
					if (!refMatrixName
							.equalsIgnoreCase(((EMFExportEObjectReferenceColumnHeader) value).getMatrixName())) {
						filterMatrixNames.add(refMatrixName);
					}
				}
			}
		}

		return filterMatrixNames;
	}

	private void writeCSVHeader(Table<Integer, Integer, Object> matrix, CsvWriter csvWriter) {
		Map<Integer, Object> firstRow = matrix.row(getMatrixRowKey(1));

		// @formatter:off
		List<String> firstRowValuesAsString = firstRow.values()
				.stream()
				.map(v -> String.valueOf(v))
				.collect(Collectors.toList());
		// @formatter:on

		csvWriter.writeRecord(firstRowValuesAsString);
	}

	private void writeCSVData(Table<Integer, Integer, Object> matrix, CsvWriter csvWriter,
			Map<Object, Object> exportOptions) {
		Map<Integer, Map<Integer, Object>> matrixRowMap = matrix.rowMap();

		// @formatter:off
		List<Integer> remainingRows = matrixRowMap.keySet()
				.stream()
				.skip(1)
				.collect(Collectors.toList());
		// @formatter:on

		for (Integer rowNumber : remainingRows) {
			Map<Integer, Object> row = matrixRowMap.get(rowNumber);

			List<String> rowValuesAsString = convertValues(row.values(), exportOptions);

			csvWriter.writeRecord(rowValuesAsString);
		}
	}

	private List<String> convertValues(Collection<Object> rowValues, Map<Object, Object> exportOptions) {
		// @formatter:off
		List<String> rowValuesAsString = rowValues
				.stream()
				.map(v -> convertValue(v, exportOptions) )
				.collect(Collectors.toList());
		// @formatter:on
		return rowValuesAsString;
	}

	private String convertValue(Object v, Map<Object, Object> exportOptions) {
		if ((v == null) || (v instanceof Optional)) {
			return "";
		} else {
			if (showURIsEnabled(exportOptions) && (v instanceof EMFExportEObjectReferenceValueCell)
					&& !((EMFExportEObjectReferenceValueCell) v).isSelfReferencingModel()) {
				if (v instanceof EMFExportEObjectOneReferenceValueCell
						&& ((EMFExportEObjectOneReferenceValueCell) v).hasURI()) {
					return ((EMFExportEObjectOneReferenceValueCell) v).getURI();
				} else if (v instanceof EMFExportEObjectManyReferencesValueCell
						&& ((EMFExportEObjectManyReferencesValueCell) v).hasURIs()) {
					if (((EMFExportEObjectManyReferencesValueCell) v).getURIsCount() == 1) {
						return ((EMFExportEObjectManyReferencesValueCell) v).getURIs().get(0);
					} else {
						return Arrays.toString(((EMFExportEObjectManyReferencesValueCell) v).getURIs().toArray());
					}
				} else {
					return "";
				}

			} else {
				if ((v instanceof EMFExportEObjectManyReferencesValueCell)
						&& ((EMFExportEObjectManyReferencesValueCell) v).getRefIDsCount() == 1) {
					return ((EMFExportEObjectManyReferencesValueCell) v).getRefIDs().get(0);
				} else {
					return String.valueOf(v);
				}
			}
		}
	}

	private void writeZipEntry(ZipOutputStream zipOutputStream, String matrixName, final StringWriter csvStringWriter)
			throws IOException {
		String zipEntryName = constructZipEntryName(matrixName);
		ZipEntry zipEntry = new ZipEntry(zipEntryName);
		zipOutputStream.putNextEntry(zipEntry);

		try (InputStream bais = new ByteArrayInputStream(csvStringWriter.toString().getBytes())) {
			byte[] bytes = new byte[1024];
			int length;
			while ((length = bais.read(bytes)) >= 0) {
				zipOutputStream.write(bytes, 0, length);
			}
		}

		zipOutputStream.closeEntry();
	}

	private String constructZipEntryName(String matrixName) {
		String normalizedMatrixName = matrixName.strip().replaceAll("[()]", "").replaceAll("(?U)[^\\w\\._]+", "_");

		StringBuilder sb = new StringBuilder(100);
		sb.append(normalizedMatrixName);
		sb.append(".");
		sb.append(CSV_FILE_EXTENSION);
		return sb.toString();
	}

	private void validateClassHierarchyForRootObjects(List<EObject> eObjects) throws EMFExportException {
		Class<?> clazz = null;
		for (EObject eObject : eObjects) {
			if (clazz == null) {
				clazz = eObject.getClass();
			} else {
				if (clazz.isAssignableFrom(eObject.getClass())) {
					continue;
				} else if (eObject.getClass().isAssignableFrom(clazz)) {
					clazz = eObject.getClass();
				} else {
					throw new EMFExportException(
							String.format("Element type '%s' is not compatible with type of other elements in list!",
									eObject.getClass()));
				}
			}
		}
	}

	@Override
	protected Map<Object, Object> validateExportOptions(Map<?, ?> options) throws EMFExportException {
		Map<Object, Object> exportOptions = super.validateExportOptions(options);

		if (!flatExportMode(exportOptions) && !zipExportMode(exportOptions)) {
			throw new EMFExportException("Please specify export mode!");
		}

		if (flatExportMode(exportOptions)
				&& (exportMetadataEnabled(exportOptions) || addMappingTableEnabled(exportOptions))) {
			throw new EMFExportException(
					"Incompatible combination of export options: neither 'export metadata' nor 'generate mapping table' options can be turned on in flat CSV export mode!");
		}

		return exportOptions;
	}

	private boolean flatExportMode(Map<Object, Object> exportOptions) {
		return (exportMode(exportOptions) != null
				&& EMFCSVExportMode.valueOf(String.valueOf(exportMode(exportOptions))) == EMFCSVExportMode.FLAT);
	}

	private boolean zipExportMode(Map<Object, Object> exportOptions) {
		return (exportMode(exportOptions) == null || (exportMode(exportOptions) != null
				&& EMFCSVExportMode.valueOf(String.valueOf(exportMode(exportOptions))) == EMFCSVExportMode.ZIP));
	}

	private Object exportMode(Map<Object, Object> exportOptions) {
		return exportOptions.get(EMFCSVExportOptions.OPTION_EXPORT_MODE);
	}

	@Override
	protected boolean exportMetadataEnabled(Map<Object, Object> exportOptions) {
		if (flatExportMode(exportOptions)) {
			return ((boolean) exportOptions.getOrDefault(EMFExportOptions.OPTION_EXPORT_METADATA, Boolean.FALSE));
		} else {
			return ((boolean) exportOptions.getOrDefault(EMFExportOptions.OPTION_EXPORT_METADATA, Boolean.TRUE));
		}
	}

	@Override
	protected boolean addMappingTableEnabled(Map<Object, Object> exportOptions) {
		if (flatExportMode(exportOptions) || !exportNonContainmentEnabled(exportOptions)) {
			return ((boolean) exportOptions.getOrDefault(EMFExportOptions.OPTION_ADD_MAPPING_TABLE, Boolean.FALSE));
		} else {
			return ((boolean) exportOptions.getOrDefault(EMFExportOptions.OPTION_ADD_MAPPING_TABLE, Boolean.TRUE));
		}
	}

	protected class LookupIndexesDTO {
		// lookup index for storing information if specific one-to-one reference has
		// value
		public final Map<EMFExportRefHasValueCompositeKey, Boolean> refHasValueIndex;

		// lookup index for storing information about max number of references for
		// specific one-to-many reference
		public final Map<EMFExportRefsMaxValueCountCompositeKey, Integer> refsMaxValueCountIndex;

		// lookup index for storing information if specific column has value
		public final Map<EMFExportColumnHasValueCompositeKey, Boolean> columnHasValueIndex;

		// lookup index for storing information about max number of references for
		// specific column of type one-to-many reference
		public final Map<EMFExportColumnRefsMaxValueCountCompositeKey, Integer> columnRefsMaxValueCountIndex;

		public LookupIndexesDTO() {
			this.refHasValueIndex = new HashMap<>();
			this.refsMaxValueCountIndex = new HashMap<>();
			this.columnHasValueIndex = new HashMap<>();
			this.columnRefsMaxValueCountIndex = new HashMap<>();
		}
	}
}
