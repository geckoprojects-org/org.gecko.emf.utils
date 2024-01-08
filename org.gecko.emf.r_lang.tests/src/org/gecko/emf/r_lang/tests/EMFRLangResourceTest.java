/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
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
import static org.gecko.emf.r_lang.tests.helper.EMFRLangResourceTestHelper.createBusinessPerson;
import static org.gecko.emf.r_lang.tests.helper.EMFRLangResourceTestHelper.createFlintstonesFamily;
import static org.gecko.emf.r_lang.tests.helper.EMFRLangResourceTestHelper.createSimpsonFamily;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.exporter.EMFExportOptions;
import org.gecko.emf.exporter.r_lang.api.EMFRLangExportOptions;
import org.gecko.emf.osgi.example.model.basic.BasicFactory;
import org.gecko.emf.osgi.example.model.basic.BusinessPerson;
import org.gecko.emf.osgi.example.model.basic.Family;
import org.gecko.emf.r_lang.annotation.RequireEMFRLang;
import org.gecko.emf.r_lang.configuration.EMFRLangResource;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * EMF R Language Resource integration test.
 * 
 * @author Michal H. Siemaszko
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@RequireEMFRLang
public class EMFRLangResourceTest {

	@Order(value = -1)
	@Test
	public void testServices(@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(timeout = 2000) ServiceAware<BasicFactory> bfAware) {

		assertThat(rsAware.getServices()).hasSize(1);
		ServiceReference<ResourceSet> rsReference = rsAware.getServiceReference();
		assertThat(rsReference).isNotNull();

		assertThat(bfAware.getServices()).hasSize(1);
		ServiceReference<BasicFactory> bfReference = bfAware.getServiceReference();
		assertThat(bfReference).isNotNull();
	}

	@Test
	public void testSaveResourceToRLang(@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(timeout = 2000) ServiceAware<BasicFactory> bfAware) throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertNotNull(bfAware);
		assertThat(bfAware.getServices()).hasSize(1);
		BasicFactory factoryImpl = bfAware.getService();
		assertNotNull(factoryImpl);

		Resource resource = resourceSet.createResource(URI.createURI("testSaveResourceToRLang.RData"));
		assertNotNull(resource);
		assertTrue(resource instanceof EMFRLangResource);

		Family simpsonFamily = createSimpsonFamily(factoryImpl);
		resource.getContents().add(simpsonFamily);

		Family flintstonesFamily = createFlintstonesFamily(factoryImpl);
		resource.getContents().add(flintstonesFamily);

		BusinessPerson businessPerson = createBusinessPerson(factoryImpl);
		resource.getContents().add(businessPerson);

		Path filePath = Files.createTempFile("testSaveResourceToRLang", ".RData");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		// @formatter:off
		resource.save(fileOutputStream, 
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
	public void testSaveResourceToRLangNoOpts(@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService(timeout = 2000) ServiceAware<BasicFactory> bfAware) throws Exception {

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		assertNotNull(bfAware);
		assertThat(bfAware.getServices()).hasSize(1);
		BasicFactory factoryImpl = bfAware.getService();
		assertNotNull(factoryImpl);

		Resource resource = resourceSet.createResource(URI.createURI("testSaveResourceToRLangNoOpts.RData"));
		assertNotNull(resource);
		assertTrue(resource instanceof EMFRLangResource);

		Family simpsonFamily = createSimpsonFamily(factoryImpl);
		resource.getContents().add(simpsonFamily);

		Family flintstonesFamily = createFlintstonesFamily(factoryImpl);
		resource.getContents().add(flintstonesFamily);

		BusinessPerson businessPerson = createBusinessPerson(factoryImpl);
		resource.getContents().add(businessPerson);

		Path filePath = Files.createTempFile("testSaveResourceToRLangNoOpts", ".RData");

		OutputStream fileOutputStream = Files.newOutputStream(filePath);

		resource.save(fileOutputStream, Map.of()); // will use default options
	}
}
