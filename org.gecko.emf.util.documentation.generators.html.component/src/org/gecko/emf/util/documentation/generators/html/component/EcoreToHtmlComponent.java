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
package org.gecko.emf.util.documentation.generators.html.component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationCodeGenerator;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService;
import org.gecko.emf.util.documentation.generators.html.HtmlCodeGen;
import org.osgi.service.component.annotations.Component;

@Component(name="EcoreToHtmlComponent", service=EcoreToDocumentationService.class)
public class EcoreToHtmlComponent implements EcoreToDocumentationService {
	
	private static final String HTML_FILE_EXTENSION = ".html";
	private static final String HTML_OUTPUT_FOLDER = "html";
	private static final String HTML_WITH_MERMAID_OUTPUT_FOLDER = "html_mermaid";
	private static final String HTML_MEDIA_TYPE = "text/html";

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#canHandleMediaType(java.lang.String)
	 */
	@Override
	public boolean canHandleMediaType(String mediaType) {
		return HTML_MEDIA_TYPE.equals(mediaType);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#getOutputFileExtension()
	 */
	@Override
	public String getOutputFileExtension() {
		return HTML_FILE_EXTENSION;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#getOutputFolder(org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions)
	 */
	@Override
	public String getOutputFolder(EcoreToDocumentationOptions mode) {
		switch(mode) {
		case HTML_WITH_MERMAID_CLASS_DIAGRAM:
			return HTML_WITH_MERMAID_OUTPUT_FOLDER;
		case ONLY_HTML_CLASS_OVERVIEW: default:
			return HTML_OUTPUT_FOLDER;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#doGenerateDocumentation(org.eclipse.emf.ecore.EPackage, org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions, java.lang.String)
	 */
	@Override
	public OutputStream doGenerateDocumentation(EPackage ePackage, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException {
		EcoreToDocumentationCodeGenerator mdCodeGenerator = new HtmlCodeGen();
		CharSequence cs = mdCodeGenerator.generateDocumentation(ePackage, mode);
		File outputFile = generateOutputFile(ePackage, cs, mode, outputFolderRoot);
		try(InputStream is = new FileInputStream(outputFile); OutputStream os = new ByteArrayOutputStream();) {
			os.write(is.readAllBytes());			
			return os;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#doGenerateDocumentation(org.eclipse.emf.ecore.EClass, org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions, java.lang.String)
	 */
	@Override
	public OutputStream doGenerateDocumentation(EClass eClass, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException {
		EcoreToDocumentationCodeGenerator mdCodeGenerator = new HtmlCodeGen();
		CharSequence cs = mdCodeGenerator.generateDocumentation(eClass, mode);
		File outputFile = generateOutputFile(eClass, cs, mode, outputFolderRoot);
		try(InputStream is = new FileInputStream(outputFile); OutputStream os = new ByteArrayOutputStream();) {
			os.write(is.readAllBytes());			
			return os;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#doGenerateDocumentation(org.eclipse.emf.ecore.EClassifier, org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions, java.lang.String)
	 */
	@Override
	public OutputStream doGenerateDocumentation(EClassifier eClassifier, EcoreToDocumentationOptions mode,
			String outputFolderRoot) throws IOException {
		EcoreToDocumentationCodeGenerator mdCodeGenerator = new HtmlCodeGen();
		CharSequence cs = mdCodeGenerator.generateDocumentation(eClassifier, mode);
		File outputFile = generateOutputFile(eClassifier, cs, mode, outputFolderRoot);
		try(InputStream is = new FileInputStream(outputFile); OutputStream os = new ByteArrayOutputStream();) {
			os.write(is.readAllBytes());			
			return os;
		}
	}


}
