/*
 * Copyright 2005-2007 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Helper;

/**
 * Base connector helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ConnectorHelper extends Helper {
	/** The protocols simultaneously supported. */
	private List<Protocol> protocols;

	/**
	 * Constructor.
	 */
	public ConnectorHelper() {
		this.protocols = null;
	}

	/**
	 * Returns the protocols simultaneously supported.
	 * 
	 * @return The protocols simultaneously supported.
	 */
	public List<Protocol> getProtocols() {
		if (this.protocols == null)
			this.protocols = new ArrayList<Protocol>();
		return this.protocols;
	}

	/**
	 * Creates a new context.
	 * 
	 * @param loggerName
	 *            The JDK's logger name to use for contextual logging.
	 * @return The new context.
	 */
	@Override
	public Context createContext(String loggerName) {
		return null;
	}

	/**
	 * Handles a call.
	 * 
	 * @param request
	 *            The request to handle.
	 * @param response
	 *            The response to update.
	 */
	@Override
	public void handle(Request request, Response response) {
	}

	/** Start hook. */
	@Override
	public void start() throws Exception {
	}

	/** Stop callback. */
	@Override
	public void stop() throws Exception {
	}

}
