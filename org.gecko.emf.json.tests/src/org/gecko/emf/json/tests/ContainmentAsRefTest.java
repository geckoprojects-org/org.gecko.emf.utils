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
package org.gecko.emf.json.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emfcloud.jackson.annotations.EcoreTypeInfo;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.gecko.emf.json.constants.EMFJs;
import org.gecko.emf.util.example.model.examplemodel.Building;
import org.gecko.emf.util.example.model.examplemodel.ExampleModelPackage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

/**
 * 
 * @author ilenia
 * @since Jul 26, 2022
 */
public class ContainmentAsRefTest {
	
	@Order(-1)
	@Test
	public void testServices(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {

		checkServices(rsAware, examplePackageAware);
	}
	
	@Disabled("Setting this option has no effect currently, as all the containemnet references are output as complete objects and not as references")
	@Test 
	public void testContainementAsRefFalse(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {

		checkServices(rsAware, examplePackageAware);
		
		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);

		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleTypeByName.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));
		
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_TYPE_USE, EcoreTypeInfo.USE.NAME);
		loadOptions.put(EMFJs.OPTION_TYPE_FIELD, "type");
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, List.of(modelPackage.getNsURI()));
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}

		assertThat(inRes.getContents()).isNotNull();
		assertThat(inRes.getContents()).isNotEmpty();
		assertThat(inRes.getContents()).hasSize(1);
		assertThat(inRes.getContents().get(0)).isInstanceOf(Building.class);
		
		Building loadedObj = (Building) inRes.getContents().get(0);
		
		Map<Object, Object> saveOptions = new HashMap<Object, Object>();
		saveOptions.put(EMFJs.OPTION_SERIALIZE_CONTAINMENT_AS_HREF, false);
		
		String pathToJsonOutputFile = System.getProperty("base.path") + "/data/exampleContainmentAsRefFalse.json";
		Resource outRes = resourceSet.createResource(URI.createFileURI(pathToJsonOutputFile));
		outRes.getContents().add(loadedObj);
		
		try {
			outRes.save(saveOptions);
		} catch (Exception e) {
			fail("Error saving Resource! " + e);
		}
		
//		try (FileInputStream fis = new FileInputStream(pathToJsonOutputFile);) {
//			byte[] bytes = fis.readAllBytes();
//			String str = new String(bytes);
//			assertThat(str.contains("\t"));
//		} catch (IOException e) {
//			fail("Error reading File! " + e);
//		}
	}
	
	@Disabled("Setting this option has no effect currently, as all the containemnet references are output as complete objects and not as references")
	@Test 
	public void testContainementAsRefTrue(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {

		checkServices(rsAware, examplePackageAware);
		
		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);

		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleTypeByName.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));
		
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_TYPE_USE, EcoreTypeInfo.USE.NAME);
		loadOptions.put(EMFJs.OPTION_TYPE_FIELD, "type");
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, List.of(modelPackage.getNsURI()));
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}

		assertThat(inRes.getContents()).isNotNull();
		assertThat(inRes.getContents()).isNotEmpty();
		assertThat(inRes.getContents()).hasSize(1);
		assertThat(inRes.getContents().get(0)).isInstanceOf(Building.class);
		
		Building loadedObj = (Building) inRes.getContents().get(0);
		
		Map<Object, Object> saveOptions = new HashMap<Object, Object>();
		saveOptions.put(EMFJs.OPTION_SERIALIZE_CONTAINMENT_AS_HREF, true);
		
		String pathToJsonOutputFile = System.getProperty("base.path") + "/data/exampleContainmentAsRefTrue.json";
		Resource outRes = resourceSet.createResource(URI.createFileURI(pathToJsonOutputFile));
		outRes.getContents().add(loadedObj);
		
		try {
			outRes.save(saveOptions);
		} catch (Exception e) {
			fail("Error saving Resource! " + e);
		}
		
//		try (FileInputStream fis = new FileInputStream(pathToJsonOutputFile);) {
//			byte[] bytes = fis.readAllBytes();
//			String str = new String(bytes);
//			assertThat(str.contains("\t"));
//		} catch (IOException e) {
//			fail("Error reading File! " + e);
//		}
	}
	
	private void checkServices(ServiceAware<ResourceSet> rsAware, ServiceAware<ExampleModelPackage> examplePackageAware) {

		assertNotNull(examplePackageAware);
		assertThat(examplePackageAware.getServices()).hasSize(1);

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);	
	}

}
