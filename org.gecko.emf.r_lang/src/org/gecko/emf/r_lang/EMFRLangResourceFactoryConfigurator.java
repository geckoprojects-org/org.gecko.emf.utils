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
package org.gecko.emf.r_lang;

import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.osgi.annotation.ConfiguratorType;
import org.gecko.emf.osgi.annotation.provide.EMFConfigurator;
import org.gecko.emf.osgi.configurator.ResourceFactoryConfigurator;
import org.gecko.emf.r_lang.configuration.EMFRLangResource;
import org.gecko.emf.r_lang.configuration.EMFRLangResourceFactory;
import org.gecko.emf.r_lang.constants.EMFRLangConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of the {@link ResourceFactoryConfigurator} to provide support for {@link EMFRLangResource}.
 * 
 * It provides the {@link EMFRLangResourceFactory} for the following identifiers:
 * 
 * <ul>
 * 	<li>Extension: RData
 * 	<li>contentType: text/x-R
 * </ul>
 * 
 * @author Michal H. Siemaszko
 */
@Component(name = "EMFRLangConfigurator", immediate = true, service = ResourceFactoryConfigurator.class)
//@formatter:off
@EMFConfigurator(
		configuratorName  = EMFRLangConstants.EMFRLANG_CAPABILITY_NAME,
		configuratorType = ConfiguratorType.RESOURCE_FACTORY,
		fileExtension = {
				EMFRLangConstants.EMFRLANG_FILE_EXTENSION
				},
		contentType = {
				EMFRLangConstants.EMFRLANG_CONTENT_TYPE
				}
		)
//@formatter:on
public class EMFRLangResourceFactoryConfigurator implements ResourceFactoryConfigurator {

	@Reference(target = ("(component.name=EMFRLangExporter)"))
	private EMFExporter emfRLangExporter;
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#configureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void configureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().put(EMFRLangConstants.EMFRLANG_FILE_EXTENSION, createRLangFactory());
		
		registry.getContentTypeToFactoryMap().put(EMFRLangConstants.EMFRLANG_CONTENT_TYPE, createRLangFactory());
	}
	
	private EMFRLangResourceFactory createRLangFactory() {
		return new EMFRLangResourceFactory(emfRLangExporter);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#unconfigureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void unconfigureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().remove(EMFRLangConstants.EMFRLANG_FILE_EXTENSION);

		registry.getContentTypeToFactoryMap().remove(EMFRLangConstants.EMFRLANG_CONTENT_TYPE);
	}
}
