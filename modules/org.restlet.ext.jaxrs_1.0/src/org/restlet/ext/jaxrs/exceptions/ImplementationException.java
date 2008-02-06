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

package org.restlet.ext.jaxrs.exceptions;

/**
 * Indicates, that the JAX-RS implementation observed, that it contains an error
 * in itself. An example could be an empty collection, were an empty collection
 * must not be possible. Will be removed later.
 * 
 * @author Stephan Koops
 * 
 */
public class ImplementationException extends JaxRsRuntimeException  {

    private static final long serialVersionUID = 5635188228961655076L;

    /**
     * 
     * @param message
     */
    public ImplementationException(String message) {
        super(message);
    }

    /**
     * 
     * @param cause
     */
    public ImplementationException(Throwable cause) {
        super(cause);
    }
    
    /**
     * 
     * @param message
     * @param cause
     */
    public ImplementationException(String message, Throwable cause) {
        super(message, cause);
    }
}