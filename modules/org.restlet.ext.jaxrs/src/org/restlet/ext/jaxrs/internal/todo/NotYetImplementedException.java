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

package org.restlet.ext.jaxrs.internal.todo;

/**
 * A NotYetImplementedException is thrown, when a method is not yet implemented
 * and should be implemented later. On the final implementation this class will
 * be removed, because than there is nothing not implemented.
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