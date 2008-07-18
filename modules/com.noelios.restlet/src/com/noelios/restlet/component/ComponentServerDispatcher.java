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

import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.TemplateDispatcher;

/**
 * Component server dispatcher.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentServerDispatcher extends TemplateDispatcher {
    /**
     * Constructor.
     * 
     * @param context
     *            The component context.
     */
    public ComponentServerDispatcher(ComponentContext context) {
        super(context);
    }

    @Override
    protected void doHandle(Request request, Response response) {
        super.doHandle(request, response);

        // This causes the baseRef of the resource reference to be set
        // as if it had actually arrived from a server connector.
        request.getResourceRef().setBaseRef(
                request.getResourceRef().getHostIdentifier());

        // Ask the server router to actually handle the call
        getComponentContext().getComponentHelper().getServerRouter().handle(
                request, response);
    }

    /**
     * Returns the component context.
     * 
     * @return The component context.
     */
    private ComponentContext getComponentContext() {
        return (ComponentContext) getContext();
    }

}
