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

import javax.ws.rs.Path;

/**
 * This kind of exception is thrown, when an &#64{@link Path} annotation
 * contains illegal characters.
 * 
 * @author Stephan Koops
 */
public class IllegalPathException extends JaxRsException {

    private static final long serialVersionUID = 6796414811480666857L;

    private static String createMessage(IllegalArgumentException iae, Path path) {
        if (iae != null) {
            final Throwable cause = iae.getCause();
            if (cause != null) {
                final String message = cause.getMessage();
                if ((message == null) || (message.length() == 0)) {
                    return "The given path (" + path + ") is invalid";
                }
            }
        }
        return null;
    }

    private final Path path;

    /**
     * 
     * @param path
     *            the invalid path
     * @param iae
     */
    public IllegalPathException(Path path, IllegalArgumentException iae) {
        super(createMessage(iae, path), iae);
        this.path = path;
    }

    /**
     * 
     * @param path
     * @param message
     */
    public IllegalPathException(Path path, String message) {
        super(message);
        this.path = path;
    }

    /**
     * 
     * @param path
     * @param message
     * @param iae
     */
    public IllegalPathException(Path path, String message,
            IllegalArgumentException iae) {
        super(message, iae);
        this.path = path;
    }

    /**
     * Returns the cause {@link IllegalArgumentException}. If not available, it
     * is created.
     * 
     * @see java.lang.Throwable#getCause()
     */
    @Override
    public IllegalArgumentException getCause() {
        final Throwable cause = super.getCause();
        if (cause instanceof IllegalArgumentException) {
            return (IllegalArgumentException) cause;
        }
        final IllegalArgumentException iae = new IllegalArgumentException(
                getMessage());
        if (cause != null) {
            iae.setStackTrace(cause.getStackTrace());
        }
        return iae;
    }

    /**
     * Returns the Illegal Path.
     * 
     * @return the Illegal Path.
     */
    public Path getPath() {
        return this.path;
    }
}
