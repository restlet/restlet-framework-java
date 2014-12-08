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

package org.restlet.ext.jaxrs.internal.exceptions;

/**
 * Indicates, that the JAX-RS implementation observed, that it contains an error
 * in itself. An example is, if a default provider could not be loaded.
 * 
 * @author Stephan Koops
 */
public class ImplementationException extends JaxRsRuntimeException {

    private static final long serialVersionUID = 5635188228961655076L;

    /**
     * @param message
     */
    public ImplementationException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ImplementationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public ImplementationException(Throwable cause) {
        super(cause);
    }
}
