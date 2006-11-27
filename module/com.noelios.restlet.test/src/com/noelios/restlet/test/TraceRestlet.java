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

package com.noelios.restlet.test;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Trace Restlet.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TraceRestlet extends Restlet {
	public TraceRestlet(Context context) {
		super(context);
	}

	/**
	 * Handles a uniform call.
	 * 
	 * @param request
	 *            The request to handle.
	 * @param response
	 *            The response to update.
	 */
	public void handle(Request request, Response response) {
		String message = "Hello World!" + "\nYour IP address is "
				+ request.getClientInfo().getAddress()
				+ "\nYour request URI is: "
				+ request.getResourceRef().toString();
		response.setEntity(message, MediaType.TEXT_PLAIN);
	}

}
