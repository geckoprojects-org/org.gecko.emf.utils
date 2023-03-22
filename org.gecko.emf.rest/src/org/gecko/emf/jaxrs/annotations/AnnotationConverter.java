/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.jaxrs.annotations;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * API to handle custom Annotations to create EMF Load or Save Options
 * @author Juergen Albert
 * @since 29 May 2018
 */
public interface AnnotationConverter {
	
	
	/**
	 * Checks if the {@link Annotation} can be handled
	 * @param annotation the {@link Annotation} to handle
	 * @param serialize if true, this annotation is used while serializing, false means deserialization
	 * @return true if the converter should be applied.
	 */
	boolean canHandle(Annotation annotation, boolean serialize);
	
	/**
	 * Converts the given {@link Annotation} and adds the Putput to the given options {@link Map}
	 * @param annotation the {@link Annotation} to convert
	 * @param serialize if true, this annotation is used while serializing, false means deserialization
	 * @param options the {@link Map} with the EMF Options
	 */
	void convertAnnotation(Annotation annotation, boolean serialize, Map<Object,Object> options);

}
