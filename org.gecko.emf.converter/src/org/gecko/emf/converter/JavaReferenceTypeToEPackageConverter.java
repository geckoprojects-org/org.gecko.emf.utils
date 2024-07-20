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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.EcoreFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java Reference Type to EPackage converter - converts Java reference types (
 * classes, interfaces, enums, collections ) to dynamic EMF model.
 * 
 * @author Michal H. Siemaszko
 */
@Component(name = "JavaReferenceTypeToEPackageConverter", scope = ServiceScope.SINGLETON)
public class JavaReferenceTypeToEPackageConverter extends AbstractJavaToEPackageConverter
		implements JavaToEPackageConverter {
	private static final Logger LOG = LoggerFactory.getLogger(JavaReferenceTypeToEPackageConverter.class);

	// @formatter:off
	private static final List<Class<?>> BUILTIN_METHOD_TYPES = List.of(
			Object.class,
			Enum.class,
			Annotation.class,
			Exception.class,
			Map.class,
			List.class,
			Array.class,
			Comparable.class);
	// @formatter:on

	private static final Method[] BUILTIN_METHODS = getBuiltInMethods();

	private static final Map<Class<?>, Method[]> CACHED_METHODS = Collections
			.synchronizedMap(new WeakHashMap<Class<?>, Method[]>());

	public JavaReferenceTypeToEPackageConverter() {
		super(LOG);
	}

	@Override
	protected EClassifier createEClassifier(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		String eClassifierName = constructEClassifierName(javaType);
		String eClassifierPackageName = javaType.getPackageName();

		if (dynamicEClassifierExists(ePackage, eClassifierName, eClassifierPackageName)) {
			LOG.debug("EClassifier {} already exists in package {}!", eClassifierName, eClassifierPackageName);

			if (isCustomEDataType(ePackage, eClassifierName, eClassifierPackageName)) {
				return (EDataType) findEClassifierByName(ePackage, eClassifierName, eClassifierPackageName);
			} else if (isEnumType(javaType)) {
				return (EEnum) findEClassifierByName(ePackage, eClassifierName, eClassifierPackageName);
			} else {
				return (EClass) findEClassifierByName(ePackage, eClassifierName, eClassifierPackageName);
			}
		}

		if (maybeCustomEDataType(javaType)) {
			return createCustomEDataType(eFactory, ePackage, javaType);
		} else if (isEnumType(javaType)) {
			return createEEnum(eFactory, ePackage, javaType, eClassifierName);
		} else {
			return createEClass(eFactory, ePackage, javaType, eClassifierName);
		}
	}

	@Override
	protected EClass createEClass(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType, String eClassifierName) {
		EClass eClass = super.createEClass(eFactory, ePackage, javaType, eClassifierName);

		Method[] methods = getMethods(javaType);

		for (Method method : methods) {
			createEOperation(eFactory, ePackage, eClass, method);
		}

		return eClass;
	}

	private void createEOperation(EcoreFactory eFactory, EPackage ePackage, EClass eClass, Method method) {
		LOG.debug("Creating EOperation {} in EClass {}!", method.getName(), eClass.getName());

		EOperation eOperation = eFactory.createEOperation();
		eOperation.setName(method.getName());

		Class<?> methodReturnType = method.getReturnType();

		if ((methodReturnType != null) && !void.class.isAssignableFrom(methodReturnType)) {
			if (isArrayType(methodReturnType) && !isPredefinedEDataType(methodReturnType)) {
				eOperation.setEType(getEClassifierForJavaType(eFactory, ePackage, methodReturnType.getComponentType()));
				eOperation.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
			} else {
				eOperation.setEType(getEClassifierForJavaType(eFactory, ePackage, method.getReturnType()));
			}
		}

		if (method.getParameterCount() > 0) {
			Parameter[] parameters = method.getParameters();

			for (int i = 0; i < method.getParameterCount(); i++) {
				Parameter parameter = parameters[i];

				EParameter eParameter = eFactory.createEParameter();
				eParameter.setName(parameter.getName());

				Class<?> parameterType = parameter.getType();

				if (isArrayType(parameterType) && !isPredefinedEDataType(parameterType)) {
					eParameter
							.setEType(getEClassifierForJavaType(eFactory, ePackage, parameterType.getComponentType()));
					eParameter.setUpperBound(ETypedElement.UNBOUNDED_MULTIPLICITY);
				} else {
					eParameter.setEType(getEClassifierForJavaType(eFactory, ePackage, parameterType));
				}

				eOperation.getEParameters().add(eParameter);
			}
		}

		Class<?>[] exceptionTypes = method.getExceptionTypes();
		if (exceptionTypes.length > 0) {
			for (int i = 0; i < exceptionTypes.length; i++) {
				eOperation.getEExceptions().add(getEDataTypeForJavaType(eFactory, ePackage, exceptionTypes[i]));
			}
		}

		if (!eOperationExists(eClass, eOperation)) {
			eClass.getEOperations().add(eOperation);
		}
	}

	protected boolean eOperationExists(EClass eClass, EOperation eOperation1) {
		return eClass.getEOperations().stream().anyMatch(eOperation2 -> eOperationMatches(eOperation1, eOperation2));
	}

	protected boolean eOperationMatches(EOperation eOperation1, EOperation eOperation2) {
		return (eOperation1.getName()).equals(eOperation2.getName())
				&& eOperationParametersMatch(eOperation1, eOperation2);
	}

	protected boolean eOperationParametersMatch(EOperation eOperation1, EOperation eOperation2) {
		boolean parametersMatch = eOperation1.getEParameters().size() == eOperation2.getEParameters().size();

		if (parametersMatch && (!eOperation1.getEParameters().isEmpty() && !eOperation2.getEParameters().isEmpty())) {
			List<EParameter> eOperation1EParameters = eOperation1.getEParameters();
			List<EParameter> eOperation2EParameters = eOperation2.getEParameters();

			for (int i = 0; i < eOperation1EParameters.size(); i++) {
				parametersMatch = (eOperation1EParameters.get(i).getEType() == eOperation2EParameters.get(i)
						.getEType());
				if (!parametersMatch) {
					break;
				}
			}
		}

		return parametersMatch;
	}

	@Override
	protected EClassifier getEClassifierForJavaType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		String eClassifierName = constructEClassifierName(javaType);
		String eClassifierPackageName = javaType.getPackageName();

		if (isPredefinedEDataType(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else if (dynamicEClassifierExists(ePackage, eClassifierName, eClassifierPackageName)) {
			if (isCustomEDataType(ePackage, eClassifierName, eClassifierPackageName)) {
				return (EDataType) findEClassifierByName(ePackage, eClassifierName, eClassifierPackageName);
			} else if (isEnumType(javaType)) {
				return (EEnum) findEClassifierByName(ePackage, eClassifierName, eClassifierPackageName);
			} else {
				return (EClass) findEClassifierByName(ePackage, eClassifierName, eClassifierPackageName);
			}
		} else {
			if (maybeCustomEDataType(javaType)) {
				return createCustomEDataType(eFactory, ePackage, javaType);
			} else if (isEnumType(javaType)) {
				return createEEnum(eFactory, ePackage, javaType, eClassifierName);
			} else {
				return createEClass(eFactory, ePackage, javaType, eClassifierName);
			}
		}
	}

	private boolean isCustomEDataType(EPackage ePackage, String eClassifierName, String eClassifierPackageName) {
		String[] packageNameParts = extractPackageNameParts(eClassifierPackageName);

		// @formatter:off
		return flattenEClassifierTree(ePackage).stream()
				.anyMatch(eClassifier -> eClassifierMatches(eClassifier, eClassifierName, packageNameParts)
						&& EDataType.class.isAssignableFrom(eClassifier.getClass()));
		// @formatter:on
	}

	private boolean maybeCustomEDataType(Class<?> javaType) {
		return (javaType.getPackageName().startsWith("java.") || java.lang.Throwable.class.isAssignableFrom(javaType));
	}

	private EDataType createCustomEDataType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		EDataType eDataType = eFactory.createEDataType();
		eDataType.setName(javaType.getSimpleName());
		eDataType.setInstanceClass(javaType);

		EPackage eSubPackage = getOrCreateESubPackage(eFactory, ePackage, javaType.getPackageName());
		eSubPackage.getEClassifiers().add(eDataType);

		return eDataType;
	}

	private EDataType getEDataTypeForJavaType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		String eClassifierName = constructEClassifierName(javaType);
		String eClassifierPackageName = javaType.getPackageName();

		if (isPredefinedEDataType(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else if (dynamicEClassifierExists(ePackage, eClassifierName, eClassifierPackageName)) {
			try {
				return (EDataType) findEClassifierByName(ePackage, eClassifierName, eClassifierPackageName);
			} catch (Throwable t) {
				return null;
			}

		} else {
			return createCustomEDataType(eFactory, ePackage, javaType);
		}
	}

	private Method[] getMethods(Class<?> c) {
		Method[] methods = CACHED_METHODS.get(c);
		if (methods == null) {
			List<Method> publicMethods = new ArrayList<>();

			for (Method method : c.getMethods()) {
				if (method.isSynthetic() || isBuiltInMethod(method)) {
					continue;
				}

				publicMethods.add(method);
			}

			CACHED_METHODS.put(c.getClass(), methods = publicMethods.toArray(Method[]::new));
		}
		return methods;
	}

	private boolean isBuiltInMethod(Method m) {
		return (Arrays.stream(BUILTIN_METHODS).anyMatch(om -> om.getName().equals(m.getName())
				&& Arrays.equals(om.getParameterTypes(), m.getParameterTypes())));
	}

	private static Method[] getBuiltInMethods() {
		List<Method> builtInMethods = new ArrayList<>();

		for (Class<?> builtInMethodType : BUILTIN_METHOD_TYPES) {
			builtInMethods.addAll(Arrays.asList(builtInMethodType.getMethods()));
		}

		return builtInMethods.toArray(Method[]::new);
	}
}
