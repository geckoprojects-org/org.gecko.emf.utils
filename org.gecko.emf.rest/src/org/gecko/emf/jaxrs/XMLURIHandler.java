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

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLResource;

public class XMLURIHandler implements XMLResource.URIHandler{

	private static final Logger logger = Logger.getLogger("XMLURIHandler");
	private final URI resourceURI;

	public XMLURIHandler() {
		this(null);
	}

	public XMLURIHandler(URI uri) {
		resourceURI = uri;
	}

	@Override
	public void setBaseURI(URI uri) {
		// We use not base Uri here
	}

	@Override
	public URI resolve(URI uri) {
		if(uri.lastSegment() != null && uri.lastSegment().endsWith(".ecore")){
			//Due to he fact that the Server should
			List<String> segmentsList = uri.segmentsList();
			URI result = URI.createPlatformPluginURI(segmentsList.get(segmentsList.size() - 3), false);
			result = result.appendSegment(segmentsList.get(segmentsList.size() - 2));
			result = result.appendSegment(segmentsList.get(segmentsList.size() - 1));
			result = result.appendFragment(uri.fragment());
			result = result.appendQuery(uri.query());
			return result;
		}
		return uri.resolve(resourceURI);
	}

	@Override
	public URI deresolve(URI uri) {
		if("platform".equals(uri.scheme())){
			return uri;
		}
		if(uri.trimFragment().toString().startsWith(resourceURI.toString())){
			return uri.deresolve(resourceURI);
		}
		if (uri.segmentCount() == 0) {
			logger.severe(()->"De-resolving with segment count '0'! Uri to deresolve is: " + uri.toString());
			return null;
		}
		URI newURI = null;
		logger.fine(()->"De-resolving Uri with segment count > 0! Uri to deresolve is: " + uri.toString());
		if (uri.segmentCount() == 2) {
			newURI = URI.createURI(uri.toString().replace("mongodb://" + uri.authority() + "/" + uri.segment(0) +"/", ""));
		} else {
			newURI = URI.createURI(uri.toString().replace("mongodb://" + uri.authority() + "/" + uri.segment(0) +"/", "../"));
		}
		return newURI;
	}
}