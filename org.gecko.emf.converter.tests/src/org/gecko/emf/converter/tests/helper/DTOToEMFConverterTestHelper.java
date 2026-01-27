/**
 * Copyright (c) 2012 - 2023 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.converter.tests.helper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.dto.DTO;
import org.osgi.framework.Version;

/**
 * Helper for
 * {@link org.gecko.emf.converter.tests.DTOToEObjectConverterTest}
 * 
 * @author Michal H. Siemaszko
 */
public class DTOToEMFConverterTestHelper {

	public static class ConverterTestBasicDTO extends DTO {
		public long longPrimitiveField;
		public boolean booleanPrimitiveField;
		public String stringField;
	}

	public static class ConverterTestInheritingDTO extends ConverterTestBasicDTO {
		public byte bytePrimitiveField;
		public short shortPrimitiveField;
		public int intPrimitiveField;
		public float floatPrimitiveField;
		public double doublePrimitiveField;
		public char charPrimitiveField;
	}

	public static class ConverterTestAllSupportedTypesDTO extends DTO {

		// Primitive types
		public byte bytePrimitiveField;
		public short shortPrimitiveField;
		public int intPrimitiveField;
		public long longPrimitiveField;
		public float floatPrimitiveField;
		public double doublePrimitiveField;
		public boolean booleanPrimitiveField;
		public char charPrimitiveField;

		// Wrapper classes for the primitive types
		public Byte byteWrapperField;
		public Short shortWrapperField;
		public Integer intWrapperField;
		public Long longWrapperField;
		public Float floatWrapperField;
		public Double doubleWrapperField;
		public Boolean booleanWrapperField;
		public Character charWrapperField;

		// String
		public String stringField;

		// enum
		public ConverterTestSampleEnum enumField;

		// Version
		public Version versionField;

		// Data Transfer Objects
		public ConverterTestBasicDTO dtoField;

		// List
		public List<ConverterTestBasicDTO> listField;

		// Set
		public Set<ConverterTestBasicDTO> setField;

		// Map
		public Map<Integer, ConverterTestBasicDTO> mapWithPrimitiveWrapperKeysField;
		public Map<String, ConverterTestBasicDTO> mapWithStringKeysField;
		public Map<ConverterTestSampleEnum, ConverterTestBasicDTO> mapWithEnumKeysField;
		public Map<Version, ConverterTestBasicDTO> mapWithVersionKeysField;

		// array
		public byte[] bytePrimitiveArrayField;
		public short[] shortPrimitiveArrayField;
		public int[] intPrimitiveArrayField;
		public long[] longPrimitiveArrayField;
		public float[] floatPrimitiveArrayField;
		public double[] doublePrimitiveArrayField;
		public boolean[] booleanPrimitiveArrayField;
		public char[] charPrimitiveArrayField;

		public Byte[] byteWrapperArrayField;
		public Short[] shortWrapperArrayField;
		public Integer[] intWrapperArrayField;
		public Long[] longWrapperArrayField;
		public Float[] floatWrapperArrayField;
		public Double[] doubleWrapperArrayField;
		public Boolean[] booleanWrapperArrayField;
		public Character[] charWrapperArrayField;

		public String[] stringArrayField;

		public ConverterTestBasicDTO[] dtoArrayField;
	}

	public enum ConverterTestSampleEnum {
		ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE;
	}
}
