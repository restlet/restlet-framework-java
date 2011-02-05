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
     * @return (static the created ConvertRepresentationException for the
     *         compiler)
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