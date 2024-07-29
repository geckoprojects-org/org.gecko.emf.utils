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
package org.gecko.util.emf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author mark
 * @since 29.07.2024
 */
public class TestCopyInto {

	@Test
	public void simpleTest() {
		EClass eclass1 = EcoreFactory.eINSTANCE.createEClass();
		eclass1.setAbstract(true);
		eclass1.setName("Test");
		EAttribute attr1 = EcoreFactory.eINSTANCE.createEAttribute();
		attr1.setName("name");
		attr1.setEType(EcorePackage.eINSTANCE.getEString());
		eclass1.getEStructuralFeatures().add(attr1);
		
		EClass eclass2 = EcoreFactory.eINSTANCE.createEClass();
		
		assertFalse(eclass2.isAbstract());
		assertNull(eclass2.getName());
		assertTrue(eclass2.getEAllAttributes().isEmpty());
		
		EcoreUtil.copyInto(eclass1, eclass2);
		
		assertEquals(eclass1.getName(), eclass2.getName());
		assertTrue(eclass2.isAbstract());
		assertFalse(eclass2.getEAllAttributes().isEmpty());
		EAttribute attr2 = eclass2.getEAllAttributes().get(0);
		assertNotEquals(attr1, attr2);
		
		assertEquals(attr1.getName(), attr2.getName());
		assertEquals(attr1.getEType(), attr2.getEType());
	}
}
