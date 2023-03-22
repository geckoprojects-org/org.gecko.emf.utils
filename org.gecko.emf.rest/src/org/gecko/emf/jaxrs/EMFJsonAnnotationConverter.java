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
package org.gecko.emf.jaxrs;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.jaxrs.annotations.AnnotationConverter;
import org.gecko.emf.jaxrs.annotations.json.EMFJSONConfig;
import org.gecko.emf.jaxrs.annotations.json.RootElement;
import org.gecko.emf.json.constants.EMFJs;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.eclipse.emfcloud.jackson.annotations.EcoreTypeInfo.USE;
import org.eclipse.emfcloud.jackson.databind.EMFContext;

/**
 * Converts the EMFJSON Annotations {@link RootElement} and {@link EMFJSONConfig} to EMF Load or Save Options
 * 
 * @author Juergen Albert
 * @since 24 Jun 2018
 */
@Component
public class EMFJsonAnnotationConverter implements AnnotationConverter {

	@Reference
	ComponentServiceObjects<ResourceSet> setServiceObjects;
	
	/* 
	 * (non-Javadoc)
	 * @see org.geckoprojects.emf.rest.api.AnnotationConverter#canHandle(java.lang.annotation.Annotation, boolean)
	 */
	@Override
	public boolean canHandle(Annotation annotation, boolean serialize) {
		return annotation instanceof RootElement || annotation instanceof EMFJSONConfig;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.geckoprojects.emf.rest.api.AnnotationConverter#convertAnnotation(java.lang.annotation.Annotation, boolean)
	 */
	@Override
	public void convertAnnotation(Annotation annotation, boolean serialize, Map<Object, Object> options) {
		if(annotation instanceof RootElement) {
			RootElement element = (RootElement) annotation;
			
			ResourceSet set = setServiceObjects.getService();
			EClass eClass = (EClass) set.getEObject(URI.createURI(element.rootClassUri()), true);
			setServiceObjects.ungetService(set);
			
			if(eClass == null) {
				throw new IllegalArgumentException(String.format("%s can't be resolved to a EClass", element.rootClassUri()));
			}
			options.put(EMFContext.Attributes.ROOT_ELEMENT, eClass);
		} else if(annotation instanceof EMFJSONConfig) {
			EMFJSONConfig config = (EMFJSONConfig) annotation;
			
			options.put(EMFJs.OPTION_DATE_FORMAT, config.dateFormat());
			options.put(EMFJs.OPTION_INDENT_OUTPUT, config.indentOutput());
			options.put(EMFJs.OPTION_SERIALIZE_DEFAULT_VALUE, config.serializeDefaultValues());
			options.put(EMFJs.OPTION_SERIALIZE_TYPE, config.serializeTypes());
			options.put(EMFJs.OPTION_USE_ID, config.useId());
			options.put(EMFJs.OPTION_TYPE_USE, switchUse(config.typeUSE()));
			
			if(!"".contentEquals(config.refFieldName())) {
				options.put(EMFJs.OPTION_REF_FIELD, config.refFieldName());
			}
			if(!"".contentEquals(config.idFieldName())) {
				options.put(EMFJs.OPTION_ID_FIELD, config.idFieldName());
			}
			if(!"".contentEquals(config.typeFieldName())) {
				options.put(EMFJs.OPTION_TYPE_FIELD, config.typeFieldName());
			}
			if(!"".contentEquals(config.typePackageUri())) {
				options.put(EMFJs.OPTION_TYPE_PACKAGE_URI, config.typePackageUri());
			}
		}
	}
	
	private USE switchUse(EMFJSONConfig.USE toSwitch) {
		switch (toSwitch) {
		case CLASS:
			return USE.CLASS;
		case NAME:
			return USE.NAME;
		default:
			return USE.URI;
		}
	}

}
