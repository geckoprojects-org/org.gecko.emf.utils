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
import static org.gecko.emf.converter.tests.helper.DTOToEObjectConverterImplTestHelper.eClassifiersTotalCount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.converter.JavaToEPackageConverter;
import org.junit.jupiter.api.Disabled;
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

/**
 * Integration test for
 * {@link org.gecko.emf.converter.JavaReferenceTypeToEPackageConverter}
 *
 * @author Michal H. Siemaszko
 */
@Testable
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JavaReferenceTypeToEPackageConverterTest {
	private static final String JAKARTA_WS_RS_API_JAR_PATH = getArtifactM2RepoPath("jakarta.ws.rs", "jakarta.ws.rs-api",
			"3.1.0");
	private static final String JAKARTA_XML_BIND_API_JAR_PATH = getArtifactM2RepoPath("jakarta.xml.bind",
			"jakarta.xml.bind-api", "3.0.1");

	private static final String ORG_APACHE_FELIX_HTTP_SERVLET_API = getArtifactM2RepoPath("org.apache.felix",
			"org.apache.felix.http.servlet-api", "2.1.0");

	private static final String PACKAGE_NAME = "javareferencetype_to_epackage_converter_test";
	private static final String NS_URI = "http://gecko.org/test/model/converter/1.0";
	private static final String NS_PREFIX = "tests";

	@Order(value = -1)
	@Test
	public void testServices(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=JavaReferenceTypeToEPackageConverter)") ServiceAware<JavaToEPackageConverter> javaReferenceTypeToEPackageConverterAware) {

		assertThat(javaReferenceTypeToEPackageConverterAware.getServices()).hasSize(1);
		ServiceReference<JavaToEPackageConverter> javaReferenceTypeToEPackageConverterReference = javaReferenceTypeToEPackageConverterAware
				.getServiceReference();
		assertThat(javaReferenceTypeToEPackageConverterReference).isNotNull();
	}

	/**
	 * When run via Jenkins build ( see this project's `Jenkinsfile`), Maven
	 * repository location is per workspace, set via `maven.repo.local` system
	 * property passed to Gradle via command line.
	 *
	 * Unfortunately, due to nature of how integration tests are run, Gradle command
	 * line system properties are not propagated, hence these these two test cases
	 * are disabled.
	 *
	 * If run in your local development environment, without overriding Maven
	 * repository location, you can safely enable them.
	 */
	@Disabled
	@Test
	public void testConvertJakartaRESTfulWSAPI(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=JavaReferenceTypeToEPackageConverter)") ServiceAware<JavaToEPackageConverter> javaReferenceTypeToEPackageConverterAware)
			throws Exception {
		assertThat(javaReferenceTypeToEPackageConverterAware.getServices()).hasSize(1);
		JavaToEPackageConverter javaReferenceTypeToEPackageConverterService = javaReferenceTypeToEPackageConverterAware
				.getService();
		assertThat(javaReferenceTypeToEPackageConverterService).isNotNull();

		EPackage dynamicEPackageFromJavaReferenceTypes = javaReferenceTypeToEPackageConverterService.convert(
				PACKAGE_NAME, NS_URI, NS_PREFIX, Paths.get(JAKARTA_WS_RS_API_JAR_PATH),
				Paths.get(JAKARTA_XML_BIND_API_JAR_PATH));
		assertNotNull(dynamicEPackageFromJavaReferenceTypes);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromJavaReferenceTypes.getName());
		assertEquals(NS_URI, dynamicEPackageFromJavaReferenceTypes.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromJavaReferenceTypes.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromJavaReferenceTypes.getNsURI(),
				dynamicEPackageFromJavaReferenceTypes);

		assertThat(eClassifiersTotalCount(dynamicEPackageFromJavaReferenceTypes)).isEqualTo(182);
	}

	@Disabled
	@Test
	public void testConvertJakartaRESTfulWSAPIAndSerializeToStaticEMF(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=JavaReferenceTypeToEPackageConverter)") ServiceAware<JavaToEPackageConverter> javaReferenceTypeToEPackageConverterAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) throws Exception {
		assertThat(javaReferenceTypeToEPackageConverterAware.getServices()).hasSize(1);
		JavaToEPackageConverter javaReferenceTypeToEPackageConverterService = javaReferenceTypeToEPackageConverterAware
				.getService();
		assertThat(javaReferenceTypeToEPackageConverterService).isNotNull();

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		EPackage dynamicEPackageFromJavaReferenceTypes = javaReferenceTypeToEPackageConverterService.convert(
				PACKAGE_NAME, NS_URI, NS_PREFIX, Paths.get(JAKARTA_WS_RS_API_JAR_PATH),
				Paths.get(JAKARTA_XML_BIND_API_JAR_PATH));
		assertNotNull(dynamicEPackageFromJavaReferenceTypes);

		EAnnotation versionEAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
		versionEAnnotation.setSource("Version");
		versionEAnnotation.getDetails().put("value", "3.1.0");
		dynamicEPackageFromJavaReferenceTypes.getEAnnotations().add(versionEAnnotation);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromJavaReferenceTypes.getName());
		assertEquals(NS_URI, dynamicEPackageFromJavaReferenceTypes.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromJavaReferenceTypes.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromJavaReferenceTypes.getNsURI(),
				dynamicEPackageFromJavaReferenceTypes);

		assertThat(eClassifiersTotalCount(dynamicEPackageFromJavaReferenceTypes)).isEqualTo(182);

		Resource resource = resourceSet.createResource(URI.createFileURI("jakarta.ws.rs-api-3.1.0.ecore"));

		resource.getContents().add(dynamicEPackageFromJavaReferenceTypes);
		resource.save(null);
	}

	// Example of JAR which contains packages from different namespaces -
	// `org.apache.felix:org.apache.felix.http.servlet-api:2.1.0` contains packages
	// from both `jakarta.servlet` and `javax.servlet` namespaces; there there are
	// many JARs like such!
	@Disabled
	@Test
	public void testConvertOrgApacheFelixHttpServletApiAndSerializeToStaticEMF(
			@InjectService(cardinality = 1, timeout = 4000, filter = "(component.name=JavaReferenceTypeToEPackageConverter)") ServiceAware<JavaToEPackageConverter> javaReferenceTypeToEPackageConverterAware,
			@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware) throws Exception {
		assertThat(javaReferenceTypeToEPackageConverterAware.getServices()).hasSize(1);
		JavaToEPackageConverter javaReferenceTypeToEPackageConverterService = javaReferenceTypeToEPackageConverterAware
				.getService();
		assertThat(javaReferenceTypeToEPackageConverterService).isNotNull();

		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);

		String nsURI = "https://geckoprojects.org/jakarta/servlet/2.1.0/";

		EPackage dynamicEPackageFromJavaReferenceTypes = javaReferenceTypeToEPackageConverterService
				.convert(PACKAGE_NAME, nsURI, NS_PREFIX, Paths.get(ORG_APACHE_FELIX_HTTP_SERVLET_API));
		assertNotNull(dynamicEPackageFromJavaReferenceTypes);

		EAnnotation versionEAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
		versionEAnnotation.setSource("Version");
		versionEAnnotation.getDetails().put("value", "2.1.0");
		dynamicEPackageFromJavaReferenceTypes.getEAnnotations().add(versionEAnnotation);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromJavaReferenceTypes.getName());
		assertEquals(nsURI, dynamicEPackageFromJavaReferenceTypes.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromJavaReferenceTypes.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromJavaReferenceTypes.getNsURI(),
				dynamicEPackageFromJavaReferenceTypes);

		assertThat(eClassifiersTotalCount(dynamicEPackageFromJavaReferenceTypes)).isEqualTo(119);

		Resource resource = resourceSet
				.createResource(URI.createFileURI("org.apache.felix.http.servlet-api-2.1.0.ecore"));

		resource.getContents().add(dynamicEPackageFromJavaReferenceTypes);
		resource.save(null);
	}

	public static String getArtifactM2RepoPath(String groupId, String artifactId, String version) {
		try {
			File userHome = new File(System.getProperty("user.home"));

			Path m2Repository = Paths.get(userHome.getCanonicalPath(), ".m2/repository");

			Path projectHome = Paths.get(m2Repository.toAbsolutePath().toString(), groupId.replace('.', '/'));

			StringBuilder artifactName = new StringBuilder();
			artifactName.append(artifactId);
			artifactName.append("-");
			artifactName.append(version);
			artifactName.append(".jar");

			return Paths.get(projectHome.toAbsolutePath().toString(), artifactId, version, artifactName.toString())
					.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
