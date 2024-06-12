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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DTO to EPackage converter.
 * 
 * @author Michal H. Siemaszko
 */
@Component(name = "DTOToEPackageConverter", scope = ServiceScope.SINGLETON)
public class DTOToEPackageConverter extends AbstractJavaToEPackageConverter implements JavaToEPackageConverter {
	private static final Logger LOG = LoggerFactory.getLogger(DTOToEPackageConverter.class);

	public DTOToEPackageConverter() {
		super(LOG);
	}
}
