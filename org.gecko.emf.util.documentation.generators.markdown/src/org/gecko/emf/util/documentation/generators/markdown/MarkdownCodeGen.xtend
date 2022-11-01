/*
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
package org.gecko.emf.util.documentation.generators.markdown;

import java.util.List
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.EMap
import org.eclipse.emf.ecore.EAnnotation
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EEnumLiteral
import org.eclipse.emf.ecore.EModelElement
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.EStructuralFeature
import org.gecko.emf.util.documentation.generators.apis.EcoreToClassDiagramCodeGenerator
import org.gecko.emf.util.documentation.generators.mermaid.MermaidClassDiagramCodeGen
import org.gecko.emf.util.documentation.generators.plantuml.PlantumlClassDiagramCodeGen
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationOptions
import org.gecko.emf.util.documentation.generators.apis.EcoreToDocumentationCodeGenerator

class MarkdownCodeGen implements EcoreToDocumentationCodeGenerator{
	
	EcoreToClassDiagramCodeGenerator classDiagramCodeGen;
	String diagramStartSyntax;
	String diagramEndSyntax = "```";
	
	def generateMarkdownDoc(EPackage epackage, EcoreToDocumentationOptions mode) {
		if(EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM.equals(mode)) {
			classDiagramCodeGen = new MermaidClassDiagramCodeGen();
			diagramStartSyntax = "```mermaid";
		}
		else if(EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM.equals(mode)) {
			classDiagramCodeGen = new PlantumlClassDiagramCodeGen();
			diagramStartSyntax = "```plantuml";
		}
			'''
# Package: «epackage.name»
«"\n"»
«IF classDiagramCodeGen !== null»
## Class Diagram
«"\n"»
«diagramStartSyntax»
«classDiagramCodeGen.generateClassDiagram(epackage)»
«diagramEndSyntax»
«"\n"»
«ENDIF»
«toEClassifierOverview(epackage.EClassifiers)»
	'''
	}

	
	def toEClassifierOverview(EList<EClassifier> eclassifiers) {
var List<EClass> classes = eclassifiers.filter[ec | ec instanceof EClass].map[ec | ec as EClass].toList;	
var List<EEnum> enums = eclassifiers.filter[ec | ec instanceof EEnum].map[ec | ec as EEnum].toList;	
'''
## Classes Overview
«"\n"»
«FOR eclass: classes»
«toClassOverview(eclass)»
«"\n"»
«"\n"»
«ENDFOR»
«"\n"»
## Enumerators Overview
«"\n"»
«IF enums.empty»
None.
«ELSE»
«FOR enumerator: enums»
«toEnumOverview(enumerator)»
«"\n"»
«"\n"»
«ENDFOR»
«ENDIF»
'''
}
	
	
	def toEnumOverview(EEnum enumerator)
	'''
### Enumerator: «enumerator.name»
«"\n"»
#### Description
«"\n"»
«toModelElementDescription(enumerator)»
«"\n"»
#### Literals
«"\n"»
«toLiteralOverview(enumerator.ELiterals)»
	'''	

	def toLiteralOverview(EList<EEnumLiteral> literals) 
	'''
«IF literals.empty»
None.
«ELSE»
| Literal| Description|
| -----| -----------|
«FOR literal: literals»
|«literal.name»|«toModelElementDescription(literal)»|
«ENDFOR»
«ENDIF»
	'''

	def toClassOverview(EClass eclass)
	'''
### Class: «eclass.name»
«"\n"»
#### Description
«"\n"»
«toModelElementDescription(eclass)»
«"\n"»
«IF classDiagramCodeGen !== null»
#### Class Diagram
«"\n"»
«diagramStartSyntax»
«classDiagramCodeGen.generateClassDiagram(eclass)»
«diagramEndSyntax»
«"\n"»
«ENDIF»
#### Fields
«"\n"»
«toClassStructuralFeaturesDescription(eclass.EAttributes)»
«"\n"»
#### References
«"\n"»
«toClassStructuralFeaturesDescription(eclass.EReferences)»
«"\n"»
	'''
	
	
		def <T extends EStructuralFeature> toClassStructuralFeaturesDescription(EList<T> features) {
		'''
«IF features.empty»
None.
«ELSE»
| Name| Type| Bounds| Description|
| -----| ----| ------| -----------|
«FOR feature: features»
|«feature.name»|«feature.EType.name»|«feature.lowerBound»..«feature.upperBound»|«toModelElementDescription(feature)»|
«ENDFOR»
«ENDIF»
	'''	
		}
	
	
	def String toModelElementDescription(EModelElement element) {
		for(EAnnotation annotation : element.EAnnotations) {
			var EMap<String, String> details = annotation.details;
			if(details.containsKey("documentation")) {
				return details.get("documentation").trim;
			}
			
		}	
		return "None."	
	}
	
	override generateDocumentation(EPackage ePackage, EcoreToDocumentationOptions docGenOption) {
		generateMarkdownDoc(ePackage, docGenOption);
	}
	
	override generateDocumentation(EClass eClass, EcoreToDocumentationOptions docGenOption) {
		if(EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM.equals(docGenOption)) {
			classDiagramCodeGen = new MermaidClassDiagramCodeGen();
			diagramStartSyntax = "```mermaid";
		}
		else if(EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM.equals(docGenOption)) {
			classDiagramCodeGen = new PlantumlClassDiagramCodeGen();
			diagramStartSyntax = "```plantuml";
		}
		toClassOverview(eClass);
	}
	
}