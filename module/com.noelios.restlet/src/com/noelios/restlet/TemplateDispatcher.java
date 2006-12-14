/*
 * Copyright 2005-2006 Noelios Consulting.
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

import org.restlet.Dispatcher;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Default call dispatcher.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TemplateDispatcher extends Dispatcher {
    /** The helper dispatcher. */
    private Dispatcher helper;

    /**
     * Constructor.
     * 
     * @param helper
     *            The helper dispatcher.
     */
    public TemplateDispatcher(Dispatcher helper) {
        this.helper = helper;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        Protocol protocol = request.getProtocol();

        if (protocol == null) {
            throw new UnsupportedOperationException(
                    "Unable to determine the protocol to use for this call.");
        } else {
            // Create the template
//            TemplateReference rt = new TemplateReference(request
//                    .getResourceRef().toString(true, false));

            // Format the target URI
//            String targetUri = rt.format(request).toString();
//            request.setResourceRef(targetUri);

            // Actually dispatch the formatted URI
            this.helper.handle(request, response);
        }
    }

}
