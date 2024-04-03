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
package org.gecko.emf.bson.annotation;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.gecko.emf.json.annotation.RequireEMFJson;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.annotation.bundle.Requirement;

@Documented
@Retention(CLASS)
@Target({ TYPE, PACKAGE })
@Requirement(
		namespace = EMFNamespaces.EMF_CONFIGURATOR_NAMESPACE,
		name = "RESOURCE_FACTORY",
		filter = "(" + EMFNamespaces.EMF_CONFIGURATOR_NAME + "=EMFBson)"
		)
/**
 * Metaannotation to generate a Require Capability for EMFBson
 * @author Mark Hoffmann
 * @since 10 Oct 2018
 */
@RequireEMFJson
public @interface RequireEMFBson {

}
