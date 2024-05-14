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
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.Converters;
import org.osgi.util.converter.TypeRule;

/**
 * Factory class to obtain a preconfigured DTO to EObject Converter as well as
 * the standard converter or a new standard converter builder.
 * 
 * Based on {@link org.osgi.util.converter.Converters}
 * 
 * @author Michal H. Siemaszko
 */
public class DTOToEObjectConverters {
	private DTOToEObjectConverters() {
	}

	public static Converter customConverter(EPackage... dynamicEPackages) {
		return newCustomConverterBuilder(dynamicEPackages).build();
	}

	public static ConverterBuilder newCustomConverterBuilder(EPackage... dynamicEPackages) {
		ConverterBuilder cb = Converters.newConverterBuilder();

		cb.rule(new DTOToEObjectConverterFunction(dynamicEPackages));

		cb.rule(new TypeRule<Object, EObject>(Object.class, EObject.class,
				new DTOToEObjectConverterTypeRuleFunction(dynamicEPackages)));

		return cb;
	}
}
