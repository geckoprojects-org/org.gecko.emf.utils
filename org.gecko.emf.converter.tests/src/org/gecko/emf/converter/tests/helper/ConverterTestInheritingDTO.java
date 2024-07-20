/**
 * Copyright (c) 2012 - 2024 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.converter.tests.helper;

/**
 * Sample DTO for {@link org.gecko.emf.converter.tests.DTOToEObjectConverterTest}
 * 
 * @author Michal H. Siemaszko
 */
public class ConverterTestInheritingDTO extends ConverterTestBasicDTO {
	public byte bytePrimitiveField;
	public short shortPrimitiveField;
	public int intPrimitiveField;
	public float floatPrimitiveField;
	public double doublePrimitiveField;
	public char charPrimitiveField;
}