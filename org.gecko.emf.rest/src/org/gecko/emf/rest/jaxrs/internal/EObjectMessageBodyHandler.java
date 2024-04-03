/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.rest.jaxrs.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.gecko.emf.osgi.ResourceSetFactory;
import org.gecko.emf.rest.annotations.AnnotationConverter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jaxrs.whiteboard.JaxrsWhiteboardConstants;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsApplicationSelect;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsExtension;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsName;

/**
 * {@link MessageBodyReader} and {@link MessageBodyWriter} that handle {@link EObject}.
 * This readers read and write XMI from a {@link org.eclipse.emf.ecore.resource.Resource}
 * @author Mark Hoffmann
 * @param <R> the reader type, must be an {@link EObject}
 * @param <W> the writer type, must be an {@link EObject}
 * @since 30.05.2012
 */
@Component(
		service = {MessageBodyReader.class, MessageBodyWriter.class},
		enabled = true,
		scope = ServiceScope.SINGLETON
	)
@JaxrsExtension
@JaxrsName("EMFEObjectMessagebodyReaderWriter")
@JaxrsApplicationSelect("(|(emf=true)("+ JaxrsWhiteboardConstants.JAX_RS_NAME + "=.default))")
@Provider
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class EObjectMessageBodyHandler<R extends EObject, W extends EObject> extends BaseJaxRsEMFMessageBodyReaderWriter<R, W>{

	@Reference
	private ResourceSetFactory resourceSetFactory;
	
	/*
	 * (non-Javadoc)
	 * @see jakarta.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		ResourceSetFactory setFactory = getResourceSetFactory();
		ResourceSet resourceSet = setFactory.createResourceSet();
		return EObject.class.isAssignableFrom(type) && resourceSet.getResourceFactoryRegistry()
				.getContentTypeToFactoryMap().containsKey(mediaType.getType() + "/" + mediaType.getSubtype());
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType, jakarta.ws.rs.core.MultivaluedMap, java.io.OutputStream)
	 */
	@Override
	public void writeTo(W t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException,
			WebApplicationException {
		ResourceSetFactory setFactory = getResourceSetFactory();
		ResourceSet resourceSet = setFactory.createResourceSet();
		Resource resource = t.eResource();
		boolean cleanUp = false;
		if(resource == null){
			cleanUp = true;
			ResourceFactoryImpl factory = (ResourceFactoryImpl) resourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap().get(mediaType.getType() + "/" + mediaType.getSubtype());
			resource = factory.createResource(URI.createURI("http://test.test"));
			resourceSet.getResources().add(resource);
			resource.getContents().add(t);
		}
		
		super.writeResourceTo(resource, Resource.class, genericType, annotations, mediaType, httpHeaders, entityStream);
		
		if(cleanUp){
			resource.getContents().remove(t);
			resource.getResourceSet().getResources().remove(resource);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		ResourceSetFactory setFactory = getResourceSetFactory();
		ResourceSet resourceSet = setFactory.createResourceSet();
		return EObject.class.isAssignableFrom(type) && resourceSet.getResourceFactoryRegistry()
				.getContentTypeToFactoryMap().containsKey(mediaType.getType() + "/" + mediaType.getSubtype());
	}

	/*
	 * (non-Javadoc)
	 * @see jakarta.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], jakarta.ws.rs.core.MediaType, jakarta.ws.rs.core.MultivaluedMap, java.io.InputStream)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public R readFrom(Class<R> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		Resource resource = super.readResourceFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);

		if(resource.getContents().size() > 0){
			try {
				R result = (R) resource.getContents().get(0);
				return result;
			} finally {
				resource.getContents().clear();
				ResourceSet rs = resource.getResourceSet();
				rs.getResources().remove(resource);
			}
		}

		return null;
	}

	@Override
	public long getSize(W t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Reference(unbind = "removeAnnotationConverter", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addAnnotationConverter(AnnotationConverter converter) {
		annotationConverters.add(converter);
	}

	public void removeAnnotationConverter(AnnotationConverter converter) {
		annotationConverters.add(converter);
	}

	@Override
	protected ResourceSetFactory getResourceSetFactory() {
		return resourceSetFactory;
	}
}
