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
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

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
@ObjectClassDefinition
public @interface ModelTransformatorConfig {

	@AttributeDefinition(name = "transformator ID", description = "A Required transformator id")
	String transformator_id();

	@AttributeDefinition(name = "Template URI", description = "The URI, where the template can be found.")
	String qvt_template_uri() default "";
	
	@AttributeDefinition(name = "Template Path", description = "The Path inside a Bundle. Must start with the Bundle Symbolic Name. Will be overwritten by the Template URI.")
	String qvt_template_path() default "";

	@AttributeDefinition(name = "Model Target Filter", description = "The Filter for the required Model, to be available.")
	String qvt_model_target();
}
