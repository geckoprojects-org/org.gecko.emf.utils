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

import java.util.Objects;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;

/**
 * An extension to {@link org.eclipse.emf.ecore.util.EcoreUtil} 
 * @author Mark Hoffmann
 * @since 29.07.2024
 */
public class EcoreUtil {

	/**
	 * Copies all content from the 'from' parameter into the 'into' object.
	 * All values in the 'into' object will be overwritten 
	 * @param <T>
	 * @param from the source of the values
	 * @param into the target where all data will be copied into
	 */
	public static <T extends EObject> void copyInto(T from, T into) {
		GeckoCopier copier = new GeckoCopier(into);
	    copier.copy(from);
	    copier.copyReferences();
	    
	}
	
	/**
	 * Extend {@link Copier} and make copy into an existing object possible
	 * @author Mark Hoffmann
	 * @since 29.07.2024
	 */
	static class GeckoCopier extends org.eclipse.emf.ecore.util.EcoreUtil.Copier {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
		private EObject intoObject;
		
		/**
		 * Creates a new instance.
		 * @param intoObject an object to copy into, can be <code>null</code>
		 */
		public GeckoCopier(EObject intoObject) {
			this.intoObject = intoObject;
		}
		
		/* 
		 * (non-Javadoc)
		 * @see org.eclipse.emf.ecore.util.EcoreUtil.Copier#createCopy(org.eclipse.emf.ecore.EObject)
		 */
		@Override
		protected EObject createCopy(EObject eObject) {
			if (Objects.nonNull(intoObject)) {
				EClass intoClass = intoObject.eClass();
				EClass fromClass = eObject.eClass();
				if (intoClass.equals(fromClass)) {
					return intoObject;
				}
			}
			return super.createCopy(eObject);
		}
		
	}
}
