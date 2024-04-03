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
package org.gecko.qvt.osgi.api;

/**
 * Constants for registering blackboxes
 * @author Mark Hoffmann
 * @author Juergen Albert
 * @since 12.11.2017
 */
public interface ModelTransformationConstants {
	
	/*
	 * The property prefix
	 */
	public static final String QVT_BLACKBOX_PREFIX = "qvt.blackbox.";

	/*
	 * The property name of the blackbox registered service
	 */
	public static final String QVT_BLACKBOX_CONDITION = "qvt.blackbox.condition";

	/*
	 * The property name of the blackbox registered service
	 */
	public static final String QVT_BLACKBOX = "qvt.blackbox";

	/*
	 * The class of the Blackbox for easy targeting
	 */
	public static final String BLACKBOX_CLASS_NAME = "qvt.blackbox.class.name";
	
	/*
	 * The name of the Blackbox
	 */
	public static final String BLACKBOX_MODULENAME = "qvt.blackbox.module.name";
	
	/*
	 * The name to use as import in qvto templates:
	 * Usually this is the full qualified class name of the Blackbox class
	 */
	public static final String BLACKBOX_QUALIFIED_UNIT_NAME = "qvt.blackbox.unit.qualified.name";

	/*
	 * The path to a template qvto file in style like this <bsn>:<version>/<path-to-file>.qvto
	 */
	public static final String TEMPLATE_PATH = "qvt.template.path";

	/*
	 * A URI for a template. Takes presedent over the {@lin ModelTransformationConstants#TEMPLATE_PATH} if set.
	 */
	public static final String TEMPLATE_URI = "qvt.template.uri";
	/*
	 * The target for the needed models in ldap style e.g. (&(emf.model.name=modelA)(emf.model.name=modelB))
	 */
	public static final String MODEL_TARGET = "qvt.model.target";

	public static final String TRANSFORMATOR_COMPONENT_NAME  = "QVTModelTransformator";

	/** If a trafo is registered with a blackbox, it can be identified via this property. If non was given, that uri or path is used as the id */
	public static final String TRANSFORMATOR_ID  = "transformator.id";

}
