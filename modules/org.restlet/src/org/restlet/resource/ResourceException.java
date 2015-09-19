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

package org.restlet.resource;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;

/**
 * Encapsulates a response status and the optional cause as a checked exception.
 * 
 * @author Jerome Louvel
 */
public class ResourceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** The resource associated to this exception. Could be null. */
    private final Resource resource;

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
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     */
    public ResourceException(int code, String reasonPhrase) {
        this(new Status(code, reasonPhrase));
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code of the encapsulated status.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     * @param description
     *            The description of the encapsulated status.
     */
    public ResourceException(int code, String reasonPhrase, String description) {
        this(new Status(code, reasonPhrase, description));
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
    public ResourceException(int code, String name, String description, String uri) {
        this(new Status(code, name, description, uri));
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code of the encapsulated status.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     * @param description
     *            The description of the encapsulated status.
     * @param uri
     *            The URI of the specification describing the method.
     * @param cause
     *            The wrapped cause error or exception.
     */
    public ResourceException(int code, String reasonPhrase, String description, String uri, Throwable cause) {
        this(new Status(code, cause, reasonPhrase, description, uri), cause);
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
     * @param code
     *            The specification code.
     * @param throwable
     *            The related error or exception.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     */
    public ResourceException(int code, Throwable throwable, String reasonPhrase) {
        this(new Status(code, throwable, reasonPhrase, null, null));
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param throwable
     *            The related error or exception.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     * @param description
     *            The longer description.
     */
    public ResourceException(int code, Throwable throwable, String reasonPhrase, String description) {
        this(new Status(code, throwable, reasonPhrase, description, null));
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param throwable
     *            The related error or exception.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     * @param description
     *            The longer description.
     * @param uri
     *            The URI of the specification describing the method.
     */
    public ResourceException(int code, Throwable throwable, String reasonPhrase, String description, String uri) {
        this(new Status(code, throwable, reasonPhrase, description, uri));
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to associate.
     */
    public ResourceException(Status status) {
        this(status, (Throwable) ((status == null) ? null : status.getThrowable()));
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to associate.
     */
    public ResourceException(Status status, Resource resource) {
        this(status, (Throwable) ((status == null) ? null : status.getThrowable()), resource);
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
        this(new Status(status, cause, null, description), cause);
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
        this(status, cause, null);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to associate.
     * @param cause
     *            The wrapped cause error or exception.
     */
    public ResourceException(Status status, Throwable cause, Resource resource) {
        super((status == null) ? null : status.toString(), cause);
        this.status = status;
        this.resource = resource;
    }

    /**
     * Constructor that set the status to {@link org.restlet.data.Status#SERVER_ERROR_INTERNAL} including the
     * related error or exception.
     * 
     * @param cause
     *            The wrapped cause error or exception.
     */
    public ResourceException(Throwable cause) {
        this(new Status(Status.SERVER_ERROR_INTERNAL, cause), cause);
    }

    /**
     * Returns the request associated to this exception.
     * 
     * @return The request associated to this exception.
     */
    public Request getRequest() {
        return (this.resource != null) ? this.resource.getRequest() : null;
    }

    /**
     * Returns the resource associated to this exception.
     * 
     * @return The resource associated to this exception.
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     * Returns the response associated to this exception.
     * 
     * @return The response associated to this exception.
     */
    public Response getResponse() {
        return (this.resource != null) ? this.resource.getResponse() : null;
    }

    /**
     * Returns the status associated to this exception.
     * 
     * @return The status associated to this exception.
     */
    public Status getStatus() {
        return this.status;
    }
}
