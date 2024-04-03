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
package org.gecko.emf.csv.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.gecko.emf.csv.tests.helper.EMFCSVExporterTestHelper.createBasicPackageResourceSet;
import static org.gecko.emf.csv.tests.helper.EMFCSVExporterTestHelper.createBusinessPerson;
import static org.gecko.emf.csv.tests.helper.EMFCSVExporterTestHelper.createFlintstonesFamily;
import static org.gecko.emf.csv.tests.helper.EMFCSVExporterTestHelper.createRequest;
import static org.gecko.emf.csv.tests.helper.EMFCSVExporterTestHelper.createSimpsonFamily;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.gecko.emf.csv.tests.helper.EMFCSVExporterTestHelper;
import org.gecko.emf.exporter.EMFExportException;
import org.gecko.emf.exporter.EMFExportOptions;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.exporter.csv.api.EMFCSVExportMode;
import org.gecko.emf.exporter.csv.api.EMFCSVExportOptions;
import org.gecko.emf.exporter.csv.api.annotations.RequireEMFCSVExporter;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.emf.osgi.example.model.basic.Address;
import org.gecko.emf.osgi.example.model.basic.BasicFactory;
import org.gecko.emf.osgi.example.model.basic.BasicPackage;
import org.gecko.emf.osgi.example.model.basic.BusinessPerson;
import org.gecko.emf.osgi.example.model.basic.Family;
import org.gecko.emf.osgi.example.model.basic.Person;
import org.gecko.emf.utilities.Request;
import org.gecko.emf.utilities.UtilitiesFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import trees.TreesPackage;

/**
 * EMF CSV exporter integration test.
 * 
 * @author Michal H. Siemaszko
 */
@Testable
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequireEMFCSVExporter
@RequireEMF
public class EMFCSVExporterTest {

	private static final String TREES_DATASET_XMI = System.getProperty("TREES_DATASET_XMI");

	@Order(value = -1)
	@Test
	public void testServices(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware) {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		ServiceReference<EMFExporter> emfCsvExporterReference = emfCsvExporterAware.getServiceReference();
		assertThat(emfCsvExporterReference).isNotNull();
	}

	@Test
	public void testExportExampleModelBasicInvalidClassHierarchyException(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);

		Path filePath = Files.createTempFile("testExportExampleModelBasicInvalidClassHierarchyException", ".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// having 'BusinessPerson' instance passed in list of objects to be exported
		// will fail class hierarchy validation, triggered when FLAT export mode is
		// chosen - instance of 'Family' is passed as first element and it determines
		// valid class hierarchy
		assertThatExceptionOfType(EMFExportException.class).isThrownBy(() -> {
			// @formatter:off
			emfCsvExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily, businessPerson), fileOutputStream, 
					Map.of(
							EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//							EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//							EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to false in FLAT export mode
//							EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false in FLAT export mode
//							EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//							EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
							EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.FLAT
						)
					);
			// @formatter:on
		});
	}

	@Test
	public void testExportExampleModelBasicExportNonContainmentDisabledAddMappingTableEnabledException(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);

		Path filePath = Files.createTempFile(
				"testExportExampleModelBasicExportNonContainmentDisabledAddMappingTableEnabledException", ".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// incompatible combination of export options: 'export non-containment
		// references' option cannot be turned off if 'generate mapping table' option is
		// turned on!
		assertThatExceptionOfType(EMFExportException.class).isThrownBy(() -> {
			// @formatter:off
			emfCsvExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily, businessPerson), fileOutputStream, 
					Map.of(
							EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//							EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//							EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true in ZIP export mode
							EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//							EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//							EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
							EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.ZIP
						)
					);
			// @formatter:on
		});
	}

	@Test
	public void testExportExampleModelBasicFlatExportModeNoExportMetadataNorAddMappingTableEnabledException(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);

		Path filePath = Files.createTempFile(
				"testExportExampleModelBasicFlatExportModeNoExportMetadataNorAddMappingTableEnabledException", ".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// incompatible combination of export options: neither 'export metadata' nor
		// 'generate mapping table' options can be turned on in flat CSV export mode!
		assertThatExceptionOfType(EMFExportException.class).isThrownBy(() -> {
			// @formatter:off
			emfCsvExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily, businessPerson), fileOutputStream, 
					Map.of(
							EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//							EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//							EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to false in FLAT export mode
							EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false in FLAT export mode
//							EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//							EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
							EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.FLAT
						)
					);
			// @formatter:on
		});
	}

	@Test
	public void testExportExampleModelBasicEObjectsToCsvFlatModeNonContainmenEnabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		Path filePath = Files.createTempFile("testExportExampleModelBasicEObjectsToCsvFlatModeNonContainmenEnabled",
				".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.FLAT
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportExampleModelBasicEObjectsToCsvZipModeNonContainmentDisabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);

		Path filePath = Files.createTempFile("testExportExampleModelBasicEObjectsToCsvZipModeNonContainmentDisabled",
				".zip");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily, businessPerson), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true in ZIP export mode
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to true in ZIP export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.ZIP
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelResourceToCsvFlatModeNonContainmentDisabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		ResourceSet resourceSet = createBasicPackageResourceSet(basicPackage);
		Resource xmiResource = resourceSet
				.createResource(URI.createURI("testExportUtilModelResourceToCsvFlatModeNonContainmentDisabled.test"));
		assertNotNull(xmiResource);

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);
		xmiResource.getContents().add(request1);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);
		xmiResource.getContents().add(request2);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);
		xmiResource.getContents().add(request3);

		Path filePath = Files.createTempFile("testExportUtilModelResourceToCsvFlatModeNonContainmentDisabled", ".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportResourceTo(xmiResource, fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.FLAT
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportExampleModelBasicResourceToCsvFlatModeDifferentRootObjectsNonContainmentDisabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		ResourceSet resourceSet = createBasicPackageResourceSet(basicPackage);
		Resource xmiResource = resourceSet.createResource(URI.createURI(
				"testExportExampleModelBasicResourceToCsvFlatModeDifferentRootObjectsNonContainmentDisabled.test"));
		assertNotNull(xmiResource);

		Address address = EMFCSVExporterTestHelper.createSimpsonsAddress(basicFactory);

		Person homerSimpson = EMFCSVExporterTestHelper.createHomerSimpson(basicFactory, address);
		xmiResource.getContents().add(homerSimpson);

		Person margeSimpson = EMFCSVExporterTestHelper.createMargeSimpson(basicFactory, address);
		xmiResource.getContents().add(margeSimpson);

		Person bartSimpson = EMFCSVExporterTestHelper.createBartSimpson(basicFactory, address);
		xmiResource.getContents().add(bartSimpson);

		Person lisaSimpson = EMFCSVExporterTestHelper.createLisaSimpson(basicFactory, address);
		xmiResource.getContents().add(lisaSimpson);

		Person maggieSimpson = EMFCSVExporterTestHelper.createMaggieSimpson(basicFactory, address);
		xmiResource.getContents().add(maggieSimpson);

		homerSimpson.getRelatives().add(margeSimpson);
		homerSimpson.getRelatives().add(bartSimpson);
		homerSimpson.getRelatives().add(lisaSimpson);
		homerSimpson.getRelatives().add(maggieSimpson);

		margeSimpson.getRelatives().add(homerSimpson);
		margeSimpson.getRelatives().add(bartSimpson);
		margeSimpson.getRelatives().add(lisaSimpson);
		margeSimpson.getRelatives().add(maggieSimpson);

		bartSimpson.getRelatives().add(homerSimpson);
		bartSimpson.getRelatives().add(margeSimpson);
		bartSimpson.getRelatives().add(lisaSimpson);
		bartSimpson.getRelatives().add(maggieSimpson);

		lisaSimpson.getRelatives().add(homerSimpson);
		lisaSimpson.getRelatives().add(margeSimpson);
		lisaSimpson.getRelatives().add(bartSimpson);
		lisaSimpson.getRelatives().add(maggieSimpson);

		maggieSimpson.getRelatives().add(homerSimpson);
		maggieSimpson.getRelatives().add(margeSimpson);
		maggieSimpson.getRelatives().add(lisaSimpson);
		maggieSimpson.getRelatives().add(maggieSimpson);

		homerSimpson.getTags().add(EMFCSVExporterTestHelper.createMultiLevelTag(basicFactory,
				EMFCSVExporterTestHelper.createUniquePrefix(10)));

		homerSimpson.setBigInt(BigInteger.TEN);

		homerSimpson.getBigDec().add(BigDecimal.ZERO);
		homerSimpson.getBigDec().add(BigDecimal.ONE);
		homerSimpson.getBigDec().add(BigDecimal.TEN);

		homerSimpson.setImage(EMFCSVExporterTestHelper.createByteArr());

		homerSimpson.getProperties()
				.putAll(EMFCSVExporterTestHelper.createProperties(EMFCSVExporterTestHelper.createUniquePrefix(10)));

		Path filePath = Files.createTempFile(
				"testExportExampleModelBasicResourceToCsvFlatModeDifferentRootObjectsNonContainmentDisabled", ".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportResourceTo(xmiResource, fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to true
//						EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.FLAT
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportExampleModelBasicResourceToCsvFlatModeNonContainmentDisabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		ResourceSet resourceSet = createBasicPackageResourceSet(basicPackage);
		Resource xmiResource = resourceSet.createResource(
				URI.createURI("testExportExampleModelBasicResourceToCsvFlatModeNonContainmentDisabled.test"));
		assertNotNull(xmiResource);

		Family simpsonFamily = createSimpsonFamily(basicFactory);
		xmiResource.getContents().add(simpsonFamily);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);
		xmiResource.getContents().add(flintstonesFamily);

		Path filePath = Files.createTempFile("testExportExampleModelBasicResourceToCsvFlatModeNonContainmentDisabled",
				".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportResourceTo(xmiResource, fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to true
//						EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.FLAT
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportExampleModelBasicResourceToCsvZipModeNonContainmenEnabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		ResourceSet resourceSet = createBasicPackageResourceSet(basicPackage);
		Resource xmiResource = resourceSet.createResource(
				URI.createURI("testExportExampleModelBasicResourceToCsvZipModeNonContainmenEnabled.test"));
		assertNotNull(xmiResource);

		Family simpsonFamily = createSimpsonFamily(basicFactory);
		xmiResource.getContents().add(simpsonFamily);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);
		xmiResource.getContents().add(flintstonesFamily);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);
		xmiResource.getContents().add(businessPerson);

		Path filePath = Files.createTempFile("testExportExampleModelBasicResourceToCsvZipModeNonContainmenEnabled",
				".zip");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportResourceTo(xmiResource, fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true in ZIP export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to true in ZIP export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.ZIP
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelEObjectsToCsvFlatModeNonContainmenEnabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware)
			throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile("testExportUtilModelEObjectsToCsvFlatModeNonContainmenEnabled", ".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.FLAT
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelEObjectsToCsvZipModeNonContainmenEnabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware)
			throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile("testExportUtilModelEObjectsToCsvZipModeNonContainmenEnabled", ".zip");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true in ZIP export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to true in ZIP export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.ZIP
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelEObjectsToCsvZipModeNonContainmentDisabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware)
			throws Exception {

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile("testExportUtilModelEObjectsToCsvZipModeNonContainmentDisabled", ".zip");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true in ZIP export mode
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to true in ZIP export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.ZIP
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportTreesModelEObjectsToCsvFlatModeNonContainmentDisabled(
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware)
			throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		// register model
		EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
		packageRegistry.put(TreesPackage.eNS_URI, TreesPackage.eINSTANCE);

		// register xmi
		Map<String, Object> extensionFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionFactoryMap.put("xmi", new XMIResourceFactoryImpl());

		Resource resource = resourceSet.getResource(URI.createFileURI(new File(TREES_DATASET_XMI).getAbsolutePath()),
				true);

		Path filePath = Files.createTempFile("testExportTreesModelEObjectsToCsvFlatModeNonContainmentDisabled", ".csv");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportEObjectsTo(resource.getContents(), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false in FLAT export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.FLAT
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportTreesModelEObjectsToCsvZipModeNonContainmenEnabled(
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFCSVExporter)") ServiceAware<EMFExporter> emfCsvExporterAware)
			throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertThat(emfCsvExporterAware.getServices()).hasSize(1);
		EMFExporter emfCsvExporterService = emfCsvExporterAware.getService();
		assertThat(emfCsvExporterService).isNotNull();

		// register model
		EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
		packageRegistry.put(TreesPackage.eNS_URI, TreesPackage.eINSTANCE);

		// register xmi
		Map<String, Object> extensionFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionFactoryMap.put("xmi", new XMIResourceFactoryImpl());

		Resource resource = resourceSet.getResource(URI.createFileURI(new File(TREES_DATASET_XMI).getAbsolutePath()),
				true);

		Path filePath = Files.createTempFile("testExportTreesModelEObjectsToCsvZipModeNonContainmenEnabled", ".zip");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfCsvExporterService.exportEObjectsTo(resource.getContents(), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true in ZIP export mode
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to true in ZIP export mode
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFCSVExportOptions.OPTION_EXPORT_MODE, EMFCSVExportMode.ZIP
					)
				);
		// @formatter:on
	}
}
