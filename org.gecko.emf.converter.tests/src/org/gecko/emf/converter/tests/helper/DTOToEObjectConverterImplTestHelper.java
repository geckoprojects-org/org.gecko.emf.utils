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
package org.gecko.emf.converter.tests.helper;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

/**
 * Helper for
 * {@link org.gecko.emf.converter.tests.DTOToEObjectConverterImplTest}
 * 
 * @author Michal H. Siemaszko
 */
public class DTOToEObjectConverterImplTestHelper {
	
	public static long eClassifiersTotalCount(EPackage ePackage) {
		// @formatter:off
		return Stream
				.concat(ePackage.getEClassifiers().stream(),
						ePackage.getESubpackages().stream().flatMap(p -> p.getEClassifiers().stream()))
				.count();
		// @formatter:on		
	}
	
	public static EClassifier findEClassifierByName(EPackage ePackage, String eClassifierName) {
		// @formatter:off
		return Stream
				.concat(ePackage.getEClassifiers().stream(),
						ePackage.getESubpackages().stream().flatMap(p -> p.getEClassifiers().stream()))
				.filter(eClassifier -> eClassifierName.equals(eClassifier.getName()))
				.findFirst()
				.orElseThrow();
		// @formatter:on
	}
}
