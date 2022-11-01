/**
 * Copyright (c) 2012 - 2022 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.emf.util.documentation.generators.markdown.component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationCodeGenerator;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions;
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService;
import org.gecko.emf.util.documentation.generators.markdown.MarkdownCodeGen;
import org.osgi.service.component.annotations.Component;

/**
 * 
 * @author ilenia
 * @since Oct 19, 2022
 */
@Component(name="EcoreToMarkdownComponent", service=EcoreToDocumentationService.class)
public class EcoreToMarkdownComponent implements EcoreToDocumentationService {
	
	private static final String MD_FILE_EXTENSION = ".md";
	private static final String MD_OUTPUT_FOLDER = "md";
	private static final String MD_WITH_MERMAID_OUTPUT_FOLDER = "md_mermaid";
	private static final String MD_WITH_PLANTUML_OUTPUT_FOLDER = "md_plantuml";
	private static final String MD_MEDIA_TYPE = "text/markdown";
	

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#getOutputFileExtension()
	 */
	@Override
	public String getOutputFileExtension() {
		return MD_FILE_EXTENSION;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#getOutputFolder(org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions)
	 */
	@Override
	public String getOutputFolder(EcoreToDocumentationOptions mode) {
		switch(mode) {
		case MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM:
			return MD_WITH_MERMAID_OUTPUT_FOLDER;
		case MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM:
			return MD_WITH_PLANTUML_OUTPUT_FOLDER;
		case ONLY_MARKDOWN_CLASS_OVERVIEW: default:
			return MD_OUTPUT_FOLDER;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#doGenerateDocumentation(org.eclipse.emf.ecore.EPackage, org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions, java.lang.String)
	 */
	@Override
	public OutputStream doGenerateDocumentation(EPackage ePackage, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException {
		EcoreToDocumentationCodeGenerator mdCodeGenerator = new MarkdownCodeGen();
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
	public OutputStream doGenerateDocumentation(EClass eClass, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException{
		EcoreToDocumentationCodeGenerator mdCodeGenerator = new MarkdownCodeGen();
		CharSequence cs = mdCodeGenerator.generateDocumentation(eClass, mode);
		File outputFile = generateOutputFile(eClass, cs, mode, outputFolderRoot);
		try(InputStream is = new FileInputStream(outputFile); OutputStream os = new ByteArrayOutputStream();) {
			os.write(is.readAllBytes());			
			return os;
		}
	}

	
	/* 
	 * (non-Javadoc)
	 * @see org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationService#canHandleMediaType(java.lang.String)
	 */
	@Override
	public boolean canHandleMediaType(String mediaType) {
		if(MD_MEDIA_TYPE.equals(mediaType)) {
			return true;
		}
		return false;
	}

}
