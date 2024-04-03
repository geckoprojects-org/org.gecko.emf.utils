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
package org.gecko.emf.csv;

import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
import org.gecko.emf.csv.configuration.EMFCSVResource;
import org.gecko.emf.csv.configuration.EMFCSVResourceFactory;
import org.gecko.emf.csv.constants.EMFCSVConstants;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.osgi.annotation.ConfiguratorType;
import org.gecko.emf.osgi.annotation.provide.EMFConfigurator;
import org.gecko.emf.osgi.configurator.ResourceFactoryConfigurator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Implementation of the {@link ResourceFactoryConfigurator} to provide support for {@link EMFCSVResource}.
 * 
 * It provides the {@link EMFCSVResourceFactory} for the following identifiers:
 * 
 * <ul>
 * 	<li>Extension: csv
 * 	<li>contentType: application/csv
 * </ul>
 * 
 * @author Michal H. Siemaszko
 */
@Component(name = "EMFCSVConfigurator", immediate = true, service = ResourceFactoryConfigurator.class)
//@formatter:off
@EMFConfigurator(
		configuratorName  = EMFCSVConstants.EMFCSV_CAPABILITY_NAME,
		configuratorType = ConfiguratorType.RESOURCE_FACTORY,
		fileExtension = {
				EMFCSVConstants.EMFCSV_FILE_EXTENSION
				},
		contentType = {
				EMFCSVConstants.EMFCSV_CONTENT_TYPE
				}
		)
//@formatter:on
public class EMFCSVResourceFactoryConfigurator implements ResourceFactoryConfigurator {

	@Reference(target = ("(component.name=EMFCSVExporter)"))
	private EMFExporter emfCSVExporter;
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#configureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void configureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().put(EMFCSVConstants.EMFCSV_FILE_EXTENSION, createCSVFactory());
		
		registry.getContentTypeToFactoryMap().put(EMFCSVConstants.EMFCSV_CONTENT_TYPE, createCSVFactory());
	}
	
	private EMFCSVResourceFactory createCSVFactory() {
		return new EMFCSVResourceFactory(emfCSVExporter);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see de.dim.emf.osgi.ResourceFactoryConfigurator#unconfigureResourceFactory(org.eclipse.emf.ecore.resource.Resource.Factory.Registry)
	 */
	@Override
	public void unconfigureResourceFactory(Registry registry) {
		registry.getExtensionToFactoryMap().remove(EMFCSVConstants.EMFCSV_FILE_EXTENSION);

		registry.getContentTypeToFactoryMap().remove(EMFCSVConstants.EMFCSV_CONTENT_TYPE);
	}
}
