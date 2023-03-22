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
package org.gecko.emf.util.documentation.generators.apis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EcoreToDocumentationService{

	boolean canHandleMediaType(String mediaType);
	String getOutputFileExtension();
	String getOutputFolder(EcoreToDocumentationOptions mode);
	OutputStream doGenerateDocumentation(EPackage ePackage, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException;
	
	/**
	 * @param eClass
	 * @param mode
	 * @param outputFolderRoot
	 * @return
	 * @throws IOException
	 * @deprecated use {@link doGenerateDocumentation(EClassifier eClassifier, EcoreToDocumentationOptions mode, String outputFolderRoot)} instead
	 */
	@Deprecated
	OutputStream doGenerateDocumentation(EClass eClass, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException;
	
	OutputStream doGenerateDocumentation(EClassifier eClassifier, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException;

	default File generateOutputFile(EPackage ePackage, CharSequence generatedDoc, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException {
		String outputFileName = ePackage.getName().concat(getOutputFileExtension());
		return doCreateOutputFile(outputFileName, generatedDoc, mode, outputFolderRoot);
	}
	
	/**
	 * @param eClass
	 * @param generatedDoc
	 * @param mode
	 * @param outputFolderRoot
	 * @return
	 * @throws IOException
	 * @deprecated use {@link generateOutputFile(EClassifier eClassifier, CharSequence generatedDoc, EcoreToDocumentationOptions mode, String outputFolderRoot) } instead
	 */
	@Deprecated
	default File generateOutputFile(EClass eClass, CharSequence generatedDoc, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException {
		String outputFileName = eClass.getEPackage().getName().concat("_").concat(eClass.getName()).concat(getOutputFileExtension());
		return doCreateOutputFile(outputFileName, generatedDoc, mode, outputFolderRoot);
	}
	
	default File generateOutputFile(EClassifier eClassifier, CharSequence generatedDoc, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException {
		String outputFileName = eClassifier.getEPackage().getName().concat("_").concat(eClassifier.getName()).concat(getOutputFileExtension());
		return doCreateOutputFile(outputFileName, generatedDoc, mode, outputFolderRoot);
	}
	
	default File doCreateOutputFile(String outputFileName, CharSequence generatedDoc, EcoreToDocumentationOptions mode, String outputFolderRoot) throws IOException {
		Path outputPath = Paths.get(outputFolderRoot, getOutputFolder(mode), outputFileName);
		Files.deleteIfExists(outputPath);
		Files.createDirectories(outputPath.getParent());
		File outputFile = Files.createFile(outputPath).toFile();
		try(PrintWriter pw = new PrintWriter(new FileWriter(outputFile))) {
			pw.write(generatedDoc.toString());
		}
		return outputFile;
	}

}
