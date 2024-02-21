/**
 * Copyright (c) 2012 - 2017 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.qvt.osgi.tests;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.m2m.qvt.oml.TransformationExecutor;
import org.eclipse.m2m.qvt.oml.TransformationExecutor.BlackboxRegistry;
import org.gecko.emf.osgi.example.model.basic.Address;
import org.gecko.emf.osgi.example.model.basic.BasicFactory;
import org.gecko.emf.osgi.example.model.basic.GenderType;
import org.gecko.emf.osgi.example.model.basic.Person;
import org.gecko.qvt.osgi.api.ModelTransformationConstants;
import org.gecko.qvt.osgi.api.ModelTransformator;
import org.gecko.qvt.osgi.tests.bbox.BlackboxTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.condition.Condition;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Test QVT Mapping
 * @author mark
 * @since 20.10.2017
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class QVTTransformatorIntegrationTest  {

	@InjectBundleContext
	BundleContext context;
	
	@Test
	@WithFactoryConfiguration(name = "testSimple", factoryPid = ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, location = "?", properties = @Property(key = ModelTransformationConstants.TEMPLATE_PATH, value = "org.gecko.qvt.osgi.tests/PersonTransformation.qvto"))
	public void testSimple(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		r1.getContents().add(p1);
		
		ModelTransformator transformator = transformatorAware.waitForService(500);
		assertNotNull(transformator);
		
		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
	}

	@Test
	@WithFactoryConfiguration(name = "testWithRemoteTemplate", factoryPid = ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, location = "?", properties = @Property(key = ModelTransformationConstants.TEMPLATE_URI, value = "https://raw.githubusercontent.com/geckoprojects-org/org.gecko.emf.utils/main/org.gecko.qvt.osgi.tests/transforms/PersonTransformation.qvto"))
	public void testWithRemoteTemplate(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		r1.getContents().add(p1);
		
		ModelTransformator transformator = transformatorAware.waitForService(500);
		assertNotNull(transformator);
		
		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
	}

	@Test
	@WithFactoryConfiguration(name = "testExampleWithDeps",  factoryPid = ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, location = "?", properties = @Property(key = ModelTransformationConstants.TEMPLATE_PATH, value = "org.gecko.qvt.osgi.tests/PersonTransformationWithDeps.qvto"))
	public void testExampleWithDeps(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		Address address = BasicFactory.eINSTANCE.createAddress();
		address.setCity("Gera");
		address.setStreet("Kurt-Keicher");
		p1.setAddress(address);
		
		r1.getContents().add(p1);
		
		ModelTransformator transformator = transformatorAware.waitForService(500);
		assertNotNull(transformator);
		
		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
		assertNotNull(resultPerson.getAddress());
		Address resultAddress = resultPerson.getAddress();
		assertEquals("SesamKurt-Keicher", resultAddress.getStreet());
		assertEquals("MyBeautifulGera", resultAddress.getCity());
	}
	
	@Test
	@WithFactoryConfiguration(name = "testExampleWithDepsLibrary",  factoryPid = ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, location = "?", properties = @Property(key = ModelTransformationConstants.TEMPLATE_PATH, value = "org.gecko.qvt.osgi.tests/PersonTransformationWithDepsLib.qvto"))
	public void testExampleWithDepsLibrary(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		Address address = BasicFactory.eINSTANCE.createAddress();
		address.setCity("Gera");
		address.setStreet("Kurt-Keicher");
		p1.setAddress(address);
		
		r1.getContents().add(p1);
		
		ModelTransformator transformator = transformatorAware.waitForService(500);
		assertNotNull(transformator);
		
		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
		assertNotNull(resultPerson.getAddress());
		Address resultAddress = resultPerson.getAddress();
		assertEquals("SesamKurt-Keicher", resultAddress.getStreet());
		assertEquals("MyBeautifulGera", resultAddress.getCity());
	}
	
	@Test
	public void testExampleWithBlackbox01(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService ConfigurationAdmin admin,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException, IOException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		Address address = BasicFactory.eINSTANCE.createAddress();
		address.setCity("Gera");
		address.setStreet("Kurt-Keicher");
		p1.setAddress(address);
		
		r1.getContents().add(p1);
		
		BlackboxRegistry.INSTANCE.registerModule(BlackboxTest.class);
		
		Configuration configuration = admin.createFactoryConfiguration(ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, "?");
		
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(ModelTransformationConstants.TEMPLATE_PATH, "org.gecko.qvt.osgi.tests/PersonTransformationWithBlackbox.qvto");
		
		configuration.update(props);
		
		ModelTransformator transformator = transformatorAware.waitForService(500);
		assertNotNull(transformator);
		
		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
		assertNotNull(resultPerson.getAddress());
		Address resultAddress = resultPerson.getAddress();
		assertEquals("Kurt-KeicherCopy", resultAddress.getStreet());
		assertEquals("GeraCopy", resultAddress.getCity());
	}
	
	@Test
	public void testExampleWithBlackbox02(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService ConfigurationAdmin admin,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException, IOException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		Address address = BasicFactory.eINSTANCE.createAddress();
		address.setCity("Gera");
		address.setStreet("Kurt-Keicher");
		p1.setAddress(address);
		
		r1.getContents().add(p1);
		
		TransformationExecutor.BlackboxRegistry.INSTANCE.registerModule(BlackboxTest.class);
		
		Configuration configuration = admin.createFactoryConfiguration(ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, "?");
		
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(ModelTransformationConstants.TEMPLATE_PATH, "org.gecko.qvt.osgi.tests/PersonTransformationWithBlackbox.qvto");
		
		configuration.update(props);
		
		ModelTransformator transformator = transformatorAware.waitForService(500);
		assertNotNull(transformator);
		
		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
		assertNotNull(resultPerson.getAddress());
		Address resultAddress = resultPerson.getAddress();
		assertEquals("Kurt-KeicherCopy", resultAddress.getStreet());
		assertEquals("GeraCopy", resultAddress.getCity());
	}
	
	@Test
	public void testExampleWithBlackboxAlternativeName01(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService ConfigurationAdmin admin,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException, IOException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		Address address = BasicFactory.eINSTANCE.createAddress();
		address.setCity("Gera");
		address.setStreet("Kurt-Keicher");
		p1.setAddress(address);
		
		r1.getContents().add(p1);
		
		TransformationExecutor.BlackboxRegistry.INSTANCE.registerModule(BlackboxTest.class, "org.gecko.MyBB", "MyBBTest");
		
		Configuration configuration = admin.createFactoryConfiguration(ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, "?");
		
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(ModelTransformationConstants.TEMPLATE_PATH, "org.gecko.qvt.osgi.tests/PersonTransformationWithBlackboxAltName.qvto");
		
		configuration.update(props);
		
		ModelTransformator transformator = transformatorAware.waitForService(500);
		assertNotNull(transformator);
		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
		assertNotNull(resultPerson.getAddress());
		Address resultAddress = resultPerson.getAddress();
		assertEquals("Kurt-KeicherCopy", resultAddress.getStreet());
		assertEquals("GeraCopy", resultAddress.getCity());
	}
	
	@Test
	public void testExampleWithBlackboxAlternativeName02(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService ConfigurationAdmin admin,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException, IOException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		Address address = BasicFactory.eINSTANCE.createAddress();
		address.setCity("Gera");
		address.setStreet("Kurt-Keicher");
		p1.setAddress(address);
		
		r1.getContents().add(p1);
		
		TransformationExecutor.BlackboxRegistry.INSTANCE.registerModule(BlackboxTest.class);
		
		Configuration configuration = admin.createFactoryConfiguration(ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, "?");
		
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(ModelTransformationConstants.TEMPLATE_PATH, "org.gecko.qvt.osgi.tests/PersonTransformationWithBlackbox.qvto");
		
		configuration.update(props);
		
		ModelTransformator transformator = transformatorAware.waitForService(500);
		assertNotNull(transformator);
		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
		assertNotNull(resultPerson.getAddress());
		Address resultAddress = resultPerson.getAddress();
		assertEquals("Kurt-KeicherCopy", resultAddress.getStreet());
		assertEquals("GeraCopy", resultAddress.getCity());
	}
	
	@Test
	public void testExampleWithBlackboxService01(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService ConfigurationAdmin admin,
			@InjectService(cardinality = 0) ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException, IOException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		Address address = BasicFactory.eINSTANCE.createAddress();
		address.setCity("Gera");
		address.setStreet("Kurt-Keicher");
		p1.setAddress(address);
		
		r1.getContents().add(p1);
		
		Configuration configuration = admin.createFactoryConfiguration(ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, "?");
		
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put(ModelTransformationConstants.TEMPLATE_PATH, "org.gecko.qvt.osgi.tests/PersonTransformationWithBlackboxService.qvto");
//		
		BlackboxTest bbt = new BlackboxTest();
		
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(ModelTransformationConstants.QVT_BLACKBOX, "true");
		properties.put(ModelTransformationConstants.BLACKBOX_MODULENAME, "MyserviceBB");
		properties.put(ModelTransformationConstants.BLACKBOX_QUALIFIED_UNIT_NAME, "org.gecko.service.MyBB");
		
		ServiceRegistration<BlackboxTest> bbRegistration = context.registerService(BlackboxTest.class, bbt, properties);
		
		configuration.update(props);
		
		ModelTransformator transformator= transformatorAware.waitForService(500);
		assertNotNull(transformator);

		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("Hoffmannin", resultPerson.getLastName());
		assertNotNull(resultPerson.getAddress());
		Address resultAddress = resultPerson.getAddress();
		assertEquals("Kurt-KeicherCopy", resultAddress.getStreet());
		assertEquals("GeraCopy", resultAddress.getCity());
	}

	@Test
	public void testBlackboxWithTrafoRegistration(
			@InjectService ResourceSet rs, 
			@InjectService BasicFactory factory,
			@InjectService(cardinality = 0, filter = "(" + ModelTransformationConstants.TRANSFORMATOR_ID + "=testTrafo)") ServiceAware<ModelTransformator> transformatorAware
			) throws InterruptedException, IOException{
		Resource r1 = rs.createResource(URI.createURI("tmp.test"));
		Person p1 = BasicFactory.eINSTANCE.createPerson();
		p1.setFirstName("Mark");
		p1.setLastName("Hoffmann");
		p1.setGender(GenderType.MALE);
		Address address = BasicFactory.eINSTANCE.createAddress();
		address.setCity("Gera");
		address.setStreet("Kurt-Keicher");
		p1.setAddress(address);
		
		r1.getContents().add(p1);
		
		context.registerService(Condition.class, Condition.INSTANCE, new Hashtable<>(Collections.singletonMap(Condition.CONDITION_ID, "test")));
		
		ModelTransformator transformator= transformatorAware.waitForService(1000);
		assertNotNull(transformator);

		EObject result = transformator.doTransformation(p1);
		assertNotNull(result);
		assertTrue(result instanceof Person);
		Person resultPerson = (Person) result;
		assertEquals(GenderType.FEMALE, resultPerson.getGender());
		assertEquals("Markin", resultPerson.getFirstName());
		assertEquals("HoffmannBlackBox", resultPerson.getLastName());
	}
	
}