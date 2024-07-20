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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

/**
 * Helper for {@link org.gecko.emf.converter.tests.DTOToEPackageConverterTest}
 * and
 * {@link org.gecko.emf.converter.tests.JavaReferenceTypeToEPackageConverterTest}
 * 
 * @author Michal H. Siemaszko
 */
public class EPackageConverterTestHelper {

	public static long eClassifiersTotalCount(EPackage ePackage) {
		return flattenEClassifierTree(ePackage).stream().count();
	}

	public static EClassifier findEClassifierByName(EPackage ePackage, String eClassifierName,
			String eClassifierPackageName) {

		String[] packageNameParts = extractPackageNameParts(eClassifierPackageName);

		// @formatter:off
		return flattenEClassifierTree(ePackage).stream()
				.filter(eClassifier -> eClassifierMatches(eClassifier, eClassifierName, packageNameParts) )
				.findFirst()
				.orElseThrow();
		// @formatter:on
	}

	private static List<EClassifier> flattenEClassifierTree(EPackage ePackage) {
		// @formatter:off
		return flattenEPackageTree(ePackage).stream()
				.flatMap(ep -> ep.getEClassifiers().stream())
				.collect(Collectors.toList());
		// @formatter:on
	}

	private static List<EPackage> flattenEPackageTree(EPackage ePackage) {
		List<EPackage> flattenedEPackageTreeAsList = new ArrayList<>(List.of(ePackage));

		flattenedEPackageTree(ePackage, flattenedEPackageTreeAsList);

		return flattenedEPackageTreeAsList;
	}

	private static void flattenedEPackageTree(EPackage ePackage, List<EPackage> flattenedEPackageTreeAsList) {
		if (ePackage.getESubpackages().size() > 0) {
			for (EPackage eSubPackage : ePackage.getESubpackages()) {
				if (!flattenedEPackageTreeAsList.contains(eSubPackage)) {
					flattenedEPackageTreeAsList.add(eSubPackage);
				}

				flattenedEPackageTree(eSubPackage, flattenedEPackageTreeAsList);
			}
		}
	}

	private static String[] extractPackageNameParts(String packageName) {
		return packageName.split("\\.");
	}

	private static boolean eClassifierMatches(EClassifier eClassifier, String typeName, String... packageNameParts) {
		boolean eClassifierNameMatches = typeName.equals(eClassifier.getName());
		boolean eClassifierPackageNameMatches = false;

		EPackage eClassifierEPackage = eClassifier.getEPackage();

		for (String packageNamePart : packageNameParts) {
			Optional<EPackage> nestedESubPackageOptional = findNestedESubPackageByNamePart(eClassifierEPackage,
					packageNamePart);
			if (nestedESubPackageOptional.isPresent() && packageNamePart.equals(eClassifier.getEPackage().getName())) {
				eClassifierPackageNameMatches = true;
				break;
			}
		}

		return (eClassifierNameMatches && eClassifierPackageNameMatches);
	}

	private static Optional<EPackage> findNestedESubPackageByNamePart(EPackage ePackage, String packageNamePart) {
		List<EPackage> flattenedEPackageTreeAsList = flattenEPackageTree(ePackage);

		// @formatter:off
		return flattenedEPackageTreeAsList.stream()
				.filter(eSubPackage -> packageNamePart.equals(eSubPackage.getName()))
				.findFirst();
		// @formatter:on
	}
}
