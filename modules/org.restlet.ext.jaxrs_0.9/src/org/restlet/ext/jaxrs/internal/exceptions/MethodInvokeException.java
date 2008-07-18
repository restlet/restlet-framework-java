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

import java.lang.reflect.InvocationTargetException;

/**
 * Indicates, that an invoke of a resource method was not possible. If the
 * invoked method throws a Throwable, the {@link InvocationTargetException} must
 * be used.
 * 
 * @author Stephan Koops
 */
public class MethodInvokeException extends JaxRsException {

    private static final long serialVersionUID = 1766085431784085073L;

    /**
     * @param message
     */
    public MethodInvokeException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public MethodInvokeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public MethodInvokeException(Throwable cause) {
        super(cause);
    }
}