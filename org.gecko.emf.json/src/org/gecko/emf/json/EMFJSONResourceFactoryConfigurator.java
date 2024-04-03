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
package org.gecko.emf.json;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.eclipse.emfcloud.jackson.module.EMFModule;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;
import org.gecko.emf.json.configuration.ConfigurableJsonResourceFactory;
import org.gecko.emf.json.constants.EMFJs;
import org.gecko.emf.osgi.annotation.ConfiguratorType;
import org.gecko.emf.osgi.annotation.provide.EMFConfigurator;
import org.gecko.emf.osgi.configurator.ResourceFactoryConfigurator;
import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

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
@Component(name="EMFJsonConfigurator", immediate=true, service=ResourceFactoryConfigurator.class)
@EMFConfigurator(
		configuratorName  =EMFJs. EMFSJON_CAPABILITY_NAME,
		configuratorType = ConfiguratorType.RESOURCE_FACTORY,
		fileExtension = {
				"json",
				"yml",
				"yaml",
				"properties"
		},
		contentType = {
				"application/yml",
				"application/yaml",
				"application/json",
				"application/x-json",
				"application/emf-json",
				"text/x-java-properties"
			}
		)
public class EMFJSONResourceFactoryConfigurator implements ResourceFactoryConfigurator{

	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#configureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void configureResourceFactory(Registry registry) {

		registry.getExtensionToFactoryMap().put("yml", createYamlFactory());
		registry.getExtensionToFactoryMap().put("yaml", createYamlFactory());
		registry.getContentTypeToFactoryMap().put("application/yml", createYamlFactory());
		registry.getContentTypeToFactoryMap().put("application/yaml", createYamlFactory());
		
		registry.getExtensionToFactoryMap().put("properties", createPropertiesFactory());
		registry.getProtocolToFactoryMap().put("prop", createPropertiesFactory());
		registry.getContentTypeToFactoryMap().put("text/x-java-properties", createPropertiesFactory());
		
		registry.getExtensionToFactoryMap().put("json", createConfigurableFactory());
		registry.getProtocolToFactoryMap().put("json", createConfigurableFactory());
		registry.getContentTypeToFactoryMap().put("application/json", createConfigurableFactory());
		registry.getContentTypeToFactoryMap().put("application/x-json", createConfigurableFactory());
		registry.getContentTypeToFactoryMap().put("application/emf-json", createConfigurableFactory());
	}
	
	private JsonResourceFactory createYamlFactory() {
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getDefault());

		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setDateFormat(dateFormat);
		mapper.setTimeZone(TimeZone.getDefault());
		mapper.registerModule(new EMFModule());
		
		return new ConfigurableJsonResourceFactory(mapper);
	}
	
	private JsonResourceFactory createPropertiesFactory() {
		ObjectMapper mapper = new ObjectMapper(new JavaPropsFactory());
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
		dateFormat.setTimeZone(TimeZone.getDefault());
		
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.setDateFormat(dateFormat);
		mapper.setTimeZone(TimeZone.getDefault());
		mapper.registerModule(new EMFModule());
		
		return new ConfigurableJsonResourceFactory(mapper);
	}
	
	private JsonResourceFactory createConfigurableFactory() {
		return new ConfigurableJsonResourceFactory();
	}

	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#unconfigureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void unconfigureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().remove("yml");
		registry.getExtensionToFactoryMap().remove("yaml");
		registry.getContentTypeToFactoryMap().remove("application/yml");
		registry.getContentTypeToFactoryMap().remove("application/yaml");
		
		registry.getExtensionToFactoryMap().remove("properties");
		registry.getProtocolToFactoryMap().remove("prop");
		registry.getContentTypeToFactoryMap().remove("text/x-java-properties");
		
		registry.getExtensionToFactoryMap().remove("json");
		registry.getContentTypeToFactoryMap().remove("application/json");
		registry.getContentTypeToFactoryMap().remove("application/x-json");
		registry.getContentTypeToFactoryMap().remove("application/emf-json");
		registry.getProtocolToFactoryMap().remove("json");
	}

}
