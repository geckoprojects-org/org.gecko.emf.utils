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
package org.gecko.emf.bson.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.gecko.emf.osgi.example.model.basic.BasicFactory;
import org.gecko.emf.osgi.example.model.basic.BasicPackage;
import org.gecko.emf.osgi.example.model.basic.Contact;
import org.gecko.emf.osgi.example.model.basic.ContactContextType;
import org.gecko.emf.osgi.example.model.basic.ContactType;
import org.gecko.emf.osgi.example.model.basic.GenderType;
import org.gecko.emf.osgi.example.model.basic.Person;
import org.gecko.emf.osgi.example.model.basic.util.BasicResourceFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.undercouch.bson4jackson.BsonFactory;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class BsonConfiguratorTest {
	
	@BeforeEach
	public void beforeEach(@InjectService(timeout = 2000) BasicFactory bp) {
		System.out.println("Test");
	}

	@Test
	public void testBson(@InjectService ConfigurationAdmin ca) throws IOException {
		Configuration c = ca.getConfiguration("foo");
		System.out.println(c.getPid());
		System.out.println(c.getFactoryPid());

		c = ca.getFactoryConfiguration("foo", "bar");
		System.out.println(c.getPid());
		System.out.println(c.getFactoryPid());
		
		 c = ca.getConfiguration("foo~bar");
		System.out.println(c.getPid());//foo~bar
		System.out.println(c.getFactoryPid());//foo
	}
	
	@Test
	public void testBson(@InjectService(timeout = 2000) ServiceAware<ResourceSet> rsAware,
			@InjectService BasicFactory bf, 
			@InjectService BasicPackage bp) {
		
		assertNotNull(rsAware);
		assertThat(rsAware.getServices()).hasSize(1);
		ResourceSet resourceSet = rsAware.getService();
		assertNotNull(resourceSet);
		
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("test", new BasicResourceFactoryImpl());
	
		Person p = createSamplePerson(bf);
		Resource xmiResource = resourceSet.createResource(URI.createURI("person.test"));
		assertNotNull(xmiResource);
		xmiResource.getContents().add(p);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			xmiResource.save(baos, null);
			System.out.println(baos.toString());
		} catch (IOException e) {
			fail("Not expected save exception for XMI");
		}
		
		Resource bsonResource = resourceSet.createResource(URI.createURI("person.bson"));
		assertNotNull(bsonResource);
		bsonResource.getContents().add(EcoreUtil.copy(p));
		baos = new ByteArrayOutputStream();
		try {
			bsonResource.save(baos, null);
		} catch (IOException e) {
			e.printStackTrace();
			fail("Not expected save exception Bson " + e.getMessage());
		}
		
		Resource bsonLoadResource = resourceSet.createResource(URI.createURI("person_load.bson"));
		assertNotNull(bsonLoadResource);
		assertNotEquals(bsonLoadResource, bsonResource);
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		try {
			bsonLoadResource.load(bais, null);
		} catch (IOException e) {
			fail("Not expected load exception Bson");
			e.printStackTrace();
		}
		assertFalse(bsonLoadResource.getContents().isEmpty());
		
		Person pLoaded = (Person) bsonLoadResource.getContents().get(0);
		
		assertEquals(p.getFirstName(), pLoaded.getFirstName());
		assertEquals(p.getLastName(), pLoaded.getLastName());
		assertEquals(p.getGender(), pLoaded.getGender());
		assertEquals(p.getContact().size(), pLoaded.getContact().size());
		Contact c = p.getContact().get(0);
		Contact cLoaded = pLoaded.getContact().get(0);
		assertEquals(c.getContext(), cLoaded.getContext());
		assertEquals(c.getType(), cLoaded.getType());
		assertEquals(c.getValue(), cLoaded.getValue());
		
	}
	
	public static ObjectMapper createBsonObjectMapper() {
        ObjectMapper mapper = new ObjectMapper(new BsonFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        return mapper;
    }

	/**
	 * 
	 */
	private Person createSamplePerson(BasicFactory bf) {
		Person p = bf.createPerson();
		p.setId("mh");
		p.setFirstName("Mark");
		p.setLastName("Hoffmann");
		p.setGender(GenderType.MALE);
		
		Contact email = bf.createContact();
		email.setContext(ContactContextType.WORK);
		email.setType(ContactType.EMAIL);
		email.setValue("mh@mycomp.de");
		
		p.getContact().add(email);
		return p;
	}
}
