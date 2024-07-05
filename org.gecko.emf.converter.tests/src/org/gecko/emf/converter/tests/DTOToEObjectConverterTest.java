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
import org.gecko.emf.converter.DTOToEObjectConverters;
import org.gecko.emf.converter.DTOToEPackageConverter;
import org.gecko.emf.converter.tests.helper.ConverterTestBasicDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.converter.Converter;
import org.osgi.util.converter.ConverterBuilder;
import org.osgi.util.function.Function;

/**
 * Integration test for {@link org.gecko.emf.converter.DTOToEObjectConverters}
 * 
 * @author Michal H. Siemaszko
 */
@Testable
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class DTOToEObjectConverterTest {
	private static final String PACKAGE_NAME = "dto_to_eobject_converter_test";
	private static final String NS_URI = "http://gecko.org/test/model/converter/1.0";
	private static final String NS_PREFIX = "tests";

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

		Converter dto2EObjectConverter = DTOToEObjectConverters.customConverter(dynamicEPackageFromDTOs);

		EObject mySimpleEObject = dto2EObjectConverter.convert(dto).to(EObject.class);
		assertNotNull(mySimpleEObject);
	}

	@Test
	public void testDTO2EObjectConverterBuilderWithBuiltInTypeRule() {

		ConverterTestBasicDTO dto = new ConverterTestBasicDTO();
		dto.longPrimitiveField = Long.MIN_VALUE;
		dto.booleanPrimitiveField = true;
		dto.stringField = "hello";

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX,
				dto.getClass());
		assertNotNull(dynamicEPackageFromDTOs);

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		ConverterBuilder dto2EObjectConverterBuilder = DTOToEObjectConverters
				.newCustomConverterBuilder(dynamicEPackageFromDTOs);

		// .. add some more custom rules as needed

		Converter dto2EObjectConverter = dto2EObjectConverterBuilder.build();

		EObject mySimpleEObject = dto2EObjectConverter.convert(dto).to(EObject.class);
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

		Converter dto2EObjectConverter = DTOToEObjectConverters.customConverter(dynamicEPackageFromDTOs);

		Function<Object, EObject> cf = dto2EObjectConverter.function().to(EObject.class);

		EObject mySimpleEObject = cf.apply(dto);
		assertNotNull(mySimpleEObject);
	}

	@Test
	public void testDTO2EObjectConverterBuilderWithBuiltInFunction() throws Exception {

		ConverterTestBasicDTO dto = new ConverterTestBasicDTO();
		dto.longPrimitiveField = Long.MIN_VALUE;
		dto.booleanPrimitiveField = true;
		dto.stringField = "hello";

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX,
				dto.getClass());
		assertNotNull(dynamicEPackageFromDTOs);

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		ConverterBuilder dto2EObjectConverterBuilder = DTOToEObjectConverters
				.newCustomConverterBuilder(dynamicEPackageFromDTOs);

		// .. add some more custom rules as needed

		Converter dto2EObjectConverter = dto2EObjectConverterBuilder.build();

		Function<Object, EObject> cf = dto2EObjectConverter.function().to(EObject.class);

		EObject mySimpleEObject = cf.apply(dto);
		assertNotNull(mySimpleEObject);
	}
}
