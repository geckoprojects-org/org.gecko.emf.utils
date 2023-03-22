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
package org.gecko.emf.json.configuration;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emfcloud.jackson.handlers.URIHandler;

/**
 * 
 * @author Juergen Albet
 * @since 12 Nov 2018
 */
public class XMLResourceUriHandlerWrapper implements URIHandler {

	private org.eclipse.emf.ecore.xmi.XMLResource.URIHandler xmlUriHandler;

	/**
	 * Creates a new instance.
	 */
	public XMLResourceUriHandlerWrapper(XMLResource.URIHandler xmlUriHandler) {
		this.xmlUriHandler = xmlUriHandler;
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.emfjson.jackson.handlers.URIHandler#deresolve(org.eclipse.emf.common.util.URI, org.eclipse.emf.common.util.URI)
	 */
	@Override
	public URI deresolve(URI baseURI, URI uri) {
		return xmlUriHandler != null ? xmlUriHandler.deresolve(uri) : null;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.emfjson.jackson.handlers.URIHandler#resolve(org.eclipse.emf.common.util.URI, org.eclipse.emf.common.util.URI)
	 */
	@Override
	public URI resolve(URI baseURI, URI uri) {
		return xmlUriHandler != null ?  xmlUriHandler.resolve(uri) : null;
	}

}
