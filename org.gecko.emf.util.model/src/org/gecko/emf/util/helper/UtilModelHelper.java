/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.util.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.gecko.emf.utilities.FeaturePath;

/**
 * Helper class for the Collections model
 * @author Mark Hoffmann
 * @since 24.11.2017
 */
public class UtilModelHelper {
	
	/**
	 * Returns a list of objects or <code>null</code>, in case of an error
	 * @param featurePath the feature path
	 * @param context the context object to get the values from
	 * @return a {@link List} of objects or <code>null</code>
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> getFeaturePathValue(FeaturePath featurePath, EObject context) {
		if (featurePath == null || context == null || featurePath.getFeature().isEmpty()) {
			return null;
		}
		if (!validateFeaturePath(featurePath, context.eClass())) {
			return null;
		}
		List<Object> resultList = new ArrayList<Object>();
		List<EObject> cachedList = new ArrayList<EObject>();
		List<EObject> cachedResult = new ArrayList<EObject>();
		cachedList.add(context);
		for (EStructuralFeature feature : featurePath.getFeature()) {
			for (EObject eo : cachedList) {
				Object o = null;
				try {
					o = eo.eGet(feature, true);
				} catch (Exception e) {
					return null;
				}
				if (o == null) {
					continue;
				}
				// get the object for the feature
				if (!feature.isMany()) {
					if (feature instanceof EAttribute) {
						// many attributes are final results
						resultList.add(o);
					} else {
						// many references are cached for the eventually next step
						cachedResult.add((EObject) o);
					}
				} else {
					// get the e-list from the object
					if (feature instanceof EAttribute) {
						EList<?> list = (EList<?>) o;
						resultList.addAll(list);
					} else {
						EList<EObject> list = (EList<EObject>) o;
						cachedResult.addAll(list);
					}

				}
			}
			if (resultList.size() > 0) {
				break;
			}
			cachedList.clear();
			cachedList.addAll(cachedResult);
			cachedResult.clear();
		}
		cachedList.clear();
		return resultList;

	}
	
	/**
	 * Returns the {@link EStructuralFeature} for the feature path or <code>null</code>, if the feature path is not valid
	 * @param featurePath the feature path
	 * @param contextClass the context class for the feature path
	 * @return {@link EStructuralFeature} on success or <code>null</code>, if the path is not valid
	 */
	public static EStructuralFeature getFeaturePathFeature(FeaturePath featurePath, EClass contextClass) {
		if (featurePath == null || contextClass == null) {
			return null;
		}
		if (featurePath.getFeature().isEmpty()) {
			return null;
		}
		EClass currentClass = contextClass;
		int lastIndex = featurePath.getFeature().size() - 1;
		for (int i = 0; i < featurePath.getFeature().size(); i++) {
			EStructuralFeature feature = featurePath.getFeature().get(i);
			List<EStructuralFeature> features = currentClass.getEAllStructuralFeatures();
			if (features.contains(feature)) {
				if (i == lastIndex) {
					return feature;
				}
				if (feature instanceof EReference) {
					EClassifier classifier = feature.getEType();
					if (classifier instanceof EClass) {
						currentClass = (EClass) classifier;
					}
				}
			} else {
				return null;
			}
		}
		return null;
	}
	
	/**
	 * @see UtilModelHelper#getFeaturePathFeature(FeaturePath, EClass)
	 * The same but it return a boolean value instead of the {@link EStructuralFeature}
	 */
	public static boolean validateFeaturePath(FeaturePath featurePath, EClass contextClass) {
		return getFeaturePathFeature(featurePath, contextClass) != null;
	}

	/**
	 * Returns the last {@link EStructuralFeature} segment from the path or <code>null</code>, if path is <code>null</code> or empty
	 * @param path the feature path
	 * @return the last {@link EStructuralFeature} segment from the path
	 */
	public static EStructuralFeature getLastFeature(FeaturePath path) {
		if (path == null || path.getFeature().isEmpty()) {
			return null;
		}
		return path.getFeature().get(path.getFeature().size() -1);
	}

	/**
	 * Returns the first {@link EStructuralFeature} segment from the path or <code>null</code>, if path is <code>null</code> or empty
	 * @param path the feature path
	 * @return the first {@link EStructuralFeature} segment from the path
	 */
	public static EStructuralFeature getFirstFeature(FeaturePath path) {
		if (path == null || path.getFeature().isEmpty()) {
			return null;
		}
		return path.getFeature().get(0);
	}
	
	/**
	 * Returns the feature at the given index
	 * @param path the feature path
	 * @param index the index
	 * @return the {@link EStructuralFeature} or <code>null</code>
	 */
	public static EStructuralFeature getFeatureAtIndex(FeaturePath path, int index) {
		if (path == null || 
				index < 0 || 
				index >= path.getFeature().size()) {
			return null;
		}
		return path.getFeature().get(index);
	}

}
