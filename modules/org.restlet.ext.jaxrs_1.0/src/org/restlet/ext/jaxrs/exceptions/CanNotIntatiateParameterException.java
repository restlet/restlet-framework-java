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
 * Thrown if a constructor or method parameter could not be instantiated.
 * @author Stephan Koop
 *
 */
public class CanNotIntatiateParameterException extends JaxRsRuntimeException {
    private static final long serialVersionUID = 951579935427584482L;

    /**
     * 
     * @param message
     */
    public CanNotIntatiateParameterException(String message) {
        super(message);
    }

    /**
     * 
     * @param cause
     */
    public CanNotIntatiateParameterException(Throwable cause) {
        super(cause);
    }

    /**
     * 
     * @param message
     * @param cause
     */
    public CanNotIntatiateParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}