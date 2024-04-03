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
package org.gecko.emf.bson;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.gecko.emf.json.configuration.ConfigurableJsonResourceFactory;
import org.gecko.emf.osgi.annotation.ConfiguratorType;
import org.gecko.emf.osgi.annotation.provide.EMFConfigurator;
import org.gecko.emf.osgi.configurator.ResourceFactoryConfigurator;
import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.undercouch.bson4jackson.BsonFactory;

/**
 * Implementation of the {@link IResourceSetConfigurator} to provide support for {@link JsResourceImpl}.
 * 
 * It provides the {@link JsResourceFactoryImpl} for the following identifiers:
 * <ul>
 * 	<li>Extension: json
 * 	<li>contentType: application/json
 * 	<li>contentType: application/x-json
 * 	<li>Protocol: json
 * </ul>
 * 
 * @author Juergen Albert
 * @since 27.06.2014
 */
@Component(name="EMFBsonConfigurator", immediate=true, service=ResourceFactoryConfigurator.class)
@EMFConfigurator(
		configuratorName = "EMFBson",
		configuratorType = ConfiguratorType.RESOURCE_FACTORY,
		fileExtension = {
				"bson"
		},
		contentType = {
				"application/bson"
			}
		)
public class EMFBsonResourceFactoryConfigurator implements ResourceFactoryConfigurator{

	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#configureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void configureResourceFactory(Registry registry) {

		registry.getExtensionToFactoryMap().put("bson", createBsonFactory());
		registry.getContentTypeToFactoryMap().put("application/bson", createBsonFactory());
		registry.getProtocolToFactoryMap().put("bson", createBsonFactory());
	}
	
	private JsonResourceFactory createBsonFactory() {
		ObjectMapper mapper = new ObjectMapper(new BsonFactory());
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getDefault());

		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setDateFormat(dateFormat);
		mapper.setTimeZone(TimeZone.getDefault());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true);
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
		mapper.registerModule(new EMFModule());
		
		return new ConfigurableJsonResourceFactory(mapper);
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#unconfigureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void unconfigureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().remove("bson");
		registry.getProtocolToFactoryMap().remove("bson");
		registry.getContentTypeToFactoryMap().remove("application/bson");
	}

}
