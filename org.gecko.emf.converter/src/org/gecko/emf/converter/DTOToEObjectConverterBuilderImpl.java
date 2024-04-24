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

import org.osgi.util.converter.AbstractConverterBuilder;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.InternalConverter;

/**
 * DTO to EObject {@link org.osgi.util.converter.ConverterBuilder} implementation
 * 
 * @author Michal H. Siemaszko
 */
class DTOToEObjectConverterBuilderImpl extends AbstractConverterBuilder implements ConverterBuilder {

	DTOToEObjectConverterBuilderImpl(InternalConverter c) {
		super(c);
	}

	@Override
	public InternalConverter build() {
		return new DTOToEObjectCustomConverterImpl(converter, rules, catchAllRules, errorHandlers);
	}
}
