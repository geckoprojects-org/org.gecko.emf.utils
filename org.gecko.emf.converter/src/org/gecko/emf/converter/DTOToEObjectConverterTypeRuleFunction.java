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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.osgi.util.function.Function;

/**
 * DTO to EObject {@link org.osgi.util.function.Function} used with
 * {@link org.osgi.util.converter.TypeRule}
 * 
 * @author Michal H. Siemaszko
 */
class DTOToEObjectConverterTypeRuleFunction implements Function<Object, EObject> {
	private EPackage[] dynamicEPackages;

	public DTOToEObjectConverterTypeRuleFunction(EPackage... dynamicEPackages) {
		this.dynamicEPackages = dynamicEPackages;
	}

	@Override
	public EObject apply(Object dtoObject) throws Exception {
		return DTOToEObjectConverterUtil.convertDTO2EObject(dtoObject, dynamicEPackages);
	}
}
