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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * 
 * @author mark
 * @since 29.07.2024
 */
public class EcoreUtil {

	public static <T extends EObject> void copyInto(T from, T into) {
		GeckoCopier copier = new GeckoCopier(into);
	    copier.copy(from);
	    copier.copyReferences();
	    
	}
	
	static class GeckoCopier extends org.eclipse.emf.ecore.util.EcoreUtil.Copier {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;
		private EObject intoObject;
		
		/**
		 * Creates a new instance.
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
			EClass intoClass = intoObject.eClass();
			EClass fromClass = eObject.eClass();
			if (intoClass.equals(fromClass)) {
				return intoObject;
			} else {
				return super.createCopy(eObject);
			}
		}
		
	}
}
