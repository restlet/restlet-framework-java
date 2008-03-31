/*
 * Copyright 2005-2007 Noelios Consulting.
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

import org.restlet.Context;
import org.restlet.Uniform;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Template;

/**
 * Default call dispatcher.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TemplateDispatcher extends Uniform {
	/** The helper dispatcher. */
	private Uniform helper;

	/** The parent context. */
	private Context context;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            The parent context.
	 * @param helper
	 *            The helper dispatcher.
	 */
	public TemplateDispatcher(Context context, Uniform helper) {
		this.context = context;
		this.helper = helper;
	}

	/**
	 * Returns the parent context.
	 * 
	 * @return The parent context.
	 */
	public Context getContext() {
		return this.context;
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
		Protocol protocol = request.getProtocol();

		if (protocol == null) {
			throw new UnsupportedOperationException(
					"Unable to determine the protocol to use for this call.");
		} else {
			String targetUri = request.getResourceRef().toString(true, false);

			if (targetUri.contains("{")) {
				// Template URI detected, create the template
				Template template = new Template(getContext().getLogger(),
						targetUri);

				// Set the formatted target URI
				request.setResourceRef(template.format(request, response));
			}

			// Actually dispatch the formatted URI
			this.helper.handle(request, response);
		}
	}
}
