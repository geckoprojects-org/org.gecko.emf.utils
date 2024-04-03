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
package org.gecko.emf.r_lang.constants;

/**
 * Constants used as options during load/save operations on an EMF R Language
 * Resource.
 * 
 * @author Michal H. Siemaszko
 */
public interface EMFRLangConstants {

	/** Constant for the general Capability Namespace **/
	static final String EMFRLANG_CAPABILITY_NAME = "EMFRLang";

	static final String EMFRLANG_FILE_EXTENSION = "RData";

	static final String EMFRLANG_CONTENT_TYPE = "text/x-R";
}