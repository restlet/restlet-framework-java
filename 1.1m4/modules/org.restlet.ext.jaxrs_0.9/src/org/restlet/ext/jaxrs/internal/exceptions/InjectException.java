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

import javax.ws.rs.core.Context;

/**
 * This kind of exception is thrown, when an object could not be injected in a
 * field or bean setter annotated with {@link Context} or other annotations.
 * 
 * @author Stephan Koops
 */
public class InjectException extends JaxRsException {
    private static final long serialVersionUID = 6796414811480666857L;

    /**
     * @param mie
     */
    public InjectException(MethodInvokeException mie) {
        super(mie.getMessage(), mie.getCause());
        this.setStackTrace(mie.getStackTrace());
    }

    /**
     * @param message
     * @param cause
     */
    public InjectException(String message, Throwable cause) {
        super(message, cause);
    }
}