/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.converter.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.gecko.emf.converter.DTOToEObjectConverterFunction;
import org.gecko.emf.converter.DTOToEObjectConverterUtil;
import org.gecko.emf.converter.DTOToEObjectConverters;
import org.gecko.emf.converter.DTOToEPackageConverter;
import org.gecko.emf.converter.tests.helper.DTOToEMFConverterTestHelper.ConverterTestBasicDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.converter.TypeRule;
import org.osgi.util.function.Function;

/**
 * Integration test for
 * {@link org.gecko.emf.converter.dto_to_eobject.DTOToEObjectConverters}
 * 
 * @author Michal H. Siemaszko
 */
@Testable
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class DTOToEObjectConverterTest {
	private static final String PACKAGE_NAME = "org.gecko.emf.converter";
	private static final String NS_URI = "http://gecko.org/test/model/converter/1.0";
	private static final String NS_PREFIX = "tests";

	@Test
	public void testStandardConverterWithDTO2EObjectConverterFunction() throws Exception {

		ConverterTestBasicDTO dto = new ConverterTestBasicDTO();
		dto.longPrimitiveField = Long.MIN_VALUE;
		dto.booleanPrimitiveField = true;
		dto.stringField = "hello";

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX,
				dto.getClass());
		assertNotNull(dynamicEPackageFromDTOs);

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		Converter standardConverter = DTOToEObjectConverters.standardConverter();

		ConverterBuilder cb = standardConverter.newConverterBuilder();
		cb.rule(new DTOToEObjectConverterFunction(dynamicEPackageFromDTOs));
		Converter converter = cb.build();

		Function<Object, EObject> cf = converter.function().to(EObject.class);

		EObject mySimpleEObject = cf.apply(dto);
		assertNotNull(mySimpleEObject);
	}

	@Test
	public void testStandardConverterWithDTO2EObjectTypeRule() {

		ConverterTestBasicDTO dto = new ConverterTestBasicDTO();
		dto.longPrimitiveField = Long.MIN_VALUE;
		dto.booleanPrimitiveField = true;
		dto.stringField = "hello";

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX,
				dto.getClass());
		assertNotNull(dynamicEPackageFromDTOs);

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		Converter standardConverter = DTOToEObjectConverters.standardConverter();

		ConverterBuilder cb = standardConverter.newConverterBuilder();

		cb.rule(new TypeRule<ConverterTestBasicDTO, EObject>(ConverterTestBasicDTO.class, EObject.class,
				new Function<ConverterTestBasicDTO, EObject>() {

					@Override
					public EObject apply(ConverterTestBasicDTO t) throws Exception {
						return DTOToEObjectConverterUtil.convertDTO2EObject(t, dynamicEPackageFromDTOs);
					}
				}));

		Converter converter = cb.build();

		EObject mySimpleEObject = converter.convert(dto).to(EObject.class);
		assertNotNull(mySimpleEObject);
	}

	@Test
	public void testDTO2EObjectConverterWithBuiltInFunction() throws Exception {

		ConverterTestBasicDTO dto = new ConverterTestBasicDTO();
		dto.longPrimitiveField = Long.MIN_VALUE;
		dto.booleanPrimitiveField = true;
		dto.stringField = "hello";

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX,
				dto.getClass());
		assertNotNull(dynamicEPackageFromDTOs);

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		Converter dto2EObjectConverter = DTOToEObjectConverters.dto2EObjectConverter(dynamicEPackageFromDTOs);

		Function<Object, EObject> cf = dto2EObjectConverter.function().to(EObject.class);

		EObject mySimpleEObject = cf.apply(dto);
		assertNotNull(mySimpleEObject);
	}

	@Test
	public void testDTO2EObjectConverterWithBuiltInTypeRule() {

		ConverterTestBasicDTO dto = new ConverterTestBasicDTO();
		dto.longPrimitiveField = Long.MIN_VALUE;
		dto.booleanPrimitiveField = true;
		dto.stringField = "hello";

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX,
				dto.getClass());
		assertNotNull(dynamicEPackageFromDTOs);

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		Converter dto2EObjectConverter = DTOToEObjectConverters.dto2EObjectConverter(dynamicEPackageFromDTOs);

		EObject mySimpleEObject = dto2EObjectConverter.convert(dto).to(EObject.class);
		assertNotNull(mySimpleEObject);
	}
}
