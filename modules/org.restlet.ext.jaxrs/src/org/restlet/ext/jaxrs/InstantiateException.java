/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
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
