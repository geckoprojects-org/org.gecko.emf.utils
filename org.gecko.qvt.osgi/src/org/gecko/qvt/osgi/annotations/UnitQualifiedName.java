/**
 * Copyright (c) 2012 - 2024 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.qvt.osgi.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.gecko.qvt.osgi.api.ModelTransformationConstants;
import org.osgi.service.component.annotations.ComponentPropertyType;

@Documented
@Retention(CLASS)
@Target(TYPE)

/**
 * Sets the unit qualified name. If nothing is given the qualified classname is used.
 * 
 * @author Juergen Albert
 * @since 4 Feb 2024
 */
@ComponentPropertyType
@RequireQVT
@QvtBlackbox
public @interface UnitQualifiedName {

	/**
	 * Prefix for the property name. This value is prepended to each property
	 * name.
	 */
	String PREFIX_ = ModelTransformationConstants.QVT_BLACKBOX_PREFIX;
	
	String value();
	
}
