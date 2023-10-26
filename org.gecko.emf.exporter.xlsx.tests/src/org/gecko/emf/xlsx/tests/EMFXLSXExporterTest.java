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
package org.gecko.emf.xlsx.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.gecko.emf.xlsx.tests.helper.EMFXLSXExporterTestHelper.createBasicPackageResourceSet;
import static org.gecko.emf.xlsx.tests.helper.EMFXLSXExporterTestHelper.createBusinessPerson;
import static org.gecko.emf.xlsx.tests.helper.EMFXLSXExporterTestHelper.createFlintstonesFamily;
import static org.gecko.emf.xlsx.tests.helper.EMFXLSXExporterTestHelper.createRequest;
import static org.gecko.emf.xlsx.tests.helper.EMFXLSXExporterTestHelper.createSimpsonFamily;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.OutputStream;
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
import org.gecko.emf.exporter.EMFExportException;
import org.gecko.emf.exporter.EMFExportOptions;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.exporter.xlsx.api.EMFXLSXExportOptions;
import org.gecko.emf.osgi.example.model.basic.BasicFactory;
import org.gecko.emf.osgi.example.model.basic.BasicPackage;
import org.gecko.emf.osgi.example.model.basic.BusinessPerson;
import org.gecko.emf.osgi.example.model.basic.Family;
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
public class EMFXLSXExporterTest {

	@Order(value = -1)
	@Test
	public void testServices(@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(timeout = 2000) ServiceAware<BasicFactory> bfAware,
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware) {

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		ServiceReference<EMFExporter> emfXlsxExporterReference = emfXlsxExporterAware.getServiceReference();
		assertThat(emfXlsxExporterReference).isNotNull();

		assertThat(rsAware.getServices()).hasSize(1);
		ServiceReference<ResourceSet> rsReference = rsAware.getServiceReference();
		assertThat(rsReference).isNotNull();

		assertThat(bfAware.getServices()).hasSize(1);
		ServiceReference<BasicFactory> bfReference = bfAware.getServiceReference();
		assertThat(bfReference).isNotNull();
	}

	@Test
	public void testExportUtilModelExportNonContainmentDisabledAddMappingTableEnabledException(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware)
			throws Exception {

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfXlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile(
				"testExportUtilModelExportNonContainmentDisabledAddMappingTableEnabledException", ".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// incompatible combination of export options: 'export non-containment
		// references' option cannot be turned off if 'generate mapping table' option is
		// turned on!
		assertThatExceptionOfType(EMFExportException.class).isThrownBy(() -> {
			// @formatter:off
			emfXlsxExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
					Map.of(
							EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//							EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false // defaults to false
							EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true // defaults to false
						)
					);
			// @formatter:on
		});
	}

	@Test
	public void testExportUtilModelExportNonContainmentDisabledGenerateLinksEnabledException(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware)
			throws Exception {

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfXlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile(
				"testExportUtilModelExportNonContainmentDisabledGenerateLinksEnabledException", ".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// incompatible combination of export options: 'export non-containment
		// references' option cannot be turned off if 'generate links for references'
		// option is turned on!
		assertThatExceptionOfType(EMFExportException.class).isThrownBy(() -> {
			// @formatter:off
			emfXlsxExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
					Map.of(
							EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//							EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false // defaults to false
							EMFXLSXExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
						)
					);
			// @formatter:on
		});
	}

	@Test
	public void testExportExampleModelBasicResourceToXlsxNonContainmenEnabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfXlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		ResourceSet resourceSet = createBasicPackageResourceSet(basicPackage);
		Resource xmiResource = resourceSet
				.createResource(URI.createURI("testExportExampleModelBasicResourceToXlsxNonContainmenEnabled.test"));
		assertNotNull(xmiResource);

		Family simpsonFamily = createSimpsonFamily(basicFactory);
		xmiResource.getContents().add(simpsonFamily);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);
		xmiResource.getContents().add(flintstonesFamily);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);
		xmiResource.getContents().add(businessPerson);

		Path filePath = Files.createTempFile("testExportExampleModelBasicResourceToXlsxNonContainmenEnabled", ".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfXlsxExporterService.exportResourceTo(xmiResource, fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFXLSXExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
						EMFXLSXExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
//						EMFXLSXExportOptions.OPTION_FREEZE_HEADER_ROW, true // defaults to true
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportExampleModelBasicEObjectsToXlsxNoExportMetadataNorAddMappingTable(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfXlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);

		Path filePath = Files
				.createTempFile("testExportExampleModelBasicEObjectsToXlsxNoExportMetadataNorAddMappingTable", ".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfXlsxExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily, businessPerson), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
						EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to true
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false // defaults to false
//						EMFXLSXExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
						EMFXLSXExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
//						EMFXLSXExportOptions.OPTION_FREEZE_HEADER_ROW, true // defaults to true
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportExampleModelBasicEObjectsToXlsxNonContainmentDisabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfXlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);

		Path filePath = Files.createTempFile("testExportExampleModelBasicEObjectsToXlsxNonContainmentDisabled",
				".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfXlsxExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily, businessPerson), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
						EMFExportOptions.OPTION_EXPORT_METADATA, false // defaults to true
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false
//						EMFXLSXExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
//						EMFXLSXExportOptions.OPTION_GENERATE_LINKS, false // defaults to false
//						EMFXLSXExportOptions.OPTION_FREEZE_HEADER_ROW, true // defaults to true
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelResourceToXlsxNonContainmenEnabled(
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware)
			throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfXlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		Resource xmiResource = resourceSet
				.createResource(URI.createURI("testExportUtilModelResourceToXlsxNonContainmenEnabled.test"));
		assertNotNull(xmiResource);

		Request request = createRequest(UtilitiesFactory.eINSTANCE);
		xmiResource.getContents().add(request);

		Path filePath = Files.createTempFile("testExportUtilModelResourceToXlsxNonContainmenEnabled", ".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfXlsxExporterService.exportResourceTo(xmiResource, fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFXLSXExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
						EMFXLSXExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
//						EMFXLSXExportOptions.OPTION_FREEZE_HEADER_ROW, true // defaults to true
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelEObjectsToXlsxNoExportMetadataNorAddMappingTable(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware)
			throws Exception {

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfXlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile("testExportUtilModelEObjectsToXlsxNoExportMetadataNorAddMappingTable",
				".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfXlsxExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
						EMFExportOptions.OPTION_EXPORT_METADATA, false, // defaults to true
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false // defaults to false
//						EMFXLSXExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
						EMFXLSXExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
//						EMFXLSXExportOptions.OPTION_FREEZE_HEADER_ROW, true // defaults to true
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelEObjectsToXlsxNonContainmentDisabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfXlsxExporterAware)
			throws Exception {

		assertThat(emfXlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfXlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile("testExportUtilModelEObjectsToXlsxNonContainmentDisabled", ".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfXlsxExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
						EMFExportOptions.OPTION_EXPORT_METADATA, false // defaults to true
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false
//						EMFXLSXExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
//						EMFXLSXExportOptions.OPTION_GENERATE_LINKS, false // defaults to false
//						EMFXLSXExportOptions.OPTION_FREEZE_HEADER_ROW, true // defaults to true
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	private static final String TREES_DATASET_XMI = System.getProperty("TREES_DATASET_XMI");

	@Test
	public void testExportTreesModelEObjectsToXLSXNonContainmenEnabled(
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFXLSXExporter)") ServiceAware<EMFExporter> emfxlsxExporterAware)
			throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertThat(emfxlsxExporterAware.getServices()).hasSize(1);
		EMFExporter emfXlsxExporterService = emfxlsxExporterAware.getService();
		assertThat(emfXlsxExporterService).isNotNull();

		// register model
		EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
		packageRegistry.put(TreesPackage.eNS_URI, TreesPackage.eINSTANCE);

		// register xmi
		Map<String, Object> extensionFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionFactoryMap.put("xmi", new XMIResourceFactoryImpl());

		Resource resource = resourceSet.getResource(URI.createFileURI(new File(TREES_DATASET_XMI).getAbsolutePath()),
				true);

		Path filePath = Files.createTempFile("testExportTreesModelEObjectsToXLSXNonContainmenEnabled", ".xlsx");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfXlsxExporterService.exportEObjectsTo(resource.getContents(), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFXLSXExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
						EMFXLSXExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
//						EMFXLSXExportOptions.OPTION_FREEZE_HEADER_ROW, true // defaults to true
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on		
	}
}
