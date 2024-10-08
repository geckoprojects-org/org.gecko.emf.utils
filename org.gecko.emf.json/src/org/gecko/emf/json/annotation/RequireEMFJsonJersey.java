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
package org.gecko.emf.json.annotation;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.osgi.annotation.bundle.Requirement;
import org.osgi.annotation.bundle.Requirements;
import org.osgi.framework.namespace.IdentityNamespace;

@Documented
@Retention(CLASS)
@Target({ TYPE, PACKAGE })
@Requirements(value = {
		@Requirement(namespace = IdentityNamespace.IDENTITY_NAMESPACE, name = "org.glassfish.jersey.ext.jersey-entity-filtering"),
		@Requirement(namespace = IdentityNamespace.IDENTITY_NAMESPACE, name = "org.glassfish.jersey.media.jersey-media-json-jackson")
})
@RequireEMFJson
/**
 * Metaannotation to generate a Require Capability for EMFJson
 * @author Juergen Albert
 * @since 22 Feb 2018
 */
public @interface RequireEMFJsonJersey {

}
