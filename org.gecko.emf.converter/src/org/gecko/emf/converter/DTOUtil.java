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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Based on {@link org.osgi.util.converter.DTOUtil}, as currently it is
 * package-private.
 * 
 * @author Michal H. Siemaszko
 */
class DTOUtil {
	private static final Method[] OBJECT_CLASS_METHODS = Object.class.getMethods();

	private DTOUtil() {
		// Do not instantiate. This is a utility class.
	}

	static boolean isDTOType(Class<?> cls, boolean ignorePublicNoArgsCtor, boolean allowStaticMethods) {
		if (!ignorePublicNoArgsCtor) {
			if (Arrays.stream(cls.getConstructors()).noneMatch(ctor -> ctor.getParameterCount() == 0)) {
				// No public zero-arg constructor, not a DTO
				return false;
			}
		}

		for (Method m : cls.getMethods()) {
			if ((!allowStaticMethods && isNotObjectClassMethod(m))
					|| (allowStaticMethods && isNotObjectClassMethod(m) && !Modifier.isStatic(m.getModifiers()))) {
				return false;
			}
		}

		boolean foundField = false;
		for (Field f : cls.getFields()) {
			int modifiers = f.getModifiers();
			if (Modifier.isStatic(modifiers)) {
				// ignore static fields
				continue;
			}

			if (!Modifier.isPublic(modifiers)) {
				return false;
			}
			foundField = true;
		}
		return foundField;
	}

	private static boolean isNotObjectClassMethod(Method m) {
		return (Arrays.stream(OBJECT_CLASS_METHODS).noneMatch(om -> om.getName().equals(m.getName())
				&& Arrays.equals(om.getParameterTypes(), m.getParameterTypes())));
	}
}
