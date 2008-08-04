/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
