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
package org.gecko.emf.r_lang.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gecko.emf.r_lang.tests.helper.EMFRLangExporterTestHelper.createBusinessPerson;
import static org.gecko.emf.r_lang.tests.helper.EMFRLangExporterTestHelper.createFlintstonesFamily;
import static org.gecko.emf.r_lang.tests.helper.EMFRLangExporterTestHelper.createRequest;
import static org.gecko.emf.r_lang.tests.helper.EMFRLangExporterTestHelper.createSimpsonFamily;
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
import org.gecko.emf.exporter.EMFExportOptions;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.exporter.r_lang.api.EMFRLangExportOptions;
import org.gecko.emf.exporter.r_lang.api.annotations.RequireEMFRLangExporter;
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
 * EMF R Language exporter integration test.
 * 
 * @author Michal H. Siemaszko
 */
@Testable
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequireEMFRLangExporter
@RequireEMF
public class EMFRLangExporterTest {

	private static final String TREES_DATASET_XMI = System.getProperty("TREES_DATASET_XMI");

	@Order(value = -1)
	@Test
	public void testServices(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFRLangExporter)") ServiceAware<EMFExporter> emfRLangExporterAware) {

		assertThat(emfRLangExporterAware.getServices()).hasSize(1);
		ServiceReference<EMFExporter> emfRLangExporterReference = emfRLangExporterAware.getServiceReference();
		assertThat(emfRLangExporterReference).isNotNull();
	}

	@Test
	public void testExportExampleModelBasicEObjectsToRLangOneDataframePerFile(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFRLangExporter)") ServiceAware<EMFExporter> emfRLangExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfRLangExporterAware.getServices()).hasSize(1);
		EMFExporter emfRLangExporterService = emfRLangExporterAware.getService();
		assertThat(emfRLangExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);

		Path filePath = Files.createTempFile("testExportExampleModelBasicEObjectsToRLangOneDataframePerFile", ".zip");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfRLangExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily, businessPerson), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
//						EMFRLangExportOptions.OPTION_DATAFRAME_PER_FILE, true // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportExampleModelBasicEObjectsToRLangAllDataframesInOneFile(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFRLangExporter)") ServiceAware<EMFExporter> emfRLangExporterAware,
			@InjectService BasicFactory basicFactory, @InjectService BasicPackage basicPackage) throws Exception {

		assertThat(emfRLangExporterAware.getServices()).hasSize(1);
		EMFExporter emfRLangExporterService = emfRLangExporterAware.getService();
		assertThat(emfRLangExporterService).isNotNull();

		Family simpsonFamily = createSimpsonFamily(basicFactory);

		Family flintstonesFamily = createFlintstonesFamily(basicFactory);

		BusinessPerson businessPerson = createBusinessPerson(basicFactory);

		Path filePath = Files.createTempFile("testExportExampleModelBasicEObjectsToRLangAllDataframesInOneFile",
				".RData");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfRLangExporterService.exportEObjectsTo(List.of(simpsonFamily, flintstonesFamily, businessPerson), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFRLangExportOptions.OPTION_DATAFRAME_PER_FILE, false // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportUtilModelEObjectsToRLangOneDataframePerFile(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFRLangExporter)") ServiceAware<EMFExporter> emfRLangExporterAware)
			throws Exception {

		assertThat(emfRLangExporterAware.getServices()).hasSize(1);
		EMFExporter emfRLangExporterService = emfRLangExporterAware.getService();
		assertThat(emfRLangExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile("testExportUtilModelEObjectsToRLangOneDataframePerFile", ".zip");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfRLangExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
//						EMFRLangExportOptions.OPTION_DATAFRAME_PER_FILE, true // defaults to true
					)
				);				
		// @formatter:on
	}

	@Test
	public void testExportUtilModelEObjectsToRLangAllDataframesInOneFile(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFRLangExporter)") ServiceAware<EMFExporter> emfRLangExporterAware)
			throws Exception {

		assertThat(emfRLangExporterAware.getServices()).hasSize(1);
		EMFExporter emfRLangExporterService = emfRLangExporterAware.getService();
		assertThat(emfRLangExporterService).isNotNull();

		Request request1 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request2 = createRequest(UtilitiesFactory.eINSTANCE);

		Request request3 = createRequest(UtilitiesFactory.eINSTANCE);

		Path filePath = Files.createTempFile("testExportUtilModelEObjectsToRLangAllDataframesInOneFile", ".RData");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfRLangExporterService.exportEObjectsTo(List.of(request1, request2, request3), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFRLangExportOptions.OPTION_DATAFRAME_PER_FILE, false // defaults to true
					)
				);
		// @formatter:on
	}

	@Test
	public void testExportTreesModelEObjectsToRLangOneDataframePerFile(
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFRLangExporter)") ServiceAware<EMFExporter> emfRLangExporterAware)
			throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertThat(emfRLangExporterAware.getServices()).hasSize(1);
		EMFExporter emfRLangExporterService = emfRLangExporterAware.getService();
		assertThat(emfRLangExporterService).isNotNull();

		// register model
		EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
		packageRegistry.put(TreesPackage.eNS_URI, TreesPackage.eINSTANCE);

		// register xmi
		Map<String, Object> extensionFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionFactoryMap.put("xmi", new XMIResourceFactoryImpl());

		Resource resource = resourceSet.getResource(URI.createFileURI(new File(TREES_DATASET_XMI).getAbsolutePath()),
				true);

		Path filePath = Files.createTempFile("testExportTreesModelEObjectsToRLangOneDataframePerFile", ".zip");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfRLangExporterService.exportEObjectsTo(resource.getContents(), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
//						EMFRLangExportOptions.OPTION_DATAFRAME_PER_FILE, true // defaults to true
					)
				);				
		// @formatter:on
	}

	@Test
	public void testExportTreesModelEObjectsToRLangAllDataframesInOneFile(
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=EMFRLangExporter)") ServiceAware<EMFExporter> emfRLangExporterAware)
			throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertThat(emfRLangExporterAware.getServices()).hasSize(1);
		EMFExporter emfRLangExporterService = emfRLangExporterAware.getService();
		assertThat(emfRLangExporterService).isNotNull();

		// register model
		EPackage.Registry packageRegistry = resourceSet.getPackageRegistry();
		packageRegistry.put(TreesPackage.eNS_URI, TreesPackage.eINSTANCE);

		// register xmi
		Map<String, Object> extensionFactoryMap = Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap();
		extensionFactoryMap.put("xmi", new XMIResourceFactoryImpl());

		Resource resource = resourceSet.getResource(URI.createFileURI(new File(TREES_DATASET_XMI).getAbsolutePath()),
				true);

		Path filePath = Files.createTempFile("testExportTreesModelEObjectsToRLangAllDataframesInOneFile", ".RData");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		emfRLangExporterService.exportEObjectsTo(resource.getContents(), fileOutputStream, 
				Map.of(
						EMFExportOptions.OPTION_LOCALE, Locale.GERMANY,
						EMFExportOptions.OPTION_EXPORT_NONCONTAINMENT, true, // defaults to false
//						EMFExportOptions.OPTION_EXPORT_METADATA, true, // defaults to true
						EMFExportOptions.OPTION_ADD_MAPPING_TABLE, true, // defaults to false
//						EMFExportOptions.OPTION_SHOW_URIS, true, // defaults to true
//						EMFExportOptions.OPTION_SHOW_REFS, true, // defaults to true
						EMFRLangExportOptions.OPTION_DATAFRAME_PER_FILE, false // defaults to true
					)
				);				
		// @formatter:on
	}
}
