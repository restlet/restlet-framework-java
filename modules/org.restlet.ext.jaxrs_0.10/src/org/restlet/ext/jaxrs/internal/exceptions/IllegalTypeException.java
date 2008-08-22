/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.exceptions;

/**
 * This kind of exception is thrown, when an annotated constructor or method
 * parameter has an illegal type.
 * 
 * @author Stephan Koops
 */
public class IllegalTypeException extends JaxRsException {
    private static final long serialVersionUID = 6796414811480666857L;

    /**
     * @param message
     */
    public IllegalTypeException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public IllegalTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public IllegalTypeException(Throwable cause) {
        super(cause);
    }
}