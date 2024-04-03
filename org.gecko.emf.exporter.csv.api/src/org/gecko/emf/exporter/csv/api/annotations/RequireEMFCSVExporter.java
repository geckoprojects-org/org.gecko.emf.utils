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
package org.gecko.emf.exporter.csv.api.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.gecko.emf.exporter.EMFExporterConstants;
import org.gecko.emf.exporter.csv.api.EMFCSVExporterConstants;
import org.osgi.annotation.bundle.Requirement;

@Documented
@Retention(CLASS)
@Target({ TYPE, PACKAGE })
@Requirement(namespace = EMFExporterConstants.EMF_EXPORTER_NAMESPACE, filter = "("
		+ EMFExporterConstants.EMF_EXPORTER_NAME + "=" + EMFCSVExporterConstants.EMF_EXPORTER_NAME + ")")

/**
 * Meta annotation to generate a Require Capability for CSV
 * {@link org.gecko.emf.exporter.EMFExporter}
 * 
 * @author Michal H. Siemaszko
 */
public @interface RequireEMFCSVExporter {

}
