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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;
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
import org.gecko.emf.converter.model.ConverterPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java to EPackage converter
 * 
 * @author Michal H. Siemaszko
 */
class JavaToEPackageConverter {
	final static Logger LOG = LoggerFactory.getLogger(JavaToEPackageConverter.class);

	final static Map<Class<?>, Field[]> CACHED_FIELDS = Collections
			.synchronizedMap(new WeakHashMap<Class<?>, Field[]>());

	final static Map<Class<?>, String> CACHED_ECLASSIFIER_NAMES = Collections
			.synchronizedMap(new WeakHashMap<Class<?>, String>());

	final static Map<Class<?>, Method> CACHED_ENUM_CUSTOM_VALUE_METHODS = Collections
			.synchronizedMap(new WeakHashMap<Class<?>, Method>());

	final static Map<Class<?>, EDataType> JAVATYPE_TO_EDATATYPE = new HashMap<>();
	static {
		// @formatter:off
		EcorePackage.eINSTANCE.getEClassifiers().stream()
			.filter(EDataType.class::isInstance)
			.map(EDataType.class::cast)
			.forEach(dt -> JAVATYPE_TO_EDATATYPE.put(dt.getInstanceClass(), dt));
		// @formatter:on

		// @formatter:off
		ConverterPackage.eINSTANCE.getEClassifiers().stream()
			.filter(EDataType.class::isInstance)
			.map(EDataType.class::cast)
			.forEach(dt -> JAVATYPE_TO_EDATATYPE.put(dt.getInstanceClass(), dt));
		// @formatter:on
	}

	// @formatter:off
	final static List<Class<?>> ATTRIBUTE_TYPES = List.of(
			byte.class,
			byte[].class,
			java.lang.Byte.class,
			java.lang.Byte[].class,

			short.class,
			short[].class,
			java.lang.Short.class,
			java.lang.Short[].class,

			int.class,
			int[].class,
			java.lang.Integer.class,
			java.lang.Integer[].class,

			long.class,
			long[].class,
			java.lang.Long.class,
			java.lang.Long[].class,

			float.class,
			float[].class,
			java.lang.Float.class,
			java.lang.Float[].class,

			double.class,
			double[].class,
			java.lang.Double.class,
			java.lang.Double[].class,

			boolean.class,
			boolean[].class,
			java.lang.Boolean.class,
			java.lang.Boolean[].class,

			char.class,
			char[].class,
			java.lang.Character.class,
			java.lang.Character[].class,

			java.lang.String.class,
			java.lang.String[].class,

			org.osgi.framework.Version.class);
	// @formatter:on

	@SafeVarargs
	static EPackage convert(String packageName, String nsURI, String nsPrefix, Class<?>... javaTypes) {
		final EcoreFactory eFactory = EcoreFactory.eINSTANCE;

		EPackage dynamicEPackage = eFactory.createEPackage();
		dynamicEPackage.setName(packageName);
		dynamicEPackage.setNsURI(nsURI);
		dynamicEPackage.setNsPrefix(nsPrefix);

		for (Class<?> javaType : javaTypes) {
			createEClassifier(eFactory, dynamicEPackage, javaType);
		}

		return dynamicEPackage;
	}

	static EClassifier createEClassifier(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		String eClassifierName = constructEClassifierName(javaType);

		if (dynamicEClassifierExists(ePackage, eClassifierName)) {
			LOG.debug("EClassifier {} already exists!", eClassifierName);

			if (isEnumType(javaType)) {
				return (EEnum) ePackage.getEClassifier(eClassifierName);
			} else {
				return (EClass) ePackage.getEClassifier(eClassifierName);
			}
		}

		if (isEnumType(javaType)) {
			return createEEnum(eFactory, ePackage, javaType, eClassifierName);
		} else {
			return createEClass(eFactory, ePackage, javaType, eClassifierName);
		}
	}

	static String constructEClassifierName(Class<?> javaType) {
		if (CACHED_ECLASSIFIER_NAMES.containsKey(javaType)) {
			LOG.debug("EClassifier name for {} already exists!", javaType.getCanonicalName());
			return CACHED_ECLASSIFIER_NAMES.get(javaType);
		}

		String eClassifierName = (javaType.getCanonicalName().replaceFirst((javaType.getPackageName() + "."), ""))
				.replaceAll("\\.", "");

		LOG.debug("Constructed EClassifier name {} for {}", eClassifierName, javaType.getCanonicalName());

		CACHED_ECLASSIFIER_NAMES.put(javaType, eClassifierName);

		return eClassifierName;
	}

	static EClass createEClass(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType, String eClassifierName) {
		LOG.debug("Creating EClass named {} for {}", eClassifierName, javaType.getCanonicalName());

		Field[] fields = getFields(javaType);

		EClass eClass = eFactory.createEClass();
		eClass.setName(eClassifierName);

		ePackage.getEClassifiers().add(eClass);

		for (Field field : fields) {
			createEStructuralFeature(eFactory, ePackage, eClass, field);
		}

		return eClass;
	}

	static void createEStructuralFeature(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
		LOG.debug("Creating EStructuralFeature {} in EClass {}!", field.getName(), eClass.getName());

		if (isAttributeType(field.getType())) {

			createEAttribute(eFactory, eClass, field);

		} else if (isEnumType(field.getType())) {

			createEEnum(eFactory, ePackage, eClass, field);

		} else if (isArrayType(field.getType())) {

			createArray(eFactory, ePackage, eClass, field);

		} else if (isCollectionType(field.getType())) {

			Class<?> collectionActualType = getCollectionActualType(field.getGenericType());
			if (collectionActualType == null) {
				LOG.error("Could not determine actual type for collection specified in field {}! Skipping..",
						field.getName());
				return;
			}

			createEReference(eFactory, ePackage, eClass, collectionActualType, field.getName(),
					ETypedElement.UNBOUNDED_MULTIPLICITY);

		} else if (isMapType(field.getType())) {

			Class<?> mapKeyActualType = getMapKeyActualType(field.getGenericType());
			Class<?> mapValueActualType = getMapValueActualType(field.getGenericType());
			if ((mapKeyActualType == null) || (mapValueActualType == null)) {
				LOG.error("Could not determine actual type for map {} specified in field {}! Skipping..",
						((mapKeyActualType == null) ? "key" : "value"), field.getName());
				return;
			}

			createEMap(eFactory, ePackage, eClass, field, mapKeyActualType, mapValueActualType);

		} else {

			createEReference(eFactory, ePackage, eClass, field, ETypedElement.UNSPECIFIED_MULTIPLICITY);
		}
	}

	static void createEAttribute(EcoreFactory eFactory, EClass eClass, Field field) {
		createEAttribute(eFactory, eClass, getEDataTypeForJavaType(field.getType()), field);
	}

	static void createEAttribute(EcoreFactory eFactory, EClass eClass, EDataType eType, Field field) {
		EAttribute eAttribute = eFactory.createEAttribute();
		eAttribute.setName(field.getName());
		eAttribute.setEType(eType);

		eClass.getEStructuralFeatures().add(eAttribute);
	}

	static void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field, int upperBound) {
		createEReference(eFactory, ePackage, eClass, field.getType(), field.getName(), upperBound);
	}

	static void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Class<?> javaType,
			String fieldName, int upperBound) {
		createEReference(eFactory, ePackage, eClass, getEClassifierForJavaType(eFactory, ePackage, javaType), fieldName,
				upperBound);
	}

	static void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, EClassifier eType,
			String fieldName, int upperBound) {
		EReference eReference = eFactory.createEReference();
		eReference.setName(fieldName);
		eReference.setEType(eType);
		eReference.setContainment(true);
		eReference.setUpperBound(upperBound);

		eClass.getEStructuralFeatures().add(eReference);
	}

	static void createEEnum(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
		String eClassifierName = constructEClassifierName(field.getType());

		EEnum eEnum = null;

		if (dynamicEClassifierExists(ePackage, eClassifierName)) {
			eEnum = (EEnum) ePackage.getEClassifier(eClassifierName);
		} else {
			eEnum = createEEnum(eFactory, ePackage, field.getType(), eClassifierName);
		}

		createEAttribute(eFactory, eClass, eEnum, field);
	}

	static EEnum createEEnum(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType, String eClassifierName) {
		LOG.debug("Creating EEnum named {} for {}", eClassifierName, javaType.getCanonicalName());

		Field[] enumTypeFields = getFields(javaType);

		Object[] enumTypeConstants = javaType.getEnumConstants();

		EEnum eEnum = eFactory.createEEnum();
		eEnum.setName(eClassifierName);

		ePackage.getEClassifiers().add(eEnum);

		for (int i = 0; i < enumTypeFields.length; i++) {
			Field enumTypeField = enumTypeFields[i];
			if (enumTypeField.isEnumConstant()) {
				EEnumLiteral eEnumLiteral = eFactory.createEEnumLiteral();
				eEnumLiteral.setName(enumTypeField.getName());
				eEnumLiteral.setValue(getEnumValue(javaType, enumTypeConstants, i));
				eEnumLiteral.setLiteral(String.valueOf(enumTypeConstants[i]));

				eEnum.getELiterals().add(eEnumLiteral);
			}
		}

		return eEnum;
	}

	static int getEnumValue(Class<?> enumType, Object[] enumTypeConstants, int index) {
		Integer customEnumValue = getEnumCustomValue(enumType, enumTypeConstants, index);
		return (customEnumValue != null) ? customEnumValue.intValue() : index;
	}

	static Integer getEnumCustomValue(Class<?> enumType, Object[] enumTypeConstants, int index) {
		Integer customEnumValue = null;

		Method customValueMethod = null;

		if (CACHED_ENUM_CUSTOM_VALUE_METHODS.containsKey(enumType)) {
			customValueMethod = CACHED_ENUM_CUSTOM_VALUE_METHODS.get(enumType);
			if (customValueMethod == null) {
				return customEnumValue;
			}
		} else {
			Optional<Field> customValueFieldOptional = Arrays.stream(enumType.getDeclaredFields()).filter(
					f -> (!f.isEnumConstant() && !Modifier.isStatic(f.getModifiers()) && f.getType() == int.class))
					.findFirst();

			if (customValueFieldOptional.isPresent()) {
				Optional<Method> customValueMethodOptional = Arrays
						.stream(enumType
								.getDeclaredMethods())
						.filter(m -> (!Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers())
								&& (m.getReturnType() == int.class) && StringUtils.containsIgnoreCase(m.getName(),
										customValueFieldOptional.get().getName())))
						.findFirst();
				if (customValueMethodOptional.isPresent()) {
					CACHED_ENUM_CUSTOM_VALUE_METHODS.put(enumType, customValueMethodOptional.get());
				} else {
					CACHED_ENUM_CUSTOM_VALUE_METHODS.put(enumType, null);
				}
			} else {
				CACHED_ENUM_CUSTOM_VALUE_METHODS.put(enumType, null);
			}
		}

		if (customValueMethod != null) {
			try {
				customEnumValue = Integer.valueOf((int) customValueMethod.invoke(enumTypeConstants[index]));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				LOG.error("Error obtaining custom enum value!", e);
			}
		}

		return customEnumValue;
	}

	static void createArray(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
		String arrayTypeName = constructArrayTypeName(field);

		EDataType arrayType = null;

		if (dynamicEClassifierExists(ePackage, arrayTypeName)) {
			arrayType = (EDataType) ePackage.getEClassifier(arrayTypeName);
		} else {
			arrayType = eFactory.createEDataType();
			arrayType.setName(arrayTypeName);

			ePackage.getEClassifiers().add(arrayType);
		}

		createEAttribute(eFactory, eClass, arrayType, field);
	}

	static String constructArrayTypeName(Field field) {
		StringBuilder sb = new StringBuilder();
		sb.append(field.getType().getComponentType().getSimpleName());
		sb.append("Array");
		return sb.toString();
	}

	static void createEMap(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field,
			Class<?> mapKeyActualType, Class<?> mapValueActualType) {
		String mapEntryEClassName = constructMapEntryEClassName(mapKeyActualType, mapValueActualType);

		EClass mapEntryEClass = null;

		if (dynamicEClassifierExists(ePackage, mapEntryEClassName)) {
			mapEntryEClass = (EClass) ePackage.getEClassifier(mapEntryEClassName);
		} else {
			mapEntryEClass = eFactory.createEClass();
			mapEntryEClass.setName(mapEntryEClassName);
			mapEntryEClass.setInstanceClassName("java.util.Map$Entry");

			// key
			EAttribute dynamicMapEntryKeyEAttribute = eFactory.createEAttribute();
			dynamicMapEntryKeyEAttribute.setName("key");
			dynamicMapEntryKeyEAttribute.setEType(getEClassifierForJavaType(eFactory, ePackage, mapKeyActualType));
			mapEntryEClass.getEStructuralFeatures().add(dynamicMapEntryKeyEAttribute);

			// value
			EAttribute dynamicMapEntryValueEAttribute = eFactory.createEAttribute();
			dynamicMapEntryValueEAttribute.setName("value");
			dynamicMapEntryValueEAttribute.setEType(getEClassifierForJavaType(eFactory, ePackage, mapValueActualType));
			mapEntryEClass.getEStructuralFeatures().add(dynamicMapEntryValueEAttribute);

			ePackage.getEClassifiers().add(mapEntryEClass);
		}

		// reference to map entry class
		createEReference(eFactory, ePackage, eClass, mapEntryEClass, field.getName(),
				ETypedElement.UNBOUNDED_MULTIPLICITY);
	}

	static String constructMapEntryEClassName(Class<?> mapKeyActualType, Class<?> mapValueActualType) {
		StringBuilder sb = new StringBuilder();
		sb.append(mapKeyActualType.getSimpleName());
		sb.append("To");
		sb.append(mapValueActualType.getSimpleName());
		sb.append("Map");
		return sb.toString();
	}

	static boolean dynamicEClassifierExists(EPackage ePackage, String eClassName) {
		return ePackage.getEClassifiers().stream().anyMatch(e -> eClassName.equals(e.getName()));
	}

	static EClassifier getEClassifierForJavaType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		String eClassifierName = constructEClassifierName(javaType);

		if (JAVATYPE_TO_EDATATYPE.containsKey(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else if (dynamicEClassifierExists(ePackage, eClassifierName)) {
			if (isEnumType(javaType)) {
				return (EEnum) ePackage.getEClassifier(eClassifierName);
			} else {
				return (EClass) ePackage.getEClassifier(eClassifierName);
			}
		} else {
			if (isEnumType(javaType)) {
				return createEEnum(eFactory, ePackage, javaType, eClassifierName);
			} else {
				return createEClass(eFactory, ePackage, javaType, eClassifierName);
			}
		}
	}

	static EDataType getEDataTypeForJavaType(Class<?> javaType) {
		if (JAVATYPE_TO_EDATATYPE.containsKey(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else {
			throw new IllegalArgumentException(String.format("Type %s is not supported!", javaType.getName()));
		}
	}

	static boolean isAttributeType(Class<?> javaType) {
		return ATTRIBUTE_TYPES.contains(javaType);
	}

	static boolean isArrayType(Class<?> javaType) {
		return javaType.isArray();
	}

	static boolean isEnumType(Class<?> javaType) {
		return javaType.isEnum();
	}

	static boolean isCollectionType(Class<?> javaType) {
		return (javaType.isAssignableFrom(java.util.List.class) || javaType.isAssignableFrom(java.util.Set.class));
	}

	static boolean isMapType(Class<?> javaType) {
		return (javaType.isAssignableFrom(java.util.Map.class));
	}

	@SuppressWarnings("unchecked")
	static <T> Class<T> getCollectionActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 0) {
			return ((Class<T>) typeArguments[0]);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	static <T> Class<T> getMapKeyActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 1) {
			return ((Class<T>) typeArguments[0]);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	static <T> Class<T> getMapValueActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 1) {
			return ((Class<T>) typeArguments[1]);
		} else {
			return null;
		}
	}

	static Type[] getActualTypes(Type genericType) {
		return ((java.lang.reflect.ParameterizedType) genericType).getActualTypeArguments();
	}

	/**
	 * based on
	 * {@link osgi.enroute.dtos.bndlib.provider.DTOsProvider.getFields(Class<?>)}
	 **/
	static Field[] getFields(Class<?> c) {
		Field fields[] = CACHED_FIELDS.get(c);
		if (fields == null) {
			List<Field> publicFields = new ArrayList<>();

			for (Field field : c.getFields()) {
				if (field.isSynthetic()) {
					continue;
				}

				publicFields.add(field);
			}

			CACHED_FIELDS.put(c.getClass(), fields = publicFields.toArray(new Field[publicFields.size()]));
		}
		return fields;
	}
}
