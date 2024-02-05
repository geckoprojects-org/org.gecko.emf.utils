/**
 * Copyright (c) 2012 - 2024 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.qvt.osgi.api;

/**
 * Exception when an error during the transformation occurs
 * @author Mark Hoffmann
 * @since 22.11.2018
 */
public class TransformationException extends Exception {

	/** serialVersionUID */
	private static final long serialVersionUID = 2631794786091593104L;
	
	/**
	 * Creates a new instance.
	 */
	public TransformationException(String message) {
		super(message);
	}
	
	/**
	 * Creates a new instance.
	 */
	public TransformationException(String message, Throwable cause) {
		super(message, cause);
	}

}
