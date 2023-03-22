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

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.jaxrs.annotations.AnnotationConverter;
import org.gecko.emf.osgi.ResourceSetFactory;
import org.osgi.annotation.bundle.Capability;
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
 * {@link MessageBodyReader} and {@link MessageBodyWriter} that handle {@link Resource}.
 * This readers read and write XMI from a {@link org.eclipse.emf.ecore.resource.Resource}
 * @author Juergen Albert
 * @param <R> the reader type, must be an {@link Resource}
 * @param <W> the writer type, must be an {@link Resource}}
 * @since 30.05.2012
 */
@Component(
		service = {MessageBodyReader.class, MessageBodyWriter.class},
		enabled = true,
		scope = ServiceScope.PROTOTYPE
		)
@Capability(namespace = "gecko.rest.addon", name = "messagebody.emf")
@JaxrsExtension
@JaxrsName("EMFResourcesMessageBodyReaderWriter")
@JaxrsApplicationSelect("(|(emf=true)("+ JaxrsWhiteboardConstants.JAX_RS_NAME + "=.default))")
@Provider
@Produces(MediaType.WILDCARD)
@Consumes(MediaType.WILDCARD)
public class EMFResourceMessageBodyHandler<R extends Resource, W extends Resource> extends AbstractEMFMessageBodyReaderWriter<R, W>{

	@Reference
	private ResourceSetFactory resourceSetFactory;
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return isReadWritable(type, mediaType);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public long getSize(W t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType)
	 */
	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return isReadWritable(type, mediaType);
	}

	/**
	 * Checks if the MessageBodyReader / -Writer can be handled
	 * @param type the class type
	 * @param mediaType the media type
	 * @return <code>true</code>, if the MBR/MBW can be used, otherwise <code>false</code>
	 */
	private boolean isReadWritable(Class<?> type, MediaType mediaType) {
		ResourceSetFactory setFactory = getResourceSetFactory();
		ResourceSet resourceSet = setFactory.createResourceSet();
		return Resource.class.isAssignableFrom(type) && resourceSet.getResourceFactoryRegistry()
				.getContentTypeToFactoryMap().containsKey(mediaType.getType() + "/" + mediaType.getSubtype());
	}

	/* (non-Javadoc)
	 * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.InputStream)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public R readFrom(Class<R> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
		return (R) super.readResourceFrom( (Class<Resource>) type, genericType, annotations, mediaType, httpHeaders, entityStream);
	}

	/* (non-Javadoc)
	 * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object, java.lang.Class, java.lang.reflect.Type, java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
	 */
	@Override
	public void writeTo(W t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
		super.writeResourceTo(t, type, genericType, annotations, mediaType, httpHeaders, entityStream);
	}
	
	
	public ResourceSetFactory getResourceSetFactory() {
		return resourceSetFactory;
	}
	
	@Reference(unbind = "removeAnnotationConverter", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addAnnotationConverter(AnnotationConverter converter) {
		annotationConverters.add(converter);
	}

	public void removeAnnotationConverter(AnnotationConverter converter) {
		annotationConverters.add(converter);
	}

}