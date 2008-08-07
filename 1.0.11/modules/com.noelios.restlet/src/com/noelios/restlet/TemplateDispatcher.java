/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
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
