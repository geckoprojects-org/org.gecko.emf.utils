/**
 * Copyright (c) 2012 - 2024 Data In Motion and others.
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
package org.gecko.emf.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.emf.common.util.URI;
import org.gecko.emf.rest.common.internal.XMLURIHandler;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author ungei
 * @since 19 Sep 2024
 */
public class TestUriHandler {

	@Test
	public void testBasicDeresolve() {
		
		URI input = URI.createURI("http://models.gecko.org/models/test/1.0/basic");
		URI toTest = URI.createURI("basic#1234");
		URI result = URI.createURI("basic#1234");
		
		XMLURIHandler handler = new XMLURIHandler(input);
		URI deresolve = handler.deresolve(toTest);
		assertEquals(result.toString(), deresolve.toString());
	}

	@Test
	public void testBasicDeresolveRelative() {
		
		URI input = URI.createURI("http://models.gecko.org/models/test/1.0/basic");
		URI toTest = URI.createURI("http://models.gecko.org/models/test/1.0/basic#1234");
		URI result = URI.createURI("#1234");
		
		XMLURIHandler handler = new XMLURIHandler(input);
		URI deresolve = handler.deresolve(toTest);
		assertEquals(result.toString(), deresolve.toString());
	}
	
}
