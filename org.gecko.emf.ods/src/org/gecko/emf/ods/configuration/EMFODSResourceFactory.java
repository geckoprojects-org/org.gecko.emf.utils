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
package org.gecko.emf.ods.configuration;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.exporter.EMFExporterConstants;
import org.gecko.emf.exporter.ods.api.EMFODSExporterConstants;
import org.osgi.annotation.bundle.Requirement;

/**
 * {@link ResourceFactoryImpl} for the ODS resource.
 * 
 * @author Michal H. Siemaszko
 */
@Requirement(namespace = EMFExporterConstants.EMF_EXPORTER_NAMESPACE, filter = "("
		+ EMFExporterConstants.EMF_EXPORTER_NAME + "=" + EMFODSExporterConstants.EMF_EXPORTER_NAME + ")")
public class EMFODSResourceFactory extends ResourceFactoryImpl {
	private EMFExporter emfODSExporter;

	/**
	 * Creates a new instance.
	 */
	public EMFODSResourceFactory() {
		super();
	}

	public EMFODSResourceFactory(EMFExporter emfODSExporter) {
		super();

		this.emfODSExporter = emfODSExporter;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl#createResource(org.eclipse.emf.common.util.URI)
	 */	
	@Override
	public Resource createResource(URI uri) {
		if (emfODSExporter != null) {
			return new EMFODSResource(uri, emfODSExporter);
		} else {
			return new EMFODSResource(uri);
		}
	}
}
