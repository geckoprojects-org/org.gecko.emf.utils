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
package org.gecko.emf.exporter;

/**
 * Defines export options which can be passed via options map.
 * 
 * @author Michal H. Siemaszko
 */
public interface EMFExportOptions {

	// locale to use
	String OPTION_LOCALE = "LOCALE";

	// export non-containment references, in addition to containment references
	// (exported by default)
	String OPTION_EXPORT_NONCONTAINMENT = "EXPORT_NONCONTAINMENT";

	// extract and export metadata as additional sheets
	String OPTION_EXPORT_METADATA = "EXPORT_METADATA";

	// generate mapping table
	String OPTION_ADD_MAPPING_TABLE = "ADD_MAPPING_TABLE";

	// show URIs instead of IDs
	String OPTION_SHOW_URIS = "SHOW_URIS";

	// show columns containing references
	String OPTION_SHOW_REFS = "SHOW_REFS";
}
