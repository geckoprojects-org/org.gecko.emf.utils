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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.gecko.emf.jaxrs.annotations.AnnotationConverter;
import org.gecko.emf.jaxrs.annotations.ContentNotEmpty;
import org.gecko.emf.jaxrs.annotations.EMFResourceOptions;
import org.gecko.emf.jaxrs.annotations.ResourceEClass;
import org.gecko.emf.jaxrs.annotations.ResourceOption;
import org.gecko.emf.jaxrs.annotations.ValidateContent;
import org.gecko.emf.json.constants.EMFJs;
import org.gecko.emf.osgi.ResourceSetFactory;
import org.gecko.emf.osgi.model.info.EMFModelInfo;
import org.osgi.service.component.annotations.Reference;

/**
 * The basic EMF {@link Resource} {@link MessageBodyReader} and {@link MessageBodyWriter}
 * @author Juergen Albert
 */
public abstract class AbstractEMFMessageBodyReaderWriter<R,W> implements MessageBodyReader<R>, MessageBodyWriter<W>{

	protected List<AnnotationConverter> annotationConverters = new LinkedList<>();
	
	@Reference
	EMFModelInfo modelInfo;

	/**
	 * default constructor
	 */
	public AbstractEMFMessageBodyReaderWriter() {
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
	public void writeResourceTo(Resource t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
		try {
			ResourceSetFactory setFactory = getResourceSetFactory();
			ResourceSet resourceSet = setFactory.createResourceSet();
			ResourceFactoryImpl factory = (ResourceFactoryImpl) resourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap().get(mediaType.getType() + "/" + mediaType.getSubtype());
			Resource referenceResource = factory.createResource(URI.createURI("http://test.test"));
			resourceSet.getResources().add(referenceResource);
			boolean removeFromResourceSet = true;
			if(t.getClass().equals(referenceResource.getClass())){
				referenceResource = t;
				removeFromResourceSet = false;
			} else {

				resourceSet.getResources().add(referenceResource);
				for(EObject eObject : t.getContents()){
					referenceResource.getContents().add(EcoreUtil.copy(eObject));
				}
			}

			HashMap<Object, Object> options = new HashMap<>();
			options.put(XMLResource.OPTION_SCHEMA_LOCATION,
					Boolean.TRUE);
			options.put(XMLResource.OPTION_URI_HANDLER, new XMLURIHandler(t.getURI()));

			handleAnnotedOptions(annotations, options, resourceSet, true);

			referenceResource.save(entityStream, options);

			if(removeFromResourceSet){
				referenceResource.getResourceSet().getResources().remove(referenceResource);
			}
		} catch (WebApplicationException wae) {
			throw wae;
		} catch (Exception e) {
			String errorText = String.format("[%s] Error serializing outgoing object", genericType.getTypeName());
			Response r = Response.serverError().entity(errorText).type(MediaType.TEXT_PLAIN).build();
			throw new WebApplicationException(r);
		} 
	}

	/**
	 * Reads the annotions for {@link EMFResourceOptions} and adds them to the given {@link Map}
	 * @param annotations the {@link Annotation} Array to parse
	 * @param options the options {@link Map} to add the annotations content to
	 * @param resourceSet the {@link ResourceSet} to use
	 */
	private void handleAnnotedOptions(Annotation[] annotations, Map<Object,Object> options, ResourceSet resourceSet, boolean serialize) {
		for(Annotation annotation : annotations){
			if(annotation instanceof ResourceOption){
				addEMFResourceOption(options, (ResourceOption) annotation, resourceSet);
			} else if(annotation instanceof EMFResourceOptions){
				EMFResourceOptions opts = (EMFResourceOptions) annotation;
				for(ResourceOption opt : opts.options()){
					addEMFResourceOption(options, opt,resourceSet);
				}
			} else {
				//Lets look if we have an annotation converter
				for (AnnotationConverter annotationConverter : annotationConverters) {
					if(annotationConverter.canHandle(annotation, serialize)) {
						annotationConverter.convertAnnotation(annotation, serialize, options);
					}
				}
			}

		}
	}

	/**
	 * Adds a {@link ResourceOption} to the given options {@link Map};
	 * @param options the options {@link HMap} to add the annotations content to
	 * @param annotation the {@link ResourceOption} to parse
	 * @param resourceSet the {@link ResourceSet} to use
	 */
	private void addEMFResourceOption(Map<Object, Object> options, ResourceOption annotation, ResourceSet resourceSet) {
		options.put(annotation.key(), createValue(annotation, resourceSet));
	}

	/**
	 * Parses the String representation of the value into the desired type.
	 * @param opt the {@link ResourceOption} to create the value from
	 * @param resourceSet the {@link ResourceSet} to use
	 * @return the {@link ResourceOption#value()} as the type specified by {@link ResourceOption#valueType()}
	 */
	private Object createValue(ResourceOption opt, ResourceSet resourceSet) {
		if(opt.valueType().equals(String.class)){
			return opt.value();
		} else if (opt.valueType().equals(Integer.class)) {
			return Integer.parseInt(opt.value());
		} else if (opt.valueType().equals(Double.class)) {
			return Double.parseDouble(opt.value());
		} else if (opt.valueType().equals(Long.class)) {
			return Long.parseLong(opt.value());
		} else if (opt.valueType().equals(Boolean.class)) {
			return Boolean.parseBoolean(opt.value());
		} else if (opt.valueType().equals(EClass.class)) {
			return resourceSet.getEObject(URI.createURI(opt.value()), Boolean.TRUE);
		} else if (opt.valueType().equals(SimpleDateFormat.class)) {
			return new SimpleDateFormat(opt.value());
		}
		return null;
	}

	/**
	 * Checks and executes checks according to the annotations 
	 * @param resource the {@link Resource} to check
	 * @param annotations tha available {@link Annotation}s
	 */
	private void checkResourceByAnnotation(Resource resource, Annotation[] annotations) {
		for(Annotation annotation : annotations){
			if(annotation instanceof ContentNotEmpty){
				handleContentNotEmpty(resource, (ContentNotEmpty) annotation);
			}
			if(annotation instanceof ResourceEClass){
				handleResourceEClass(resource, annotation);
			}
			if(annotation instanceof ValidateContent){
				handleValidateContent(resource);
			}
		}

	}

	/**
	 * Handles the content validation
	 * @param resource the EMF resource to validate
	 */
	private void handleValidateContent(Resource resource) {
		if (resource== null) {
			return;
		}
		for(EObject eObject : resource.getContents()){
			Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eObject);
			if(diagnostic.getSeverity() == Diagnostic.ERROR){
				Response res = Response.status(400).entity(buildDiagnosticMessage(diagnostic)).build();
				throw new WebApplicationException(res);
			}
		}
	}

	/**
	 * Handles the {@link ResourceEClass} annotaiton
	 * @param resource the EMF resource
	 * @param annotation the annotaiton
	 */
	private void handleResourceEClass(Resource resource, Annotation annotation) {
		if (resource == null || annotation == null) {
			return;
		}
		String eClassName = ((ResourceEClass) annotation).value();
		for(EObject eObject : resource.getContents()){
			if(!eObject.eClass().getName().equals(eClassName)){
				List<Variant> encoded = Variant.encodings(eClassName).build();
				Response res = Response.notAcceptable(encoded).build();
				throw new WebApplicationException(res);
			}
		}
	}

	/**
	 * Handles the content not empty annoation
	 * @param resource the EMF resource
	 * @param annotation the content not empty annotation
	 */
	private void handleContentNotEmpty(Resource resource, ContentNotEmpty annotation) {
		if (resource == null || annotation == null) {
			return;
		}
		if(resource.getContents().isEmpty()){
			List<Variant> encoded = Variant.encodings(annotation.message()).build();
			Response res = Response.notAcceptable(encoded).build();
			throw new WebApplicationException(res);
		}
	}

	/**
	 * Writes the diagnostic content to a String
	 * @param diagnostic the {@link Diagnostic} to serialise
	 * @return the message as a {@link String}
	 */
	private String buildDiagnosticMessage(Diagnostic diagnostic) {
		StringBuilder builder = new StringBuilder();
		buildDiagnosticMessage(diagnostic, builder , "");
		return builder.toString();
	}

	/**
	 * Recursively writes the given {@link Diagnostic} and all {@link Diagnostic#getChildren()} to the given {@link StringBuilder} 
	 * @param diagnostic the current {@link Diagnostic}
	 * @param stringBuilder the {@link StringBuilder} to use
	 * @param intend the intent to use before the message
	 */
	private void buildDiagnosticMessage(Diagnostic diagnostic,
			StringBuilder stringBuilder, String intend) {
		stringBuilder.append(intend);
		stringBuilder.append(diagnostic.getMessage());
		stringBuilder.append(System.lineSeparator());
		for (Diagnostic diagnosticChild : diagnostic.getChildren()) {
			buildDiagnosticMessage(diagnosticChild, stringBuilder, intend + "  ");
		}
	}

	/**
	 * @param type
	 * @param genericType
	 * @param annotations
	 * @param mediaType
	 * @param httpHeaders
	 * @param entityStream
	 * @param modelInfo 
	 * @return
	 * @throws IOException
	 * @throws WebApplicationException
	 */
	public Resource readResourceFrom(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
		try {
			ResourceSetFactory setFactory = getResourceSetFactory();
			ResourceSet resourceSet = setFactory.createResourceSet();	
			ResourceFactoryImpl factory = (ResourceFactoryImpl) resourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap().get(mediaType.getType() + "/" + mediaType.getSubtype());
			Resource resource = factory.createResource(URI.createURI("temp/id"));
			resourceSet.getResources().add(resource);
			Map<Object, Object> options = new HashMap<>();

			XMLURIHandler xmluriHandler = new XMLURIHandler(resource.getURI());
			options.put(XMLResource.OPTION_URI_HANDLER, xmluriHandler);

			handleAnnotedOptions(annotations, options, resourceSet, false);

			if(!options.containsKey(EMFJs.OPTION_ROOT_ELEMENT)) {
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
			throw new WebApplicationException(r);
		} 
	}

	protected abstract ResourceSetFactory getResourceSetFactory();
}