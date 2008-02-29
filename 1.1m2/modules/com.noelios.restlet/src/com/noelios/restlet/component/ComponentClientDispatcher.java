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

package com.noelios.restlet.component;

import java.util.logging.Logger;

import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.TemplateDispatcher;

/**
 * Component client dispatcher.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentClientDispatcher extends TemplateDispatcher {
    /**
     * Constructor.
     * 
     * @param componentContext
     *                The component context.
     */
    public ComponentClientDispatcher(ComponentContext componentContext) {
        super(componentContext);
    }

    /**
     * Returns the component context.
     * 
     * @return The component context.
     */
    private ComponentContext getComponentContext() {
        return (ComponentContext) getContext();
    }

    @Override
    protected void doHandle(Request request, Response response) {
        Protocol protocol = request.getProtocol();

        if (protocol.equals(Protocol.RIAP)) {
            // Consider that the request is confidential
            request.setConfidential(true);

            // Let's dispatch it
            LocalReference cr = new LocalReference(request.getResourceRef());

            if (cr.getRiapAuthorityType() == LocalReference.RIAP_COMPONENT) {
                // This causes the baseRef of the resource reference to be set
                // as if it had actually arrived from a server connector.
                request.getResourceRef().setBaseRef(
                        request.getResourceRef().getHostIdentifier());

                // Ask the private internal route to handle the call
                if (getComponentContext() != null) {
                    getComponentContext().getComponentHelper().getComponent()
                            .getInternalRouter().handle(request, response);
                }
            } else {
                Logger
                        .getLogger(
                                ComponentClientDispatcher.class
                                        .getCanonicalName())
                        .warning(
                                "Unknown RIAP authority. Only \"component\" is supported.");
            }
        } else {
            getComponentContext().getComponentHelper().getClientRouter()
                    .handle(request, response);
        }
    }

}
