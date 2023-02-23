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
package org.gecko.emf.json.constants;

/**
 * Constants used as options during load/save operations on a Resource.
 */
public final class EMFJs {
	
	/** Constant for the general Capability Namespace **/	
	public static final String EMFSJON_CAPABILITY_NAME = "EMFJson";
	
	/**
	 * Sets the root element to be loaded from a JSON document without type field.
	 * <p>
	 * Value must be an object of type EClass.
	 * </p>
	 * <p>
	 * Default value is null.
	 * </p>
	 */
	public static final String OPTION_ROOT_ELEMENT = "OPTION_ROOT_ELEMENT";
	/**
	 * When value is true, the writer will include a type to each JSON objects.
	 * <p>
	 * Default value is true.
	 * </p>
	 */
	public static final String OPTION_SERIALIZE_TYPE = "OPTION_SERIALIZE_TYPE";
	/**
	 * When value is true, the writer will include default values for attributes. *
	 * <p>
	 * Default value is false.
	 * </p>
	 */
	public static final String OPTION_SERIALIZE_DEFAULT_VALUE = "OPTION_SERIALIZE_DEFAULT_VALUE";
	/**
	 * When value is true, the writer will indent the output JSON document.
	 * <p>
	 * Default value is true
	 * </p>
	 */
	public static final String OPTION_INDENT_OUTPUT = "OPTION_INDENT_OUTPUT";

	/**
	 * When value is true, the writer will include an _id key to each json objects
	 * and sets as value the fragment identifier.
	 * <p>
	 * Default value is false
	 * </p>
	 */
	public static final String OPTION_USE_ID = "OPTION_USE_ID";
	/**
	 * Specify the field name that will be use to denote the type of objects.
	 * <p>
	 * By default eClass will be use
	 * </p>
	 */
	public static final String OPTION_TYPE_FIELD = "OPTION_TYPE_FIELD";
	/**
	 * Specify the that will be use to denote the type of objects.
	 * <p>
	 * By default URI will be use
	 * </p>
	 */
	public static final String OPTION_TYPE_USE = "OPTION_TYPE_USE";
	/**
	 * Specify the list of package uri that will be use to denote the type of objects.
	 * <p>
	 * By default an empty list will be used
	 * </p>
	 */
	public static final String OPTION_TYPE_PACKAGE_URIS = "OPTION_TYPE_PACKAGE_URIS";
	/**
	 * Specify the field name that will be use to denote a reference. This option is
	 * use when a reference is created as a json object.
	 * <p>
	 * By default $ref will be use
	 * </p>
	 */
	public static final String OPTION_REF_FIELD = "OPTION_REF_FIELD";
	/**
	 * Specify the field name that will be use to denote the id of objects.
	 * <p>
	 * By default _id will be use
	 * </p>
	 */
	public static final String OPTION_ID_FIELD = "OPTION_ID_FIELD";
	/**
	 * Specify the date format that will be use to parse and write dates.
	 * <p>
	 * By default the date format is yyyy-MM-dd'T'HH:mm:ss
	 * </p>
	 */
	public static final String OPTION_DATE_FORMAT = "OPTION_DATE_FORMAT";
	/**
	 * Can be use to pass an Option object.
	 */
	public static final String OPTIONS_OBJECT = "OPTIONS_OBJECT";
	/**
	 * Option to pass a problem handler to the mapper
	 */
	public static final String OPTIONS_PROBLEM_HANDLER = "PROBLEM_HANDLER";

	private EMFJs() {
	}

}