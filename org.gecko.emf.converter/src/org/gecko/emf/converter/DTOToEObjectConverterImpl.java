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
import org.osgi.dto.DTO;
import org.osgi.util.converter.AbstractConverter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.Functioning;
import org.osgi.util.converter.InternalConverter;
import org.osgi.util.converter.InternalConverting;
import org.osgi.util.converter.TypeRule;
import org.osgi.util.function.Function;

/**
 * DTO to EObject {@link org.osgi.util.converter.Converter} implementation
 * 
 * @author Michal H. Siemaszko
 */
class DTOToEObjectConverterImpl extends AbstractConverter implements InternalConverter {

	/* 
	 * (non-Javadoc)
	 * @see org.osgi.util.converter.Converter#convert(java.lang.Object)
	 */
	@Override
	public InternalConverting convert(Object obj) {
		return new DTOToEObjectConvertingImpl(this, obj);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.osgi.util.converter.Converter#function()
	 */
	@Override
	public Functioning function() {
		return new DTOToEObjectFunctioningImpl(this);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.osgi.util.converter.Converter#newConverterBuilder()
	 */
	@Override
	public ConverterBuilder newConverterBuilder() {
		return new DTOToEObjectConverterBuilderImpl(this);
	}

	void addRules(ConverterBuilder cb, EPackage... dynamicEPackages) {
		addStandardRules(cb);
		addDTO2EObjectRule(cb, dynamicEPackages);
	}

	void addDTO2EObjectRule(ConverterBuilder cb, EPackage... dynamicEPackages) {
		cb.rule(new TypeRule<DTO, EObject>(DTO.class, EObject.class, new Function<DTO, EObject>() {

			@Override
			public EObject apply(DTO t) throws Exception {
				return DTOToEObjectConverterUtil.convertDTO2EObject(t, dynamicEPackages);
			}
		}));
	}

	void addDTO2EObjectConverterFunction(ConverterBuilder cb, EPackage... dynamicEPackages) {
		cb.rule(new DTOToEObjectConverterFunction(dynamicEPackages));
	}
}
