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
package org.gecko.emf.converter.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.converter.JavaReferenceTypeToEPackageConverter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration test for {@link org.gecko.emf.converter.JavaReferenceTypeToEPackageConverter}
 * 
 * @author Michal H. Siemaszko
 */
@Testable
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class JavaReferenceTypeToEPackageConverterTest {
	private static final String JAKARTA_WS_RS_API_JAR_PATH = "/home/michal/.m2/repository/jakarta/ws/rs/jakarta.ws.rs-api/3.1.0/jakarta.ws.rs-api-3.1.0.jar";
	private static final String JAKARTA_XML_BIND_API_JAR_PATH = "/home/michal/.m2/repository/jakarta/xml/bind/jakarta.xml.bind-api/3.0.1/jakarta.xml.bind-api-3.0.1.jar";

	private static final String PACKAGE_NAME = "dto_to_epackage_converter_test";
	private static final String NS_URI = "http://gecko.org/test/model/converter/1.0";
	private static final String NS_PREFIX = "tests";

	@Test
	public void testConvertJakartaRESTfulWSAPI() throws Exception {
		EPackage dynamicEPackageFromDTOs = JavaReferenceTypeToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX,
				Paths.get(JAKARTA_WS_RS_API_JAR_PATH), Paths.get(JAKARTA_XML_BIND_API_JAR_PATH));
		assertNotNull(dynamicEPackageFromDTOs);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromDTOs.getName());
		assertEquals(NS_URI, dynamicEPackageFromDTOs.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromDTOs.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		assertThat(dynamicEPackageFromDTOs.getEClassifiers()).hasSize(146);
	}

	@Test
	public void testConvertJakartaRESTfulWSAPIAndSerializeToStaticEMF(
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) throws Exception {
		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		EPackage dynamicEPackageFromDTOs = JavaReferenceTypeToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX,
				Paths.get(JAKARTA_WS_RS_API_JAR_PATH), Paths.get(JAKARTA_XML_BIND_API_JAR_PATH));
		assertNotNull(dynamicEPackageFromDTOs);

		EAnnotation versionEAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
		versionEAnnotation.setSource("Version");
		versionEAnnotation.getDetails().put("value", "3.1.0");
		dynamicEPackageFromDTOs.getEAnnotations().add(versionEAnnotation);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromDTOs.getName());
		assertEquals(NS_URI, dynamicEPackageFromDTOs.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromDTOs.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		assertThat(dynamicEPackageFromDTOs.getEClassifiers()).hasSize(146);

		Resource resource = resourceSet.createResource(URI.createFileURI("jakarta.ws.rs-api-3.1.0.ecore"));

		resource.getContents().add(dynamicEPackageFromDTOs);
		resource.save(null);
	}
}
