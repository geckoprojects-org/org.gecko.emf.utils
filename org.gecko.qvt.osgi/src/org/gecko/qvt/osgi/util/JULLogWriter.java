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
package org.gecko.qvt.osgi.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.m2m.qvt.oml.util.Log;

/**
 * Logwriter for java.util.Logger
 * @author Mark Hoffmann
 * @since 12.11.2017
 */
public class JULLogWriter implements Log {
	
	private final Logger logger;
	
	public JULLogWriter(String loggerName) {
		logger = Logger.getLogger(loggerName);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.m2m.qvt.oml.util.Log#log(int, java.lang.String, java.lang.Object)
	 */
	@Override
	public void log(int level, String message, Object param) {
		Level julLevel = getJulLevel(level);
		log(julLevel, message, param);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.m2m.qvt.oml.util.Log#log(int, java.lang.String)
	 */
	@Override
	public void log(int level, String message) {
		log(level, message, null);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.m2m.qvt.oml.util.Log#log(java.lang.String, java.lang.Object)
	 */
	@Override
	public void log(String message, Object param) {
		log(Level.INFO, message, null);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.m2m.qvt.oml.util.Log#log(java.lang.String)
	 */
	@Override
	public void log(String message) {
		log(message, null);
	}
	
	/**
	 * Logs into JUL logger
	 * @param level the log level
	 * @param message the log message
	 * @param param the log parameter
	 */
	private void log(Level level, String message, Object param) {
		if (param == null) {
			logger.log(level, message);
		} else {
			if (param instanceof Throwable) {
				logger.log(level, message, (Throwable)param);
			} else {
				logger.log(level, message, param);
			}
		}
	}
	
	/**
	 * Returns the JUL log level
	 * @param level the org.eclipse.core.runtime.IStatus values
	 * @return the JUL log level
	 */
	private Level getJulLevel(int level) {
		switch (level) {
		case 0:
			return Level.FINE;
		case 1:
			return Level.INFO;
		case 2:
			return Level.WARNING;
		case 4:
		case 8:
			return Level.SEVERE;
		default:
			return Level.INFO;
		}
	}

}
