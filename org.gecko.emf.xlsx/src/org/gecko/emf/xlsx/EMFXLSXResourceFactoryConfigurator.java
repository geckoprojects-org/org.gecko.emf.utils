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
package org.gecko.emf.xlsx;

import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.osgi.annotation.ConfiguratorType;
import org.gecko.emf.osgi.annotation.provide.EMFConfigurator;
import org.gecko.emf.osgi.configurator.ResourceFactoryConfigurator;
import org.gecko.emf.xlsx.configuration.EMFXLSXResource;
import org.gecko.emf.xlsx.configuration.EMFXLSXResourceFactory;
import org.gecko.emf.xlsx.constants.EMFXLSXConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of the {@link ResourceFactoryConfigurator} to provide support for {@link EMFXLSXResource}.
 * 
 * It provides the {@link EMFXLSXResourceFactory} for the following identifiers:
 * <ul>
 * 	<li>Extension: xlsx
 * 	<li>contentType: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
 * </ul>
 * 
 * @author Michal H. Siemaszko
 */
@Component(name = "EMFXLSXConfigurator", immediate = true, service = ResourceFactoryConfigurator.class)
//@formatter:off
@EMFConfigurator(
		configuratorName  = EMFXLSXConstants.EMFXLSX_CAPABILITY_NAME,
		configuratorType = ConfiguratorType.RESOURCE_FACTORY,
		fileExtension = {
				EMFXLSXConstants.EMFXLSX_FILE_EXTENSION
				},
		contentType = {
				EMFXLSXConstants.EMFXLSX_CONTENT_TYPE
				}
		)
//@formatter:on
public class EMFXLSXResourceFactoryConfigurator implements ResourceFactoryConfigurator {

	@Reference(target = ("(component.name=EMFXLSXExporter)"))
	private EMFExporter emfXLSXExporter;
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#configureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void configureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().put(EMFXLSXConstants.EMFXLSX_FILE_EXTENSION, createXLSXFactory());
		
		registry.getContentTypeToFactoryMap().put(EMFXLSXConstants.EMFXLSX_CONTENT_TYPE, createXLSXFactory());
	}
	
	private EMFXLSXResourceFactory createXLSXFactory() {
		return new EMFXLSXResourceFactory(emfXLSXExporter);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#unconfigureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void unconfigureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().remove(EMFXLSXConstants.EMFXLSX_FILE_EXTENSION);

		registry.getContentTypeToFactoryMap().remove(EMFXLSXConstants.EMFXLSX_CONTENT_TYPE);
	}
}
