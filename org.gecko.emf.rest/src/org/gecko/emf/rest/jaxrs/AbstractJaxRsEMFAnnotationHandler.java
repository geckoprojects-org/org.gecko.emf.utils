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
package org.gecko.emf.rest.jaxrs;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.gecko.emf.rest.annotations.ContentNotEmpty;
import org.gecko.emf.rest.annotations.ResourceEClass;
import org.gecko.emf.rest.common.AbstractEMFAnnotationHandler;

/**
 * Base class to handle EMF resource annotation and turn them into EMF load or
 * save options
 * 
 * @author Mark Hoffmann
 */
public abstract class AbstractJaxRsEMFAnnotationHandler extends AbstractEMFAnnotationHandler {

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.rest.common.AbstractEMFAnnotationHandler#handleValidateContent(org.eclipse.emf.ecore.resource.Resource)
	 */
	@Override
	protected void handleValidateContent(Resource resource) {
		if (resource == null) {
			return;
		}
		for (EObject eObject : resource.getContents()) {
			Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eObject);
			if (diagnostic.getSeverity() == Diagnostic.ERROR) {
				Response res = Response.status(400).entity(buildDiagnosticMessage(diagnostic)).build();
				throw new WebApplicationException(res);
			}
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.rest.common.AbstractEMFAnnotationHandler#handleResourceEClass(org.eclipse.emf.ecore.resource.Resource, java.lang.annotation.Annotation)
	 */
	@Override
	protected void handleResourceEClass(Resource resource, Annotation annotation) {
		if (resource == null || annotation == null) {
			return;
		}
		String eClassName = ((ResourceEClass) annotation).value();
		for (EObject eObject : resource.getContents()) {
			if (!eObject.eClass().getName().equals(eClassName)) {
				List<Variant> encoded = Variant.encodings(eClassName).build();
				Response res = Response.notAcceptable(encoded).build();
				throw new WebApplicationException(res);
			}
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.rest.common.AbstractEMFAnnotationHandler#handleContentNotEmpty(org.eclipse.emf.ecore.resource.Resource, org.gecko.emf.rest.annotations.ContentNotEmpty)
	 */
	@Override
	protected void handleContentNotEmpty(Resource resource, ContentNotEmpty annotation) {
		if (resource == null || annotation == null) {
			return;
		}
		if (resource.getContents().isEmpty()) {
			List<Variant> encoded = Variant.encodings(annotation.message()).build();
			Response res = Response.notAcceptable(encoded).build();
			throw new WebApplicationException(res);
		}
	}
}