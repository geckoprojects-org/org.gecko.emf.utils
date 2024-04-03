package org.gecko.qvt.osgi.tests.bbox;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.m2m.qvt.oml.blackbox.java.Module;
import org.eclipse.m2m.qvt.oml.blackbox.java.Operation;
import org.gecko.emf.osgi.example.model.basic.Address;
import org.gecko.emf.osgi.example.model.basic.BasicPackage;
import org.gecko.qvt.osgi.annotations.QvtBlackbox;
import org.gecko.qvt.osgi.annotations.TemplatePath;
import org.gecko.qvt.osgi.annotations.UnitQualifiedName;
import org.osgi.service.component.annotations.Component;

/**
 * <p>QVT Blackbox for the sdg tlc communication state.</p>
 * <p>Copyright (c) SWARCO TRAFFIC SYSTEMS GMBH 2017</p>
 *
 * @author   Mark Hoffmann
 * @version  10.11.2017
 */
@Module(packageURIs={BasicPackage.eNS_URI})
public class BlackboxTest {

	/**
	 * Contextual method that can be called on each {@link com.swarco.sdg.sdgtlc.CommunicationStateType}
	 * @param self the instance the method is called from
	 * @return the int value of the {@link com.swarco.sdg.sdgtlc.CommunicationStateType} 
	 */
	@Operation(contextual=true)
	public static Address getCopyAddress(Address self) {
		Address copy = EcoreUtil.copy(self);
		copy.setCity(self.getCity() + "Copy");
		copy.setStreet(self.getStreet() + "Copy");
		return copy;
	}

}