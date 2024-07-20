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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.osgi.util.converter.Converters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DTO to EObject utility methods
 * 
 * @author Michal H. Siemaszko
 */
class DTOToEObjectConverterUtil {
	private final static Logger LOG = LoggerFactory.getLogger(DTOToEObjectConverterUtil.class);

	public final static EObject convertDTO2EObject(Object dtoObject, EPackage... ePackages) {
		String eClassifierName = dtoObject.getClass().getSimpleName();
		String eClassifierPackageName = dtoObject.getClass().getPackageName();

		if (!DTOUtil.isDTOType(dtoObject.getClass(), true, true)) {
			LOG.warn(" {} is not DTO-like !", eClassifierName);
			return null;
		}

		EPackage containingEPackage = findContainingEPackage(eClassifierPackageName, ePackages);
		if (containingEPackage == null) {
			LOG.warn(" Could not find any EPackage containing EClassifier {} !", eClassifierName);
			return null;
		}

		EClassifier eClassifier = findEClassifier(eClassifierName, containingEPackage);
		if (eClassifier == null) {
			LOG.warn(" Could not find any EClassifier {} !", eClassifierName);
			return null;
		}

		Map<?, ?> dtoAsMap = Converters.standardConverter().convert(dtoObject).sourceAsDTO().to(Map.class);

		EClass eClass = (EClass) eClassifier;

		EFactory eFactory = containingEPackage.getEFactoryInstance();

		EObject eObject = eFactory.create(eClass);

		for (EStructuralFeature eStructuralFeature : eClass.getEAllStructuralFeatures()) {
			String eStructuralFeatureName = eStructuralFeature.getName();

			if (dtoAsMap.containsKey(eStructuralFeatureName)) {
				Object dtoFieldValue = dtoAsMap.get(eStructuralFeatureName);

				eObject.eSet(eStructuralFeature, dtoFieldValue);
			}
		}

		return eObject;
	}

	private static EPackage findContainingEPackage(String eClassifierPackageName, EPackage... ePackages) {
		String[] packageNameParts = extractPackageNameParts(eClassifierPackageName);

		// @formatter:off
		return Arrays.asList(ePackages).stream()
				.flatMap(ePackage -> flattenEPackageTree(ePackage).stream())
				.filter(eSubPackage -> Arrays.equals(packageNameParts, getEPackageFlattenedNameParts(eSubPackage)))
				.findFirst()
				.orElse(null);
		// @formatter:on
	}

	private static EClassifier findEClassifier(String name, EPackage ePackage) {
		// @formatter:off
		return ePackage.getEClassifiers().stream()
				.filter(c -> name.equalsIgnoreCase(c.getName()))
				.findFirst()
				.orElse(null);
		// @formatter:on
	}

	@SuppressWarnings("unused")
	private static EClassifier findEClassifier(String name, EPackage... ePackages) {
		// @formatter:off
		return Arrays.asList(ePackages).stream()
				.flatMap(p -> p.getEClassifiers().stream())
				.filter(c -> name.equalsIgnoreCase(c.getName()))
				.findFirst()
				.orElse(null);
		// @formatter:on
	}

	private static String[] getEPackageFlattenedNameParts(EPackage ePackage) {
		List<String> flattenedEPackagNameParts = new ArrayList<>(List.of(ePackage.getName()));

		getEPackageFlattenedNameParts(ePackage, flattenedEPackagNameParts);

		// drop top-level package name
		flattenedEPackagNameParts.remove(flattenedEPackagNameParts.size() - 1);

		Collections.reverse(flattenedEPackagNameParts);

		return flattenedEPackagNameParts.toArray(String[]::new);
	}

	private static void getEPackageFlattenedNameParts(EPackage ePackage, List<String> flattenedEPackagNameParts) {
		if (ePackage.getESuperPackage() != null) {
			flattenedEPackagNameParts.add(ePackage.getESuperPackage().getName());

			getEPackageFlattenedNameParts(ePackage.getESuperPackage(), flattenedEPackagNameParts);
		}
	}

	private static String[] extractPackageNameParts(String packageName) {
		return packageName.split("\\.");
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
}
