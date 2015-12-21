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

package org.restlet.ext.jaxrs.internal.todo;

/**
 * A NotYetImplementedException is thrown, when a method is not yet implemented
 * and should be implemented later. On the final implementation this class will
 * be removed, because than there is nothing not implemented.
 * 
 * @author Stephan Koops
 */
public class WillNotBeImplementedException extends
        UnsupportedOperationException {
    private static final long serialVersionUID = -746394839280273085L;

    /**
     * 
     */
    public WillNotBeImplementedException() {
        super();
    }

    /**
     * 
     * @param message
     */
    public WillNotBeImplementedException(String message) {
        super(message);
    }

    /**
     * 
     * @param message
     * @param cause
     */
    public WillNotBeImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param cause
     */
    public WillNotBeImplementedException(Throwable cause) {
        super(cause);
    }
}
