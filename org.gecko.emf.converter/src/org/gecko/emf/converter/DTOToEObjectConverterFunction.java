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

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.osgi.util.converter.ConverterFunction;

/**
 * DTO to EObject {@link org.osgi.util.converter.ConverterFunction}
 * 
 * @author Michal H. Siemaszko
 */
public class DTOToEObjectConverterFunction implements ConverterFunction {
	private List<EPackage> dynamicEPackages;

	public DTOToEObjectConverterFunction(EPackage... dynamicEPackages) {
		this.dynamicEPackages = List.of(dynamicEPackages);
	}

	@Override
	public Object apply(Object obj, Type targetType) throws Exception {
		if ((DTOUtil.isDTOType(obj.getClass(), false))
				&& ((targetType instanceof Class) && EObject.class.isAssignableFrom((Class<?>) targetType))) {
			return DTOToEObjectConverterUtil.INSTANCE.convertDTO2EObject(obj,
					dynamicEPackages.toArray(new EPackage[0]));
		}

		return ConverterFunction.CANNOT_HANDLE;
	}
}
