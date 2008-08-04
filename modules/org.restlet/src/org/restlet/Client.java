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

package org.restlet;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Engine;
import org.restlet.util.Helper;

/**
 * Connector acting as a generic client. It internally uses one of the available
 * connectors registered with the current Restlet implementation.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Client extends Connector {
	/** The helper provided by the implementation. */
	private Helper helper;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            The context.
	 * @param protocols
	 *            The connector protocols.
	 */
	public Client(Context context, List<Protocol> protocols) {
		super(context, protocols);

		if ((protocols != null) && (protocols.size() > 0)) {
			if (Engine.getInstance() != null) {
				this.helper = Engine.getInstance().createHelper(this);
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            The context.
	 * @param protocol
	 *            The connector protocol.
	 */
	public Client(Context context, Protocol protocol) {
		this(context, Arrays.asList(protocol));
	}

	/**
	 * Constructor.
	 * 
	 * @param protocols
	 *            The connector protocols.
	 */
	public Client(List<Protocol> protocols) {
		this(null, protocols);
	}

	/**
	 * Constructor.
	 * 
	 * @param protocol
	 *            The connector protocol.
	 */
	public Client(Protocol protocol) {
		this(null, protocol);
	}

	/**
	 * Returns the helper provided by the implementation.
	 * 
	 * @return The helper provided by the implementation.
	 */
	private Helper getHelper() {
		return this.helper;
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
		init(request, response);
		if (getHelper() != null)
			getHelper().handle(request, response);
	}

	@Override
	public void start() throws Exception {
		if (isStopped()) {
			super.start();
			if (getHelper() != null)
				getHelper().start();
		}
	}

	@Override
	public void stop() throws Exception {
		if (isStarted()) {
			if (getHelper() != null)
				getHelper().stop();
			super.stop();
		}
	}

}
