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
package org.gecko.emf.ods.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.gecko.emf.ods.tests.helper.EMFODSExporterTestHelper.createBasicPackageResourceSet;
import static org.gecko.emf.ods.tests.helper.EMFODSExporterTestHelper.createBusinessPerson;
import static org.gecko.emf.ods.tests.helper.EMFODSExporterTestHelper.createFlintstonesFamily;
import static org.gecko.emf.ods.tests.helper.EMFODSExporterTestHelper.createRequest;
import static org.gecko.emf.ods.tests.helper.EMFODSExporterTestHelper.createSimpsonFamily;
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
import org.gecko.emf.exporter.ods.api.EMFODSExportOptions;
import org.gecko.emf.exporter.ods.api.annotations.RequireEMFODSExporter;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
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
 * EMF ODS exporter integration test.
 * 
 * @author Michal H. Siemaszko
 */
@Testable
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequireEMFODSExporter
@RequireEMF
public class EMFODSExporterTest {

	private static final String TREES_DATASET_XMI = System.getProperty("TREES_DATASET_XMI");

	@Order(value = -1)
	@Test
	public void testServices(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFODSExporter)") ServiceAware<EMFExporter> emfOdsExporterAware) {

		assertThat(emfOdsExporterAware.getServices()).hasSize(1);
		ServiceReference<EMFExporter> emfOdsExporterReference = emfOdsExporterAware.getServiceReference();
		assertThat(emfOdsExporterReference).isNotNull();
	}

	@Test
	public void testExportUtilModelExportNonContainmentDisabledAddMappingTableEnabledException(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFODSExporter)") ServiceAware<EMFExporter> emfOdsExporterAware)
			throws Exception {

		assertThat(emfOdsExporterAware.getServices()).hasSize(1);
		EMFExporter emfOdsExporterService = emfOdsExporterAware.getService();
		assertThat(emfOdsExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile(
				"testExportUtilModelExportNonContainmentDisabledAddMappingTableEnabledException", ".ods");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// incompatible combination of export options: 'export non-containment
		// references' option cannot be turned off if 'generate mapping table' option is
		// turned on!
		assertThatExceptionOfType(EMFExportException.class).isThrownBy(() -> {
			// @formatter:off
			emfOdsExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
					Map.of(
							EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//							EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
							EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true // defaults to false
						)
					);
			// @formatter:on
		});
	}

	@Test
	public void testExportUtilModelExportNonContainmentDisabledGenerateLinksEnabledException(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFODSExporter)") ServiceAware<EMFExporter> emfOdsExporterAware)
			throws Exception {

		assertThat(emfOdsExporterAware.getServices()).hasSize(1);
		EMFExporter emfOdsExporterService = emfOdsExporterAware.getService();
		assertThat(emfOdsExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files
				.createTempFile("testExportUtilModelExportNonContainmentDisabledGenerateLinksEnabledException", ".ods");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// incompatible combination of export options: 'export non-containment
		// references' option cannot be turned off if 'generate links for references'
		// option is turned on!
		assertThatExceptionOfType(EMFExportException.class).isThrownBy(() -> {
			// @formatter:off
			emfOdsExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
					Map.of(
							EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
//							EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
							EMFODSExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
						)
					);
			// @formatter:on
		});
	}

	@Test
	public void testExportExampleModelBasicResourceToOdsExportNonContainmentEnabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFODSExporter)") ServiceAware<EMFExporter> emfOdsExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfOdsExporterAware.getServices()).hasSize(1);
		EMFExporter emfOdsExporterService = emfOdsExporterAware.getService();
		assertThat(emfOdsExporterService).isNotNull();

		ResourceSet resourceSet = createBasicPackageResourceSet(basicPackage);
		Resource xmiResource = resourceSet.createResource(
				URI.createURI("testExportExampleModelBasicResourceToOdsExportNonContainmentEnabled.test"));
		assertNotNull(xmiResource);

		Family simpsonFamily = createSimpsonFamily(basicFactory);
		xmiResource.getContents().add(simpsonFamily);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);
		xmiResource.getContents().add(flintstonesFamily);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);
		xmiResource.getContents().add(businessPerson);

		Path filePath = Files.createTempFile("testExportExampleModelBasicResourceToOdsExportNonContainmentEnabled",
				".ods");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfOdsExporterService.exportResourceTo(xmiResource, fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFODSExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
						EMFODSExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportExampleModelBasicResourceToOdsExportNonContainmentDisabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFODSExporter)") ServiceAware<EMFExporter> emfOdsExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfOdsExporterAware.getServices()).hasSize(1);
		EMFExporter emfOdsExporterService = emfOdsExporterAware.getService();
		assertThat(emfOdsExporterService).isNotNull();

		ResourceSet resourceSet = createBasicPackageResourceSet(basicPackage);
		Resource xmiResource = resourceSet.createResource(
				URI.createURI("testExportExampleModelBasicResourceToOdsExportNonContainmentDisabled.test"));
		assertNotNull(xmiResource);

		Family simpsonFamily = createSimpsonFamily(basicFactory);
		xmiResource.getContents().add(simpsonFamily);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);
		xmiResource.getContents().add(flintstonesFamily);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);
		xmiResource.getContents().add(businessPerson);

		Path filePath = Files.createTempFile("testExportExampleModelBasicResourceToOdsExportNonContainmentDisabled",
				".ods");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfOdsExporterService.exportResourceTo(xmiResource, fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY
//						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, false, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
//						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, false, // defaults to false
//						EMFODSExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
//						EMFODSExportOptions.OPTION_GENERATE_LINKS, false // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelEObjectsToOdsNonContainmentEnabled(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFODSExporter)") ServiceAware<EMFExporter> emfOdsExporterAware)
			throws Exception {

		assertThat(emfOdsExporterAware.getServices()).hasSize(1);
		EMFExporter emfOdsExporterService = emfOdsExporterAware.getService();
		assertThat(emfOdsExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile("testExportUtilModelEObjectsToOdsNonContainmentEnabled", ".ods");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfOdsExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFODSExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
						EMFODSExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportTreesModelEObjectsToODSNonContainmentEnabled(
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFODSExporter)") ServiceAware<EMFExporter> emfOdsExporterAware)
			throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertThat(emfOdsExporterAware.getServices()).hasSize(1);
		EMFExporter emfOdsExporterService = emfOdsExporterAware.getService();
		assertThat(emfOdsExporterService).isNotNull();

		// register model
		EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
		packageRegistry.put(TreesPackage.eNS_URI, TreesPackage.eINSTANCE);

		// register xmi
		Map<String, Object> extensionFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionFactoryMap.put("xmi", new XMIResourceFactoryImpl());

		Resource resource = resourceSet.getResource(URI.createFileURI(new File(TREES_DATASET_XMI).getAbsolutePath()),
				true);

		Path filePath = Files.createTempFile("testExportTreesModelEObjectsToODSNonContainmentEnabled", ".ods");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfOdsExporterService.exportEObjectsTo(resource.getContents(), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFODSExportOptions.OPTION_ADJUST_COLUMN_WIDTH, true, // defaults to true
						EMFODSExportOptions.OPTION_GENERATE_LINKS, true // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
					)
				);
		// @formatter:on		
	}
}
