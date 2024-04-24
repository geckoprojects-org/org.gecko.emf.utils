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
package org.gecko.emf.converter;

import org.osgi.util.converter.AbstractConverting;
import org.osgi.util.converter.Converting;
import org.osgi.util.converter.InternalConverter;
import org.osgi.util.converter.InternalConverting;

/**
 * DTO to EObject {@link org.osgi.util.converter.Converting} implementation
 * 
 * @author Michal H. Siemaszko
 */
class DTOToEObjectConvertingImpl extends AbstractConverting implements Converting, InternalConverting {

	DTOToEObjectConvertingImpl(InternalConverter converter, Object obj) {
		super(converter, obj);
	}
}
