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
 * This exception is thrown, if an required annotation is missing.
 * 
 * @author Stephan Koops
 */
public class MissingAnnotationException extends JaxRsException {

    private static final long serialVersionUID = -1900088092745095670L;

    /**
     * @param message
     */
    public MissingAnnotationException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public MissingAnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public MissingAnnotationException(Throwable cause) {
        super(cause);
    }
}