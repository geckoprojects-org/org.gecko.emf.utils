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

import java.util.List;

import org.eclipse.emf.ecore.EObject;

/**
 * Model transformator that can be used for model-2-model-transformation
 * @author Mark Hoffmann
 * @author Juergen Albert
 * @since 20.10.2017
 */
public interface ModelTransformator {

	/**
	 * Starts the batch transformation programmatic 
	 * http://wiki.eclipse.org/QVTOML/Examples/InvokeInJava
	 * https://www.eclipse.org/forums/index.php/t/853024/
	 * The objects are detached after the transformation. 
	 * @param inObjects list of objects to transform as batch
	 */
	public <T extends EObject> List<T> doTransformations(List<? extends EObject> inObjects);
	
	/**
	 * Starts the transformation programmatic 
	 * http://wiki.eclipse.org/QVTOML/Examples/InvokeInJava
	 * https://www.eclipse.org/forums/index.php/t/853024/
	 * The object is detached after the transformation.
	 * @param inObject the object to be transformed
	 */
	public <T extends EObject> T doTransformation(EObject inObject);
	
	/**
	 * Starts the transformation programmatic 
	 * http://wiki.eclipse.org/QVTOML/Examples/InvokeInJava
	 * https://www.eclipse.org/forums/index.php/t/853024/
	 * The object is  detached after the transformation. 
	 * @param inObject the object to be transformed
	 */
	public <T extends EObject> T doTransformation(List<? extends EObject> inObjects);
}
