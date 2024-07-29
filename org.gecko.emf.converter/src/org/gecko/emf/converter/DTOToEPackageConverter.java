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
import org.gecko.emf.converter.model.ConverterPackage;
import org.osgi.dto.DTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DTO to EPackge converter
 * 
 * @author Michal H. Siemaszko
 */
public class DTOToEPackageConverter {
	private final static Logger LOG = LoggerFactory.getLogger(DTOToEPackageConverter.class);

	private final static Map<Class<?>, Field[]> CACHED_FIELDS = Collections
			.synchronizedMap(new WeakHashMap<Class<?>, Field[]>());

	private final static Map<Class<?>, EDataType> JAVATYPE_TO_EDATATYPE = new HashMap<>();
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
	private final static List<Class<?>> ATTRIBUTE_TYPES = List.of(
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

	private DTOToEPackageConverter() {
		// Do not instantiate. This is a utility class.
	}

	@SafeVarargs
	public final static EPackage convert(String packageName, String nsURI, String nsPrefix,
			Class<? extends DTO>... dtoClasses) {

		final EcoreFactory eFactory = EcoreFactory.eINSTANCE;

		EPackage dynamicEPackage = eFactory.createEPackage();
		dynamicEPackage.setName(packageName);
		dynamicEPackage.setNsURI(nsURI);
		dynamicEPackage.setNsPrefix(nsPrefix);

		for (Class<? extends DTO> dtoClass : dtoClasses) {
			createEClass(eFactory, dynamicEPackage, dtoClass);
		}

		return dynamicEPackage;
	}

	private static EClass createEClass(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
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

	private static void createEStructuralFeature(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
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

	private static void createEAttribute(EcoreFactory eFactory, EClass eClass, Field field) {
		createEAttribute(eFactory, eClass, getEDataTypeForJavaType(field.getType()), field);
	}

	private static void createEAttribute(EcoreFactory eFactory, EClass eClass, EDataType eType, Field field) {
		EAttribute eAttribute = eFactory.createEAttribute();
		eAttribute.setName(field.getName());
		eAttribute.setEType(eType);

		eClass.getEStructuralFeatures().add(eAttribute);
	}

	private static void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field,
			int upperBound) {
		createEReference(eFactory, ePackage, eClass, field.getType(), field.getName(), upperBound);
	}

	private static void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Class<?> javaType,
			String fieldName, int upperBound) {
		createEReference(eFactory, ePackage, eClass, getEClassifierForJavaType(eFactory, ePackage, javaType), fieldName,
				upperBound);
	}

	private static void createEReference(EcoreFactory eFactory, EPackage ePackage, EClass eClass, EClassifier eType,
			String fieldName, int upperBound) {
		EReference eReference = eFactory.createEReference();
		eReference.setName(fieldName);
		eReference.setEType(eType);
		eReference.setContainment(true);
		eReference.setUpperBound(upperBound);

		eClass.getEStructuralFeatures().add(eReference);
	}

	private static void createEEnum(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
		EEnum eEnum = null;

		if (!dynamicEClassifierExists(ePackage, field.getType().getSimpleName())) {
			eEnum = createEEnum(eFactory, ePackage, field.getType());
		} else {
			eEnum = (EEnum) ePackage.getEClassifier(field.getType().getSimpleName());
		}

		createEAttribute(eFactory, eClass, eEnum, field);
	}

	private static EEnum createEEnum(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
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

	private static void createArray(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field) {
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

	private static String constructArrayTypeName(Field field) {
		StringBuilder sb = new StringBuilder();
		sb.append(field.getType().getComponentType().getSimpleName());
		sb.append("Array");
		return sb.toString();
	}

	private static void createEMap(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Field field,
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

	private static String constructMapEntryEClassName(Class<?> mapKeyActualType, Class<?> mapValueActualType) {
		StringBuilder sb = new StringBuilder();
		sb.append(mapKeyActualType.getSimpleName());
		sb.append("To");
		sb.append(mapValueActualType.getSimpleName());
		sb.append("Map");
		return sb.toString();
	}

	private static boolean dynamicEClassifierExists(EPackage ePackage, String eClassName) {
		return ePackage.getEClassifiers().stream().anyMatch(e -> eClassName.equals(e.getName()));
	}

	private static boolean dynamicEClassifierExists(EPackage ePackage, Class<?> eClass) {
		return dynamicEClassifierExists(ePackage, eClass.getSimpleName());
	}

	private static EClassifier getEClassifierForJavaType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
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

	private static EDataType getEDataTypeForJavaType(Class<?> javaType) {
		if (JAVATYPE_TO_EDATATYPE.containsKey(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else {
			throw new IllegalArgumentException(String.format("Type %s is not supported!", javaType.getName()));
		}
	}

	private static boolean isAttributeType(Class<?> javaType) {
		return ATTRIBUTE_TYPES.contains(javaType);
	}

	private static boolean isArrayType(Class<?> javaType) {
		return javaType.isArray();
	}

	private static boolean isEnumType(Class<?> javaType) {
		return javaType.isEnum();
	}

	private static boolean isCollectionType(Class<?> javaType) {
		return (javaType.isAssignableFrom(java.util.List.class) || javaType.isAssignableFrom(java.util.Set.class));
	}

	private static boolean isMapType(Class<?> javaType) {
		return (javaType.isAssignableFrom(java.util.Map.class));
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> getCollectionActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 0) {
			return ((Class<T>) typeArguments[0]);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> getMapKeyActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 1) {
			return ((Class<T>) typeArguments[0]);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> getMapValueActualType(Type genericType) {
		Type[] typeArguments = getActualTypes(genericType);
		if (typeArguments.length > 1) {
			return ((Class<T>) typeArguments[1]);
		} else {
			return null;
		}
	}

	private static Type[] getActualTypes(Type genericType) {
		return ((java.lang.reflect.ParameterizedType) genericType).getActualTypeArguments();
	}

	/**
	 * based on
	 * {@link osgi.enroute.dtos.bndlib.provider.DTOsProvider.getFields(Class<?>)}
	 **/
	private static Field[] getFields(Class<?> c) {
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
