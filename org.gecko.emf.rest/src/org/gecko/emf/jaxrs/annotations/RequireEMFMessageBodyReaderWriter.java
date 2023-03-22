/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.jaxrs.annotations;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.osgi.annotation.bundle.Requirement;

@Documented
@Retention(CLASS)
@Target({ TYPE, PACKAGE })
@Requirement(
		namespace = "gecko.rest.addon",
		name = "messagebody.emf"
		)
/**
 * Metaannotation to generate a Require Capability for EMFJson
 * @author Juergen Albert
 * @since 22 Feb 2018
 */
public @interface RequireEMFMessageBodyReaderWriter{

}
