/**
 * Copyright (c) 2012 - 2017 Data In Motion and others.
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

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.m2m.internal.qvt.oml.blackbox.java.JavaBlackboxProvider;
import org.eclipse.m2m.qvt.oml.TransformationExecutor.BlackboxRegistry;
import org.eclipse.m2m.qvt.oml.blackbox.java.Module;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.gecko.qvt.osgi.api.ModelTransformationConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.condition.Condition;
import org.osgi.util.tracker.ServiceTracker;

/**
 * A Whiteboard for BlackBox Services
 * @author Juergen Albert
 * @since 02.02.2024
 */
@Component
public class BlackBoxWhiteboard {

	private final Logger logger = Logger.getLogger(BlackBoxWhiteboard.class.getName());
	private volatile ServiceTracker<?, ?> blackboxTracker;

	ServiceRegistration<Condition> conditionRegistration = null;
	
	List<String> blackBoxClassNames = new CopyOnWriteArrayList<>();
	List<String> moduleNames = new CopyOnWriteArrayList<>();
	List<String> unitQualifiedNames = new CopyOnWriteArrayList<>();
	
	Map<ServiceReference<?>, Configuration> configs = new ConcurrentHashMap<>();
	
	@Reference
	ConfigurationAdmin configAdmin;
	
	@Activate
	public void activate(BundleContext context) {
		try {
			
			conditionRegistration = context.registerService(Condition.class, Condition.INSTANCE, new Hashtable<>(Collections.singletonMap(Condition.CONDITION_ID, ModelTransformationConstants.QVT_BLACKBOX_CONDITION)));
			
			Filter bbFilter = FrameworkUtil.createFilter("(" + ModelTransformationConstants.QVT_BLACKBOX + "=true)");
			BlackboxRegistry blackboxRegistry = BlackboxRegistry.INSTANCE;
			blackboxTracker = new ServiceTracker<Object, Object>(context, bbFilter, null) {
				
				/* 
				 * (non-Javadoc)
				 * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
				 */
				@Override
				public Object addingService(ServiceReference<Object> reference) {
					String moduleName = (String) reference.getProperty(ModelTransformationConstants.BLACKBOX_MODULENAME);
					String unitQualifiedName = (String) reference.getProperty(ModelTransformationConstants.BLACKBOX_QUALIFIED_UNIT_NAME);
					Object blackbox = context.getService(reference);
					if (moduleName == null ) {
						moduleName = blackbox.getClass().getSimpleName();
					}
					
					if (unitQualifiedName == null) {
						unitQualifiedName = blackbox.getClass().getPackage().getName() + JavaBlackboxProvider.CLASS_NAME_SEPARATOR + moduleName;
					}
					
					if (unitQualifiedName != null) {
						blackboxRegistry.registerModule(blackbox.getClass(), unitQualifiedName, moduleName);
					} 
					moduleNames.add(moduleName);
					unitQualifiedNames.add(unitQualifiedName);
					blackBoxClassNames.add(blackbox.getClass().getName());
					updateCondition();
					if(reference.getProperty(ModelTransformationConstants.TEMPLATE_PATH) != null || reference.getProperty(ModelTransformationConstants.TEMPLATE_URI) != null) {
						registerTransformator(reference, blackbox);
					}
					return blackbox;
				}
				
				/* 
				 * (non-Javadoc)
				 * @see org.osgi.util.tracker.ServiceTracker#modifiedService(org.osgi.framework.ServiceReference, java.lang.Object)
				 */
				@Override
				public void modifiedService(ServiceReference<Object> reference, Object service) {
					try {
						updateConfiguration(reference, service, configs.get(reference));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					super.modifiedService(reference, service);
				}
				
				/* 
				 * (non-Javadoc)
				 * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
				 */
				@Override
				public void removedService(ServiceReference<Object> reference, Object service) {
					Configuration removed = configs.remove(reference);
					if(removed != null) {
						try {
							removed.delete();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					context.ungetService(reference);
					super.removedService(reference, service);
				}
			};
			blackboxTracker.open();
		} catch (InvalidSyntaxException e) {
			logger.log(Level.SEVERE, "Cannot open tracker to track blackboxes, because of wrong filter", e);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "An error occured trying to track blackbox services", e);
		}
	}

	private void registerTransformator(ServiceReference<Object> reference, Object blackbox) {
		try {
			Configuration configuration = configAdmin.createFactoryConfiguration(ModelTransformationConstants.TRANSFORMATOR_COMPONENT_NAME, "?");
			configs.put(reference, configuration);
			updateConfiguration(reference, blackbox, configuration);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private void updateConfiguration(ServiceReference<Object> reference, Object blackbox,
			Configuration configuration) throws IOException {
		if(configuration != null) {
			Module annotation = blackbox.getClass().getAnnotation(Module.class);
			String[] packageURIs = (annotation == null) ? new String[] {} : annotation.packageURIs();
			
			Dictionary<String, Object> props = new Hashtable<String, Object>();
			
			String id = getProperty(reference, ModelTransformationConstants.QVT_BLACKBOX_PREFIX + ModelTransformationConstants.TRANSFORMATOR_ID);
			String uri = getProperty(reference, ModelTransformationConstants.TEMPLATE_URI);
			String path = getProperty(reference, ModelTransformationConstants.TEMPLATE_PATH);
			
			if(id == null) {
				id = uri == null ? path : uri;
			}
			props.put(ModelTransformationConstants.TRANSFORMATOR_ID, id);
			if(uri != null) {
				props.put(ModelTransformationConstants.TEMPLATE_URI, uri);
			} else {
				props.put(ModelTransformationConstants.TEMPLATE_PATH, path);
			}
			String modelFilter = createModelFilter(packageURIs);
			props.put(ModelTransformationConstants.MODEL_TARGET, modelFilter);
			props.put("osgi.ds.satisfying.condition", "(" + ModelTransformationConstants.BLACKBOX_CLASS_NAME + "=" + blackbox.getClass().getName() + ")");
			
			configuration.update(props);
		}
	}
	
	private String getProperty(ServiceReference<Object> reference, String key) {
		Object property = reference.getProperty(key);
		if(property instanceof String) {
			return (String) property;
		} else if(property instanceof String[]) {
			return ((String[]) property)[0];
		}
		return null;
	}
	
	/**
	 * @param packageURIs
	 * @return
	 */
	private String createModelFilter(String[] packageURIs) {
		if(packageURIs.length == 1) {
			return createModelFilter(packageURIs[0]);
		} 
		StringBuilder builder = new StringBuilder("(&");
		for (int i = 0; i < packageURIs.length; i++) {
			builder.append(createModelFilter(packageURIs[i]));
		}
		builder.append(")");
		return builder.toString();
	}

	private String createModelFilter(String packageURI) {
		return "(" + EMFNamespaces.EMF_MODEL_NSURI + "=" + packageURI + ")";
		 
	}
	
	@Deactivate
	public void deactivate() {
		if (blackboxTracker != null) {
			blackboxTracker.close();
		}
	}
	
	private void updateCondition() {
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(Condition.CONDITION_ID, ModelTransformationConstants.QVT_BLACKBOX_CONDITION);
		props.put(ModelTransformationConstants.BLACKBOX_CLASS_NAME, blackBoxClassNames.toArray(new String[blackBoxClassNames.size()]));
		props.put(ModelTransformationConstants.BLACKBOX_MODULENAME, moduleNames.toArray(new String[moduleNames.size()]));
		props.put(ModelTransformationConstants.BLACKBOX_QUALIFIED_UNIT_NAME, unitQualifiedNames.toArray(new String[unitQualifiedNames.size()]));
		conditionRegistration.setProperties(props);
	}
}
