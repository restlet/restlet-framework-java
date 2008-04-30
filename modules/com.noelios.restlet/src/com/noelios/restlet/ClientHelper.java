/*
 * Copyright 2005-2008 Noelios Consulting.
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

import org.restlet.Client;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Client connector helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ClientHelper extends ConnectorHelper<Client> {

	/**
	 * The number of milliseconds the client should wait for a response before
	 * aborting the request and setting its status to an error status.
	 */
	private int connectTimeout = 0;

	/**
	 * Constructor.
	 * 
	 * @param client
	 *            The client to help.
	 */
	public ClientHelper(Client client) {
		super(client);
		this.connectTimeout = client.getConnectTimeout();
	}

	@Override
	public void handle(Request request, Response response) {
	}

	/**
	 * Returns the connection timeout.
	 * 
	 * @return The connection timeout.
	 */
	public int getConnectTimeout() {
		return connectTimeout;
	}

	/**
	 * Sets the connection timeout.
	 * 
	 * @param connectTimeout
	 *            The connection timeout.
	 */
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

}
