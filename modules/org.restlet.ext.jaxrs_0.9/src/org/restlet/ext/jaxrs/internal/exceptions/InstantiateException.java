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
package org.restlet.ext.jaxrs.internal.exceptions;

import java.lang.reflect.Method;

/**
 * Thrown if a provider, a root resource class or a resource class could not be
 * instantiated.
 * 
 * @author Stephan Koops
 */
public class InstantiateException extends JaxRsException {
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