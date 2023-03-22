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
package org.gecko.emf.bson.handler;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;

import de.undercouch.bson4jackson.deserializers.BsonDateDeserializer;

/**
 * De-serialization problem handler to handle BSON dates that are EMBEDDED_OBJECT_VALUES instead of String
 * @author Mark Hoffmann
 * @since 21.12.2018
 */
public class BsonDateProblemHandler extends DeserializationProblemHandler {

	/* 
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.deser.DeserializationProblemHandler#handleUnexpectedToken(com.fasterxml.jackson.databind.DeserializationContext, com.fasterxml.jackson.databind.JavaType, com.fasterxml.jackson.core.JsonToken, com.fasterxml.jackson.core.JsonParser, java.lang.String)
	 */
	@Override
	public Object handleUnexpectedToken(DeserializationContext ctxt, JavaType targetType, JsonToken t, JsonParser p,
			String failureMsg) throws IOException {
		if (targetType.hasRawClass(Date.class) && p.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
			BsonDateDeserializer deser = new BsonDateDeserializer();
			return deser.deserialize(p, ctxt);
		}
		return super.handleUnexpectedToken(ctxt, targetType, t, p, failureMsg);
	}
	
}
