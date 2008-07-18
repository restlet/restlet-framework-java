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

/**
 * Thrown if the entity could not be deserialized.
 * 
 * @author Stephan Koops
 */
public class ConvertRepresentationException extends JaxRsException {
    private static final long serialVersionUID = 951579935427584482L;

    /**
     * Throws a message, that the given String value could not be converted to a
     * primitive.
     * 
     * @param paramType
     * @param unparseableValue
     * @param cause
     * @return
     * @throws ConvertRepresentationException
     */
    public static ConvertRepresentationException object(Class<?> paramType,
            Object unparseableValue, Throwable cause)
            throws ConvertRepresentationException {
        throw new ConvertRepresentationException("Could not convert "
                + unparseableValue + " to a " + paramType.getName(), cause);
    }

    /**
     * @param message
     */
    private ConvertRepresentationException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    private ConvertRepresentationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    private ConvertRepresentationException(Throwable cause) {
        super(cause);
    }
}