/**
 * Copyright (c) 2012 - 2014 Data In Motion and others.
 * All rights reserved. 
 * 
 * This program and the accompanying materials are made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.gecko.qvt.osgi.component;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.m2m.qvt.oml.BasicModelExtent;
import org.eclipse.m2m.qvt.oml.ExecutionContextImpl;
import org.eclipse.m2m.qvt.oml.ExecutionDiagnostic;
import org.eclipse.m2m.qvt.oml.ModelExtent;
import org.eclipse.m2m.qvt.oml.TransformationExecutor;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.qvt.osgi.api.ModelTransformationConstants;
import org.gecko.qvt.osgi.api.ModelTransformationNamespace;
import org.gecko.qvt.osgi.api.ModelTransformator;
import org.gecko.qvt.osgi.util.JULLogWriter;
import org.osgi.annotation.bundle.Capability;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * QVT Implementation of a model transformator
 * @author Mark Hoffmann
 * @author Juergen Albert
 * @since 20.10.2017
 */
@RequireConfigurationAdmin
@RequireEMF
@Capability( namespace = ModelTransformationNamespace.NAMESPACE, name = "qvto")
@Requirement(namespace = ModelTransformationNamespace.COMPANION, name = "ecore.fragment")
@Requirement(namespace = ModelTransformationNamespace.COMPANION, name = "ocl.fragment")
@Component(name = ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, service = ModelTransformator.class, configurationPolicy = ConfigurationPolicy.REQUIRE, immediate = true)
public class QVTModelTransformator implements ModelTransformator, ModelTransformationConstants {

	private static final String VALIDATION_MESSAGE = "%sSource: [%s] Message [%s]";
	private static final Logger logger = Logger.getLogger(QVTModelTransformator.class.getName());
	private URI templateUri = null;
	private TransformationExecutor executor = null;
	private ExecutionContextImpl context = null;

	@Reference(name = "qvt.model")
	private ResourceSet resourceSet;
	
	private BundleContext bundleContext;
	
	/**
	 * Initializes the transformation engine and does a warm-up for the executor to reduce execution time 
	 * @throws URISyntaxException 
	 */
	@Activate
	void init(ComponentContext componentContext, Map<String, Object> properties) throws URISyntaxException {
		this.bundleContext = componentContext.getBundleContext();
		if(properties.containsKey(TEMPLATE_URI)) {
			String uriString = (String) properties.get(TEMPLATE_URI);
			if(uriString != null && !uriString.trim().isEmpty()) {
				templateUri = URI.createURI(TEMPLATE_URI);
			}
		} 
		if(templateUri == null) {
			String templatePath = (String) properties.get(TEMPLATE_PATH);
			templateUri = getTemplateUri(templatePath);
		}
		if ( templateUri == null) {
			throw new IllegalArgumentException("Error initializing QVT helper without template or/and resource set");
		}
		executor = new TransformationExecutor(templateUri, resourceSet.getPackageRegistry());
		Diagnostic result = executor.loadTransformation();
		if (result.getSeverity() == Diagnostic.OK) {
			context = new ExecutionContextImpl();
			context.setConfigProperty("keepModeling", true);
			// to log from QVTO during development uncomment the line below
			context.setLog(new JULLogWriter("o.e.q.o.qvtTransformatorExecutor"));
		} else {
			String msg = getDiagnosticMessage(result);
			logger.log(Level.SEVERE, String.format("Error loading transformation template: %s", msg));
			throw new IllegalStateException(msg);
		}
	}
	

	@Deactivate
	public void dispose() {
		if (executor != null) {
			executor.cleanup();
			executor = null;
		}
		if (context != null) {
			context = null;
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.qvt.osgi.api.ModelTransformator#startTransformations(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EObject> List<T> doTransformations(List<? extends EObject> inObjects) {
		if (inObjects == null) {
			throw new IllegalStateException("Error transforming object with null instance or no resource set");
		}
		try {
			// create the input extent with its initial contents
			ModelExtent input = new BasicModelExtent(inObjects);    
			// create an empty extent to catch the output
			ModelExtent output = new BasicModelExtent();
			ExecutionDiagnostic result = executor.execute(context, input, output);
			if(result.getSeverity() == Diagnostic.OK) {
				// the output objects got captured in the output extent
				List<? extends EObject> outObjects = output.getContents();
				logger.fine("QVT transformation succeeded with: " + outObjects.size() + " elements");
				return (List<T>) outObjects;
			} else {
				String message = getDiagnosticMessage(result);
				throw new IllegalStateException(String.format("Error executing transformation because of diagnostic errors: %s", message));
			}
		} catch (Exception e) {
			throw new IllegalStateException("Error transforming model from " + inObjects.toString(), e);
		}
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.qvt.osgi.api.ModelTransformator#startTransformation(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EObject> T doTransformation(List<? extends EObject> inObjects) {
		List<? extends EObject> outObjects = doTransformations(inObjects);

		if (outObjects.size() > 0) {
			return (T) outObjects.get(0);
		}
		throw new IllegalStateException("Transformation failed with no result object");
	}

	/* 
	 * (non-Javadoc)
	 * @see org.gecko.qvt.osgi.api.ModelTransformator#startTransformation(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public <T extends EObject> T doTransformation(EObject inObject) {
		return doTransformation(ECollections.singletonEList(inObject));
	}
	
    /**
     * Returns a message for a diagnostic
     * @param diagnostic the {@link Diagnostic}
     */
    private String getDiagnosticMessage(Diagnostic diagnostic) {
        StringBuilder message = new StringBuilder();
        createValidationMessage("", diagnostic, message);
        return message.toString();
    }
    
    private void createValidationMessage(String indent, Diagnostic diagnostic, StringBuilder message) {
    	String separator = System.getProperty("line.separator");
        message.append(String.format(VALIDATION_MESSAGE, indent, diagnostic.getSource(), diagnostic.getMessage()));
        message.append(separator);
        diagnostic.getChildren().forEach(d -> createValidationMessage("  " + indent , d, message));
    }
    
    /**
	 * Returns the bundle from the given bsn version string.
	 * This parameter is expected in the format:
	 * <bsn>:<version>, where the version part is optional.
	 * @param bsnVersionString the {@link String} in the format from above
	 */
	private Bundle getBundle(String bsnVersionString) {
		String[] bsnVersion = bsnVersionString.split(":");
		String bsn = bsnVersion[0];
		Version version = null;
		if (bsnVersion.length == 2) {
			version = Version.parseVersion(bsnVersion[1]);
		}
		Set<Bundle> candidates = new TreeSet<>(new Comparator<Bundle>() {
	
			/* 
			 * (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Bundle o1, Bundle o2) {
				return o1.getVersion().compareTo(o2.getVersion());
			}
		});
		for (Bundle b : bundleContext.getBundles()) {
			if (bsn.equalsIgnoreCase(b.getSymbolicName())) {
				if (version == null) {
					candidates.add(b);
				} else {
					if (b.getVersion().compareTo(version) == 0) {
						return b;
					} else {
						continue;
					}
				}
			}
		}
		if (candidates.isEmpty()) {
			throw new IllegalStateException("There is no bundle with this bsn and version '" + bsn + ":" + version + "'");
		} else {
			return candidates.stream().findFirst().get();
		}
	}

	/**
	 * Loads the template from the given path
	 * @throws URISyntaxException 
	 */
	private URI getTemplateUri(String templatePath) throws URISyntaxException {
		String[] segments = templatePath.split("/");
		URL url = bundleContext.getBundle().getResource(templatePath);
		if(url == null) {
			if (segments.length < 2) {
				
				throw new IllegalStateException("There are at least two segments expected in the ecore path");
			}
			Bundle bundle = getBundle(segments[0]);
			String path = templatePath.replace(segments[0], "");
			url = bundle.getResource(path);
			if (url == null) {
				throw new IllegalStateException("There was no template found at '" + segments[0] + path + "'");
			}
		}
		java.net.URI uri = url.toURI();
		return URI.createHierarchicalURI(uri.getScheme(), uri.getAuthority(), null, uri.getPath().split("/"), null, null);
	}

}
