/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gwt;

import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;

/**
 * Callback associated to the uniform interface. This abstract class is
 * typically sub-classed and instantiated by the user applications. It contains a
 * single method called by the Restlet-GWT library when a request has been
 * processed.
 * 
 * @author Jerome Louvel
 */
public interface Callback {

    /**
     * This method is called in all cases, even if a communication error occurs.
     * When it is invoked the Response instance will have been updated.
     * 
     * @param request
     *            The request processed.
     * @param response
     *            The updated response.
     */
    public void onEvent(Request request, Response response);

}
