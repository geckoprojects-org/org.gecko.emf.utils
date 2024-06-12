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
import org.eclipse.emf.ecore.EcoreFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java Reference Type to EPackage converter.
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

		if (dynamicEClassifierExists(ePackage, eClassifierName)) {
			LOG.debug("EClassifier {} already exists!", eClassifierName);

			if (isCustomEDataType(ePackage, eClassifierName)) {
				return (EDataType) ePackage.getEClassifier(eClassifierName);
			} else if (isEnumType(javaType)) {
				return (EEnum) ePackage.getEClassifier(eClassifierName);
			} else {
				return (EClass) ePackage.getEClassifier(eClassifierName);
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

		if ((method.getReturnType() != null) && !void.class.isAssignableFrom(method.getReturnType())) {
			eOperation.setEType(getEClassifierForJavaType(eFactory, ePackage, method.getReturnType()));
		}

		if (method.getParameterCount() > 0) {
			Parameter[] parameters = method.getParameters();

			for (int i = 0; i < method.getParameterCount(); i++) {

				Parameter parameter = parameters[i];

				EParameter eParameter = eFactory.createEParameter();
				eParameter.setName(parameter.getName());
				eParameter.setEType(getEClassifierForJavaType(eFactory, ePackage, parameter.getType()));

				eOperation.getEParameters().add(eParameter);
			}
		}

		Class<?>[] exceptionTypes = method.getExceptionTypes();
		if (exceptionTypes.length > 0) {
			for (int i = 0; i < exceptionTypes.length; i++) {
				eOperation.getEExceptions().add(getEDataTypeForJavaType(eFactory, ePackage, exceptionTypes[i]));
			}
		}

		eClass.getEOperations().add(eOperation);
	}

	@Override
	protected EClassifier getEClassifierForJavaType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		String eClassifierName = constructEClassifierName(javaType);

		if (JAVATYPE_TO_EDATATYPE.containsKey(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else if (dynamicEClassifierExists(ePackage, eClassifierName)) {
			if (isCustomEDataType(ePackage, eClassifierName)) {
				return (EDataType) ePackage.getEClassifier(eClassifierName);
			} else if (isEnumType(javaType)) {
				return (EEnum) ePackage.getEClassifier(eClassifierName);
			} else {
				return (EClass) ePackage.getEClassifier(eClassifierName);
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

	private boolean isCustomEDataType(EPackage ePackage, String eClassName) {
		return ePackage.getEClassifiers().stream()
				.anyMatch(e -> eClassName.equals(e.getName()) && EDataType.class.isAssignableFrom(e.getClass()));
	}

	private boolean maybeCustomEDataType(Class<?> javaType) {
		return (javaType.getPackageName().startsWith("java") || java.lang.Throwable.class.isAssignableFrom(javaType));
	}

	private EDataType createCustomEDataType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		EDataType eDataType = eFactory.createEDataType();
		eDataType.setName(javaType.getSimpleName());
		eDataType.setInstanceClass(javaType);

		ePackage.getEClassifiers().add(eDataType);

		return eDataType;
	}

	private EDataType getEDataTypeForJavaType(EcoreFactory eFactory, EPackage ePackage, Class<?> javaType) {
		String eClassifierName = constructEClassifierName(javaType);

		if (JAVATYPE_TO_EDATATYPE.containsKey(javaType)) {
			return JAVATYPE_TO_EDATATYPE.get(javaType);
		} else if (dynamicEClassifierExists(ePackage, eClassifierName)) {
			try {
				return (EDataType) ePackage.getEClassifier(eClassifierName);
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
