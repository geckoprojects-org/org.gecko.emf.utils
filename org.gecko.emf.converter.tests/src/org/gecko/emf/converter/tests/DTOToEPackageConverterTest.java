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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.gecko.emf.converter.DTOToEPackageConverter;
import org.gecko.emf.converter.tests.helper.DTOToEMFConverterTestHelper.ConverterTestAllSupportedTypesDTO;
import org.gecko.emf.converter.tests.helper.DTOToEMFConverterTestHelper.ConverterTestBasicDTO;
import org.gecko.emf.converter.tests.helper.DTOToEMFConverterTestHelper.ConverterTestInheritingDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.annotation.Testable;
import org.osgi.framework.dto.FrameworkDTO;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration test for {@link org.gecko.emf.converter.DTOToEPackageConverter}
 * 
 * @author Michal H. Siemaszko
 */
@Testable
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class DTOToEPackageConverterTest {
	private static final String PACKAGE_NAME = "dto_to_epackage_converter_test";
	private static final String NS_URI = "http://gecko.org/test/model/converter/1.0";
	private static final String NS_PREFIX = "tests";

	@Test
	public void testConvertBasicDTO() throws Exception {
		Class<ConverterTestBasicDTO> dtoClass = ConverterTestBasicDTO.class;

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX, dtoClass);
		assertNotNull(dynamicEPackageFromDTOs);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromDTOs.getName());
		assertEquals(NS_URI, dynamicEPackageFromDTOs.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromDTOs.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		assertThat(dynamicEPackageFromDTOs.getEClassifiers()).hasSize(1);

		assertNotNull(dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName()));
		assertTrue(dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName()) instanceof EClass);
		assertThat(
				((EClass) dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName())).getEAllStructuralFeatures())
				.hasSize(3);

		List<EStructuralFeature> eAllStructuralFeatures = ((EClass) dynamicEPackageFromDTOs
				.getEClassifier(dtoClass.getSimpleName())).getEAllStructuralFeatures();

		boolean hasLongPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "longPrimitiveField".equals(((EAttribute) f).getName())
						&& (long.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasLongPrimitiveEAttribute);

		boolean hasBooleanPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "booleanPrimitiveField".equals(((EAttribute) f).getName())
						&& (boolean.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBooleanPrimitiveEAttribute);

		boolean hasStringEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "stringField".equals(((EAttribute) f).getName())
						&& (String.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasStringEAttribute);
	}

	@Test
	public void testConvertInheritingDTO() throws Exception {
		Class<ConverterTestInheritingDTO> dtoClass = ConverterTestInheritingDTO.class;

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX, dtoClass);
		assertNotNull(dynamicEPackageFromDTOs);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromDTOs.getName());
		assertEquals(NS_URI, dynamicEPackageFromDTOs.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromDTOs.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		assertThat(dynamicEPackageFromDTOs.getEClassifiers()).hasSize(1);

		assertNotNull(dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName()));
		assertTrue(dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName()) instanceof EClass);
		assertThat(
				((EClass) dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName())).getEAllStructuralFeatures())
				.hasSize(9);

		List<EStructuralFeature> eAllStructuralFeatures = ((EClass) dynamicEPackageFromDTOs
				.getEClassifier(dtoClass.getSimpleName())).getEAllStructuralFeatures();

		boolean hasBytePrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "bytePrimitiveField".equals(((EAttribute) f).getName())
						&& (byte.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBytePrimitiveEAttribute);

		boolean hasShortPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "shortPrimitiveField".equals(((EAttribute) f).getName())
						&& (short.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasShortPrimitiveEAttribute);

		boolean hasIntPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "intPrimitiveField".equals(((EAttribute) f).getName())
						&& (int.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasIntPrimitiveEAttribute);

		boolean hasLongPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "longPrimitiveField".equals(((EAttribute) f).getName())
						&& (long.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasLongPrimitiveEAttribute);

		boolean hasFloatPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "floatPrimitiveField".equals(((EAttribute) f).getName())
						&& (float.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasFloatPrimitiveEAttribute);

		boolean hasDoublePrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "doublePrimitiveField".equals(((EAttribute) f).getName())
						&& (double.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasDoublePrimitiveEAttribute);

		boolean hasBooleanPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "booleanPrimitiveField".equals(((EAttribute) f).getName())
						&& (boolean.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBooleanPrimitiveEAttribute);

		boolean hasCharPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "charPrimitiveField".equals(((EAttribute) f).getName())
						&& (char.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasCharPrimitiveEAttribute);
	}

	@Test
	public void testConvertAllSupportedTypesDTO() throws Exception {
		Class<ConverterTestAllSupportedTypesDTO> dtoClass = ConverterTestAllSupportedTypesDTO.class;

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX, dtoClass);
		assertNotNull(dynamicEPackageFromDTOs);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromDTOs.getName());
		assertEquals(NS_URI, dynamicEPackageFromDTOs.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromDTOs.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		assertThat(dynamicEPackageFromDTOs.getEClassifiers()).hasSize(8);

		assertNotNull(dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName()));
		assertTrue(dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName()) instanceof EClass);

		assertThat(
				((EClass) dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName())).getEAllStructuralFeatures())
				.hasSize(44);

		List<EStructuralFeature> eAllStructuralFeatures = ((EClass) dynamicEPackageFromDTOs
				.getEClassifier(dtoClass.getSimpleName())).getEAllStructuralFeatures();

		// Primitive types
		boolean hasBytePrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "bytePrimitiveField".equals(((EAttribute) f).getName())
						&& (byte.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBytePrimitiveEAttribute);

		boolean hasShortPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "shortPrimitiveField".equals(((EAttribute) f).getName())
						&& (short.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasShortPrimitiveEAttribute);

		boolean hasIntPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "intPrimitiveField".equals(((EAttribute) f).getName())
						&& (int.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasIntPrimitiveEAttribute);

		boolean hasLongPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "longPrimitiveField".equals(((EAttribute) f).getName())
						&& (long.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasLongPrimitiveEAttribute);

		boolean hasFloatPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "floatPrimitiveField".equals(((EAttribute) f).getName())
						&& (float.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasFloatPrimitiveEAttribute);

		boolean hasDoublePrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "doublePrimitiveField".equals(((EAttribute) f).getName())
						&& (double.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasDoublePrimitiveEAttribute);

		boolean hasBooleanPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "booleanPrimitiveField".equals(((EAttribute) f).getName())
						&& (boolean.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBooleanPrimitiveEAttribute);

		boolean hasCharPrimitiveEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "charPrimitiveField".equals(((EAttribute) f).getName())
						&& (char.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasCharPrimitiveEAttribute);

		// Wrapper classes for the primitive types
		boolean hasByteWrapperEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "byteWrapperField".equals(((EAttribute) f).getName())
						&& (Byte.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasByteWrapperEAttribute);

		boolean hasShortWrapperEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "shortWrapperField".equals(((EAttribute) f).getName())
						&& (Short.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasShortWrapperEAttribute);

		boolean hasIntWrapperEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "intWrapperField".equals(((EAttribute) f).getName())
						&& (Integer.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasIntWrapperEAttribute);

		boolean hasLongWrapperEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "longWrapperField".equals(((EAttribute) f).getName())
						&& (Long.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasLongWrapperEAttribute);

		boolean hasFloatWrapperEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "floatWrapperField".equals(((EAttribute) f).getName())
						&& (Float.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasFloatWrapperEAttribute);

		boolean hasDoubleWrapperEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "doubleWrapperField".equals(((EAttribute) f).getName())
						&& (Double.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasDoubleWrapperEAttribute);

		boolean hasBooleanWrapperEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "booleanWrapperField".equals(((EAttribute) f).getName())
						&& (Boolean.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBooleanWrapperEAttribute);

		boolean hasCharWrapperEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "charWrapperField".equals(((EAttribute) f).getName())
						&& (Character.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasCharWrapperEAttribute);

		// String
		boolean hasStringEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "stringField".equals(((EAttribute) f).getName())
						&& (String.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasStringEAttribute);

		// enum
		boolean hasEnumEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "enumField".equals(((EAttribute) f).getName())
						&& ("ConverterTestSampleEnum".equals(((EAttribute) f).getEType().getName()))));
		assertTrue(hasEnumEAttribute);

		// Version
		boolean hasVersionFieldEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "versionField".equals(((EAttribute) f).getName())
						&& (org.osgi.framework.Version.class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasVersionFieldEAttribute);

		// Reference to other Data Transfer Objects
		boolean hasDtoEReference = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EReference) && "dtoField".equals(((EReference) f).getName())
						&& ("ConverterTestBasicDTO".equals(((EReference) f).getEType().getName()))));
		assertTrue(hasDtoEReference);

		// List
		boolean hasListEReference = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EReference) && "listField".equals(((EReference) f).getName())
						&& ("ConverterTestBasicDTO".equals(((EReference) f).getEType().getName()))
						&& (0 == ((EReference) f).getLowerBound()) && (-1 == ((EReference) f).getUpperBound())));
		assertTrue(hasListEReference);

		// Set
		boolean hasSetEReference = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EReference) && "setField".equals(((EReference) f).getName())
						&& ("ConverterTestBasicDTO".equals(((EReference) f).getEType().getName()))
						&& (0 == ((EReference) f).getLowerBound()) && (-1 == ((EReference) f).getUpperBound())));
		assertTrue(hasSetEReference);

		// Map
		boolean hasMapWithStringKeysEReference = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EReference) && "mapWithStringKeysField".equals(((EReference) f).getName())
						&& ("StringToConverterTestBasicDTOMap".equals(((EReference) f).getEType().getName()))
						&& (-1 == ((EReference) f).getUpperBound())
						&& (2 == ((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().size())
						&& ("key".equals(
								((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0).getName()))
						&& ("EString".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0)
								.getEType().getName()))
						&& ("value".equals(
								((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(1).getName()))
						&& ("ConverterTestBasicDTO".equals(((EClass) ((EReference) f).getEType())
								.getEAllStructuralFeatures().get(1).getEType().getName()))));
		assertTrue(hasMapWithStringKeysEReference);

		boolean mapWithPrimitiveWrapperKeysEReference = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EReference)
						&& "mapWithPrimitiveWrapperKeysField".equals(((EReference) f).getName())
						&& ("IntegerToConverterTestBasicDTOMap".equals(((EReference) f).getEType().getName()))
						&& (-1 == ((EReference) f).getUpperBound())
						&& (2 == ((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().size())
						&& ("key".equals(
								((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0).getName()))
						&& ("EIntegerObject".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures()
								.get(0).getEType().getName()))
						&& ("value".equals(
								((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(1).getName()))
						&& ("ConverterTestBasicDTO".equals(((EClass) ((EReference) f).getEType())
								.getEAllStructuralFeatures().get(1).getEType().getName()))));
		assertTrue(mapWithPrimitiveWrapperKeysEReference);

		boolean mapWithEnumKeysEReference = eAllStructuralFeatures.stream().anyMatch(f -> ((f instanceof EReference)
				&& "mapWithEnumKeysField".equals(((EReference) f).getName())
				&& ("ConverterTestSampleEnumToConverterTestBasicDTOMap".equals(((EReference) f).getEType().getName()))
				&& (-1 == ((EReference) f).getUpperBound())
				&& (2 == ((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().size())
				&& ("key".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0).getName()))
				&& ("ConverterTestSampleEnum".equals(
						((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0).getEType().getName()))
				&& ("value".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(1).getName()))
				&& ("ConverterTestBasicDTO".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures()
						.get(1).getEType().getName()))));
		assertTrue(mapWithEnumKeysEReference);

		boolean mapWithVersionKeysEReference = eAllStructuralFeatures.stream().anyMatch(f -> ((f instanceof EReference)
				&& "mapWithVersionKeysField".equals(((EReference) f).getName())
				&& ("VersionToConverterTestBasicDTOMap".equals(((EReference) f).getEType().getName()))
				&& (-1 == ((EReference) f).getUpperBound())
				&& (2 == ((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().size())
				&& ("key".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0).getName()))
				&& ("EOSGiFrameworkVersion".equals(
						((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0).getEType().getName()))
				&& ("value".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(1).getName()))
				&& ("ConverterTestBasicDTO".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures()
						.get(1).getEType().getName()))));
		assertTrue(mapWithVersionKeysEReference);

		// array
		boolean hasBytePrimitiveArrayEAttribute = eAllStructuralFeatures.stream().anyMatch(
				f -> ((f instanceof EAttribute) && "bytePrimitiveArrayField".equals(((EAttribute) f).getName())
						&& (byte[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBytePrimitiveArrayEAttribute);

		boolean hasShortPrimitiveArrayEAttribute = eAllStructuralFeatures.stream().anyMatch(
				f -> ((f instanceof EAttribute) && "shortPrimitiveArrayField".equals(((EAttribute) f).getName())
						&& (short[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasShortPrimitiveArrayEAttribute);

		boolean hasIntPrimitiveArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "intPrimitiveArrayField".equals(((EAttribute) f).getName())
						&& (int[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasIntPrimitiveArrayEAttribute);

		boolean hasLongPrimitiveArrayEAttribute = eAllStructuralFeatures.stream().anyMatch(
				f -> ((f instanceof EAttribute) && "longPrimitiveArrayField".equals(((EAttribute) f).getName())
						&& (long[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasLongPrimitiveArrayEAttribute);

		boolean hasFloatPrimitiveArrayEAttribute = eAllStructuralFeatures.stream().anyMatch(
				f -> ((f instanceof EAttribute) && "floatPrimitiveArrayField".equals(((EAttribute) f).getName())
						&& (float[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasFloatPrimitiveArrayEAttribute);

		boolean hasDoublePrimitiveArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute)
						&& "doublePrimitiveArrayField".equals(((EAttribute) f).getName())
						&& (double[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasDoublePrimitiveArrayEAttribute);

		boolean hasBooleanPrimitiveArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute)
						&& "booleanPrimitiveArrayField".equals(((EAttribute) f).getName())
						&& (boolean[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBooleanPrimitiveArrayEAttribute);

		boolean hasCharPrimitiveArrayEAttribute = eAllStructuralFeatures.stream().anyMatch(
				f -> ((f instanceof EAttribute) && "charPrimitiveArrayField".equals(((EAttribute) f).getName())
						&& (char[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasCharPrimitiveArrayEAttribute);

		boolean hasByteWrapperArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "byteWrapperArrayField".equals(((EAttribute) f).getName())
						&& (Byte[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasByteWrapperArrayEAttribute);

		boolean hasShortWrapperArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "shortWrapperArrayField".equals(((EAttribute) f).getName())
						&& (Short[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasShortWrapperArrayEAttribute);

		boolean hasIntWrapperArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "intWrapperArrayField".equals(((EAttribute) f).getName())
						&& (Integer[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasIntWrapperArrayEAttribute);

		boolean hasLongWrapperArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "longWrapperArrayField".equals(((EAttribute) f).getName())
						&& (Long[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasLongWrapperArrayEAttribute);

		boolean hasFloatWrapperArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "floatWrapperArrayField".equals(((EAttribute) f).getName())
						&& (Float[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasFloatWrapperArrayEAttribute);

		boolean hasDoubleWrapperArrayEAttribute = eAllStructuralFeatures.stream().anyMatch(
				f -> ((f instanceof EAttribute) && "doubleWrapperArrayField".equals(((EAttribute) f).getName())
						&& (Double[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasDoubleWrapperArrayEAttribute);

		boolean hasBooleanWrapperArrayEAttribute = eAllStructuralFeatures.stream().anyMatch(
				f -> ((f instanceof EAttribute) && "booleanWrapperArrayField".equals(((EAttribute) f).getName())
						&& (Boolean[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasBooleanWrapperArrayEAttribute);

		boolean hasCharWrapperArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "charWrapperArrayField".equals(((EAttribute) f).getName())
						&& (Character[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasCharWrapperArrayEAttribute);

		boolean hasStringArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "stringArrayField".equals(((EAttribute) f).getName())
						&& (String[].class == ((EAttribute) f).getEType().getInstanceClass())));
		assertTrue(hasStringArrayEAttribute);

		boolean hasDtoArrayEAttribute = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EAttribute) && "dtoArrayField".equals(((EAttribute) f).getName())
						&& ("ConverterTestBasicDTOArray".equals(((EAttribute) f).getEType().getName()))));
		assertTrue(hasDtoArrayEAttribute);
	}

	@Test
	public void testConvertOSGiFrameworkDTO() throws Exception {
		Class<FrameworkDTO> dtoClass = FrameworkDTO.class;

		EPackage dynamicEPackageFromDTOs = DTOToEPackageConverter.convert(PACKAGE_NAME, NS_URI, NS_PREFIX, dtoClass);
		assertNotNull(dynamicEPackageFromDTOs);

		assertEquals(PACKAGE_NAME, dynamicEPackageFromDTOs.getName());
		assertEquals(NS_URI, dynamicEPackageFromDTOs.getNsURI());
		assertEquals(NS_PREFIX, dynamicEPackageFromDTOs.getNsPrefix());

		EPackage.Registry.INSTANCE.put(dynamicEPackageFromDTOs.getNsURI(), dynamicEPackageFromDTOs);

		assertThat(dynamicEPackageFromDTOs.getEClassifiers()).hasSize(4);

		assertNotNull(dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName()));
		assertTrue(dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName()) instanceof EClass);
		assertThat(
				((EClass) dynamicEPackageFromDTOs.getEClassifier(dtoClass.getSimpleName())).getEAllStructuralFeatures())
				.hasSize(3);

		List<EStructuralFeature> eAllStructuralFeatures = ((EClass) dynamicEPackageFromDTOs
				.getEClassifier(dtoClass.getSimpleName())).getEAllStructuralFeatures();

		boolean hasBundlesEReference = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EReference) && "bundles".equals(((EReference) f).getName())
						&& ("BundleDTO".equals(((EReference) f).getEType().getName()))
						&& (0 == ((EReference) f).getLowerBound()) && (-1 == ((EReference) f).getUpperBound())));
		assertTrue(hasBundlesEReference);

		boolean hasPropertiesEReference = eAllStructuralFeatures.stream().anyMatch(f -> ((f instanceof EReference)
				&& "properties".equals(((EReference) f).getName())
				&& ("StringToObjectMap".equals(((EReference) f).getEType().getName()))
				&& (-1 == ((EReference) f).getUpperBound())
				&& (2 == ((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().size())
				&& ("key".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0).getName()))
				&& ("EString".equals(
						((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(0).getEType().getName()))
				&& ("value".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(1).getName()))
				&& ("EJavaObject".equals(((EClass) ((EReference) f).getEType()).getEAllStructuralFeatures().get(1)
						.getEType().getName()))));
		assertTrue(hasPropertiesEReference);

		boolean hasServicesEReference = eAllStructuralFeatures.stream()
				.anyMatch(f -> ((f instanceof EReference) && "services".equals(((EReference) f).getName())
						&& ("ServiceReferenceDTO".equals(((EReference) f).getEType().getName()))
						&& (0 == ((EReference) f).getLowerBound()) && (-1 == ((EReference) f).getUpperBound())));
		assertTrue(hasServicesEReference);
	}
}
