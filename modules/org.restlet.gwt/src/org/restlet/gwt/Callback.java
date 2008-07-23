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

package org.restlet.gwt;

import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;

/**
 * Callback associated to the uniform interface. This abstract class is
 * typically subclassed and instantiated by the user applications. It contains a
 * single method called by the Restlet-GWT library when a request has been
 * processed.
 * 
 * @author Jerome Louvel
 */
public abstract class Callback {

    /**
     * This method is called in all cases, even if a communication error occurs.
     * When it is invoked the Response instante will have been updated.
     * 
     * @param request
     *            The request processed.
     * @param response
     *            The updated response.
     */
    public abstract void onEvent(Request request, Response response);

}
