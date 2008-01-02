/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.ext.jaxrs.todo;

/**
 * Is thrown, when a method is not yet implemented and should be implemented
 * later. On the final implementation this class will be removed, because than
 * there is nothing not implemented.
 * 
 * @author Stephan Koops
 */
public class NotYetImplementedException extends UnsupportedOperationException {
    private static final long serialVersionUID = -746394839280273085L;

    /**
     * 
     */
    public NotYetImplementedException() {
        super();
    }

    /**
     * 
     * @param message
     */
    public NotYetImplementedException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     * @param cause
     */
    public NotYetImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param cause
     */
    public NotYetImplementedException(Throwable cause) {
        super(cause);
    }
}