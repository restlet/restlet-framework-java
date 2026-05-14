/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import org.restlet.engine.Engine;

/**
 * Status to return after handling a call.
 *
 * @author Jerome Louvel
 */
public final class Status {
    private static final String BASE_ADDED_HTTP = "https://datatracker.ietf.org/doc/html/rfc6585";

    private static final String BASE_HTTP = "https://www.rfc-editor.org/rfc/rfc9110.html";
    private static final String BASE_HTTP_STATUS_CODES =
            "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes";

    private static final String BASE_RESTLET =
            "https://javadoc.io/static/org.restlet/org.restlet/" + Engine.VERSION + "/";

    /**
     * The server could not understand the request due to malformed syntax.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-400-bad-request">HTTP RFC -
     *     400 Bad Request</a>
     */
    public static final Status CLIENT_ERROR_BAD_REQUEST = new Status(400);

    /**
     * The request could not be completed due to a conflict with the current state of the resource
     * (as experienced in a version control system).
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-409-conflict">HTTP RFC - 409
     *     Conflict</a>
     */
    public static final Status CLIENT_ERROR_CONFLICT = new Status(409);

    /**
     * The user agent expects some behavior of the server (given in an Expect request-header field),
     * but this expectation could not be met by this server.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-417-expectation-failed">HTTP
     *     RFC - 417 Expectation Failed</a>
     */
    public static final Status CLIENT_ERROR_EXPECTATION_FAILED = new Status(417);

    /**
     * The server understood the request but is refusing to fulfill it as it could be explained in
     * the entity.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-403-forbidden">HTTP RFC - 403
     *     Forbidden</a>
     */
    public static final Status CLIENT_ERROR_FORBIDDEN = new Status(403);

    /**
     * The requested resource is no longer available at the server, and no forwarding address is
     * known.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-410-gone">HTTP RFC - 410
     *     Gone</a>
     */
    public static final Status CLIENT_ERROR_GONE = new Status(410);

    /**
     * The server refuses to accept the request without a defined Content-Length.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-411-length-required">HTTP RFC
     *     - 411 Length Required</a>
     */
    public static final Status CLIENT_ERROR_LENGTH_REQUIRED = new Status(411);

    /**
     * The method specified in the Request-Line is not allowed for the resource identified by the
     * Request-URI.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-405-method-not-allowed">HTTP
     *     RFC - 405 Method Not Allowed</a>
     */
    public static final Status CLIENT_ERROR_METHOD_NOT_ALLOWED = new Status(405);

    /**
     * The resource identified by the request is only capable of generating response entities whose
     * content characteristics do not match the user's requirements (in Accept* headers).
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-406-not-acceptable">HTTP RFC
     *     - 406 Not Acceptable</a>
     */
    public static final Status CLIENT_ERROR_NOT_ACCEPTABLE = new Status(406);

    /**
     * The server has not found anything matching the Request-URI, or the server does not wish to
     * reveal exactly why the request has been refused, or no other response is applicable.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-404-not-found">HTTP RFC - 404
     *     Not Found</a>
     */
    public static final Status CLIENT_ERROR_NOT_FOUND = new Status(404);

    /**
     * This code is reserved for future use.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-402-payment-required">HTTP
     *     RFC - 402 Payment Required</a>
     */
    public static final Status CLIENT_ERROR_PAYMENT_REQUIRED = new Status(402);

    /**
     * Sent by the server when the user agent asks the server to carry out a request under certain
     * conditions that are not met.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-412-precondition-failed">HTTP
     *     RFC - 412 Precondition Failed</a>
     */
    public static final Status CLIENT_ERROR_PRECONDITION_FAILED = new Status(412);

    /**
     * This code is similar to 401 (Unauthorized) but indicates that the client must first
     * authenticate itself with the proxy.
     *
     * @see <a href=
     *     "https://www.rfc-editor.org/rfc/rfc9110.html#name-407-proxy-authentication-re">HTTP RFC -
     *     407 Proxy Authentication Required</a>
     */
    public static final Status CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED = new Status(407);

    /**
     * The server is refusing to process a request because the request entity is larger than the
     * server is willing or able to process.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-413-content-too-large">HTTP
     *     RFC - 413 Request Entity Too Large</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE = new Status(413);

    /**
     * Sent by the server when an HTTP client opens a connection but has never sent a request (or
     * never sent the blank line that signals the end of the request).
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-408-request-timeout">HTTP RFC
     *     - 408 Request Timeout</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_TIMEOUT = new Status(408);

    /**
     * The server is refusing to service the request because the Request-URI is longer than the
     * server is willing to interpret.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-414-uri-too-long">HTTP RFC -
     *     414 Request-URI Too Long</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_URI_TOO_LONG = new Status(414);

    /**
     * The request includes a Range request-header field, and the selected resource is too small for
     * any of the byte-ranges to apply.
     *
     * @see <a href=
     *     "https://www.rfc-editor.org/rfc/rfc9110.html#name-416-range-not-satisfiable">HTTP RFC -
     *     416 Requested Range Not Satisfiable</a>
     */
    public static final Status CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE = new Status(416);

    /**
     * The server refuses to accept the request because the user has sent too many requests in a
     * given amount of time.
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc6585#section-4">HTTP RFC - 429 Too
     *     Many Requests</a>
     */
    public static final Status CLIENT_ERROR_TOO_MANY_REQUESTS = new Status(429);

    /**
     * The request requires user authentication.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-401-unauthorized">HTTP RFC -
     *     401 Unauthorized</a>
     */
    public static final Status CLIENT_ERROR_UNAUTHORIZED = new Status(401);

    /**
     * The server is refusing to service the request because the entity of the request is in a
     * format not supported by the requested resource for the requested method.
     *
     * @see <a href=
     *     "https://www.rfc-editor.org/rfc/rfc9110.html#name-415-unsupported-media-type">HTTP RFC -
     *     415 Unsupported Media Type</a>
     */
    public static final Status CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE = new Status(415);

    /**
     * The client connector faced an error during the communication with the remote server
     * (interruption, timeout, etc.). The status code is 1001.
     */
    public static final Status CONNECTOR_ERROR_COMMUNICATION = new Status(1001);

    /** The client connector could not connect to the remote server. The status code is 1000. */
    public static final Status CONNECTOR_ERROR_CONNECTION = new Status(1000);

    /**
     * The client connector faced an internal error during the process of a request to its server or
     * the process of a response to its client. The status code is 1002.
     */
    public static final Status CONNECTOR_ERROR_INTERNAL = new Status(1002);

    /**
     * This interim response (the client has to wait for the final response) is used to inform the
     * client that the initial part of the request has been received and has not yet been rejected
     * or completed by the server.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-100-continue">HTTP RFC - 100
     *     Continue</a>
     */
    public static final Status INFO_CONTINUE = new Status(100);

    /**
     * Warning status code, typically returned by a cache, indicating that it is intentionally
     * disconnected from the rest of the network for a period of time.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">HTTP RFC -
     *     Warning</a>
     */
    public static final Status INFO_DISCONNECTED_OPERATION = new Status(112);

    /**
     * Warning status code, typically returned by a cache, indicating that it heuristically chose a
     * freshness lifetime greater than 24 hours and the response's age is greater than 24 hours.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">HTTP RFC -
     *     Warning</a>
     */
    public static final Status INFO_HEURISTIC_EXPIRATION = new Status(113);

    /**
     * Warning status code, optionally including arbitrary information to be presented to a human
     * user, typically returned by a cache.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">HTTP RFC -
     *     Warning</a>
     */
    public static final Status INFO_MISC_WARNING = new Status(199);

    /**
     * Warning status code, typically returned by a cache, indicating that the response is stale
     * because an attempt to revalidate the response failed, due to an inability to reach the
     * server.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">HTTP RFC -
     *     Warning</a>
     */
    public static final Status INFO_REVALIDATION_FAILED = new Status(111);

    /**
     * Warning status code, typically returned by a cache, indicating that the response is stale.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">HTTP RFC -
     *     Warning</a>
     */
    public static final Status INFO_STALE_RESPONSE = new Status(110);

    /**
     * The server understands and is willing to comply with the client's request, via the Upgrade
     * message header field, for a change in the application protocol being used on this connection.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-101-switching-protocols">HTTP
     *     RFC - 101 Switching Protocols</a>
     */
    public static final Status INFO_SWITCHING_PROTOCOL = new Status(101);

    /**
     * The requested resource resides temporarily under a different URI which should not be used for
     * future requests by the client (use status codes 303 or 307 instead since this status has been
     * manifestly misused).
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-302-found">HTTP RFC - 302
     *     Found</a>
     */
    public static final Status REDIRECTION_FOUND = new Status(302);

    /**
     * The server lets the user agent choosing one of the multiple representations of the requested
     * resource, each representation having its own specific location provided in the response
     * entity.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-300-multiple-choices">HTTP
     *     RFC - 300 Multiple Choices</a>
     */
    public static final Status REDIRECTION_MULTIPLE_CHOICES = new Status(300);

    /**
     * Status code sent by the server in response to a conditional GET request in case the document
     * has not been modified.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-304-not-modified">HTTP RFC -
     *     304 Not Modified</a>
     */
    public static final Status REDIRECTION_NOT_MODIFIED = new Status(304);

    /**
     * The requested resource has been assigned a new permanent URI, and any future references to
     * this resource SHOULD use one of the returned URIs.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-301-moved-permanently">HTTP
     *     RFC - 301 Moved Permanently</a>
     */
    public static final Status REDIRECTION_PERMANENT = new Status(301);

    /**
     * The response to the request can be found under a different URI and SHOULD be retrieved using
     * a GET method on that resource.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-303-see-other">HTTP RFC - 303
     *     See Other</a>
     */
    public static final Status REDIRECTION_SEE_OTHER = new Status(303);

    /**
     * The requested resource resides temporarily under a different URI which should not be used for
     * future requests by the client.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-307-temporary-redirect">HTTP
     *     RFC - 307 Temporary Redirect</a>
     */
    public static final Status REDIRECTION_TEMPORARY = new Status(307);

    /**
     * The requested resource MUST be accessed through the proxy given by the Location field.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-305-use-proxy">HTTP RFC - 305
     *     Use Proxy</a>
     */
    public static final Status REDIRECTION_USE_PROXY = new Status(305);

    /**
     * The server, while acting as a gateway or proxy, received an invalid response from the
     * upstream server it accessed in attempting to fulfill the request.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-502-bad-gateway">HTTP RFC -
     *     502 Bad Gateway</a>
     */
    public static final Status SERVER_ERROR_BAD_GATEWAY = new Status(502);

    /**
     * The server, while acting as a gateway or proxy, could not connect to the upstream server.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-504-gateway-timeout">HTTP RFC
     *     - 504 Gateway Timeout</a>
     */
    public static final Status SERVER_ERROR_GATEWAY_TIMEOUT = new Status(504);

    /**
     * The server encountered an unexpected condition which prevented it from fulfilling the
     * request.
     *
     * @see <a href=
     *     "https://www.rfc-editor.org/rfc/rfc9110.html#name-500-internal-server-error">HTTP RFC -
     *     500 Internal Server Error</a>
     */
    public static final Status SERVER_ERROR_INTERNAL = new Status(500);

    /**
     * The server does not support the functionality required to fulfill the request.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-501-not-implemented">HTTP RFC
     *     - 501 Not Implemented</a>
     */
    public static final Status SERVER_ERROR_NOT_IMPLEMENTED = new Status(501);

    /**
     * The server is currently unable to handle the request due to a temporary overloading or
     * maintenance of the server.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-503-service-unavailable">HTTP
     *     RFC - 503 Service Unavailable</a>
     */
    public static final Status SERVER_ERROR_SERVICE_UNAVAILABLE = new Status(503);

    /**
     * The server does not support, or refuses to support, the HTTP protocol version used in the
     * request message.
     *
     * @see <a href=
     *     "https://www.rfc-editor.org/rfc/rfc9110.html#name-505-http-version-not-suppor">HTTP RFC -
     *     505 HTTP Version Not Supported</a>
     */
    public static final Status SERVER_ERROR_VERSION_NOT_SUPPORTED = new Status(505);

    /**
     * The request has been accepted for processing, but the processing has not been completed.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-202-accepted">HTTP RFC - 202
     *     Accepted</a>
     */
    public static final Status SUCCESS_ACCEPTED = new Status(202);

    /**
     * The request has been fulfilled and resulted in a new resource being created.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-201-created">HTTP RFC - 201
     *     Created</a>
     */
    public static final Status SUCCESS_CREATED = new Status(201);

    /**
     * Warning status code, optionally including arbitrary information to be presented to a human
     * user, typically returned by a cache.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">HTTP RFC -
     *     Warning</a>
     */
    public static final Status SUCCESS_MISC_PERSISTENT_WARNING = new Status(299);

    /**
     * The server has fulfilled the request but does not need to return an entity-body (for example,
     * after a DELETE), and might want to return updated meta-information.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-204-no-content">HTTP RFC -
     *     204 No Content</a>
     */
    public static final Status SUCCESS_NO_CONTENT = new Status(204);

    /**
     * The request has succeeded, but the returned meta-information in the entity-header does not
     * come from the origin server but is gathered from a local or a third-party copy.
     *
     * @see <a href=
     *     "https://www.rfc-editor.org/rfc/rfc9110.html#name-203-non-authoritative-infor">HTTP RFC -
     *     203 Non-Authoritative Information</a>
     */
    public static final Status SUCCESS_NON_AUTHORITATIVE = new Status(203);

    /**
     * The request has succeeded.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-200-ok">HTTP RFC - 200 OK</a>
     */
    public static final Status SUCCESS_OK = new Status(200);

    /**
     * The server has fulfilled the partial GET request for the resource assuming the request has
     * included a Range header field indicating the desired range.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-206-partial-content">HTTP RFC
     *     - 206 Partial Content</a>
     */
    public static final Status SUCCESS_PARTIAL_CONTENT = new Status(206);

    /**
     * The server has fulfilled the request, and the user agent SHOULD reset the document view which
     * caused the request to be sent.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-205-reset-content">HTTP RFC -
     *     205 Reset Content</a>
     */
    public static final Status SUCCESS_RESET_CONTENT = new Status(205);

    /**
     * Warning status code, typically returned by a cache or a proxy, indicating that the response
     * has been transformed.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">HTTP RFC -
     *     Warning</a>
     */
    public static final Status SUCCESS_TRANSFORMATION_APPLIED = new Status(214);

    /**
     * Check if the provided reason phrase of the status contains forbidden characters such as CR
     * and LF. An IllegalArgumentException is thrown in this case.
     *
     * @see <a href= "https://www.rfc-editor.org/rfc/rfc9110.html#name-status-codes">Status Code and
     *     Reason Phrase</a>
     * @param reasonPhrase The reason phrase to check.
     * @return The name if it is correct.
     */
    private static String checkReasonPhrase(String reasonPhrase) {
        if (reasonPhrase != null && (reasonPhrase.contains("\n") || reasonPhrase.contains("\r"))) {
            throw new IllegalArgumentException(
                    "Reason phrase of the status must not contain CR or LF characters.");
        }

        return reasonPhrase;
    }

    /**
     * Indicates if the status is a client error status, meaning "The request contains bad syntax or
     * cannot be fulfilled".
     *
     * @param code The code of the status.
     * @return True if the status is a client error status.
     */
    public static boolean isClientError(int code) {
        return (code >= 400) && (code <= 499);
    }

    /**
     * Indicates if the status is a connector error status, meaning "The connector failed to send or
     * receive an apparently valid message".
     *
     * @param code The code of the status.
     * @return True if the status is a server error status.
     */
    public static boolean isConnectorError(int code) {
        return (code >= 1000) && (code <= 1099);
    }

    /**
     * Indicates if the status is an error (client or server) status.
     *
     * @param code The code of the status.
     * @return True if the status is an error (client or server) status.
     */
    public static boolean isError(int code) {
        return isClientError(code)
                || isServerError(code)
                || isConnectorError(code)
                || isGlobalError(code);
    }

    /**
     * Indicates if the status is a client error status, meaning "The request contains bad syntax or
     * cannot be fulfilled".
     *
     * @param code The code of the status.
     * @return True if the status is a client error status.
     */
    public static boolean isGlobalError(int code) {
        return (code >= 600) && (code <= 699);
    }

    /**
     * Indicates if the status is an information status, meaning "request received, continuing
     * process".
     *
     * @param code The code of the status.
     * @return True if the status is an information status.
     */
    public static boolean isInformational(int code) {
        return (code >= 100) && (code <= 199);
    }

    /**
     * Indicates if the status is a redirection status, meaning "Further action must be taken to
     * complete the request".
     *
     * @param code The code of the status.
     * @return True if the status is a redirection status.
     */
    public static boolean isRedirection(int code) {
        return (code >= 300) && (code <= 399);
    }

    /**
     * Indicates if the status is a server error status, meaning "The server failed to fulfill an
     * apparently valid request".
     *
     * @param code The code of the status.
     * @return True if the status is a server error status.
     */
    public static boolean isServerError(int code) {
        return (code >= 500) && (code <= 599);
    }

    /**
     * Indicates if the status is a success status, meaning "The action was successfully received,
     * understood, and accepted".
     *
     * @param code The code of the status.
     * @return True if the status is a success status.
     */
    public static boolean isSuccess(int code) {
        return (code >= 200) && (code <= 299);
    }

    /**
     * Returns the status associated with a code. If an existing constant exists, then it is
     * returned, otherwise a new instance is created.
     *
     * @param code The code.
     * @return The associated status.
     */
    public static Status valueOf(int code) {
        StatusEntry statusEntry = getStatusEntry(code);
        return statusEntry == null ? new Status(code) : statusEntry.status;
    }

    private static StatusEntry getStatusEntry(final int code) {
        if (code < STATUS_ENTRIES.length && code > 0) {
            return STATUS_ENTRIES[code];
        }
        return null;
    }

    record StatusEntry(Status status, String description, String reasonPhrase, String uri) {}

    // The status code indexes the array of status entries. It allows quickly retrieving the
    // default description, default reason phrase and default uri of a status.
    // It has been preferred to a Map storage because of the conversion to an Integer object, which
    // is not optimal in terms of speed access.
    // The memory penalty is considered minimal: the map would cost about 3400 bytes and
    // the array costs about 5800 bytes.
    private static final StatusEntry[] STATUS_ENTRIES = new StatusEntry[1003];

    static {
        STATUS_ENTRIES[100] =
                new StatusEntry(
                        INFO_CONTINUE,
                        "The client should continue with its request",
                        "Continue",
                        BASE_HTTP + "#name-100-continue");
        STATUS_ENTRIES[101] =
                new StatusEntry(
                        INFO_SWITCHING_PROTOCOL,
                        "The server is willing to change the application protocol being used on this connection",
                        "Switching Protocols",
                        BASE_HTTP + "#name-101-switching-protocols");
        STATUS_ENTRIES[110] =
                new StatusEntry(
                        INFO_STALE_RESPONSE,
                        "MUST be included whenever the returned response is stale",
                        "Response is stale",
                        BASE_HTTP_STATUS_CODES);
        STATUS_ENTRIES[111] =
                new StatusEntry(
                        INFO_REVALIDATION_FAILED,
                        "MUST be included if a cache returns a stale response because an attempt to revalidate the response failed, due to an inability to reach the server",
                        "Revalidation failed",
                        BASE_HTTP_STATUS_CODES);
        STATUS_ENTRIES[112] =
                new StatusEntry(
                        INFO_DISCONNECTED_OPERATION,
                        "SHOULD be included if the cache is intentionally disconnected from the rest of the network for a period of time",
                        "Disconnected operation",
                        BASE_HTTP_STATUS_CODES);
        STATUS_ENTRIES[113] =
                new StatusEntry(
                        INFO_HEURISTIC_EXPIRATION,
                        "MUST be included if the cache heuristically chose a freshness lifetime greater than 24 hours and the response's age is greater than 24 hours",
                        "Heuristic expiration",
                        BASE_HTTP_STATUS_CODES);
        STATUS_ENTRIES[199] =
                new StatusEntry(
                        INFO_MISC_WARNING,
                        "The warning text MAY include arbitrary information to be presented to a human user, or logged. A system receiving this warning MUST NOT take any automated action, besides presenting the warning to the user",
                        "Miscellaneous warning",
                        BASE_HTTP_STATUS_CODES);
        STATUS_ENTRIES[200] =
                new StatusEntry(
                        SUCCESS_OK, "The request has succeeded", "OK", BASE_HTTP + "#name-200-ok");
        STATUS_ENTRIES[201] =
                new StatusEntry(
                        SUCCESS_CREATED,
                        "The request has been fulfilled and resulted in a new resource being created",
                        "Created",
                        BASE_HTTP + "#name-201-created");
        STATUS_ENTRIES[202] =
                new StatusEntry(
                        SUCCESS_ACCEPTED,
                        "The request has been accepted for processing, but the processing has not been completed",
                        "Accepted",
                        BASE_HTTP + "#name-202-accepted");
        STATUS_ENTRIES[203] =
                new StatusEntry(
                        SUCCESS_NON_AUTHORITATIVE,
                        "The returned meta-information is not the definitive set as available from the origin server",
                        "Non-Authoritative Information",
                        BASE_HTTP + "#name-203-non-authoritative-infor");
        STATUS_ENTRIES[204] =
                new StatusEntry(
                        SUCCESS_NO_CONTENT,
                        "The server has fulfilled the request but does not need to return an entity-body, and might want to return updated meta-information",
                        "No Content",
                        BASE_HTTP + "#name-204-no-content");
        STATUS_ENTRIES[205] =
                new StatusEntry(
                        SUCCESS_RESET_CONTENT,
                        "The server has fulfilled the request and the user agent should reset the document view which caused the request to be sent",
                        "Reset Content",
                        BASE_HTTP + "#name-205-reset-content");
        STATUS_ENTRIES[206] =
                new StatusEntry(
                        SUCCESS_PARTIAL_CONTENT,
                        "The server has fulfilled the partial get request for the resource",
                        "Partial Content",
                        BASE_HTTP + "#name-206-partial-content");
        STATUS_ENTRIES[214] =
                new StatusEntry(
                        SUCCESS_TRANSFORMATION_APPLIED,
                        "MUST be added by an intermediate cache or proxy if it applies any transformation changing the content-coding (as specified in the Content-Encoding header) or media-type (as specified in the Content-Type header) of the response, or the entity-body of the response, unless this Warning code already appears in the response",
                        "Transformation applied",
                        BASE_HTTP_STATUS_CODES);
        STATUS_ENTRIES[299] =
                new StatusEntry(
                        SUCCESS_MISC_PERSISTENT_WARNING,
                        "The warning text MAY include arbitrary information to be presented to a human user, or logged. A system receiving this warning MUST NOT take any automated action",
                        "Miscellaneous persistent warning",
                        BASE_HTTP_STATUS_CODES);
        STATUS_ENTRIES[300] =
                new StatusEntry(
                        REDIRECTION_MULTIPLE_CHOICES,
                        "The requested resource corresponds to any one of a set of representations",
                        "Multiple Choices",
                        BASE_HTTP + "#name-300-multiple-choices");
        STATUS_ENTRIES[301] =
                new StatusEntry(
                        REDIRECTION_PERMANENT,
                        "The requested resource has been assigned a new permanent URI",
                        "Moved Permanently",
                        BASE_HTTP + "#name-301-moved-permanently");
        STATUS_ENTRIES[302] =
                new StatusEntry(
                        REDIRECTION_FOUND,
                        "The requested resource can be found under a different URI",
                        "Found",
                        BASE_HTTP + "#name-302-found");
        STATUS_ENTRIES[303] =
                new StatusEntry(
                        REDIRECTION_SEE_OTHER,
                        "The response to the request can be found under a different URI",
                        "See Other",
                        BASE_HTTP + "#name-303-see-other");
        STATUS_ENTRIES[304] =
                new StatusEntry(
                        REDIRECTION_NOT_MODIFIED,
                        "The client has performed a conditional GET request and the document has not been modified",
                        "Not Modified",
                        BASE_HTTP + "#name-304-not-modified");
        STATUS_ENTRIES[305] =
                new StatusEntry(
                        REDIRECTION_USE_PROXY,
                        "The requested resource must be accessed through the proxy given by the location field",
                        "Use Proxy",
                        BASE_HTTP + "#name-305-use-proxy");
        STATUS_ENTRIES[307] =
                new StatusEntry(
                        REDIRECTION_TEMPORARY,
                        "The requested resource resides temporarily under a different URI",
                        "Temporary Redirect",
                        BASE_HTTP + "#name-307-temporary-redirect");
        STATUS_ENTRIES[400] =
                new StatusEntry(
                        CLIENT_ERROR_BAD_REQUEST,
                        "The request could not be understood by the server due to malformed syntax",
                        "Bad Request",
                        BASE_HTTP + "#name-400-bad-request");
        STATUS_ENTRIES[401] =
                new StatusEntry(
                        CLIENT_ERROR_UNAUTHORIZED,
                        "The request requires user authentication",
                        "Unauthorized",
                        BASE_HTTP + "#name-401-unauthorized");
        STATUS_ENTRIES[402] =
                new StatusEntry(
                        CLIENT_ERROR_PAYMENT_REQUIRED,
                        "This code is reserved for future use",
                        "Payment Required",
                        BASE_HTTP + "#name-402-payment-required");
        STATUS_ENTRIES[403] =
                new StatusEntry(
                        CLIENT_ERROR_FORBIDDEN,
                        "The server understood the request, but is refusing to fulfill it",
                        "Forbidden",
                        BASE_HTTP + "#name-403-forbidden");
        STATUS_ENTRIES[404] =
                new StatusEntry(
                        CLIENT_ERROR_NOT_FOUND,
                        "The server has not found anything matching the request URI",
                        "Not Found",
                        BASE_HTTP + "#name-404-not-found");
        STATUS_ENTRIES[405] =
                new StatusEntry(
                        CLIENT_ERROR_METHOD_NOT_ALLOWED,
                        "The method specified in the request is not allowed for the resource identified by the request URI",
                        "Method Not Allowed",
                        BASE_HTTP + "#name-405-method-not-allowed");
        STATUS_ENTRIES[406] =
                new StatusEntry(
                        CLIENT_ERROR_NOT_ACCEPTABLE,
                        "The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request",
                        "Not Acceptable",
                        BASE_HTTP + "#name-406-not-acceptable");
        STATUS_ENTRIES[407] =
                new StatusEntry(
                        CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED,
                        "This code is similar to Unauthorized, but indicates that the client must first authenticate itself with the proxy",
                        "Proxy Authentication Required",
                        BASE_HTTP + "#name-407-proxy-authentication-re");
        STATUS_ENTRIES[408] =
                new StatusEntry(
                        CLIENT_ERROR_REQUEST_TIMEOUT,
                        "The client did not produce a request within the time that the server was prepared to wait",
                        "Request Timeout",
                        BASE_HTTP + "#name-408-request-timeout");
        STATUS_ENTRIES[409] =
                new StatusEntry(
                        CLIENT_ERROR_CONFLICT,
                        "The request could not be completed due to a conflict with the current state of the resource",
                        "Conflict",
                        BASE_HTTP + "#name-409-conflict");
        STATUS_ENTRIES[410] =
                new StatusEntry(
                        CLIENT_ERROR_GONE,
                        "The requested resource is no longer available at the server and no forwarding address is known",
                        "Gone",
                        BASE_HTTP + "#name-410-gone");
        STATUS_ENTRIES[411] =
                new StatusEntry(
                        CLIENT_ERROR_LENGTH_REQUIRED,
                        "The server refuses to accept the request without a defined content length",
                        "Length Required",
                        BASE_HTTP + "#name-411-length-required");
        STATUS_ENTRIES[412] =
                new StatusEntry(
                        CLIENT_ERROR_PRECONDITION_FAILED,
                        "The precondition given in one or more of the request header fields evaluated to false when it was tested on the server",
                        "Precondition Failed",
                        BASE_HTTP + "#name-412-precondition-failed");
        STATUS_ENTRIES[413] =
                new StatusEntry(
                        CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE,
                        "The server is refusing to process a request because the request entity is larger than the server is willing or able to process",
                        "Request Entity Too Large",
                        BASE_HTTP + "#name-413-content-too-large");
        STATUS_ENTRIES[414] =
                new StatusEntry(
                        CLIENT_ERROR_REQUEST_URI_TOO_LONG,
                        "The server is refusing to service the request because the request URI is longer than the server is willing to interpret",
                        "Request URI Too Long",
                        BASE_HTTP + "#name-414-uri-too-long");
        STATUS_ENTRIES[415] =
                new StatusEntry(
                        CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
                        "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method",
                        "Unsupported Media Type",
                        BASE_HTTP + "#name-415-unsupported-media-type");
        STATUS_ENTRIES[416] =
                new StatusEntry(
                        CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE,
                        "For byte ranges, this means that the first byte position were greater than the current length of the selected resource",
                        "Requested Range Not Satisfiable",
                        BASE_HTTP + "#name-416-range-not-satisfiable");
        STATUS_ENTRIES[417] =
                new StatusEntry(
                        CLIENT_ERROR_EXPECTATION_FAILED,
                        "The expectation given in the request header could not be met by this server",
                        "Expectation Failed",
                        BASE_HTTP + "#name-417-expectation-failed");
        STATUS_ENTRIES[429] =
                new StatusEntry(
                        CLIENT_ERROR_TOO_MANY_REQUESTS,
                        "The server is refusing to service the request because the user has sent too many requests in a given amount of time (\\\"rate limiting\\\")",
                        "Too Many Requests",
                        BASE_ADDED_HTTP + "#section-4");
        STATUS_ENTRIES[500] =
                new StatusEntry(
                        SERVER_ERROR_INTERNAL,
                        "The server encountered an unexpected condition which prevented it from fulfilling the request",
                        "Internal Server Error",
                        BASE_HTTP + "#name-500-internal-server-error");
        STATUS_ENTRIES[501] =
                new StatusEntry(
                        SERVER_ERROR_NOT_IMPLEMENTED,
                        "The server does not support the functionality required to fulfill the request",
                        "Not Implemented",
                        BASE_HTTP + "#name-501-not-implemented");
        STATUS_ENTRIES[502] =
                new StatusEntry(
                        SERVER_ERROR_BAD_GATEWAY,
                        "The server, while acting as a gateway or proxy, received an invalid response from the upstream server it accessed in attempting to fulfill the request",
                        "Bad Gateway",
                        BASE_HTTP + "#name-502-bad-gateway");
        STATUS_ENTRIES[503] =
                new StatusEntry(
                        SERVER_ERROR_SERVICE_UNAVAILABLE,
                        "The server is currently unable to handle the request due to a temporary overloading or maintenance of the server",
                        "Service Unavailable",
                        BASE_HTTP + "#name-503-service-unavailable");
        STATUS_ENTRIES[504] =
                new StatusEntry(
                        SERVER_ERROR_GATEWAY_TIMEOUT,
                        "The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g., HTTP, FTP, LDAP) or some other auxiliary server (e.g., DNS) it needed to access in attempting to complete the request",
                        "Gateway Timeout",
                        BASE_HTTP + "#name-504-gateway-timeout");
        STATUS_ENTRIES[505] =
                new StatusEntry(
                        SERVER_ERROR_VERSION_NOT_SUPPORTED,
                        "The server does not support, or refuses to support, the protocol version that was used in the request message",
                        "Version Not Supported",
                        BASE_HTTP + "#name-505-http-version-not-suppor");
        STATUS_ENTRIES[1000] =
                new StatusEntry(
                        CONNECTOR_ERROR_CONNECTION,
                        "The connector failed to connect to the server",
                        "Connection Error",
                        BASE_RESTLET + "org/restlet/data/Status.html#CONNECTOR_ERROR_CONNECTION");
        STATUS_ENTRIES[1001] =
                new StatusEntry(
                        CONNECTOR_ERROR_COMMUNICATION,
                        "The connector failed to complete the communication with the server",
                        "Communication Error",
                        BASE_RESTLET
                                + "org/restlet/data/Status.html#CONNECTOR_ERROR_COMMUNICATION");
        STATUS_ENTRIES[1002] =
                new StatusEntry(
                        CONNECTOR_ERROR_INTERNAL,
                        "The connector encountered an unexpected condition which prevented it from fulfilling the request",
                        "Internal Connector Error",
                        BASE_RESTLET + "org/restlet/data/Status.html#CONNECTOR_ERROR_INTERNAL");
    }

    /** The specification code. */
    private final int code;

    /** The longer description. */
    private final String description;

    /** The short reason phrase displayed next to the status code in an HTTP response. */
    private final String reasonPhrase;

    /** The related error or exception. */
    private final Throwable throwable;

    /** The URI of the specification describing the method. */
    private final String uri;

    /**
     * Constructor.
     *
     * @param code The specification code.
     */
    public Status(int code) {
        this(code, null, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param code The specification code.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     */
    public Status(int code, String reasonPhrase) {
        this(code, null, reasonPhrase, null, null);
    }

    /**
     * Constructor.
     *
     * @param code The specification code.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     * @param description The longer description.
     */
    public Status(int code, String reasonPhrase, String description) {
        this(code, null, reasonPhrase, description, null);
    }

    /**
     * Constructor.
     *
     * @param code The specification code.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     * @param description The longer description.
     * @param uri The URI of the specification describing the method.
     */
    public Status(int code, String reasonPhrase, String description, String uri) {
        this(code, null, reasonPhrase, description, uri);
    }

    /**
     * Constructor.
     *
     * @param code The specification code.
     * @param throwable The related error or exception.
     */
    public Status(int code, Throwable throwable) {
        this(code, throwable, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param code The specification code.
     * @param throwable The related error or exception.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     */
    public Status(int code, Throwable throwable, String reasonPhrase) {
        this(code, throwable, reasonPhrase, null, null);
    }

    /**
     * Constructor.
     *
     * @param code The specification code.
     * @param throwable The related error or exception.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     * @param description The longer description.
     */
    public Status(int code, Throwable throwable, String reasonPhrase, String description) {
        this(code, throwable, reasonPhrase, description, null);
    }

    /**
     * Constructor.
     *
     * @param code The specification code.
     * @param throwable The related error or exception.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     * @param description The longer description.
     * @param uri The URI of the specification describing the method.
     */
    public Status(
            int code, Throwable throwable, String reasonPhrase, String description, String uri) {
        this.code = code;
        this.throwable = throwable;
        this.reasonPhrase = checkReasonPhrase(reasonPhrase);
        this.description = description;
        this.uri = uri;
    }

    /**
     * Constructor.
     *
     * @param status The status to copy.
     * @param description The description to associate.
     */
    public Status(Status status, String description) {
        this(status, null, null, description);
    }

    /**
     * Constructor.
     *
     * @param status The status to copy.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     * @param description The description to associate.
     */
    public Status(Status status, String reasonPhrase, String description) {
        this(status, null, reasonPhrase, description);
    }

    /**
     * Constructor.
     *
     * @param status The status to copy.
     * @param throwable The related error or exception.
     */
    public Status(Status status, Throwable throwable) {
        this(status, throwable, null, null);
    }

    /**
     * Constructor.
     *
     * @param status The status to copy.
     * @param throwable The related error or exception.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     */
    public Status(Status status, Throwable throwable, String reasonPhrase) {
        this(status, throwable, reasonPhrase, null);
    }

    /**
     * Constructor.
     *
     * @param status The status to copy.
     * @param throwable The related error or exception.
     * @param reasonPhrase The short reason phrase displayed next to the status code in an HTTP
     *     response.
     * @param description The description to associate.
     */
    public Status(Status status, Throwable throwable, String reasonPhrase, String description) {
        this(
                status.getCode(),
                (throwable == null) ? status.getThrowable() : throwable,
                (reasonPhrase == null) ? status.getReasonPhrase() : reasonPhrase,
                (description == null) ? status.getDescription() : description,
                status.getUri());
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Status that)) {
            return false;
        }
        return this.code == that.getCode();
    }

    /**
     * Returns the corresponding code (HTTP or custom code).
     *
     * @return The corresponding code.
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Returns the description. This value is typically used by the {@link
     * org.restlet.service.StatusService} to build a meaningful description of an error via a
     * response entity.
     *
     * @return The description.
     */
    public String getDescription() {
        if (this.description != null) {
            return this.description;
        }
        final StatusEntry statusEntry = getStatusEntry(this.code);
        return (statusEntry == null) ? null : statusEntry.description;
    }

    /**
     * Returns the reason phrase of this status. When supported by the HTTP server connector, this
     * is returned in the first line of the HTTP response, next to the status code.
     *
     * @return The reason phrase of this status.
     */
    public String getReasonPhrase() {
        if (this.reasonPhrase != null) {
            return this.reasonPhrase;
        }
        final StatusEntry statusEntry = getStatusEntry(this.code);
        return (statusEntry == null) ? null : statusEntry.reasonPhrase;
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
        final StatusEntry statusEntry = getStatusEntry(this.code);
        return (statusEntry == null) ? null : statusEntry.uri;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return getCode();
    }

    /**
     * Indicates if the status is a client error status, meaning "The request contains bad syntax or
     * cannot be fulfilled".
     *
     * @return True if the status is a client error status.
     */
    public boolean isClientError() {
        return isClientError(getCode());
    }

    /**
     * Indicates if the status is a connector error status, meaning "The connector failed to send or
     * receive an apparently valid message".
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
     * Indicates if the status is a global error status, meaning "The server has definitive
     * information about a particular user".
     *
     * @return True if the status is a global error status.
     */
    public boolean isGlobalError() {
        return isGlobalError(getCode());
    }

    /**
     * Indicates if the status is an information status, meaning "request received, continuing
     * process".
     *
     * @return True if the status is an information status.
     */
    public boolean isInformational() {
        return isInformational(getCode());
    }

    /**
     * Indicates if an error is recoverable, meaning that simply retrying after a delay could result
     * in a success. Tests {@link #isConnectorError()} and if the status is {@link
     * #CLIENT_ERROR_REQUEST_TIMEOUT} or {@link #SERVER_ERROR_GATEWAY_TIMEOUT} or {@link
     * #SERVER_ERROR_SERVICE_UNAVAILABLE}.
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
     * Indicates if the status is a redirection status, meaning "Further action must be taken to
     * complete the request".
     *
     * @return True if the status is a redirection status.
     */
    public boolean isRedirection() {
        return isRedirection(getCode());
    }

    /**
     * Indicates if the status is a server error status, meaning "The server failed to fulfill an
     * apparently valid request".
     *
     * @return True if the status is a server error status.
     */
    public boolean isServerError() {
        return isServerError(getCode());
    }

    /**
     * Indicates if the status is a success status, meaning "The action was successfully received,
     * understood, and accepted".
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
        return getReasonPhrase()
                + " ("
                + this.code
                + ")"
                + ((getDescription() == null) ? "" : " - " + getDescription());
    }
}
