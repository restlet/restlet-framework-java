/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.resource;

import org.restlet.data.Status;

/**
 * Encapsulates a response status and the optional cause as a checked exception.
 * 
 * @author Jerome Louvel
 */
public class ResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The status associated to this exception. */
    private final Status status;

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code of the encapsulated status.
     */
    public ResourceException(int code) {
        this(new Status(code));
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code of the encapsulated status.
     * @param name
     *            The name of the encapsulated status.
     * @param description
     *            The description of the encapsulated status.
     * @param uri
     *            The URI of the specification describing the method.
     */
    public ResourceException(int code, String name, String description,
            String uri) {
        this(new Status(code, name, description, uri));
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code of the encapsulated status.
     * @param name
     *            The name of the encapsulated status.
     * @param description
     *            The description of the encapsulated status.
     * @param uri
     *            The URI of the specification describing the method.
     * @param cause
     *            The wrapped cause error or exception.
     */
    public ResourceException(int code, String name, String description,
            String uri, Throwable cause) {
        this(new Status(code, cause, name, description, uri), cause);
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code of the encapsulated status.
     * @param cause
     *            The wrapped cause error or exception.
     */
    public ResourceException(int code, Throwable cause) {
        this(new Status(code, cause), cause);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to associate.
     */
    public ResourceException(Status status) {
        this(status, (Throwable) ((status == null) ? null : status
                .getThrowable()));
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param description
     *            The description of the encapsulated status.
     */
    public ResourceException(Status status, String description) {
        this(new Status(status, description));
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param description
     *            The description of the encapsulated status.
     * @param cause
     *            The wrapped cause error or exception.
     */
    public ResourceException(Status status, String description, Throwable cause) {
        this(new Status(status, cause, description), cause);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to associate.
     * @param cause
     *            The wrapped cause error or exception.
     */
    public ResourceException(Status status, Throwable cause) {
        super((status == null) ? null : status.getReasonPhrase(), cause);
        this.status = status;
    }

    /**
     * Constructor that set the status to
     * {@link org.restlet.data.Status#SERVER_ERROR_INTERNAL} including the
     * related error or exception.
     * 
     * @param cause
     *            The wrapped cause error or exception.
     */
    public ResourceException(Throwable cause) {
        this(new Status(Status.SERVER_ERROR_INTERNAL, cause), cause);
    }

    /**
     * Returns the status associated to this exception.
     * 
     * @return The status associated to this exception.
     */
    public Status getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        return getStatus().toString();
    }

}
