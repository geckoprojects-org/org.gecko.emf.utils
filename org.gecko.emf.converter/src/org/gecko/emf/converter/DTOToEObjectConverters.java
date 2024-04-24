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

import org.eclipse.emf.ecore.EPackage;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.Converters;

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

	public static Converter dto2EObjectConverter(EPackage... dynamicEPackages) {
		DTOToEObjectConverterImpl impl = new DTOToEObjectConverterImpl();
		ConverterBuilder cb = impl.newConverterBuilder();
		impl.addRules(cb, dynamicEPackages);
		impl.addDTO2EObjectConverterFunction(cb, dynamicEPackages);
		return cb.build();
	}

	public static Converter standardConverter() {
		return Converters.standardConverter();
	}

	public static ConverterBuilder newStandardConverterBuilder() {
		return Converters.newConverterBuilder();
	}
}
