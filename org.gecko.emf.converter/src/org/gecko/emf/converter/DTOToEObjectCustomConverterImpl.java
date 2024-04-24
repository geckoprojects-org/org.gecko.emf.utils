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
import java.util.Map;

import org.osgi.util.converter.AbstractCustomConverter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.ConverterFunction;
import org.osgi.util.converter.InternalConverter;

/**
 * DTO to EObject {@link org.osgi.util.converter.Converter} custom
 * implementation
 * 
 * @author Michal H. Siemaszko
 */
class DTOToEObjectCustomConverterImpl extends AbstractCustomConverter implements InternalConverter {
	DTOToEObjectCustomConverterImpl(InternalConverter converter, Map<Type, List<ConverterFunction>> rules,
			List<ConverterFunction> catchAllRules, List<ConverterFunction> errHandlers) {
		super(converter, rules, catchAllRules, errHandlers);
	}

	@Override
	public ConverterBuilder newConverterBuilder() {
		return new DTOToEObjectConverterBuilderImpl(this);
	}
}
