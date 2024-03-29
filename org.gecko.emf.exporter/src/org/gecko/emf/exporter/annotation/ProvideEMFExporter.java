/**
 * Copyright (c) 2012 - 2023 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.exporter.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.gecko.emf.exporter.EMFExporterConstants;
import org.osgi.annotation.bundle.Attribute;
import org.osgi.annotation.bundle.Capability;
import org.osgi.service.component.annotations.ComponentPropertyType;

/**
 * Marker annotation for EMF Exporters, providing a format-specific
 * {@link org.gecko.emf.exporter.EMFExporter} capability.
 * 
 * @author Michal H. Siemaszko
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.PACKAGE })
@Capability(namespace = EMFExporterConstants.EMF_EXPORTER_NAMESPACE)
@ComponentPropertyType
public @interface ProvideEMFExporter {

	@Attribute(EMFExporterConstants.EMF_EXPORTER_NAME)
	String name();
}