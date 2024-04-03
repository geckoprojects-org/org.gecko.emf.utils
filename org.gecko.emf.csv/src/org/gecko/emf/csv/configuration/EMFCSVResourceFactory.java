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
package org.gecko.emf.csv.configuration;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.gecko.emf.exporter.EMFExporter;
import org.gecko.emf.exporter.EMFExporterConstants;
import org.gecko.emf.exporter.csv.api.EMFCSVExporterConstants;
import org.osgi.annotation.bundle.Requirement;

/**
 * {@link ResourceFactoryImpl} for the CSV resource.
 * 
 * @author Michal H. Siemaszko
 */
@Requirement(namespace = EMFExporterConstants.EMF_EXPORTER_NAMESPACE, filter = "("
		+ EMFExporterConstants.EMF_EXPORTER_NAME + "=" + EMFCSVExporterConstants.EMF_EXPORTER_NAME + ")")
public class EMFCSVResourceFactory extends ResourceFactoryImpl {
	private EMFExporter emfCSVExporter;

	/**
	 * Creates a new instance.
	 */
	public EMFCSVResourceFactory() {
		super();
	}

	public EMFCSVResourceFactory(EMFExporter emfCSVExporter) {
		super();

		this.emfCSVExporter = emfCSVExporter;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl#createResource(org.eclipse.emf.common.util.URI)
	 */	
	@Override
	public Resource createResource(URI uri) {
		if (emfCSVExporter != null) {
			return new EMFCSVResource(uri, emfCSVExporter);
		} else {
			return new EMFCSVResource(uri);
		}
	}
}
