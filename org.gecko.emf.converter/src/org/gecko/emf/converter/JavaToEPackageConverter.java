/**
 * Copyright (c) 2012 - 2024 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.converter;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.emf.ecore.EPackage;

/**
 * Defines methods for converting Java types to EMF's EPackage.
 * 
 * @author Michal H. Siemaszko
 */
public interface JavaToEPackageConverter {

	EPackage convert(String packageName, String nsURI, String nsPrefix, Class<?>... javaTypes);

	EPackage convert(String packageName, String nsURI, String nsPrefix, Path mainJarFilePath,
			Path... dependenciesJarFilePaths) throws ClassNotFoundException, IOException;
}
