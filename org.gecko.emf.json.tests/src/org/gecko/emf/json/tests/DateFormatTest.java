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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emfcloud.jackson.databind.EMFContext;
import org.gecko.emf.json.constants.EMFJs;
import org.gecko.emf.util.example.model.examplemodel.Building;
import org.gecko.emf.util.example.model.examplemodel.ExampleModelPackage;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

/**
 * 
 * @author ilenia
 * @since Jul 26, 2022
 */
public class DateFormatTest {

	@Order(-1)
	@Test
	public void testServices(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {

		checkServices(rsAware, examplePackageAware);
	}

	@Test
	public void testDateFormatOnlyDate(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {

		checkServices(rsAware, examplePackageAware);

		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);

		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleDateFormat.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));

		String dateFormat = "yyyy-MM-dd";
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_DATE_FORMAT, dateFormat);

		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}

		Building loadedObj = getLoadedObject(inRes);	
		
		assertThat(loadedObj.getStartDate()).isNotNull();
		LocalDateTime ldt = loadedObj.getStartDate().toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDateTime();
		assertThat(ldt.getMinute()).isEqualTo(0);
	}
	
	@Test
	public void testDateFormatDateTime(@InjectService(timeout = 2000) ServiceAware<ExampleModelPackage> examplePackageAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) {

		checkServices(rsAware, examplePackageAware);

		ExampleModelPackage modelPackage = examplePackageAware.getService();
		assertNotNull(modelPackage);

		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		String pathToJsonInputFile = System.getProperty("base.path") + "/data/exampleDateFormat.json";
		Resource inRes = resourceSet.createResource(URI.createFileURI(pathToJsonInputFile));

		String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
		Map<Object, Object> loadOptions = new HashMap<Object, Object>();
		loadOptions.put(EMFContext.Attributes.ROOT_ELEMENT, (EClass) modelPackage.getEClassifier("Building"));
		loadOptions.put(EMFJs.OPTION_DATE_FORMAT, dateFormat);

		try {
			inRes.load(loadOptions);
		} catch (Exception e) {
			fail("Error loading Resource! " + e);
		}
		
		
		Building loadedObj = getLoadedObject(inRes);	
		
		assertThat(loadedObj.getStartDate()).isNotNull();
		LocalDateTime ldt = loadedObj.getStartDate().toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDateTime();
		assertThat(ldt.getMinute()).isNotEqualTo(0);
	}

	private void checkServices(ServiceAware<ResourceSet> rsAware, ServiceAware<ExampleModelPackage> examplePackageAware) {

		assertNotNull(examplePackageAware);
		assertThat(examplePackageAware.getServices()).hasSize(1);

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
	}

	private Building getLoadedObject(Resource inRes) {

		assertThat(inRes.getContents()).isNotEmpty();
		assertThat(inRes.getContents()).hasSize(1);
		assertThat(inRes.getContents().get(0)).isInstanceOf(Building.class);

		return (Building) inRes.getContents().get(0);
	}
	
	
}
