/**
 */
package org.gecko.emf.pushstream.impl;

import java.util.concurrent.BlockingQueue;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.gecko.emf.pushstream.*;

import org.osgi.util.pushstream.PushEvent;
import org.osgi.util.pushstream.PushEventSource;
import org.osgi.util.pushstream.PushStream;
import org.osgi.util.pushstream.PushStreamBuilder;
import org.osgi.util.pushstream.PushStreamProvider;
import org.osgi.util.pushstream.SimplePushEventSource;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PushstreamFactoryImpl extends EFactoryImpl implements PushstreamFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static PushstreamFactory init() {
		try {
			PushstreamFactory thePushstreamFactory = (PushstreamFactory)EPackage.Registry.INSTANCE.getEFactory(PushstreamPackage.eNS_URI);
			if (thePushstreamFactory != null) {
				return thePushstreamFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new PushstreamFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PushstreamFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case PushstreamPackage.SIMPLE_PUSH_STREAM_PROVIDER: return createSimplePushStreamProvider();
			case PushstreamPackage.CUSTOM_PUSH_STREAM_PROVIDER: return createCustomPushStreamProvider();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case PushstreamPackage.EPUSH_STREAM:
				return createEPushStreamFromString(eDataType, initialValue);
			case PushstreamPackage.PUSH_STREAM_PROVIDER:
				return createPushStreamProviderFromString(eDataType, initialValue);
			case PushstreamPackage.PUSH_STREAM_BUILDER:
				return createPushStreamBuilderFromString(eDataType, initialValue);
			case PushstreamPackage.ESIMPLE_PUSH_EVENT_SOURCE:
				return createESimplePushEventSourceFromString(eDataType, initialValue);
			case PushstreamPackage.EPUSH_EVENT_SOURCE:
				return createEPushEventSourceFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case PushstreamPackage.EPUSH_STREAM:
				return convertEPushStreamToString(eDataType, instanceValue);
			case PushstreamPackage.PUSH_STREAM_PROVIDER:
				return convertPushStreamProviderToString(eDataType, instanceValue);
			case PushstreamPackage.PUSH_STREAM_BUILDER:
				return convertPushStreamBuilderToString(eDataType, instanceValue);
			case PushstreamPackage.ESIMPLE_PUSH_EVENT_SOURCE:
				return convertESimplePushEventSourceToString(eDataType, instanceValue);
			case PushstreamPackage.EPUSH_EVENT_SOURCE:
				return convertEPushEventSourceToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SimplePushStreamProvider createSimplePushStreamProvider() {
		SimplePushStreamProviderImpl simplePushStreamProvider = new SimplePushStreamProviderImpl();
		return simplePushStreamProvider;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public CustomPushStreamProvider createCustomPushStreamProvider() {
		CustomPushStreamProviderImpl customPushStreamProvider = new CustomPushStreamProviderImpl();
		return customPushStreamProvider;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public PushStream<EObject> createEPushStreamFromString(EDataType eDataType, String initialValue) {
		return (PushStream<EObject>)super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEPushStreamToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PushStreamProvider createPushStreamProviderFromString(EDataType eDataType, String initialValue) {
		return (PushStreamProvider)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPushStreamProviderToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public PushStreamBuilder<EObject, BlockingQueue<PushEvent<? extends EObject>>> createPushStreamBuilderFromString(EDataType eDataType, String initialValue) {
		return (PushStreamBuilder<EObject, BlockingQueue<PushEvent<? extends EObject>>>)super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPushStreamBuilderToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public SimplePushEventSource<EObject> createESimplePushEventSourceFromString(EDataType eDataType, String initialValue) {
		return (SimplePushEventSource<EObject>)super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertESimplePushEventSourceToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	public PushEventSource<EObject> createEPushEventSourceFromString(EDataType eDataType, String initialValue) {
		return (PushEventSource<EObject>)super.createFromString(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEPushEventSourceToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PushstreamPackage getPushstreamPackage() {
		return (PushstreamPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static PushstreamPackage getPackage() {
		return PushstreamPackage.eINSTANCE;
	}

} //PushstreamFactoryImpl
