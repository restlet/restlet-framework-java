/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.container;

import org.restlet.Client;
import org.restlet.Container;
import org.restlet.Router;

/**
 * Router that collects calls from all applications and dispatches them to the
 * appropriate client connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ClientRouter extends Router {
	/** The parent container. */
	private Container container;

	/**
	 * Constructor.
	 * 
	 * @param container
	 *            The parent container.
	 */
	public ClientRouter(Container container) {
		super(container.getContext());
		this.container = container;
	}

	/** Starts the Restlet. */
	public void start() throws Exception {
		for (Client client : getContainer().getClients()) {
			getScorers().add(new ClientScorer(this, client));
		}

		super.start();
	}

	/**
	 * Returns the parent container.
	 * 
	 * @return The parent container.
	 */
	private Container getContainer() {
		return container;
	}
}
