/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet.data;

/**
 * Status to return after handling a call.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class Status extends Metadata {
    private static final String BASE_HTTP = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html";

    private static final String BASE_WEBDAV = "http://www.webdav.org/specs/rfc2518.html";

    private static final String BASE_RESTLET = "http://www.restlet.org/docs/api/";

    public static final Status INFO_CONTINUE = new Status(100);

    public static final Status INFO_SWITCHING_PROTOCOL = new Status(101);

    public static final Status INFO_PROCESSING = new Status(102);

    public static final Status SUCCESS_OK = new Status(200);

    public static final Status SUCCESS_CREATED = new Status(201);

    public static final Status SUCCESS_ACCEPTED = new Status(202);

    public static final Status SUCCESS_NON_AUTHORITATIVE = new Status(203);

    public static final Status SUCCESS_NO_CONTENT = new Status(204);

    public static final Status SUCCESS_RESET_CONTENT = new Status(205);

    public static final Status SUCCESS_PARTIAL_CONTENT = new Status(206);

    public static final Status SUCCESS_MULTI_STATUS = new Status(207);

    public static final Status REDIRECTION_MULTIPLE_CHOICES = new Status(300);

    public static final Status REDIRECTION_PERMANENT = new Status(301);

    public static final Status REDIRECTION_FOUND = new Status(302);

    public static final Status REDIRECTION_SEE_OTHER = new Status(303);

    public static final Status REDIRECTION_NOT_MODIFIED = new Status(304);

    public static final Status REDIRECTION_USE_PROXY = new Status(305);

    public static final Status REDIRECTION_TEMPORARY = new Status(307);

    public static final Status CLIENT_ERROR_BAD_REQUEST = new Status(400);

    public static final Status CLIENT_ERROR_UNAUTHORIZED = new Status(401);

    public static final Status CLIENT_ERROR_PAYMENT_REQUIRED = new Status(402);

    public static final Status CLIENT_ERROR_FORBIDDEN = new Status(403);

    public static final Status CLIENT_ERROR_NOT_FOUND = new Status(404);

    public static final Status CLIENT_ERROR_METHOD_NOT_ALLOWED = new Status(405);

    public static final Status CLIENT_ERROR_NOT_ACCEPTABLE = new Status(406);

    public static final Status CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED = new Status(
            407);

    public static final Status CLIENT_ERROR_REQUEST_TIMEOUT = new Status(408);

    public static final Status CLIENT_ERROR_CONFLICT = new Status(409);

    public static final Status CLIENT_ERROR_GONE = new Status(410);

    public static final Status CLIENT_ERROR_LENGTH_REQUIRED = new Status(411);

    public static final Status CLIENT_ERROR_PRECONDITION_FAILED = new Status(
            412);

    public static final Status CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE = new Status(
            413);

    public static final Status CLIENT_ERROR_REQUEST_URI_TOO_LONG = new Status(
            414);

    public static final Status CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE = new Status(
            415);

    public static final Status CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE = new Status(
            416);

    public static final Status CLIENT_ERROR_EXPECTATION_FAILED = new Status(417);

    public static final Status CLIENT_ERROR_UNPROCESSABLE_ENTITY = new Status(
            422);

    public static final Status CLIENT_ERROR_LOCKED = new Status(423);

    public static final Status CLIENT_ERROR_FAILED_DEPENDENCY = new Status(424);

    public static final Status SERVER_ERROR_INTERNAL = new Status(500);

    public static final Status SERVER_ERROR_NOT_IMPLEMENTED = new Status(501);

    public static final Status SERVER_ERROR_BAD_GATEWAY = new Status(502);

    public static final Status SERVER_ERROR_SERVICE_UNAVAILABLE = new Status(
            503);

    public static final Status SERVER_ERROR_GATEWAY_TIMEOUT = new Status(504);

    public static final Status SERVER_ERROR_VERSION_NOT_SUPPORTED = new Status(
            505);

    public static final Status SERVER_ERROR_INSUFFICIENT_STORAGE = new Status(
            507);

    public static final Status CONNECTOR_ERROR_CONNECTION = new Status(1000);

    public static final Status CONNECTOR_ERROR_COMMUNICATION = new Status(1001);

    public static final Status CONNECTOR_ERROR_INTERNAL = new Status(1002);

    /**
     * Indicates if the status is a client error status.
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a client error status.
     */
    public static boolean isClientError(int code) {
        boolean result = false;

        switch (code) {
        case 400:
        case 401:
        case 402:
        case 403:
        case 404:
        case 405:
        case 406:
        case 407:
        case 408:
        case 409:
        case 410:
        case 411:
        case 412:
        case 413:
        case 414:
        case 415:
        case 416:
        case 417:
        case 422:
        case 423:
        case 424:
            result = true;
            break;
        }

        return result;
    }

    /**
     * Indicates if the status is a connector error status.
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a server error status.
     */
    public static boolean isConnectorError(int code) {
        boolean result = false;

        switch (code) {
        case 1000:
        case 1001:
        case 1002:
            result = true;
            break;
        }

        return result;
    }

    /**
     * Indicates if the status is an error (client or server) status.
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is an error (client or server) status.
     */
    public static boolean isError(int code) {
        return isClientError(code) || isServerError(code)
                || isConnectorError(code);
    }

    /**
     * Indicates if the status is an information status.
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is an information status.
     */
    public static boolean isInfo(int code) {
        boolean result = false;

        switch (code) {
        case 100:
        case 101:
        case 102:
            result = true;
            break;
        }

        return result;
    }

    /**
     * Indicates if the status is a redirection status.
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a redirection status.
     */
    public static boolean isRedirection(int code) {
        boolean result = false;

        switch (code) {
        case 300:
        case 301:
        case 302:
        case 303:
        case 304:
        case 305:
        case 307:
            result = true;
            break;
        }

        return result;
    }

    /**
     * Indicates if the status is a server error status.
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a server error status.
     */
    public static boolean isServerError(int code) {
        boolean result = false;

        switch (code) {
        case 500:
        case 501:
        case 502:
        case 503:
        case 504:
        case 505:
        case 507:
            result = true;
            break;
        }

        return result;
    }

    /**
     * Indicates if the status is a success status.
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a success status.
     */
    public static boolean isSuccess(int code) {
        boolean result = false;

        switch (code) {
        case 200:
        case 201:
        case 202:
        case 203:
        case 204:
        case 205:
        case 206:
        case 207:
            result = true;
            break;
        }

        return result;
    }

    /**
     * Returns the status associated to a code. If an existing constant exists
     * then it is returned, otherwise a new instance is created.
     * 
     * @param code
     *            The code.
     * @return The associated status.
     */
    public static Status valueOf(int code) {
        Status result = null;

        switch (code) {
        case 100:
            result = INFO_CONTINUE;
            break;
        case 101:
            result = INFO_SWITCHING_PROTOCOL;
            break;
        case 102:
            result = INFO_PROCESSING;
            break;

        case 200:
            result = SUCCESS_OK;
            break;
        case 201:
            result = SUCCESS_CREATED;
            break;
        case 202:
            result = SUCCESS_ACCEPTED;
            break;
        case 203:
            result = SUCCESS_NON_AUTHORITATIVE;
            break;
        case 204:
            result = SUCCESS_NO_CONTENT;
            break;
        case 205:
            result = SUCCESS_RESET_CONTENT;
            break;
        case 206:
            result = SUCCESS_PARTIAL_CONTENT;
            break;
        case 207:
            result = SUCCESS_MULTI_STATUS;
            break;

        case 300:
            result = REDIRECTION_MULTIPLE_CHOICES;
            break;
        case 301:
            result = REDIRECTION_PERMANENT;
            break;
        case 302:
            result = REDIRECTION_FOUND;
            break;
        case 303:
            result = REDIRECTION_SEE_OTHER;
            break;
        case 304:
            result = REDIRECTION_NOT_MODIFIED;
            break;
        case 305:
            result = REDIRECTION_USE_PROXY;
            break;
        case 307:
            result = REDIRECTION_TEMPORARY;
            break;

        case 400:
            result = CLIENT_ERROR_BAD_REQUEST;
            break;
        case 401:
            result = CLIENT_ERROR_UNAUTHORIZED;
            break;
        case 402:
            result = CLIENT_ERROR_PAYMENT_REQUIRED;
            break;
        case 403:
            result = CLIENT_ERROR_FORBIDDEN;
            break;
        case 404:
            result = CLIENT_ERROR_NOT_FOUND;
            break;
        case 405:
            result = CLIENT_ERROR_METHOD_NOT_ALLOWED;
            break;
        case 406:
            result = CLIENT_ERROR_NOT_ACCEPTABLE;
            break;
        case 407:
            result = CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED;
            break;
        case 408:
            result = CLIENT_ERROR_REQUEST_TIMEOUT;
            break;
        case 409:
            result = CLIENT_ERROR_CONFLICT;
            break;
        case 410:
            result = CLIENT_ERROR_GONE;
            break;
        case 411:
            result = CLIENT_ERROR_LENGTH_REQUIRED;
            break;
        case 412:
            result = CLIENT_ERROR_PRECONDITION_FAILED;
            break;
        case 413:
            result = CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE;
            break;
        case 414:
            result = CLIENT_ERROR_REQUEST_URI_TOO_LONG;
            break;
        case 415:
            result = CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;
            break;
        case 416:
            result = CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE;
            break;
        case 417:
            result = CLIENT_ERROR_EXPECTATION_FAILED;
            break;
        case 422:
            result = CLIENT_ERROR_UNPROCESSABLE_ENTITY;
            break;
        case 423:
            result = CLIENT_ERROR_LOCKED;
            break;
        case 424:
            result = CLIENT_ERROR_FAILED_DEPENDENCY;
            break;

        case 500:
            result = SERVER_ERROR_INTERNAL;
            break;
        case 501:
            result = SERVER_ERROR_NOT_IMPLEMENTED;
            break;
        case 502:
            result = SERVER_ERROR_BAD_GATEWAY;
            break;
        case 503:
            result = SERVER_ERROR_SERVICE_UNAVAILABLE;
            break;
        case 504:
            result = SERVER_ERROR_GATEWAY_TIMEOUT;
            break;
        case 505:
            result = SERVER_ERROR_VERSION_NOT_SUPPORTED;
            break;
        case 507:
            result = SERVER_ERROR_INSUFFICIENT_STORAGE;
            break;

        case 1000:
            result = CONNECTOR_ERROR_CONNECTION;
            break;
        case 1001:
            result = CONNECTOR_ERROR_COMMUNICATION;
            break;
        case 1002:
            result = CONNECTOR_ERROR_INTERNAL;
            break;
        }

        return result;
    }

    /** The specification code. */
    private int code;

    /** The URI of the specification describing the method. */
    private String uri;

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     */
    public Status(int code) {
        this(code, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param name
     *            The name.
     * @param description
     *            The description.
     * @param uri
     *            The URI of the specification describing the method.
     */
    public Status(int code, final String name, final String description, final String uri) {
        super(name, description);
        this.code = code;
        this.uri = uri;
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param description
     *            The description to associate.
     */
    public Status(final Status status, final String description) {
        this(status.getCode(), status.getName(), description, status.getUri());
    }

    /**
     * Indicates if the status is equal to a given one.
     * 
     * @param object
     *            The object to compare to.
     * @return True if the status is equal to a given one.
     */
    @Override
    public boolean equals(final Object object) {
        return (object instanceof Status)
                && (this.code == ((Status) object).getCode());
    }

    /**
     * Returns the HTTP code.
     * 
     * @return The HTTP code.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Returns the description.
     * 
     * @return The description.
     */
    public String getDescription() {
        String result = super.getDescription();

        if (result == null) {
            switch (this.code) {
            case 100:
                result = "The client should continue with its request";
                break;
            case 101:
                result = "The server is willing to change the application protocol being used on this connection";
                break;
            case 102:
                result = "Interim response used to inform the client that the server has accepted the complete request, but has not yet completed it";
                break;

            case 200:
                result = "The request has succeeded";
                break;
            case 201:
                result = "The request has been fulfilled and resulted in a new resource being created";
                break;
            case 202:
                result = "The request has been accepted for processing, but the processing has not been completed";
                break;
            case 203:
                result = "The returned metainformation is not the definitive set as available from the origin server";
                break;
            case 204:
                result = "The server has fulfilled the request but does not need to return an entity-body, and might want to return updated metainformation";
                break;
            case 205:
                result = "The server has fulfilled the request and the user agent should reset the document view which caused the request to be sent";
                break;
            case 206:
                result = "The server has fulfilled the partial get request for the resource";
                break;
            case 207:
                result = "Provides status for multiple independent operations";
                break;

            case 300:
                result = "The requested resource corresponds to any one of a set of representations";
                break;
            case 301:
                result = "The requested resource has been assigned a new permanent URI";
                break;
            case 302:
                result = "The requested resource can be found under a different URI";
                break;
            case 303:
                result = "The response to the request can be found under a different URI";
                break;
            case 304:
                result = "The client has performed a conditional GET request and the document has not been modified";
                break;
            case 305:
                result = "The requested resource must be accessed through the proxy given by the location field";
                break;
            case 307:
                result = "The requested resource resides temporarily under a different URI";
                break;

            case 400:
                result = "The request could not be understood by the server due to malformed syntax";
                break;
            case 401:
                result = "The request requires user authentication";
                break;
            case 402:
                result = "This code is reserved for future use";
                break;
            case 403:
                result = "The server understood the request, but is refusing to fulfill it";
                break;
            case 404:
                result = "The server has not found anything matching the request URI";
                break;
            case 405:
                result = "The method specified in the request is not allowed for the resource identified by the request URI";
                break;
            case 406:
                result = "The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request";
                break;
            case 407:
                result = "This code is similar to Unauthorized, but indicates that the client must first authenticate itself with the proxy";
                break;
            case 408:
                result = "The client did not produce a request within the time that the server was prepared to wait";
                break;
            case 409:
                result = "The request could not be completed due to a conflict with the current state of the resource";
                break;
            case 410:
                result = "The requested resource is no longer available at the server and no forwarding address is known";
                break;
            case 411:
                result = "The server refuses to accept the request without a defined content length";
                break;
            case 412:
                result = "The precondition given in one or more of the request header fields evaluated to false when it was tested on the server";
                break;
            case 413:
                result = "The server is refusing to process a request because the request entity is larger than the server is willing or able to process";
                break;
            case 414:
                result = "The server is refusing to service the request because the request URI is longer than the server is willing to interpret";
                break;
            case 415:
                result = "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method";
                break;
            case 416:
                result = "For byte ranges, this means that the first byte position were greater than the current length of the selected resource";
                break;
            case 417:
                result = "The expectation given in the request header could not be met by this server";
                break;
            case 422:
                result = "The server understands the content type of the request entity and the syntax of the request entity is correct but was unable to process the contained instructions";
                break;
            case 423:
                result = "The source or destination resource of a method is locked";
                break;
            case 424:
                result = "The method could not be performed on the resource because the requested action depended on another action and that action failed";
                break;

            case 500:
                result = "The server encountered an unexpected condition which prevented it from fulfilling the request";
                break;
            case 501:
                result = "The server does not support the functionality required to fulfill the request";
                break;
            case 502:
                result = "The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request";
                break;
            case 503:
                result = "The server is currently unable to handle the request due to a temporary overloading or maintenance of the server";
                break;
            case 504:
                result = "The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary server (e.g. DNS) it needed to access in attempting to complete the request";
                break;
            case 505:
                result = "The server does not support, or refuses to support, the protocol version that was used in the request message";
                break;
            case 507:
                result = "The method could not be performed on the resource because the server is unable to store the representation needed to successfully complete the request";
                break;

            case 1000:
                result = "The connector failed to connect to the server";
                break;
            case 1001:
                result = "The connector failed to complete the communication with the server";
                break;
            case 1002:
                result = "The connector encountered an unexpected condition which prevented it from fulfilling the request";
                break;
            }
        }

        return result;
    }

    /**
     * Returns the name of this status.
     * 
     * @return The name of this status.
     */
    public String getName() {
        String result = super.getName();

        if (result == null) {
            switch (this.code) {
            case 100:
                result = "Continue";
                break;
            case 101:
                result = "Switching Protocols";
                break;
            case 102:
                result = "Processing";
                break;

            case 200:
                result = "OK";
                break;
            case 201:
                result = "Created";
                break;
            case 202:
                result = "Accepted";
                break;
            case 203:
                result = "Non-Authoritative Information";
                break;
            case 204:
                result = "No Content";
                break;
            case 205:
                result = "Reset Content";
                break;
            case 206:
                result = "Partial Content";
                break;
            case 207:
                result = "Multi-Status";
                break;

            case 300:
                result = "Multiple Choices";
                break;
            case 301:
                result = "Moved Permanently";
                break;
            case 302:
                result = "Found";
                break;
            case 303:
                result = "See Other";
                break;
            case 304:
                result = "Not Modified";
                break;
            case 305:
                result = "Use Proxy";
                break;
            case 307:
                result = "Temporary Redirect";
                break;

            case 400:
                result = "Bad Request";
                break;
            case 401:
                result = "Unauthorized";
                break;
            case 402:
                result = "Payment Required";
                break;
            case 403:
                result = "Forbidden";
                break;
            case 404:
                result = "Not Found";
                break;
            case 405:
                result = "Method Not Allowed";
                break;
            case 406:
                result = "Not Acceptable";
                break;
            case 407:
                result = "Proxy Authentication Required";
                break;
            case 408:
                result = "Request Timeout";
                break;
            case 409:
                result = "Conflict";
                break;
            case 410:
                result = "Gone";
                break;
            case 411:
                result = "Length Required";
                break;
            case 412:
                result = "Precondition Failed";
                break;
            case 413:
                result = "Request Entity Too Large";
                break;
            case 414:
                result = "Request URI Too Long";
                break;
            case 415:
                result = "Unsupported Media Type";
                break;
            case 416:
                result = "Requested Range Not Satisfiable";
                break;
            case 417:
                result = "Expectation Failed";
                break;
            case 422:
                result = "Unprocessable Entity";
                break;
            case 423:
                result = "Locked";
                break;
            case 424:
                result = "Failed Dependency";
                break;

            case 500:
                result = "Internal Server Error";
                break;
            case 501:
                result = "Not Implemented";
                break;
            case 502:
                result = "Bad Gateway";
                break;
            case 503:
                result = "Service Unavailable";
                break;
            case 504:
                result = "Gateway Timeout";
                break;
            case 505:
                result = "Version Not Supported";
                break;
            case 507:
                result = "Insufficient Storage";
                break;

            case 1000:
                result = "Connection Error";
                break;
            case 1001:
                result = "Communication Error";
                break;
            case 1002:
                result = "Internal Connector Error";
                break;
            }
        }

        return result;
    }

    /**
     * Returns the URI of the specification describing the status.
     * 
     * @return The URI of the specification describing the status.
     */
    public String getUri() {
        String result = this.uri;

        if (result == null) {
            switch (this.code) {
            case 100:
                result = BASE_HTTP + "#sec10.1.1";
                break;
            case 101:
                result = BASE_HTTP + "#sec10.1.2";
                break;
            case 102:
                result = BASE_WEBDAV + "#STATUS_102";
                break;

            case 200:
                result = BASE_HTTP + "#sec10.2.1";
                break;
            case 201:
                result = BASE_HTTP + "#sec10.2.2";
                break;
            case 202:
                result = BASE_HTTP + "#sec10.2.3";
                break;
            case 203:
                result = BASE_HTTP + "#sec10.2.4";
                break;
            case 204:
                result = BASE_HTTP + "#sec10.2.5";
                break;
            case 205:
                result = BASE_HTTP + "#sec10.2.6";
                break;
            case 206:
                result = BASE_HTTP + "#sec10.2.7";
                break;
            case 207:
                result = BASE_WEBDAV + "#STATUS_207";
                break;

            case 300:
                result = BASE_HTTP + "#sec10.3.1";
                break;
            case 301:
                result = BASE_HTTP + "#sec10.3.2";
                break;
            case 302:
                result = BASE_HTTP + "#sec10.3.3";
                break;
            case 303:
                result = BASE_HTTP + "#sec10.3.4";
                break;
            case 304:
                result = BASE_HTTP + "#sec10.3.5";
                break;
            case 305:
                result = BASE_HTTP + "#sec10.3.6";
                break;
            case 307:
                result = BASE_HTTP + "#sec10.3.8";
                break;

            case 400:
                result = BASE_HTTP + "#sec10.4.1";
                break;
            case 401:
                result = BASE_HTTP + "#sec10.4.2";
                break;
            case 402:
                result = BASE_HTTP + "#sec10.4.3";
                break;
            case 403:
                result = BASE_HTTP + "#sec10.4.4";
                break;
            case 404:
                result = BASE_HTTP + "#sec10.4.5";
                break;
            case 405:
                result = BASE_HTTP + "#sec10.4.6";
                break;
            case 406:
                result = BASE_HTTP + "#sec10.4.7";
                break;
            case 407:
                result = BASE_HTTP + "#sec10.4.8";
                break;
            case 408:
                result = BASE_HTTP + "#sec10.4.9";
                break;
            case 409:
                result = BASE_HTTP + "#sec10.4.10";
                break;
            case 410:
                result = BASE_HTTP + "#sec10.4.11";
                break;
            case 411:
                result = BASE_HTTP + "#sec10.4.12";
                break;
            case 412:
                result = BASE_HTTP + "#sec10.4.13";
                break;
            case 413:
                result = BASE_HTTP + "#sec10.4.14";
                break;
            case 414:
                result = BASE_HTTP + "#sec10.4.15";
                break;
            case 415:
                result = BASE_HTTP + "#sec10.4.16";
                break;
            case 416:
                result = BASE_HTTP + "#sec10.4.17";
                break;
            case 417:
                result = BASE_HTTP + "#sec10.4.18";
                break;
            case 422:
                result = BASE_WEBDAV + "#STATUS_422";
                break;
            case 423:
                result = BASE_WEBDAV + "#STATUS_423";
                break;
            case 424:
                result = BASE_WEBDAV + "#STATUS_424";
                break;

            case 500:
                result = BASE_HTTP + "#sec10.5.1";
                break;
            case 501:
                result = BASE_HTTP + "#sec10.5.2";
                break;
            case 502:
                result = BASE_HTTP + "#sec10.5.3";
                break;
            case 503:
                result = BASE_HTTP + "#sec10.5.4";
                break;
            case 504:
                result = BASE_HTTP + "#sec10.5.5";
                break;
            case 505:
                result = BASE_HTTP + "#sec10.5.6";
                break;
            case 507:
                result = BASE_WEBDAV + "#STATUS_507";
                break;

            case 1000:
                result = BASE_RESTLET
                        + "org/restlet/data/Statuses.html#CONNECTOR_ERROR_CONNECTION";
                break;
            case 1001:
                result = BASE_RESTLET
                        + "org/restlet/data/Statuses.html#CONNECTOR_ERROR_COMMUNICATION";
                break;
            case 1002:
                result = BASE_RESTLET
                        + "org/restlet/data/Statuses.html#CONNECTOR_ERROR_INTERNAL";
                break;
            }
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getCode();
    }

    /**
     * Indicates if the status is a client error status.
     * 
     * @return True if the status is a client error status.
     */
    public boolean isClientError() {
        return isClientError(getCode());
    }

    /**
     * Indicates if the status is a connector error status.
     * 
     * @return True if the status is a connector error status.
     */
    public boolean isConnectorError() {
        return isConnectorError(getCode());
    }

    /**
     * Indicates if the status is an error (client or server) status.
     * 
     * @return True if the status is an error (client or server) status.
     */
    public boolean isError() {
        return isError(getCode());
    }

    /**
     * Indicates if the status is an information status.
     * 
     * @return True if the status is an information status.
     */
    public boolean isInfo() {
        return isInfo(getCode());
    }

    /**
     * Indicates if the status is a redirection status.
     * 
     * @return True if the status is a redirection status.
     */
    public boolean isRedirection() {
        return isRedirection(getCode());
    }

    /**
     * Indicates if the status is a server error status.
     * 
     * @return True if the status is a server error status.
     */
    public boolean isServerError() {
        return isServerError(getCode());
    }

    /**
     * Indicates if the status is a success status.
     * 
     * @return True if the status is a success status.
     */
    public boolean isSuccess() {
        return isSuccess(getCode());
    }

    /**
     * Returns the name of the status followed by its HTTP code.
     * 
     * @return The name of the status followed by its HTTP code.
     */
    public String toString() {
        return getName() + " (" + this.code + ")";
    }
}
