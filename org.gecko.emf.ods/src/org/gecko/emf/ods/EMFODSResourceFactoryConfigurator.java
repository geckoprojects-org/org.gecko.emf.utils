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
package org.gecko.emf.ods;

import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.ods.configuration.EMFODSResource;
import org.gecko.emf.ods.configuration.EMFODSResourceFactory;
import org.gecko.emf.ods.constants.EMFODSConstants;
import org.gecko.emf.osgi.annotation.ConfiguratorType;
import org.gecko.emf.osgi.annotation.provide.EMFConfigurator;
import org.gecko.emf.osgi.configurator.ResourceFactoryConfigurator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of the {@link ResourceFactoryConfigurator} to provide support for {@link EMFODSResource}.
 * 
 * It provides the {@link EMFODSResourceFactory} for the following identifiers:
 * <ul>
 * 	<li>Extension: ods
 * 	<li>contentType: application/vnd.oasis.opendocument.spreadsheet
 * </ul>
 * 
 * @author Michal H. Siemaszko
 */
@Component(name = "EMFODSConfigurator", immediate = true, service = ResourceFactoryConfigurator.class)
//@formatter:off
@EMFConfigurator(
		configuratorName  = EMFODSConstants.EMFODS_CAPABILITY_NAME,
		configuratorType = ConfiguratorType.RESOURCE_FACTORY,
		fileExtension = {
				EMFODSConstants.EMFODS_FILE_EXTENSION
				},
		contentType = {
				EMFODSConstants.EMFODS_CONTENT_TYPE
				}
		)
//@formatter:on
public class EMFODSResourceFactoryConfigurator implements ResourceFactoryConfigurator {

	@Reference(target = ("(component.name=EMFODSExporter)"))
	private EMFExporter emfODSExporter;
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#configureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void configureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().put(EMFODSConstants.EMFODS_FILE_EXTENSION, createODSFactory());
		
		registry.getContentTypeToFactoryMap().put(EMFODSConstants.EMFODS_CONTENT_TYPE, createODSFactory());
	}
	
	private EMFODSResourceFactory createODSFactory() {
		return new EMFODSResourceFactory(emfODSExporter);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#unconfigureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void unconfigureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().remove(EMFODSConstants.EMFODS_FILE_EXTENSION);

		registry.getContentTypeToFactoryMap().remove(EMFODSConstants.EMFODS_CONTENT_TYPE);
	}
}
