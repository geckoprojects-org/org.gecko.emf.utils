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
package org.gecko.emf.rest.jakartars.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Arrays;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.gecko.emf.json.constants.EMFJs;
import org.gecko.emf.osgi.ResourceSetFactory;
import org.gecko.emf.osgi.model.info.EMFModelInfo;
import org.gecko.emf.rest.annotations.RessourceOverwriteContentType;
import org.gecko.emf.rest.common.internal.XMLURIHandler;
import org.gecko.emf.rest.jakartars.AbstractJakartaRSEMFAnnotationHandler;
import org.osgi.service.component.annotations.Reference;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * The basic EMF {@link Resource} {@link MessageBodyReader} and
 * {@link MessageBodyWriter}
 * 
 * @author Juergen Albert
 */
public abstract class BaseJakartaRSEMFMessageBodyReaderWriter<R, W> extends AbstractJakartaRSEMFAnnotationHandler
		implements MessageBodyReader<R>, MessageBodyWriter<W> {

	@Reference
	EMFModelInfo modelInfo;

	/**
	 * default constructor
	 */
	public BaseJakartaRSEMFMessageBodyReaderWriter() {
	}

	/**
	 * @param t
	 * @param type
	 * @param genericType
	 * @param annotations
	 * @param mediaType
	 * @param httpHeaders
	 * @param entityStream
	 * @throws IOException
	 * @throws WebApplicationException
	 */
	public void writeResourceTo(Resource t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		try {
			
			String contentType = determinContentType(mediaType,annotations);
			ResourceSetFactory setFactory = getResourceSetFactory();
			ResourceSet resourceSet = setFactory.createResourceSet();
			ResourceFactoryImpl factory = (ResourceFactoryImpl) resourceSet.getResourceFactoryRegistry()
					.getContentTypeToFactoryMap().get(contentType);
			Resource referenceResource = factory.createResource(URI.createURI("http://test.test"));
			resourceSet.getResources().add(referenceResource);
			boolean removeFromResourceSet = true;
			if (t.getClass().equals(referenceResource.getClass())) {
				referenceResource = t;
				removeFromResourceSet = false;
			} else {

				resourceSet.getResources().add(referenceResource);
				for (EObject eObject : t.getContents()) {
					referenceResource.getContents().add(EcoreUtil.copy(eObject));
				}
			}

			HashMap<Object, Object> options = new HashMap<>();
			options.put(XMLResource.OPTION_SCHEMA_LOCATION, Boolean.TRUE);
			options.put(XMLResource.OPTION_URI_HANDLER, new XMLURIHandler(t.getURI()));

			handleAnnotedOptions(annotations, options, resourceSet, true);

			referenceResource.save(entityStream, options);

			if (removeFromResourceSet) {
				referenceResource.getResourceSet().getResources().remove(referenceResource);
			}
		} catch (WebApplicationException wae) {
			throw wae;
		} catch (Exception e) {
			String errorText = String.format("[%s] Error serializing outgoing object", genericType.getTypeName());
			Response r = Response.serverError().entity(errorText).type(MediaType.TEXT_PLAIN).build();
			throw new WebApplicationException(e, r);
		}
	}

	/**
	 * @param type
	 * @param genericType
	 * @param annotations
	 * @param mediaType
	 * @param httpHeaders
	 * @param entityStream
	 * @return
	 * @throws IOException
	 * @throws WebApplicationException
	 */
	public Resource readResourceFrom(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		try {
			ResourceSetFactory setFactory = getResourceSetFactory();
			ResourceSet resourceSet = setFactory.createResourceSet();
			String contentType = determinContentType(mediaType,annotations);
			ResourceFactoryImpl factory = (ResourceFactoryImpl) resourceSet.getResourceFactoryRegistry()
					.getContentTypeToFactoryMap().get(contentType);
			Resource resource = factory.createResource(URI.createURI("temp/id"));
			resourceSet.getResources().add(resource);
			Map<Object, Object> options = new HashMap<>();

			XMLURIHandler xmluriHandler = new XMLURIHandler(resource.getURI());
			options.put(XMLResource.OPTION_URI_HANDLER, xmluriHandler);

			handleAnnotedOptions(annotations, options, resourceSet, false);

			if (!options.containsKey(EMFJs.OPTION_ROOT_ELEMENT)) {
				modelInfo.getEClassifierForClass(type).ifPresent(ec -> options.put(EMFJs.OPTION_ROOT_ELEMENT, ec));
			}

			resource.load(entityStream, options);
			checkResourceByAnnotation(resource, annotations);
			return resource;
		} catch (WebApplicationException wae) {
			throw wae;
		} catch (Exception e) {
			String errorText = String.format("[%s] Error de-serializing incoming data", genericType.getTypeName());
			Response r = Response.serverError().entity(errorText).type(MediaType.TEXT_PLAIN).build();
			throw new WebApplicationException(e, r);
		}
	}
	

	protected abstract ResourceSetFactory getResourceSetFactory();
	
	private String determinContentType(MediaType mediatype, Annotation[] annotations) {
		return Arrays.asList(annotations).stream().filter(RessourceOverwriteContentType.class::isInstance)
				.map(RessourceOverwriteContentType.class::cast)
				.map(RessourceOverwriteContentType::value).findFirst()
				.orElseGet(()->mediatype.getType() + "/" + mediatype.getSubtype());	
				
	
	}
}