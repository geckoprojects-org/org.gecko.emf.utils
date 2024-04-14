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

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.osgi.dto.DTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DTO to EPackge converter
 * 
 * @author Michal H. Siemaszko
 */
public enum DTOToEPackageConverter {
	INSTANCE;

	private static final Logger LOG = LoggerFactory.getLogger(DTOToEPackageConverter.class);

	private final static Map<Class<?>, Field[]> CACHED_FIELDS = Collections
			.synchronizedMap(new WeakHashMap<Class<?>, Field[]>());

	private final static Map<Class<?>, EDataType> JAVATYPE_TO_EDATATYPE = new HashMap<>();
	static {
		JAVATYPE_TO_EDATATYPE.put(byte.class, EcorePackage.eINSTANCE.getEByte());
		JAVATYPE_TO_EDATATYPE.put(Byte.class, EcorePackage.eINSTANCE.getEByteObject());

		JAVATYPE_TO_EDATATYPE.put(short.class, EcorePackage.eINSTANCE.getEShort());
		JAVATYPE_TO_EDATATYPE.put(Short.class, EcorePackage.eINSTANCE.getEShortObject());

		JAVATYPE_TO_EDATATYPE.put(int.class, EcorePackage.eINSTANCE.getEInt());
		JAVATYPE_TO_EDATATYPE.put(Integer.class, EcorePackage.eINSTANCE.getEIntegerObject());

		JAVATYPE_TO_EDATATYPE.put(long.class, EcorePackage.eINSTANCE.getELong());
		JAVATYPE_TO_EDATATYPE.put(Long.class, EcorePackage.eINSTANCE.getELongObject());

		JAVATYPE_TO_EDATATYPE.put(float.class, EcorePackage.eINSTANCE.getEFloat());
		JAVATYPE_TO_EDATATYPE.put(Float.class, EcorePackage.eINSTANCE.getEFloatObject());

		JAVATYPE_TO_EDATATYPE.put(double.class, EcorePackage.eINSTANCE.getEDouble());
		JAVATYPE_TO_EDATATYPE.put(Double.class, EcorePackage.eINSTANCE.getEDoubleObject());

		JAVATYPE_TO_EDATATYPE.put(boolean.class, EcorePackage.eINSTANCE.getEBoolean());
		JAVATYPE_TO_EDATATYPE.put(Boolean.class, EcorePackage.eINSTANCE.getEBooleanObject());

		JAVATYPE_TO_EDATATYPE.put(char.class, EcorePackage.eINSTANCE.getEChar());
		JAVATYPE_TO_EDATATYPE.put(Character.class, EcorePackage.eINSTANCE.getECharacterObject());

		JAVATYPE_TO_EDATATYPE.put(String.class, EcorePackage.eINSTANCE.getEString());

		JAVATYPE_TO_EDATATYPE.put(Object.class, EcorePackage.eINSTANCE.getEJavaObject());

		JAVATYPE_TO_EDATATYPE.put(byte[].class, EcorePackage.eINSTANCE.getEByteArray());
		JAVATYPE_TO_EDATATYPE.put(short[].class, createShortPrimitiveArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(int[].class, createIntPrimitiveArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(long[].class, createLongPrimitiveArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(float[].class, createFloatPrimitiveArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(double[].class, createDoublePrimitiveArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(boolean[].class, createBooleanPrimitiveArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(char[].class, createCharPrimitiveArrayEDataType());

		JAVATYPE_TO_EDATATYPE.put(Byte[].class, createByteWrapperArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(Short[].class, createShortWrapperArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(Integer[].class, createIntWrapperArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(Long[].class, createLongWrapperArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(Float[].class, createFloatWrapperArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(Double[].class, createDoubleWrapperArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(Boolean[].class, createBooleanWrapperArrayEDataType());
		JAVATYPE_TO_EDATATYPE.put(Character[].class, createCharWrapperArrayEDataType());

		JAVATYPE_TO_EDATATYPE.put(String[].class, createStringArrayEDataType());

		JAVATYPE_TO_EDATATYPE.put(org.osgi.framework.Version.class, createVersionEDataType());
	}

	@SafeVarargs
	public final EPackage convert(String packageName, String nsURI, String nsPrefix,
			Class<? extends DTO>... dtoClasses) {

		final EcoreFactory eFactory = EcoreFactory.eINSTANCE;

		EPackage dynamicEPackage = eFactory.createEPackage();
		dynamicEPackage.setName(packageName);
		dynamicEPackage.setNsURI(nsURI);
		dynamicEPackage.setNsPrefix(nsPrefix);

		for (Class<? extends DTO> dtoClass : dtoClasses) {
			createEClass(eFactory, dynamicEPackage, dtoClass);
		}

		EPackage.Registry.INSTANCE.put(dynamicEPackage.getNsURI(), dynamicEPackage);

		return dynamicEPackage;
	}

	private EClass createEClass(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		if (dynamicEClassifierExists(ePackage, javaType.getSimpleName())) {
			LOG.debug(" EClassifier {} already exists!", javaType.getSimpleName());
			return (EClass) ePackage.getEClassifier(javaType.getSimpleName());
		}

		Field[] fields = getFields(javaType);

		EClass eClass = eFactory.createEClass();
		eClass.setName(javaType.getSimpleName());

		for (Field field : fields) {
			createEStructuralFeature(eFactory, ePackage, eClass, field);
		}

		ePackage.getEClassifiers().add(eClass);

		return eClass;
	}

	private void createEStructuralFeature(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
		if (isAttributeType(field.getType())) {

			createEAttribute(eFactory, eClass, field);

		} else if (isEnumType(field.getType())) {

			createEEnum(eFactory, ePackage, eClass, field);

		} else if (isArrayType(field.getType())) {

			createArray(eFactory, ePackage, eClass, field);

		} else if (isCollectionType(field.getType())) {

			Class<?> collectionActualType = getCollectionActualType(field.getGenericType());
			if (collectionActualType == null) {
				LOG.error(" Could not determine actual type for collection specified in field {}! Skipping..",
						field.getName());
				return;
			}

			createEReference(eFactory, ePackage, eClass, collectionActualType, field.getName(),
					ETypedElement.UNBOUNDED_MULTIPLICITY);

		} else if (isMapType(field.getType())) {

			Class<?> mapKeyActualType = getMapKeyActualType(field.getGenericType());
			Class<?> mapValueActualType = getMapValueActualType(field.getGenericType());
			if ((mapKeyActualType == null) || (mapValueActualType == null)) {
				LOG.error(" Could not determine actual type for map {} specified in field {}! Skipping..",
						((mapKeyActualType == null) ? "key" : "value"), field.getName());
				return;
			}

			createEMap(eFactory, ePackage, eClass, field, mapKeyActualType, mapValueActualType);

		} else {

			createEReference(eFactory, ePackage, eClass, field, ETypedElement.UNSPECIFIED_MULTIPLICITY);
		}
	}

	private void createEAttribute(EcoreFactory eFactory, EClass eClass, Field field) {
		createEAttribute(eFactory, eClass, getEDataTypeForJavaType(field.getType()), field);
	}

	private void createEAttribute(EcoreFactory eFactory, EClass eClass, EDataType eType, Field field) {
		EAttribute eAttribute = eFactory.createEAttribute();
		eAttribute.setName(field.getName());
		eAttribute.setEType(eType);

		eClass.getEStructuralFeatures().add(eAttribute);
	}

	private void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field,
			int upperBound) {
		createEReference(eFactory, ePackage, eClass, field.getType(), field.getName(), upperBound);
	}

	private void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Class<?> javaType,
			String fieldName, int upperBound) {
		createEReference(eFactory, ePackage, eClass, getEClassifierForJavaType(eFactory, ePackage, javaType), fieldName,
				upperBound);
	}

	private void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, EClassifier eType,
			String fieldName, int upperBound) {
		EReference eReference = eFactory.createEReference();
		eReference.setName(fieldName);
		eReference.setEType(eType);
		eReference.setContainment(true);
		eReference.setUpperBound(upperBound);

		eClass.getEStructuralFeatures().add(eReference);
	}

	private void createEEnum(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
		EEnum eEnum = null;

		if (!dynamicEClassifierExists(ePackage, field.getType().getSimpleName())) {
			eEnum = createEEnum(eFactory, ePackage, field.getType());
		} else {
			eEnum = (EEnum) ePackage.getEClassifier(field.getType().getSimpleName());
		}

		createEAttribute(eFactory, eClass, eEnum, field);
	}

	private EEnum createEEnum(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		Field[] enumTypeFields = getFields(javaType);

		EEnum eEnum = eFactory.createEEnum();
		eEnum.setName(javaType.getSimpleName());

		for (Field enumTypeField : enumTypeFields) {
			EEnumLiteral eEnumLiteral = eFactory.createEEnumLiteral();
			eEnumLiteral.setName(enumTypeField.getName());
			eEnumLiteral.setLiteral(enumTypeField.getName());

			eEnum.getELiterals().add(eEnumLiteral);
		}

		ePackage.getEClassifiers().add(eEnum);
		return eEnum;
	}

	private void createArray(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
		EClassifier dynamicCustomTypeArrayComponentTypeClass = getEClassifierForJavaType(eFactory, ePackage,
				field.getType().getComponentType());

		String arrayTypeName = constructArrayTypeName(field);

		EDataType arrayType = null;

		if (!dynamicEClassifierExists(ePackage, arrayTypeName)) {
			arrayType = EcoreFactory.eINSTANCE.createEDataType();
			arrayType.setName(arrayTypeName);

			ePackage.getEClassifiers().add(arrayType);
		} else {
			arrayType = (EDataType) ePackage.getEClassifier(arrayTypeName);
		}

		createEAttribute(eFactory, eClass, arrayType, field);
	}

	private String constructArrayTypeName(Field field) {
		StringBuilder sb = new StringBuilder();
		sb.append(field.getType().getComponentType().getSimpleName());
		sb.append("Array");
		return sb.toString();
	}

	private void createEMap(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field,
			Class<?> mapKeyActualType, Class<?> mapValueActualType) {
		String mapEntryEClassName = constructMapEntryEClassName(mapKeyActualType, mapValueActualType);

		EClass mapEntryEClass = null;

		if (!dynamicEClassifierExists(ePackage, mapEntryEClassName)) {
			mapEntryEClass = eFactory.createEClass();
			mapEntryEClass.setName(mapEntryEClassName);
			mapEntryEClass.setInstanceClassName("java.util.Map$Entry");

			// key
			EReference dynamicMapEntryKeyEReference = eFactory.createEReference();
			dynamicMapEntryKeyEReference.setName("key");
			dynamicMapEntryKeyEReference.setEType(getEClassifierForJavaType(eFactory, ePackage, mapKeyActualType));
			mapEntryEClass.getEStructuralFeatures().add(dynamicMapEntryKeyEReference);

			// value
			EReference dynamicMapEntryValueEReference = eFactory.createEReference();
			dynamicMapEntryValueEReference.setName("value");
			dynamicMapEntryValueEReference.setEType(getEClassifierForJavaType(eFactory, ePackage, mapValueActualType));
			mapEntryEClass.getEStructuralFeatures().add(dynamicMapEntryValueEReference);

			ePackage.getEClassifiers().add(mapEntryEClass);

		} else {
			mapEntryEClass = (EClass) ePackage.getEClassifier(mapEntryEClassName);
		}

		// reference to map entry class
		createEReference(eFactory, ePackage, eClass, mapEntryEClass, field.getName(),
				ETypedElement.UNBOUNDED_MULTIPLICITY);
	}

	private String constructMapEntryEClassName(Class<?> mapKeyActualType, Class<?> mapValueActualType) {
		StringBuilder sb = new StringBuilder();
		sb.append(mapKeyActualType.getSimpleName());
		sb.append("To");
		sb.append(mapValueActualType.getSimpleName());
		sb.append("Map");
		return sb.toString();
	}

	private boolean dynamicEClassifierExists(EPackage ePackage, String eClassName) {
		return ePackage.getEClassifiers().stream().anyMatch(e -> eClassName.equals(e.getName()));
	}

	private boolean dynamicEClassifierExists(EPackage ePackage, Class<?> eClass) {
		return dynamicEClassifierExists(ePackage, eClass.getSimpleName());
	}

	private EClassifier getEClassifierForJavaType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		if (JAVATYPE_TO_EDATATYPE.containsKey(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else if (dynamicEClassifierExists(ePackage, javaType)) {
			if (isEnumType(javaType)) {
				return (EEnum) ePackage.getEClassifier(javaType.getSimpleName());
			} else {
				return (EClass) ePackage.getEClassifier(javaType.getSimpleName());
			}
		} else {
			if (isEnumType(javaType)) {
				return createEEnum(eFactory, ePackage, javaType);
			} else {
				return createEClass(eFactory, ePackage, javaType);
			}
		}
	}

	private EDataType getEDataTypeForJavaType(Class<?> javaType) {
		if (JAVATYPE_TO_EDATATYPE.containsKey(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else {
			throw new IllegalArgumentException(String.format("Type %s is not supported!", javaType.getName()));
		}
	}

	private boolean isAttributeType(Class<?> javaType) {
		return (JAVATYPE_TO_EDATATYPE.containsKey(javaType));
	}

	private boolean isArrayType(Class<?> javaType) {
		return javaType.isArray();
	}

	private boolean isEnumType(Class<?> javaType) {
		return javaType.isEnum();
	}

	private boolean isCollectionType(Class<?> javaType) {
		return (javaType.isAssignableFrom(java.util.List.class) || javaType.isAssignableFrom(java.util.Set.class));
	}

	private boolean isMapType(Class<?> javaType) {
		return (javaType.isAssignableFrom(java.util.Map.class));
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> getCollectionActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 0) {
			return ((Class<T>) typeArguments[0]);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> getMapKeyActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 1) {
			return ((Class<T>) typeArguments[0]);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> getMapValueActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 1) {
			return ((Class<T>) typeArguments[1]);
		} else {
			return null;
		}
	}

	private Type[] getActualTypes(Type genericType) {
		return ((java.lang.reflect.ParameterizedType) genericType).getActualTypeArguments();
	}

	private static EDataType createShortPrimitiveArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("ShortPrimitiveArray");
		type.setInstanceClass(short[].class);
		return type;
	}

	private static EDataType createIntPrimitiveArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("IntPrimitiveArray");
		type.setInstanceClass(int[].class);
		return type;
	}

	private static EDataType createLongPrimitiveArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("LongPrimitiveArray");
		type.setInstanceClass(long[].class);
		return type;
	}

	private static EDataType createFloatPrimitiveArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("FloatPrimitiveArray");
		type.setInstanceClass(float[].class);
		return type;
	}

	private static EDataType createDoublePrimitiveArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("DoublePrimitiveArray");
		type.setInstanceClass(double[].class);
		return type;
	}

	private static EDataType createBooleanPrimitiveArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("BooleanPrimitiveArray");
		type.setInstanceClass(boolean[].class);
		return type;
	}

	private static EDataType createCharPrimitiveArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("CharPrimitiveArray");
		type.setInstanceClass(char[].class);
		return type;
	}

	private static EDataType createByteWrapperArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("ByteWrapperArray");
		type.setInstanceClass(Byte[].class);
		return type;
	}

	private static EDataType createShortWrapperArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("ShortWrapperArray");
		type.setInstanceClass(Short[].class);
		return type;
	}

	private static EDataType createIntWrapperArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("IntWrapperArray");
		type.setInstanceClass(Integer[].class);
		return type;
	}

	private static EDataType createLongWrapperArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("LongWrapperArray");
		type.setInstanceClass(Long[].class);
		return type;
	}

	private static EDataType createFloatWrapperArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("FloatWrapperArray");
		type.setInstanceClass(Float[].class);
		return type;
	}

	private static EDataType createDoubleWrapperArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("DoubleWrapperArray");
		type.setInstanceClass(Double[].class);
		return type;
	}

	private static EDataType createBooleanWrapperArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("BooleanWrapperArray");
		type.setInstanceClass(Boolean[].class);
		return type;
	}

	private static EDataType createCharWrapperArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("CharWrapperArray");
		type.setInstanceClass(Character[].class);
		return type;
	}

	private static EDataType createStringArrayEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("StringArray");
		type.setInstanceClass(String[].class);
		return type;
	}

	private static EDataType createVersionEDataType() {
		EDataType type = EcoreFactory.eINSTANCE.createEDataType();
		type.setName("Version");
		type.setInstanceClass(org.osgi.framework.Version.class);
		return type;
	}

	/**
	 * based on
	 * {@link osgi.enroute.dtos.bndlib.provider.DTOsProvider.getFields(Class<?>)}
	 **/
	private Field[] getFields(Class<?> c) {
		Field fields[] = CACHED_FIELDS.get(c);
		if (fields == null) {
			List<Field> publicFields = new ArrayList<>();

			for (Field field : c.getFields()) {
				if (field.isSynthetic()) {
					continue;
				}

				publicFields.add(field);
			}
			Collections.sort(publicFields, new Comparator<Field>() {

				@Override
				public int compare(Field o1, Field o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			CACHED_FIELDS.put(c.getClass(), fields = publicFields.toArray(new Field[publicFields.size()]));
		}
		return fields;
	}
}
