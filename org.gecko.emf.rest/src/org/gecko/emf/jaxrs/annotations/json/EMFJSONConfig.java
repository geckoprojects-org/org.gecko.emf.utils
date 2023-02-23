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
package org.gecko.emf.jaxrs.annotations.json;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.gecko.emf.json.annotation.RequireEMFJson;
import org.gecko.emf.json.constants.EMFJs;

/**
 * Provides a convinient Way to configure EMFJSON Serialization for JaxRS Endpoints.
 * @author Juergen Albert
 * @since 24 Jun 2018
 */
@Documented
@Target({METHOD, PARAMETER, CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@RequireEMFJson
public @interface EMFJSONConfig {
	
	/**
	 * Sets the data format to be parsed. The default value is set to the JavaScript 
	 * date format behavior. 
	 * return @see {@link EMFJs#OPTION_DATE_FORMAT} default is yyyy-MM-dd'T'HH:mm:ss.SSSXXX
	 */
	String dateFormat() default "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	
	/**
	 * return @see {@link EMFJs#OPTION_INDENT_OUTPUT} default is true
	 */
	boolean indentOutput() default true;
	
	/**
	 * return @see {@link EMFJs#OPTION_SERIALIZE_DEFAULT_VALUE} default is false
	 */
	boolean serializeDefaultValues() default false;
	
	/**
	 * return @see {@link EMFJs#OPTION_SERIALIZE_TYPE} default is true
	 */
	boolean serializeTypes() default true;
	
	/**
	 * return @see {@link EMFJs#OPTION_USE_ID} default is false
	 */
	boolean useId() default false;
	
	/**
	 * return @see {@link EMFJs#OPTION_REF_FIELD} default is &ref
	 */
	String refFieldName() default "";
	
	/**
	 * return @see {@link EMFJs#OPTION_ID_FIELD} default is _id
	 */
	String idFieldName() default "";
	
	/**
	 * return @see {@link EMFJs#OPTION_TYPE_FIELD} default is eClass
	 */
	String typeFieldName() default "";
	
	/**
	 * return @see {@link EMFJs#OPTION_TYPE_USE} default is eClass
	 */
	USE typeUSE() default USE.URI;
	
	/**
	 * return @see {@link EMFJs#OPTION_TYPE_PACKAGE_URIS} default is an empty array
	 */
	String[] typePackageUris() default {};
	
	public enum USE {
		URI,
		NAME,
		CLASS
	}
	
}
