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

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Chainer helper that sets and unset the helper's context on the current
 * thread.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * 
 * @param <T>
 */
public abstract class ContextHelper<T extends Restlet> extends ChainHelper<T> {

    /**
     * Constructor.
     * 
     * @param helped
     *                The helped Restlet.
     */
    public ContextHelper(T helpedRestlet) {
        super(helpedRestlet);
    }

    /**
     * Constructor.
     * 
     * @param helpedRestlet
     *                The helped Restlet.
     * @param parentContext
     *                The parent context, typically the component's context.
     */
    public ContextHelper(T helpedRestlet, Context parentContext) {
        super(helpedRestlet, parentContext);
    }

    /**
     * In addition to default's behavior, it sets the context on the current
     * thread by calling {@link Context#setCurrent(Context)}, then it invokes
     * the {@link #handle(Request, Response)} method and finally restores the
     * initial context.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The request to handle.
     */
    @Override
    public void handle(Request request, Response response) {
        // Associate the context to the current thread
        Context previousContext = Context.getCurrent();
        Context.setCurrent(getContext());

        // Associate the response to the current thread
        Response.setCurrent(response);

        // Actually handle call
        super.handle(request, response);

        // Restore the previous context
        Context.setCurrent(previousContext);
    }

}
