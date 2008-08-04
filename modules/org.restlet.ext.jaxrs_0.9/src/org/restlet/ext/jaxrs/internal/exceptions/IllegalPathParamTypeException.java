/*
 * Copyright 2005-2008 Noelios Technologies.
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

import javax.ws.rs.PathParam;

/**
 * Thrown, if a type of a &#64;{@link PathParam} annotated parameter, field or
 * bean setter is not valid.
 * 
 * @author Stephan Koops
 */
public class IllegalPathParamTypeException extends IllegalParamTypeException {

    private static final long serialVersionUID = 6438816194995561330L;

    /**
     * @param message
     */
    public IllegalPathParamTypeException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param throwable
     */
    public IllegalPathParamTypeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * @param throwable
     */
    public IllegalPathParamTypeException(Throwable throwable) {
        super(throwable);
    }
}
