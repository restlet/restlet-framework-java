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

package org.restlet.data;

import org.restlet.engine.Edition;
import org.restlet.engine.Engine;

/**
 * Status to return after handling a call.
 * 
 * @author Jerome Louvel
 */
public final class Status {
    private static final String BASE_ADDED_HTTP = "http://tools.ietf.org/html/rfc6585";

    private static final String BASE_HTTP = "http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html";

    private static final String BASE_RESTLET = "http://restlet.org/learn/javadocs/"
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
     * The server refuses to accept the request because the user has sent too
     * many requests in a given amount of time.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc6585#section-4">HTTP RFC -
     *      10.4.12 429 Too Many Requests</a>
     */
    public static final Status CLIENT_ERROR_TOO_MANY_REQUESTS = new Status(429);

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
     * Warning status code, typically returned by a cache, indicating that the
     * response is stale.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status INFO_STALE_RESPONSE = new Status(110);

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
     * Warning status code, optionally including arbitrary information to be
     * presented to a human user, typically returned by a cache.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status SUCCESS_MISC_PERSISTENT_WARNING = new Status(299);

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
     * Warning status code, typically returned by a cache or a proxy, indicating
     * that the response has been transformed.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46">HTTP
     *      RFC - 14.46 Warning</a>
     */
    public static final Status SUCCESS_TRANSFORMATION_APPLIED = new Status(214);

    /**
     * Check if the provided reason phrase of the status contains forbidden
     * characters such as CR and LF. An IllegalArgumentException is thrown in
     * this case.
     * 
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1.1">Status
     *      Code and Reason Phrase</a>
     * @param reasonPhrase
     *            The reason phrase to check.
     * @return The name if it is correct.
     */
    private static String checkReasonPhrase(String reasonPhrase) {
        if (reasonPhrase != null) {
            if (reasonPhrase.contains("\n") || reasonPhrase.contains("\r")) {
                throw new IllegalArgumentException(
                        "Reason phrase of the status must not contain CR or LF characters.");
            }
        }

        return reasonPhrase;
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
        switch (code) {
        case 100:
            return INFO_CONTINUE;
        case 101:
            return INFO_SWITCHING_PROTOCOL;
        case 102:
            return INFO_PROCESSING;
        case 110:
            return INFO_STALE_RESPONSE;
        case 111:
            return INFO_REVALIDATION_FAILED;
        case 112:
            return INFO_DISCONNECTED_OPERATION;
        case 113:
            return INFO_HEURISTIC_EXPIRATION;
        case 199:
            return INFO_MISC_WARNING;

        case 200:
            return SUCCESS_OK;
        case 201:
            return SUCCESS_CREATED;
        case 202:
            return SUCCESS_ACCEPTED;
        case 203:
            return SUCCESS_NON_AUTHORITATIVE;
        case 204:
            return SUCCESS_NO_CONTENT;
        case 205:
            return SUCCESS_RESET_CONTENT;
        case 206:
            return SUCCESS_PARTIAL_CONTENT;
        case 207:
            return SUCCESS_MULTI_STATUS;
        case 214:
            return SUCCESS_TRANSFORMATION_APPLIED;
        case 299:
            return SUCCESS_MISC_PERSISTENT_WARNING;

        case 300:
            return REDIRECTION_MULTIPLE_CHOICES;
        case 301:
            return REDIRECTION_PERMANENT;
        case 302:
            return REDIRECTION_FOUND;
        case 303:
            return REDIRECTION_SEE_OTHER;
        case 304:
            return REDIRECTION_NOT_MODIFIED;
        case 305:
            return REDIRECTION_USE_PROXY;
        case 307:
            return REDIRECTION_TEMPORARY;

        case 400:
            return CLIENT_ERROR_BAD_REQUEST;
        case 401:
            return CLIENT_ERROR_UNAUTHORIZED;
        case 402:
            return CLIENT_ERROR_PAYMENT_REQUIRED;
        case 403:
            return CLIENT_ERROR_FORBIDDEN;
        case 404:
            return CLIENT_ERROR_NOT_FOUND;
        case 405:
            return CLIENT_ERROR_METHOD_NOT_ALLOWED;
        case 406:
            return CLIENT_ERROR_NOT_ACCEPTABLE;
        case 407:
            return CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED;
        case 408:
            return CLIENT_ERROR_REQUEST_TIMEOUT;
        case 409:
            return CLIENT_ERROR_CONFLICT;
        case 410:
            return CLIENT_ERROR_GONE;
        case 411:
            return CLIENT_ERROR_LENGTH_REQUIRED;
        case 412:
            return CLIENT_ERROR_PRECONDITION_FAILED;
        case 413:
            return CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE;
        case 414:
            return CLIENT_ERROR_REQUEST_URI_TOO_LONG;
        case 415:
            return CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;
        case 416:
            return CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE;
        case 417:
            return CLIENT_ERROR_EXPECTATION_FAILED;
        case 422:
            return CLIENT_ERROR_UNPROCESSABLE_ENTITY;
        case 423:
            return CLIENT_ERROR_LOCKED;
        case 424:
            return CLIENT_ERROR_FAILED_DEPENDENCY;
        case 429:
            return CLIENT_ERROR_TOO_MANY_REQUESTS;

        case 500:
            return SERVER_ERROR_INTERNAL;
        case 501:
            return SERVER_ERROR_NOT_IMPLEMENTED;
        case 502:
            return SERVER_ERROR_BAD_GATEWAY;
        case 503:
            return SERVER_ERROR_SERVICE_UNAVAILABLE;
        case 504:
            return SERVER_ERROR_GATEWAY_TIMEOUT;
        case 505:
            return SERVER_ERROR_VERSION_NOT_SUPPORTED;
        case 507:
            return SERVER_ERROR_INSUFFICIENT_STORAGE;

        case 1000:
            return CONNECTOR_ERROR_CONNECTION;
        case 1001:
            return CONNECTOR_ERROR_COMMUNICATION;
        case 1002:
            return CONNECTOR_ERROR_INTERNAL;

        default:
            return new Status(code);
        }
    }

    /** The specification code. */
    private final int code;

    /** The longer description. */
    private final String description;

    /**
     * The short reason phrase displayed next to the status code in a HTTP
     * response.
     */
    private volatile String reasonPhrase;

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
        this(code, null, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     */
    public Status(int code, String reasonPhrase) {
        this(code, null, reasonPhrase, null, null);
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     * @param description
     *            The longer description.
     */
    public Status(int code, String reasonPhrase, String description) {
        this(code, null, reasonPhrase, description, null);
    }

    /**
     * Constructor.
     * 
     * @param code
     *            The specification code.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     * @param description
     *            The longer description.
     * @param uri
     *            The URI of the specification describing the method.
     */
    public Status(int code, String reasonPhrase, String description, String uri) {
        this(code, null, reasonPhrase, description, uri);
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
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     */
    public Status(int code, Throwable throwable, String reasonPhrase) {
        this(code, throwable, reasonPhrase, null, null);
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
    public Status(int code, Throwable throwable, String reasonPhrase,
            String description) {
        this(code, throwable, reasonPhrase, description, null);
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
    public Status(int code, Throwable throwable, String reasonPhrase,
            String description, String uri) {
        this.code = code;
        this.throwable = throwable;
        this.reasonPhrase = checkReasonPhrase(reasonPhrase);
        this.description = description;
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
    public Status(Status status, String description) {
        this(status, null, null, description);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param description
     *            The description to associate.
     */
    public Status(Status status, String reasonPhrase, String description) {
        this(status, null, reasonPhrase, description);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param throwable
     *            The related error or exception.
     */
    public Status(Status status, Throwable throwable) {
        this(status, throwable, null, null);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param throwable
     *            The related error or exception.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     */
    public Status(Status status, Throwable throwable, String reasonPhrase) {
        this(status, throwable, reasonPhrase, null);
    }

    /**
     * Constructor.
     * 
     * @param status
     *            The status to copy.
     * @param throwable
     *            The related error or exception.
     * @param reasonPhrase
     *            The short reason phrase displayed next to the status code in a
     *            HTTP response.
     * @param description
     *            The description to associate.
     */
    public Status(Status status, Throwable throwable, String reasonPhrase,
            String description) {
        this(status.getCode(), (throwable == null) ? status.getThrowable()
                : throwable, (reasonPhrase == null) ? status.getReasonPhrase()
                : reasonPhrase, (description == null) ? status.getDescription()
                : description, status.getUri());
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
     * Returns the description. This value is typically used by the
     * {@link org.restlet.service.StatusService} to build a meaningful
     * description of an error via a response entity.
     * 
     * @return The description.
     */
    public String getDescription() {
        if (this.description != null) {
            return this.description;
        }

        switch (this.code) {
        case 100:
            return "The client should continue with its request";
        case 101:
            return "The server is willing to change the application protocol being used on this connection";
        case 102:
            return "Interim response used to inform the client that the server has accepted the complete request, but has not yet completed it";
        case 110:
            return "MUST be included whenever the returned response is stale";
        case 111:
            return "MUST be included if a cache returns a stale response because an attempt to revalidate the response failed, due to an inability to reach the server";
        case 112:
            return "SHOULD be included if the cache is intentionally disconnected from the rest of the network for a period of time";
        case 113:
            return "MUST be included if the cache heuristically chose a freshness lifetime greater than 24 hours and the response's age is greater than 24 hours";
        case 199:
            return "The warning text MAY include arbitrary information to be presented to a human user, or logged. A system receiving this warning MUST NOT take any automated action, besides presenting the warning to the user";

        case 200:
            return "The request has succeeded";
        case 201:
            return "The request has been fulfilled and resulted in a new resource being created";
        case 202:
            return "The request has been accepted for processing, but the processing has not been completed";
        case 203:
            return "The returned meta-information is not the definitive set as available from the origin server";
        case 204:
            return "The server has fulfilled the request but does not need to return an entity-body, and might want to return updated meta-information";
        case 205:
            return "The server has fulfilled the request and the user agent should reset the document view which caused the request to be sent";
        case 206:
            return "The server has fulfilled the partial get request for the resource";
        case 207:
            return "Provides status for multiple independent operations";
        case 214:
            return "MUST be added by an intermediate cache or proxy if it applies any transformation changing the content-coding (as specified in the Content-Encoding header) or media-type (as specified in the Content-Type header) of the response, or the entity-body of the response, unless this Warning code already appears in the response";
        case 299:
            return "The warning text MAY include arbitrary information to be presented to a human user, or logged. A system receiving this warning MUST NOT take any automated action";

        case 300:
            return "The requested resource corresponds to any one of a set of representations";
        case 301:
            return "The requested resource has been assigned a new permanent URI";
        case 302:
            return "The requested resource can be found under a different URI";
        case 303:
            return "The response to the request can be found under a different URI";
        case 304:
            return "The client has performed a conditional GET request and the document has not been modified";
        case 305:
            return "The requested resource must be accessed through the proxy given by the location field";
        case 307:
            return "The requested resource resides temporarily under a different URI";

        case 400:
            return "The request could not be understood by the server due to malformed syntax";
        case 401:
            return "The request requires user authentication";
        case 402:
            return "This code is reserved for future use";
        case 403:
            return "The server understood the request, but is refusing to fulfill it";
        case 404:
            return "The server has not found anything matching the request URI";
        case 405:
            return "The method specified in the request is not allowed for the resource identified by the request URI";
        case 406:
            return "The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request";
        case 407:
            return "This code is similar to Unauthorized, but indicates that the client must first authenticate itself with the proxy";
        case 408:
            return "The client did not produce a request within the time that the server was prepared to wait";
        case 409:
            return "The request could not be completed due to a conflict with the current state of the resource";
        case 410:
            return "The requested resource is no longer available at the server and no forwarding address is known";
        case 411:
            return "The server refuses to accept the request without a defined content length";
        case 412:
            return "The precondition given in one or more of the request header fields evaluated to false when it was tested on the server";
        case 413:
            return "The server is refusing to process a request because the request entity is larger than the server is willing or able to process";
        case 414:
            return "The server is refusing to service the request because the request URI is longer than the server is willing to interpret";
        case 415:
            return "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method";
        case 416:
            return "For byte ranges, this means that the first byte position were greater than the current length of the selected resource";
        case 417:
            return "The expectation given in the request header could not be met by this server";
        case 422:
            return "The server understands the content type of the request entity and the syntax of the request entity is correct but was unable to process the contained instructions";
        case 423:
            return "The source or destination resource of a method is locked";
        case 424:
            return "The method could not be performed on the resource because the requested action depended on another action and that action failed";
        case 429:
            return "The server is refusing to service the request because the user has sent too many requests in a given amount of time (\"rate limiting\")";

        case 500:
            return "The server encountered an unexpected condition which prevented it from fulfilling the request";
        case 501:
            return "The server does not support the functionality required to fulfill the request";
        case 502:
            return "The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request";
        case 503:
            return "The server is currently unable to handle the request due to a temporary overloading or maintenance of the server";
        case 504:
            return "The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary server (e.g. DNS) it needed to access in attempting to complete the request";
        case 505:
            return "The server does not support, or refuses to support, the protocol version that was used in the request message";
        case 507:
            return "The method could not be performed on the resource because the server is unable to store the representation needed to successfully complete the request";

        case 1000:
            return "The connector failed to connect to the server";
        case 1001:
            return "The connector failed to complete the communication with the server";
        case 1002:
            return "The connector encountered an unexpected condition which prevented it from fulfilling the request";
        }

        return null;
    }

    /**
     * Returns the reason phrase of this status. When supported by the HTTP
     * server connector, this is returned in the first line of the HTTP
     * response, next to to the status code.
     * 
     * @return The reason phrase of this status.
     */
    public String getReasonPhrase() {
        if (this.reasonPhrase != null) {
            return this.reasonPhrase;
        }

        switch (this.code) {
        case 100:
            return "Continue";
        case 101:
            return "Switching Protocols";
        case 102:
            return "Processing";
        case 110:
            return "Response is stale";
        case 111:
            return "Revalidation failed";
        case 112:
            return "Disconnected operation";
        case 113:
            return "Heuristic expiration";
        case 199:
            return "Miscellaneous warning";

        case 200:
            return "OK";
        case 201:
            return "Created";
        case 202:
            return "Accepted";
        case 203:
            return "Non-Authoritative Information";
        case 204:
            return "No Content";
        case 205:
            return "Reset Content";
        case 206:
            return "Partial Content";
        case 207:
            return "Multi-Status";
        case 214:
            return "Transformation applied";
        case 299:
            return "Miscellaneous persistent warning";

        case 300:
            return "Multiple Choices";
        case 301:
            return "Moved Permanently";
        case 302:
            return "Found";
        case 303:
            return "See Other";
        case 304:
            return "Not Modified";
        case 305:
            return "Use Proxy";
        case 307:
            return "Temporary Redirect";

        case 400:
            return "Bad Request";
        case 401:
            return "Unauthorized";
        case 402:
            return "Payment Required";
        case 403:
            return "Forbidden";
        case 404:
            return "Not Found";
        case 405:
            return "Method Not Allowed";
        case 406:
            return "Not Acceptable";
        case 407:
            return "Proxy Authentication Required";
        case 408:
            return "Request Timeout";
        case 409:
            return "Conflict";
        case 410:
            return "Gone";
        case 411:
            return "Length Required";
        case 412:
            return "Precondition Failed";
        case 413:
            return "Request Entity Too Large";
        case 414:
            return "Request URI Too Long";
        case 415:
            return "Unsupported Media Type";
        case 416:
            return "Requested Range Not Satisfiable";
        case 417:
            return "Expectation Failed";
        case 422:
            return "Unprocessable Entity";
        case 423:
            return "Locked";
        case 424:
            return "Failed Dependency";
        case 429:
            return "Too Many Requests";

        case 500:
            return "Internal Server Error";
        case 501:
            return "Not Implemented";
        case 502:
            return "Bad Gateway";
        case 503:
            return "Service Unavailable";
        case 504:
            return "Gateway Timeout";
        case 505:
            return "Version Not Supported";
        case 507:
            return "Insufficient Storage";

        case 1000:
            return "Connection Error";
        case 1001:
            return "Communication Error";
        case 1002:
            return "Internal Connector Error";
        }

        return null;
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
        if (this.uri != null) {
            return this.uri;
        }

        switch (this.code) {
        case 100:
            return BASE_HTTP + "#sec10.1.1";
        case 101:
            return BASE_HTTP + "#sec10.1.2";
        case 102:
            return BASE_WEBDAV + "#STATUS_102";
        case 110:
        case 111:
        case 112:
        case 113:
        case 199:
        case 214:
        case 299:
            return "http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.46";

        case 200:
            return BASE_HTTP + "#sec10.2.1";
        case 201:
            return BASE_HTTP + "#sec10.2.2";
        case 202:
            return BASE_HTTP + "#sec10.2.3";
        case 203:
            return BASE_HTTP + "#sec10.2.4";
        case 204:
            return BASE_HTTP + "#sec10.2.5";
        case 205:
            return BASE_HTTP + "#sec10.2.6";
        case 206:
            return BASE_HTTP + "#sec10.2.7";
        case 207:
            return BASE_WEBDAV + "#STATUS_207";

        case 300:
            return BASE_HTTP + "#sec10.3.1";
        case 301:
            return BASE_HTTP + "#sec10.3.2";
        case 302:
            return BASE_HTTP + "#sec10.3.3";
        case 303:
            return BASE_HTTP + "#sec10.3.4";
        case 304:
            return BASE_HTTP + "#sec10.3.5";
        case 305:
            return BASE_HTTP + "#sec10.3.6";
        case 307:
            return BASE_HTTP + "#sec10.3.8";

        case 400:
            return BASE_HTTP + "#sec10.4.1";
        case 401:
            return BASE_HTTP + "#sec10.4.2";
        case 402:
            return BASE_HTTP + "#sec10.4.3";
        case 403:
            return BASE_HTTP + "#sec10.4.4";
        case 404:
            return BASE_HTTP + "#sec10.4.5";
        case 405:
            return BASE_HTTP + "#sec10.4.6";
        case 406:
            return BASE_HTTP + "#sec10.4.7";
        case 407:
            return BASE_HTTP + "#sec10.4.8";
        case 408:
            return BASE_HTTP + "#sec10.4.9";
        case 409:
            return BASE_HTTP + "#sec10.4.10";
        case 410:
            return BASE_HTTP + "#sec10.4.11";
        case 411:
            return BASE_HTTP + "#sec10.4.12";
        case 412:
            return BASE_HTTP + "#sec10.4.13";
        case 413:
            return BASE_HTTP + "#sec10.4.14";
        case 414:
            return BASE_HTTP + "#sec10.4.15";
        case 415:
            return BASE_HTTP + "#sec10.4.16";
        case 416:
            return BASE_HTTP + "#sec10.4.17";
        case 417:
            return BASE_HTTP + "#sec10.4.18";
        case 422:
            return BASE_WEBDAV + "#STATUS_422";
        case 423:
            return BASE_WEBDAV + "#STATUS_423";
        case 424:
            return BASE_WEBDAV + "#STATUS_424";
        case 429:
            return BASE_ADDED_HTTP + "#section-4";

        case 500:
            return BASE_HTTP + "#sec10.5.1";
        case 501:
            return BASE_HTTP + "#sec10.5.2";
        case 502:
            return BASE_HTTP + "#sec10.5.3";
        case 503:
            return BASE_HTTP + "#sec10.5.4";
        case 504:
            return BASE_HTTP + "#sec10.5.5";
        case 505:
            return BASE_HTTP + "#sec10.5.6";
        case 507:
            return BASE_WEBDAV + "#STATUS_507";

        case 1000:
            return BASE_RESTLET + "org/restlet/data/Status.html#CONNECTOR_ERROR_CONNECTION";
        case 1001:
            return BASE_RESTLET + "org/restlet/data/Status.html#CONNECTOR_ERROR_COMMUNICATION";
        case 1002:
            return BASE_RESTLET + "org/restlet/data/Status.html#CONNECTOR_ERROR_INTERNAL";
        }

        return null;
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
     * Returns the reason phrase of the status followed by its HTTP code.
     * 
     * @return The reason phrase of the status followed by its HTTP code.
     */
    @Override
    public String toString() {
        return getReasonPhrase() + " (" + this.code + ")"
                + ((getDescription() == null) ? "" : " - " + getDescription());
    }

}
