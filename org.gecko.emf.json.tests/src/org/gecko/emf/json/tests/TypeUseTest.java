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
import org.gecko.emf.util.example.model.examplemodel.Polygon;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

/**
 * 
 * @author ilenia
 * @since Jul 25, 2022
 */
public class TypeUseTest {
	
	@Order(-1)
	@Test
	public void testServices(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {
		
		checkServices(rsAware, examplePackageAware);
	}
	
	@Test
	public void testTypeUseByNameEmpty(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
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
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);		
	}
	
	@Test
	public void testTypeUseByNameNullURI(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
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
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, null);
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);	
	}
	
	@Test
	public void testTypeUseByName(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
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
		
		checkResource(inRes);	
	}
	
	@Test
	public void testTypeUseByNameArray(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
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
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, new String[] {modelPackage.getNsURI()});
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);	
	}
	
	@Test
	public void testTypeUseByNameSigle(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
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
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, modelPackage.getNsURI());
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);	
	}
	
	@Test
	public void testTypeUseByClassEmpty(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {
		
		checkServices(rsAware, examplePackageAware);
		
		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);
		
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);
		
		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleTypeByClass.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));
		
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_TYPE_USE, EcoreTypeInfo.USE.CLASS);
		loadOptions.put(EMFJs.OPTION_TYPE_FIELD, "type");
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);
	}
	
	@Test
	public void testTypeUseByClassNullURI(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {
		
		checkServices(rsAware, examplePackageAware);
		
		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);
		
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);
		
		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleTypeByClass.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));
		
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_TYPE_USE, EcoreTypeInfo.USE.CLASS);
		loadOptions.put(EMFJs.OPTION_TYPE_FIELD, "type");
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, null);
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);	
	}
	
	@Test
	public void testTypeUseByClass(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {
		
		checkServices(rsAware, examplePackageAware);
		
		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);
		
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);
		
		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleTypeByClass.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));
		
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_TYPE_USE, EcoreTypeInfo.USE.CLASS);
		loadOptions.put(EMFJs.OPTION_TYPE_FIELD, "type");
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, List.of(modelPackage.getNsURI()));
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);	
	}
	
	
	@Test
	public void testTypeUseByClassArray(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {
		
		checkServices(rsAware, examplePackageAware);
		
		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);
		
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);
		
		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleTypeByClass.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));
		
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_TYPE_USE, EcoreTypeInfo.USE.CLASS);
		loadOptions.put(EMFJs.OPTION_TYPE_FIELD, "type");
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, new String[] {modelPackage.getNsURI()});
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);
	}
	
	@Test
	public void testTypeUseByClassSingle(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {
		
		checkServices(rsAware, examplePackageAware);
		
		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);
		
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);
		
		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleTypeByClass.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));
		
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_TYPE_USE, EcoreTypeInfo.USE.CLASS);
		loadOptions.put(EMFJs.OPTION_TYPE_FIELD, "type");
		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, modelPackage.getNsURI());
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);
	}
	
	@Test
	public void testTypeUseByURI(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {
		
		checkServices(rsAware, examplePackageAware);
		
		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);
	
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);
		
		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleTypeByURI.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));
		
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_TYPE_USE, EcoreTypeInfo.USE.URI);
		loadOptions.put(EMFJs.OPTION_TYPE_FIELD, "type");
//		In this case we do not specify the option EMFJs.OPTION_TYPE_PACKAGE_URIS
//		loadOptions.put(EMFJs.OPTION_TYPE_PACKAGE_URIS, List.of(modelPackage.getNsURI()));
		
		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		checkResource(inRes);	
	}

	
	private void checkServices(ServiceAware<ResourceSet> rsAware, ServiceAware<ExampleModelPackage> examplePackageAware) {
		
		assertNotNull(examplePackageAware);
		assertThat(examplePackageAware.getServices()).hasSize(1);
		
		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
	}
	
	private void checkResource(Resource inRes) {
		
		assertThat(inRes.getContents()).isNotEmpty();
		assertThat(inRes.getContents()).hasSize(1);
		assertThat(inRes.getContents().get(0)).isInstanceOf(Building.class);
		
		Building loadedObj = (Building) inRes.getContents().get(0);
		
		assertThat(loadedObj.getLocation()).isNotNull();
		assertThat(loadedObj.getLocation()).isInstanceOf(Polygon.class);		
	}
}
