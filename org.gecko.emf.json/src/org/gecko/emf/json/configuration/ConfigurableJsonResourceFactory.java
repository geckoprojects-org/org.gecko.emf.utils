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
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emfcloud.jackson.resource.JsonResourceFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * {@link ResourceFactoryImpl} for the configurable resource
 * @author Mark Hoffmann
 * @since 19.10.2018
 */
public class ConfigurableJsonResourceFactory extends JsonResourceFactory {
	
	/**
	 * Creates a new instance.
	 */
	public ConfigurableJsonResourceFactory() {
		super();
	}
	
	/**
	 * Creates a new instance with a given object mapper
	 */
	public ConfigurableJsonResourceFactory(ObjectMapper mapper) {
		super(mapper);
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl#createResource(org.eclipse.emf.common.util.URI)
	 */
	@Override
	public Resource createResource(URI uri) {
		ObjectMapper mapper = getMapper();
		if (mapper != null) {
			return new ConfigurableJsonResource(uri, mapper);
		} else {
			return new ConfigurableJsonResource(uri);
		}
	}

}
