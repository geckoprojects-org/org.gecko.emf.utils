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

import java.util.Arrays;
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
public enum DTOToEObjectConverterUtil {
	INSTANCE;

	private static final Logger LOG = LoggerFactory.getLogger(DTOToEObjectConverterUtil.class);

	public final EObject convertDTO2EObject(Object dto, EPackage... dynamicEPackages) {
		String eClassifierName = dto.getClass().getSimpleName();

		EPackage containingEPackage = findContainingEPackage(eClassifierName, dynamicEPackages);
		if (containingEPackage == null) {
			LOG.warn(" Could not find any EPackage containing EClassifier {} !", eClassifierName);
			return null;
		}

		EClassifier eClassifier = findEClassifier(eClassifierName, containingEPackage);
		if (eClassifier == null) {
			LOG.warn(" Could not find any EClassifier {} !", eClassifierName);
			return null;
		}

		Map<?, ?> dtoAsMap = Converters.standardConverter().convert(dto).sourceAsDTO().to(Map.class);

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

	private EPackage findContainingEPackage(String eClassifierName, EPackage... dynamicEPackages) {
		// @formatter:off
		return Arrays.asList(dynamicEPackages).stream()
				.filter(p -> ( p.getEClassifier(eClassifierName) != null ) )
				.findFirst()
				.orElse(null);
		// @formatter:on
	}

	private EClassifier findEClassifier(String name, EPackage dynamicEPackage) {
		// @formatter:off
		return dynamicEPackage.getEClassifiers().stream()
				.filter(c -> name.equalsIgnoreCase(c.getName()))
				.findFirst()
				.orElse(null);
		// @formatter:on
	}

	@SuppressWarnings("unused")
	private EClassifier findEClassifier(String name, EPackage... dynamicEPackages) {
		// @formatter:off
		return Arrays.asList(dynamicEPackages).stream()
				.flatMap(p -> p.getEClassifiers().stream())
				.filter(c -> name.equalsIgnoreCase(c.getName()))
				.findFirst()
				.orElse(null);
		// @formatter:on
	}
}
