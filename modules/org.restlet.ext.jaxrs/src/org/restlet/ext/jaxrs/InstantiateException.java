/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.ext.jaxrs;

import java.lang.reflect.Method;

/**
 * Thrown if a provider, a root resource class or a resource class could not be
 * instantiated.
 * 
 * @author Stephan Koops
 */
public class InstantiateException extends Exception {
    private static final long serialVersionUID = 951579935427584482L;

    /**
     * Use this constructor, if a resource class could not be instantiated.
     * 
     * @param executeMethod
     *            the resource method that should create the resource object.
     * @param cause
     */
    public InstantiateException(Method executeMethod, Throwable cause) {
        super("The method " + executeMethod
                + " could not instantiate a resource class", cause);
    }

    /**
     * @param message
     */
    public InstantiateException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public InstantiateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public InstantiateException(Throwable cause) {
        super(cause);
    }
}