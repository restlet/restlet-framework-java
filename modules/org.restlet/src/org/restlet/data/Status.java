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

package org.restlet.data;

import org.restlet.engine.Edition;
import org.restlet.engine.Engine;

/**
 * Status to return after handling a call.
 * 
 * @author Jerome Louvel
 */
public final class Status {
    private static final String BASE_HTTP = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html";

    private static final String BASE_RESTLET = "http://www.restlet.org/documentation/"
            + Engine.MAJOR_NUMBER
            + '.'
            + Engine.MINOR_NUMBER
            + "/"
            + Edition.CURRENT.getShortName().toLowerCase() + "/api/";

    private static final String BASE_WEBDAV = "http://www.webdav.org/specs/rfc2518.html";

    /**
     * The request could not be understood by the server due to malformed
     * syntax.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.1">HTTP
     *      RFC - 10.4.1 400 Bad Request</a>
     */
    public static final Status CLIENT_ERROR_BAD_REQUEST = new Status(400);

    /**
     * The request could not be completed due to a conflict with the current
     * state of the resource (as experienced in a version control system).
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.10">HTTP
     *      RFC - 10.4.10 409 Conflict</a>
     */
    public static final Status CLIENT_ERROR_CONFLICT = new Status(409);

    /**
     * The user agent expects some behavior of the server (given in an Expect
     * request-header field), but this expectation could not be met by this
     * server.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.18">HTTP
     *      RFC - 10.4.18 417 Expectation Failed</a>
     */
    public static final Status CLIENT_ERROR_EXPECTATION_FAILED = new Status(417);

    /**
     * This status code means that the method could not be performed on the
     * resource because the requested action depended on another action and that
     * action failed.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#STATUS_424">WEBDAV
     *      RFC - 10.5 424 Failed Dependency</a>
     */
    public static final Status CLIENT_ERROR_FAILED_DEPENDENCY = new Status(424);

    /**
     * The server understood the request, but is refusing to fulfill it as it
     * could be explained in the entity.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.4">HTTP
     *      RFC - 10.4.4 403 Forbidden</a>
     */
    public static final Status CLIENT_ERROR_FORBIDDEN = new Status(403);

    /**
     * The requested resource is no longer available at the server and no
     * forwarding address is known.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.11">HTTP
     *      RFC - 10.4.11 410 Gone</a>
     */
    public static final Status CLIENT_ERROR_GONE = new Status(410);

    /**
     * The server refuses to accept the request without a defined
     * Content-Length.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.12">HTTP
     *      RFC - 10.4.12 411 Length Required</a>
     */
    public static final Status CLIENT_ERROR_LENGTH_REQUIRED = new Status(411);

    /**
     * The source or destination resource of a method is locked (or temporarily
     * involved in another process).
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#STATUS_423">WEBDAV
     *      RFC - 10.4 423 Locked</a>
     */
    public static final Status CLIENT_ERROR_LOCKED = new Status(423);

    /**
     * The method specified in the Request-Line is not allowed for the resource
     * identified by the Request-URI.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.6">HTTP
     *      RFC - 10.4.6 405 Method Not Allowed</a>
     */
    public static final Status CLIENT_ERROR_METHOD_NOT_ALLOWED = new Status(405);

    /**
     * The resource identified by the request is only capable of generating
     * response entities whose content characteristics do not match the user's
     * requirements (in Accept* headers).
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.7">HTTP
     *      RFC - 10.4.7 406 Not Acceptable</a>
     */
    public static final Status CLIENT_ERROR_NOT_ACCEPTABLE = new Status(406);

    /**
     * The server has not found anything matching the Request-URI or the server
     * does not wish to reveal exactly why the request has been refused, or no
     * other response is applicable.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5">HTTP
     *      RFC - 10.4.5 404 Not Found</a>
     */
    public static final Status CLIENT_ERROR_NOT_FOUND = new Status(404);

    /**
     * This code is reserved for future use.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.3">HTTP
     *      RFC - 10.4.3 402 Payment Required</a>
     */
    public static final Status CLIENT_ERROR_PAYMENT_REQUIRED = new Status(402);

    /**
     * Sent by the server when the user agent asks the server to carry out a
     * request under certain conditions that are not met.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.13">HTTP
     *      RFC - 10.4.13 412 Precondition Failed</a>
     */
    public static final Status CLIENT_ERROR_PRECONDITION_FAILED = new Status(
            412);

    /**
     * This code is similar to 401 (Unauthorized), but indicates that the client
     * must first authenticate itself with the proxy.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.8">HTTP
     *      RFC - 10.4.8 407 Proxy Authentication Required</a>
     */
    public static final Status CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED = new Status(
            407);

    /**
     * The server is refusing to process a request because the request entity is
     * larger than the server is willing or able to process.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.14">HTTP
     *      RFC - 10.4.14 413 Request Entity Too Large</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE = new Status(
            413);

    /**
     * Sent by the server when an HTTP client opens a connection, but has never
     * sent a request (or never sent the blank line that signals the end of the
     * request).
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.9">HTTP
     *      RFC - 10.4.9 408 Request Timeout</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_TIMEOUT = new Status(408);

    /**
     * The server is refusing to service the request because the Request-URI is
     * longer than the server is willing to interpret.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.15">HTTP
     *      RFC - 10.4.15 414 Request-URI Too Long</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_URI_TOO_LONG = new Status(
            414);

    /**
     * The request includes a Range request-header field and the selected
     * resource is too small for any of the byte-ranges to apply.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.17">HTTP
     *      RFC - 10.4.17 416 Requested Range Not Satisfiable</a>
     */
    public static final Status CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE = new Status(
            416);

    /**
     * The request requires user authentication.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.2">HTTP
     *      RFC - 10.4.2 401 Unauthorized</a>
     */
    public static final Status CLIENT_ERROR_UNAUTHORIZED = new Status(401);

    /**
     * This status code means the server understands the content type of the
     * request entity (syntactically correct) but was unable to process the
     * contained instructions.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#STATUS_422">WEBDAV
     *      RFC - 10.3 422 Unprocessable Entity</a>
     */
    public static final Status CLIENT_ERROR_UNPROCESSABLE_ENTITY = new Status(
            422);

    /**
     * The server is refusing to service the request because the entity of the
     * request is in a format not supported by the requested resource for the
     * requested method.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.16">HTTP
     *      RFC - 10.4.16 415 Unsupported Media Type</a>
     */
    public static final Status CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE = new Status(
            415);

    /**
     * The client connector faced an error during the communication with the
     * remote server (interruption, timeout, etc.). The status code is 1001.
     */
    public static final Status CONNECTOR_ERROR_COMMUNICATION = new Status(1001);

    /**
     * The client connector could not connect to the remote server. The status
     * code is 1000.
     */
    public static final Status CONNECTOR_ERROR_CONNECTION = new Status(1000);

    /**
     * The client connector faced an internal error during the process of a
     * request to its server or the process of a response to its client. The
     * status code is 1002.
     */
    public static final Status CONNECTOR_ERROR_INTERNAL = new Status(1002);

    /**
     * This interim response (the client has to wait for the final response) is
     * used to inform the client that the initial part of the request has been
     * received and has not yet been rejected or completed by the server.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.1">HTTP
     *      RFC - 10.1.1 100 Continue</a>
     */
    public static final Status INFO_CONTINUE = new Status(100);

    /**
     * This interim response is used to inform the client that the server has
     * accepted the complete request, but has not yet completed it since the
     * server has a reasonable expectation that the request will take
     * significant time to complete.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#STATUS_102">WEBDAV
     *      RFC - 10.1 102 Processing</a>
     */
    public static final Status INFO_PROCESSING = new Status(102);

    /**
     * The server understands and is willing to comply with the client's
     * request, via the Upgrade message header field, for a change in the
     * application protocol being used on this connection.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.1.2">HTTP
     *      RFC - 10.1.1 101 Switching Protocols</a>
     */
    public static final Status INFO_SWITCHING_PROTOCOL = new Status(101);

    /**
     * Warning status code, typically returned by a cache, indicating that the
     * response is stale.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status INFO_STALE_RESPONSE = new Status(110);

    /**
     * Warning status code, typically returned by a cache, indicating that the
     * response is stale because an attempt to revalidate the response failed,
     * due to an inability to reach the server.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status INFO_REVALIDATION_FAILED = new Status(111);

    /**
     * Warning status code, typically returned by a cache, indicating that it is
     * intentionally disconnected from the rest of the network for a period of
     * time.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status INFO_DISCONNECTED_OPERATION = new Status(112);

    /**
     * Warning status code, typically returned by a cache, indicating that it
     * heuristically chose a freshness lifetime greater than 24 hours and the
     * response's age is greater than 24 hours.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status INFO_HEURISTIC_EXPIRATION = new Status(113);

    /**
     * Warning status code, optionally including arbitrary information to be
     * presented to a human user, typically returned by a cache.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status INFO_MISC_WARNING = new Status(199);

    /**
     * The requested resource resides temporarily under a different URI which
     * should not be used for future requests by the client (use status codes
     * 303 or 307 instead since this status has been manifestly misused).
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.3">HTTP
     *      RFC - 10.3.3 302 Found</a>
     */
    public static final Status REDIRECTION_FOUND = new Status(302);

    /**
     * The server lets the user agent choosing one of the multiple
     * representations of the requested resource, each representation having its
     * own specific location provided in the response entity.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.1">HTTP
     *      RFC - 10.3.1 300 Multiple Choices</a>
     */
    public static final Status REDIRECTION_MULTIPLE_CHOICES = new Status(300);

    /**
     * Status code sent by the server in response to a conditional GET request
     * in case the document has not been modified.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.5">HTTP
     *      RFC - 10.3.5 304 Not Modified</a>
     */
    public static final Status REDIRECTION_NOT_MODIFIED = new Status(304);

    /**
     * The requested resource has been assigned a new permanent URI and any
     * future references to this resource SHOULD use one of the returned URIs.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.2">HTTP
     *      RFC - 10.3.2 301 Moved Permanently</a>
     */
    public static final Status REDIRECTION_PERMANENT = new Status(301);

    /**
     * The response to the request can be found under a different URI and SHOULD
     * be retrieved using a GET method on that resource.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.4">HTTP
     *      RFC - 10.3.4 303 See Other</a>
     */
    public static final Status REDIRECTION_SEE_OTHER = new Status(303);

    /**
     * The requested resource resides temporarily under a different URI which
     * should not be used for future requests by the client.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.8">HTTP
     *      RFC - 10.3.8 307 Temporary Redirect</a>
     */
    public static final Status REDIRECTION_TEMPORARY = new Status(307);

    /**
     * The requested resource MUST be accessed through the proxy given by the
     * Location field.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.3.6">HTTP
     *      RFC - 10.3.6 305 Use Proxy</a>
     */
    public static final Status REDIRECTION_USE_PROXY = new Status(305);

    /**
     * The server, while acting as a gateway or proxy, received an invalid
     * response from the upstream server it accessed in attempting to fulfill
     * the request.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.3">HTTP
     *      RFC - 10.5.3 502 Bad Gateway</a>
     */
    public static final Status SERVER_ERROR_BAD_GATEWAY = new Status(502);

    /**
     * The server, while acting as a gateway or proxy, could not connect to the
     * upstream server.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.5">HTTP
     *      RFC - 10.5.5 504 Gateway Timeout</a>
     */
    public static final Status SERVER_ERROR_GATEWAY_TIMEOUT = new Status(504);

    /**
     * This status code means the method could not be performed on the resource
     * because the server is unable to store the representation needed to
     * successfully complete the request.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#STATUS_507">WEBDAV
     *      RFC - 10.6 507 Insufficient Storage</a>
     */
    public static final Status SERVER_ERROR_INSUFFICIENT_STORAGE = new Status(
            507);

    /**
     * The server encountered an unexpected condition which prevented it from
     * fulfilling the request.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.1">HTTP
     *      RFC - 10.5.1 500 Internal Server Error</a>
     */
    public static final Status SERVER_ERROR_INTERNAL = new Status(500);

    /**
     * The server does not support the functionality required to fulfill the
     * request.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.2">HTTP
     *      RFC - 10.5.2 501 Not Implemented</a>
     */
    public static final Status SERVER_ERROR_NOT_IMPLEMENTED = new Status(501);

    /**
     * The server is currently unable to handle the request due to a temporary
     * overloading or maintenance of the server.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.4">HTTP
     *      RFC - 10.5.4 503 Service Unavailable</a>
     */
    public static final Status SERVER_ERROR_SERVICE_UNAVAILABLE = new Status(
            503);

    /**
     * The server does not support, or refuses to support, the HTTP protocol
     * version that was used in the request message.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.5.6">HTTP
     *      RFC - 10.5.6 505 HTTP Version Not Supported</a>
     */
    public static final Status SERVER_ERROR_VERSION_NOT_SUPPORTED = new Status(
            505);

    /**
     * The request has been accepted for processing, but the processing has not
     * been completed.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.3">HTTP
     *      RFC - 10.2.3 202 Accepted</a>
     */
    public static final Status SUCCESS_ACCEPTED = new Status(202);

    /**
     * The request has been fulfilled and resulted in a new resource being
     * created.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.2">HTTP
     *      RFC - 10.2.2 201 Created</a>
     */
    public static final Status SUCCESS_CREATED = new Status(201);

    /**
     * This response is used to inform the client that the HTTP response entity
     * contains a set of status codes generated during the method invocation.
     * 
     * @see <a href="http://www.webdav.org/specs/rfc2518.html#STATUS_207">WEBDAV
     *      RFC - 10.2 207 Multi-Status</a>
     */
    public static final Status SUCCESS_MULTI_STATUS = new Status(207);

    /**
     * The server has fulfilled the request but does not need to return an
     * entity-body (for example after a DELETE), and might want to return
     * updated meta-information.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.5">HTTP
     *      RFC - 10.2.5 204 No Content</a>
     */
    public static final Status SUCCESS_NO_CONTENT = new Status(204);

    /**
     * The request has succeeded but the returned meta-information in the
     * entity-header does not come from the origin server, but is gathered from
     * a local or a third-party copy.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.4">HTTP
     *      RFC - 10.2.4 203 Non-Authoritative Information</a>
     */
    public static final Status SUCCESS_NON_AUTHORITATIVE = new Status(203);

    /**
     * The request has succeeded.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.1">HTTP
     *      RFC - 10.2.1 200 OK</a>
     */
    public static final Status SUCCESS_OK = new Status(200);

    /**
     * Warning status code, typically returned by a cache or a proxy, indicating
     * that the response has been transformed.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status SUCCESS_TRANSFORMATION_APPLIED = new Status(214);

    /**
     * Warning status code, optionally including arbitrary information to be
     * presented to a human user, typically returned by a cache.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status SUCCESS_MISC_PERSISTENT_WARNING = new Status(299);

    /**
     * The server has fulfilled the partial GET request for the resource
     * assuming the request has included a Range header field indicating the
     * desired range.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.7">HTTP
     *      RFC - 10.2.7 206 Partial Content</a>
     */
    public static final Status SUCCESS_PARTIAL_CONTENT = new Status(206);

    /**
     * The server has fulfilled the request and the user agent SHOULD reset the
     * document view which caused the request to be sent.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.6">HTTP
     *      RFC - 10.2.6 205 Reset Content</a>
     */
    public static final Status SUCCESS_RESET_CONTENT = new Status(205);

    /**
     * Check if the provided name of the status contains forbidden characters
     * such as CR and LF. an IllegalArgumentException is thrown in this case.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1.1">Status
     *      Code and Reason Phrase</a>
     * @param name
     *            The name to check
     * @return The name if it is correct.
     */
    private static String checkName(String name) {
        if (name != null) {
            if (name.contains("\n") || name.contains("\r")) {
                throw new IllegalArgumentException(
                        "Name of the status must not contain CR or LF characters.");
            }
        }

        return name;
    }

    /**
     * Indicates if the status is a client error status, meaning "The request
     * contains bad syntax or cannot be fulfilled".
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a client error status.
     */
    public static boolean isClientError(int code) {
        return (code >= 400) && (code <= 499);
    }

    /**
     * Indicates if the status is a connector error status, meaning "The
     * connector failed to send or receive an apparently valid message".
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a server error status.
     */
    public static boolean isConnectorError(int code) {
        return (code >= 1000) && (code <= 1099);
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
     * Indicates if the status is a client error status, meaning "The request
     * contains bad syntax or cannot be fulfilled".
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a client error status.
     */
    public static boolean isGlobalError(int code) {
        return (code >= 600) && (code <= 699);
    }

    /**
     * Indicates if the status is an information status, meaning "request
     * received, continuing process".
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is an information status.
     */
    public static boolean isInformational(int code) {
        return (code >= 100) && (code <= 199);
    }

    /**
     * Indicates if the status is a redirection status, meaning "Further action
     * must be taken in order to complete the request".
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a redirection status.
     */
    public static boolean isRedirection(int code) {
        return (code >= 300) && (code <= 399);
    }

    /**
     * Indicates if the status is a server error status, meaning "The server
     * failed to fulfill an apparently valid request".
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a server error status.
     */
    public static boolean isServerError(int code) {
        return (code >= 500) && (code <= 599);
    }

    /**
     * Indicates if the status is a success status, meaning "The action was
     * successfully received, understood, and accepted".
     * 
     * @param code
     *            The code of the status.
     * @return True if the status is a success status.
     */
    public static boolean isSuccess(int code) {
        return (code >= 200) && (code <= 299);
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
        case 110:
            result = INFO_STALE_RESPONSE;
            break;
        case 111:
            result = INFO_REVALIDATION_FAILED;
            break;
        case 112:
            result = INFO_DISCONNECTED_OPERATION;
            break;
        case 113:
            result = INFO_HEURISTIC_EXPIRATION;
            break;
        case 199:
            result = INFO_MISC_WARNING;
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
        case 214:
            result = SUCCESS_TRANSFORMATION_APPLIED;
            break;
        case 299:
            result = SUCCESS_MISC_PERSISTENT_WARNING;
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

        default:
            result = new Status(code);
        }

        return result;
    }

    /** The specification code. */
    private final int code;

    /** The description. */
    private final String description;

    /** The name. */
    private volatile String name;

    /** The related error or exception. */
    private final Throwable throwable;

    /** The URI of the specification describing the method. */
    private final String uri;

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
    public Status(int code, final String name, final String description,
            final String uri) {
        this(code, null, name, description, uri);
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param throwable
     *            The related error or exception.
     */
    public Status(int code, Throwable throwable) {
        this(code, throwable, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param throwable
     *            The related error or exception.
     * @param name
     *            The name.
     * @param description
     *            The description.
     * @param uri
     *            The URI of the specification describing the method.
     */
    public Status(int code, Throwable throwable, final String name,
            final String description, final String uri) {
        this.name = checkName(name);
        this.description = description;
        this.throwable = throwable;
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
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param throwable
     *            The related error or exception.
     */
    public Status(final Status status, final Throwable throwable) {
        this(status.getCode(), throwable, status.getName(),
                (throwable == null) ? null : throwable.getMessage(), status
                        .getUri());
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param throwable
     *            The related error or exception.
     * @param description
     *            The description to associate.
     */
    public Status(final Status status, final Throwable throwable,
            final String description) {
        this(status.getCode(), throwable, status.getName(), description, status
                .getUri());
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
     * Returns the corresponding code (HTTP or WebDAV or custom code).
     * 
     * @return The corresponding code.
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
        String result = this.description;

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
            case 110:
                result = "MUST be included whenever the returned response is stale";
                break;
            case 111:
                result = "MUST be included if a cache returns a stale response because an attempt to revalidate the response failed, due to an inability to reach the server";
                break;
            case 112:
                result = "SHOULD be included if the cache is intentionally disconnected from the rest of the network for a period of time";
                break;
            case 113:
                result = "MUST be included if the cache heuristically chose a freshness lifetime greater than 24 hours and the response's age is greater than 24 hours";
                break;
            case 199:
                result = "The warning text MAY include arbitrary information to be presented to a human user, or logged. A system receiving this warning MUST NOT take any automated action, besides presenting the warning to the user";
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
                result = "The returned meta-information is not the definitive set as available from the origin server";
                break;
            case 204:
                result = "The server has fulfilled the request but does not need to return an entity-body, and might want to return updated meta-information";
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
            case 214:
                result = "MUST be added by an intermediate cache or proxy if it applies any transformation changing the content-coding (as specified in the Content-Encoding header) or media-type (as specified in the Content-Type header) of the response, or the entity-body of the response, unless this Warning code already appears in the response";
                break;
            case 299:
                result = "The warning text MAY include arbitrary information to be presented to a human user, or logged. A system receiving this warning MUST NOT take any automated action";
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
        String result = this.name;

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
            case 110:
                result = "Response is stale";
                break;
            case 111:
                result = "Revalidation failed";
                break;
            case 112:
                result = "Disconnected operation";
                break;
            case 113:
                result = "Heuristic expiration";
                break;
            case 199:
                result = "Miscellaneous warning";
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
            case 214:
                result = "Transformation applied";
                break;
            case 299:
                result = "Miscellaneous persistent warning";
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
     * Returns the related error or exception.
     * 
     * @return The related error or exception.
     */
    public Throwable getThrowable() {
        return this.throwable;
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
            case 110:
            case 111:
            case 112:
            case 113:
            case 199:
            case 214:
            case 299:
                result = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46";
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
                        + "org/restlet/data/Status.html#CONNECTOR_ERROR_CONNECTION";
                break;
            case 1001:
                result = BASE_RESTLET
                        + "org/restlet/data/Status.html#CONNECTOR_ERROR_COMMUNICATION";
                break;
            case 1002:
                result = BASE_RESTLET
                        + "org/restlet/data/Status.html#CONNECTOR_ERROR_INTERNAL";
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
     * Indicates if the status is a client error status, meaning "The request
     * contains bad syntax or cannot be fulfilled".
     * 
     * @return True if the status is a client error status.
     */
    public boolean isClientError() {
        return isClientError(getCode());
    }

    /**
     * Indicates if the status is a connector error status, meaning "The
     * connector failed to send or receive an apparently valid message".
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
     * Indicates if the status is a global error status, meaning "The server has
     * definitive information about a particular user".
     * 
     * @return True if the status is a global error status.
     */
    public boolean isGlobalError() {
        return isGlobalError(getCode());
    }

    /**
     * Indicates if the status is an information status, meaning "request
     * received, continuing process".
     * 
     * @return True if the status is an information status.
     */
    public boolean isInformational() {
        return isInformational(getCode());
    }

    /**
     * Indicates if an error is recoverable, meaning that simply retrying after
     * a delay could result in a success. Tests {@link #isConnectorError()} and
     * if the status is {@link #CLIENT_ERROR_REQUEST_TIMEOUT} or
     * {@link #SERVER_ERROR_GATEWAY_TIMEOUT} or
     * {@link #SERVER_ERROR_SERVICE_UNAVAILABLE}.
     * 
     * @return True if the error is recoverable.
     */
    public boolean isRecoverableError() {
        return isConnectorError()
                || equals(Status.CLIENT_ERROR_REQUEST_TIMEOUT)
                || equals(Status.SERVER_ERROR_GATEWAY_TIMEOUT)
                || equals(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
    }

    /**
     * Indicates if the status is a redirection status, meaning "Further action
     * must be taken in order to complete the request".
     * 
     * @return True if the status is a redirection status.
     */
    public boolean isRedirection() {
        return isRedirection(getCode());
    }

    /**
     * Indicates if the status is a server error status, meaning "The server
     * failed to fulfill an apparently valid request".
     * 
     * @return True if the status is a server error status.
     */
    public boolean isServerError() {
        return isServerError(getCode());
    }

    /**
     * Indicates if the status is a success status, meaning "The action was
     * successfully received, understood, and accepted".
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
    @Override
    public String toString() {
        return getName() + " (" + this.code + ")"
                + ((getDescription() == null) ? "" : " - " + getDescription());
    }

}
