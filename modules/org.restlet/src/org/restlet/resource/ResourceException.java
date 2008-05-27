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

package org.restlet.resource;

import org.restlet.data.Status;

/**
 * Encapsulates a response status and the optional cause as a checked exception.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ResourceException extends Exception {

    private static final long serialVersionUID = 1L;

    /** The status associated to this exception. */
    private final Status status;

    /**
     * Constructor.
     * 
     * @param code
     *                The specification code.
     */
    public ResourceException(final int code) {
        this(new Status(code));
    }

    /**
     * Constructor.
     * 
     * @param code
     *                The specification code.
     * @param name
     *                The name.
     * @param description
     *                The description.
     * @param uri
     *                The URI of the specification describing the method.
     */
    public ResourceException(final int code, final String name,
            final String description, final String uri) {
        this(new Status(code, name, description, uri));
    }

    /**
     * Constructor.
     * 
     * @param code
     *                The specification code.
     * @param name
     *                The name.
     * @param description
     *                The description.
     * @param uri
     *                The URI of the specification describing the method.
     * @param cause
     *                The wrapped cause error or exception.
     */
    public ResourceException(final int code, final String name,
            final String description, final String uri, final Throwable cause) {
        this(new Status(code, cause, name, description, uri), cause);
    }

    /**
     * Constructor.
     * 
     * @param code
     *                The specification code.
     * @param cause
     *                The wrapped cause error or exception.
     */
    public ResourceException(final int code, final Throwable cause) {
        this(new Status(code, cause), cause);
    }

    /**
     * Constructor.
     * 
     * @param status
     *                The status to associate.
     */
    public ResourceException(final Status status) {
        super((status == null) ? null : status.toString());
        this.status = status;
    }

    /**
     * Constructor.
     * 
     * @param status
     *                The status to copy.
     * @param description
     *                The description to associate.
     */
    public ResourceException(final Status status, final String description) {
        this(new Status(status, description));
    }

    /**
     * Constructor.
     * 
     * @param status
     *                The status to copy.
     * @param description
     *                The description to associate.
     * @param cause
     *                The wrapped cause error or exception.
     */
    public ResourceException(final Status status, final String description,
            final Throwable cause) {
        this(new Status(status, cause, description), cause);
    }

    /**
     * Constructor.
     * 
     * @param status
     *                The status to associate.
     * @param cause
     *                The wrapped cause error or exception.
     */
    public ResourceException(final Status status, final Throwable cause) {
        super((status == null) ? null : status.getDescription(), cause);
        this.status = status;
    }

    /**
     * Constructor that set the status to
     * {@link org.restlet.data.Status#SERVER_ERROR_INTERNAL} including the
     * related error or exception.
     * 
     * @param cause
     *                The wrapped cause error or exception.
     */
    public ResourceException(final Throwable cause) {
        this(new Status(Status.SERVER_ERROR_INTERNAL, cause), cause);
    }

    /**
     * Returns the status associated to this exception.
     * 
     * @return The status associated to this exception.
     */
    public Status getStatus() {
        return status;
    }

}
