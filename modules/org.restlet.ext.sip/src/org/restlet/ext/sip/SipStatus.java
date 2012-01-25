/**
 * Copyright 2005-2012 Restlet S.A.S.
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
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.sip;

import org.restlet.data.Status;

/**
 * Constants for SIP statuses.
 * 
 * @author Jerome Louvel
 */
public final class SipStatus {

    private static final String BASE_SIP = "http://tools.ietf.org/html/rfc3261#section-21";

    /**
     * This response indicates that the request has been received by the
     * next-hop server and that some unspecified action is being taken on behalf
     * of this call.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.1.1">SIP RFC
     *      - 21.1.1 100 Trying</a>
     */
    public static final Status INFO_TRYING = new Status(
            100,
            "Trying",
            "The request has been received and some unspecified action is being taken",
            BASE_SIP + ".1.1");

    /**
     * The UA receiving the INVITE is trying to alert the user. This response
     * MAY be used to initiate local ringback.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.1.2">SIP RFC
     *      - 21.1.2 180 Ringing</a>
     */
    public static final Status INFO_RINGING = new Status(180, "Ringing",
            "The UA receiving the INVITE is trying to alert the user", BASE_SIP
                    + ".1.2");

    /**
     * A server MAY use this status code to indicate that the call is being
     * forwarded to a different set of destinations.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.1.3">SIP RFC
     *      - 21.1.3 181 Call Is Being Forwarded</a>
     */
    public static final Status INFO_CALL_IS_BEING_FORWARDED = new Status(181,
            "Call Is Being Forwarded",
            "The call is being forwarded to a different set of destinations",
            BASE_SIP + ".1.3");

    /**
     * The called party is temporarily unavailable, but the server has decided
     * to queue the call rather than reject it. When the callee becomes
     * available, it will return the appropriate final status response.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.1.4">SIP RFC
     *      - 21.1.4 182 Queued</a>
     */
    public static final Status INFO_QUEUED = new Status(182, "Queued",
            "The server has decided to queue the call rather than reject it",
            BASE_SIP + ".1.4");

    /**
     * Used to convey information about the progress of the call that is not
     * otherwise classified.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.1.5">SIP RFC
     *      - 21.1.5 183 Session Progress</a>
     */
    public static final Status INFO_SESSION_PROGRESS = new Status(
            183,
            "Session Progress",
            "Conveys information about the progress of the call that is not otherwise classified",
            BASE_SIP + ".1.5");

    /**
     * The request has succeeded. The information returned with the response
     * depends on the method used in the request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.2.1">SIP RFC
     *      - 21.2.1 200 Success</a>
     */
    public static final Status SUCCESS_OK = new Status(200, "Success",
            "The request has succeeded", BASE_SIP + ".2.1");

    /**
     * The address in the request resolved to several choices, each with its own
     * specific location, and the user (or UA) can select a preferred
     * communication end point and redirect its request to that location.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.3.1">SIP RFC
     *      - 21.3.1 300 Multiple Choices</a>
     */
    public static final Status REDIRECTION_MULTIPLE_CHOICES = new Status(300,
            "Multiple Choices",
            "The address in the request resolved to several choices", BASE_SIP
                    + ".3.1");

    /**
     * The user can no longer be found at the address in the Request-URI, and
     * the requesting client SHOULD retry at the new address given by the
     * Contact header field.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.3.2">SIP RFC
     *      - 21.3.2 301 Moved Permanently</a>
     */
    public static final Status REDIRECTION_MOVED_PERMANENTLY = new Status(301,
            "Moved Permanently",
            "The user can no longer be found at the given address", BASE_SIP
                    + ".3.2");

    /**
     * The requesting client SHOULD retry the request at the new address(es)
     * given by the Contact header field.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.3.3">SIP RFC
     *      - 21.3.3 302 Moved Temporarily</a>
     */
    public static final Status REDIRECTION_MOVED_TEMPORARILY = new Status(302,
            "Moved Temporarily",
            "Please retry the request at the new given address ", BASE_SIP
                    + ".3.3");

    /**
     * The requested resource MUST be accessed through the proxy given by the
     * Contact field.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.3.4">SIP RFC
     *      - 21.3.4 305 Use Proxy</a>
     */
    public static final Status REDIRECTION_USE_PROXY = new Status(305,
            "Use Proxy",
            "The requested resource MUST be accessed through the proxy",
            BASE_SIP + ".3.4");

    /**
     * The call was not successful, but alternative services are possible.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.3.5">SIP RFC
     *      - 21.3.5 380 Alternative Service</a>
     */
    public static final Status REDIRECTION_ALTERNATIVE_SERVICE = new Status(
            380,
            "Alternative Service",
            "The call was not successful, but alternative services are possible",
            BASE_SIP + ".3.5");

    /**
     * The request could not be understood due to malformed syntax.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.1">SIP RFC
     *      - 21.4.1 400 Bad Request</a>
     */
    public static final Status CLIENT_ERROR_BAD_REQUEST = new Status(400,
            "Bad Request",
            "The request could not be understood due to malformed syntax",
            BASE_SIP + ".4.1");

    /**
     * The request requires user authentication.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.2">SIP RFC
     *      - 21.4.2 401 Unauthorized</a>
     */
    public static final Status CLIENT_ERROR_UNAUTHORIZED = new Status(401,
            "Unauthorized", "The request requires user authentication",
            BASE_SIP + ".4.2");

    /**
     * Reserved for future use.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.3">SIP RFC
     *      - 21.4.3 402 Payment Required</a>
     */
    public static final Status CLIENT_ERROR_PAYMENT_REQUIRED = new Status(402,
            "Payment Required", "Reserved for future use", BASE_SIP + ".4.3");

    /**
     * The server understood the request, but is refusing to fulfill it.
     * Authorization will not help, and the request SHOULD NOT be repeated.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.4">SIP RFC
     *      - 21.4.4 403 Forbidden</a>
     */
    public static final Status CLIENT_ERROR_FORBIDDEN = new Status(403,
            "Forbidden",
            "The server understood the request, but is refusing to fulfill it",
            BASE_SIP + ".4.4");

    /**
     * The server has definitive information that the user does not exist at the
     * domain specified in the Request-URI. This status is also returned if the
     * domain in the Request-URI does not match any of the domains handled by
     * the recipient of the request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.5">SIP RFC
     *      - 21.4.5 404 Not Found</a>
     */
    public static final Status CLIENT_ERROR_NOT_FOUND = new Status(404,
            "Not Found", "", BASE_SIP + ".4.5");

    /**
     * The method specified in the Request-Line is understood, but not allowed
     * for the address identified by the Request-URI.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.6">SIP RFC
     *      - 21.4.6 405 Method Not Allowed</a>
     */
    public static final Status CLIENT_ERROR_METHOD_NOT_ALLOWED = new Status(
            405,
            "Method Not Allowed",
            "The method is understood, but not allowed for the given request URI",
            BASE_SIP + ".4.6");

    /**
     * The resource identified by the request is only capable of generating
     * response entities that have content characteristics not acceptable
     * according to the Accept header field sent in the request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.7">SIP RFC
     *      - 21.4.7 406 Not Acceptable</a>
     */
    public static final Status CLIENT_ERROR_NOT_ACCEPTABLE = new Status(406,
            "Not Acceptable",
            "The resource is not capable of generating acceptable entities",
            BASE_SIP + ".4.7");

    /**
     * This code is similar to 401 (Unauthorized), but indicates that the client
     * MUST first authenticate itself with the proxy.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.8">SIP RFC
     *      - 21.4.8 407 Proxy Authentication Required</a>
     */
    public static final Status CLIENT_ERROR_PROXY_AUTHENTIFICATION_REQUIRED = new Status(
            407, "Proxy Authentication Required",
            "The client must first authenticate itself with the proxy.",
            BASE_SIP + ".4.8");

    /**
     * The server could not produce a response within a suitable amount of time,
     * for example, if it could not determine the location of the user in time.
     * The client MAY repeat the request without modifications at any later
     * time.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.9">SIP RFC
     *      - 21.4.9 408 Request Timeout</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_TIMEOUT = new Status(
            408,
            "Request Timeout",
            "The server could not produce a response within a suitable amount of time",
            BASE_SIP + ".4.9");

    /**
     * The requested resource is no longer available at the server and no
     * forwarding address is known. This condition is expected to be considered
     * permanent.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.10">SIP RFC
     *      - 21.4.10 410 Gone</a>
     */
    public static final Status CLIENT_ERROR_GONE = new Status(410, "Gone",
            "The requested resource is no longer available", BASE_SIP + ".4.10");

    /**
     * The server is refusing to process a request because the request
     * entity-body is larger than the server is willing or able to process. The
     * server MAY close the connection to prevent the client from continuing the
     * request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.11">SIP RFC
     *      - 21.4.11 413 Request Entity Too Large</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE = new Status(
            413,
            "Request Entity Too Large",
            "The server is refusing to process because the entity-body is larger than the server is willing or able to process",
            BASE_SIP + ".4.11");

    /**
     * The server is refusing to service the request because the Request-URI is
     * longer than the server is willing to interpret.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.12">SIP RFC
     *      - 21.4.12 414 Request-URI Too Long</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_URI_TOO_LONG = new Status(
            414,
            "Request-URI Too Long",
            "The server is refusing to service the request because the Request-URI is too long",
            BASE_SIP + ".4.12");

    /**
     * The server is refusing to service the request because the message body of
     * the request is in a format not supported by the server for the requested
     * method.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.13">SIP RFC
     *      - 21.4.13 415 Unsupported Media Type</a>
     */
    public static final Status CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE = new Status(
            415,
            "Unsupported Media Type",
            "The server is refusing to service the request because the message body of the request is in a format not supported",
            BASE_SIP + ".4.13");

    /**
     * The server cannot process the request because the scheme of the URI in
     * the Request-URI is unknown to the server.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.14">SIP RFC
     *      - 21.4.14 416 Unsupported URI Scheme</a>
     */
    public static final Status CLIENT_ERROR_UNSUPPORTED_URI_SCHEME = new Status(
            416,
            "Unsupported URI Scheme",
            "The server cannot process the request because the scheme of the URI in the Request-URI is unknown to the server.",
            BASE_SIP + ".4.14");

    /**
     * The server did not understand the protocol extension specified in a
     * Proxy-Require or Require header field.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.15">SIP RFC
     *      - 21.4.15 420 Bad Extension</a>
     */
    public static final Status CLIENT_ERROR_BAD_EXTENSION = new Status(420,
            "Bad Extension",
            "The server did not understand the protocol extension", BASE_SIP
                    + ".4.15");

    /**
     * The UAS needs a particular extension to process the request, but this
     * extension is not listed in a Supported header field in the request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.16">SIP RFC
     *      - 21.4.16 421 Extension Required</a>
     */
    public static final Status CLIENT_ERROR_EXTENSION_REQUIRED = new Status(
            421, "Extension Required",
            "The UAS needs a particular extension to process the request",
            BASE_SIP + ".4.16");

    /**
     * The server is rejecting the request because the expiration time of the
     * resource refreshed by the request is too short. This response can be used
     * by a registrar to reject a registration whose Contact header field
     * expiration time was too small.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.17">SIP RFC
     *      - 21.4.17 423 Interval Too Brief</a>
     */
    public static final Status CLIENT_ERROR_INTERVAL_TOO_BRIEF = new Status(
            423,
            "Interval Too Brief",
            "The expiration time of the resource refreshed by the request is too short",
            BASE_SIP + ".4.17");

    /**
     * The callee's end system was contacted successfully but the callee is
     * currently unavailable (for example, is not logged in, logged in but in a
     * state that precludes communication with the callee, or has activated the
     * "do not disturb" feature).
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.18">SIP RFC
     *      - 21.4.18 480 Temporarily Unavailable</a>
     */
    public static final Status CLIENT_ERROR_TEMPORARILY_UNAVAILABLE = new Status(
            480, "Temporarily Unavailable",
            "The callee is currently unavailable", BASE_SIP + ".4.18");

    /**
     * Indicates that the UAS received a request that does not match any
     * existing dialog or transaction.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.19">SIP RFC
     *      - 21.4.19 481 Call/Transaction Does Not Exist</a>
     */
    public static final Status CLIENT_ERROR_CALL_DOESNT_EXIST = new Status(
            481,
            "Call/Transaction Does Not Exist",
            "The UAS received a request that does not match any existing dialog or transaction.",
            BASE_SIP + ".4.19");

    /**
     * The server has detected a loop.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.20">SIP RFC
     *      - 21.4.20 482 Loop Detected</a>
     */
    public static final Status CLIENT_ERROR_LOOP_DETECTED = new Status(482,
            "Loop Detected", "The server has detected a loop", BASE_SIP
                    + ".4.20");

    /**
     * The server received a request that contains a Max-Forwards header field
     * with the value zero.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.21">SIP RFC
     *      - 21.4.21 483 Too Many Hops</a>
     */
    public static final Status CLIENT_ERROR_TOO_MANY_HOPS = new Status(483,
            "Too Many Hops",
            "Max-Forwards header field received with the value zero", BASE_SIP
                    + ".4.21");

    /**
     * The server received a request with a Request-URI that was incomplete.
     * Additional information SHOULD be provided in the reason phrase.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.22">SIP RFC
     *      - 21.4.22 484 Address Incomplete</a>
     */
    public static final Status CLIENT_ERROR_ADDRESS_INCOMPLETE = new Status(
            484, "Address Incomplete", "The Request-URI was incomplete",
            BASE_SIP + ".4.23");

    /**
     * The Request-URI was ambiguous. The response MAY contain a listing of
     * possible unambiguous addresses in Contact header fields. Revealing
     * alternatives can infringe on privacy of the user or the organization.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.24">SIP RFC
     *      - 21.4.24 485 Ambiguous</a>
     */
    public static final Status CLIENT_ERROR_AMBIGUOUS = new Status(485,
            "Ambiguous", "The Request-URI was ambiguous", BASE_SIP + ".4.24");

    /**
     * The callee's end system was contacted successfully, but the callee is
     * currently not willing or able to take additional calls at this end
     * system.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.25">SIP RFC
     *      - 21.4.25 486 Busy Here</a>
     */
    public static final Status CLIENT_ERROR_BUSY_HERE = new Status(
            486,
            "Busy Here",
            "The callee is currently not willing or able to take additional calls",
            BASE_SIP + ".4.25");

    /**
     * The request was terminated by a BYE or CANCEL request. This response is
     * never returned for a CANCEL request itself.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.26">SIP RFC
     *      - 21.4.26 487 Request Terminated</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_TERMINATED = new Status(
            487, "Request Terminated",
            "The request was terminated by a BYE or CANCEL request", BASE_SIP
                    + ".4.26");

    /**
     * The response has the same meaning as 606 (Not Acceptable), but only
     * applies to the specific resource addressed by the Request-URI and the
     * request may succeed elsewhere.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.27">SIP RFC
     *      - 21.4.27 488 Not Acceptable Here</a>
     */
    public static final Status CLIENT_ERROR_NOT_ACCEPTABLE_HERE = new Status(
            488, "Not Acceptable Here",
            "Some aspects of the requested resource were not acceptable",
            BASE_SIP + ".4.27");

    /**
     * The request was received by a UAS that had a pending request within the
     * same dialog.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.28">SIP RFC
     *      - 21.4.28 491 Request Pending</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_PENDING = new Status(
            491,
            "Request Pending",
            "The request was received by a UAS that had a pending request within the same dialog",
            BASE_SIP + ".4.28");

    /**
     * The request was received by a UAS that contained an encrypted MIME body
     * for which the recipient does not possess or will not provide an
     * appropriate decryption key.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.4.29">SIP RFC
     *      - 21.4.29 493 Undecipherable</a>
     */
    public static final Status CLIENT_ERROR_REQUEST_UNDECIPHERABLE = new Status(
            493,
            "Undecipherable",
            "Encrypted MIME body for which the recipient does not possess or will not provide an appropriate decryption key",
            BASE_SIP + ".4.29");

    /**
     * The server encountered an unexpected condition that prevented it from
     * fulfilling the request. The client MAY display the specific error
     * condition and MAY retry the request after several seconds.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.5.1">SIP RFC
     *      - 21.5.1 500 Server Internal Error</a>
     */
    public static final Status SERVER_ERROR_INTERNAL = new Status(
            500,
            "Server Internal Error",
            "The server encountered an unexpected condition that prevented it from fulfilling the request",
            BASE_SIP + ".5.1");

    /**
     * The server does not support the functionality required to fulfill the
     * request. This is the appropriate response when a UAS does not recognize
     * the request method and is not capable of supporting it for any user.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.5.2">SIP RFC
     *      - 21.5.2 501 Not Implemented</a>
     */
    public static final Status SERVER_ERROR_NOT_IMPLEMENTED = new Status(
            501,
            "Not Implemented",
            "The server does not support the functionality required to fulfill the request",
            BASE_SIP + ".5.2");

    /**
     * The server, while acting as a gateway or proxy, received an invalid
     * response from the downstream server it accessed in attempting to fulfill
     * the request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.5.3">SIP RFC
     *      - 21.5.3 502 Bad Gateway</a>
     */
    public static final Status SERVER_ERROR_BAD_GATEWAY = new Status(
            502,
            "Bad Gateway",
            "The server received an invalid response from the downstream server",
            BASE_SIP + ".5.3");

    /**
     * The server is temporarily unable to process the request due to a
     * temporary overloading or maintenance of the server. The server MAY
     * indicate when the client should retry the request in a Retry-After header
     * field.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.5.4">SIP RFC
     *      - 21.5.4 503 Service Unavailable</a>
     */
    public static final Status SERVER_ERROR_SERVICE_UNAVAILABLE = new Status(
            503, "Service Unavailable",
            "The server is temporarily unable to process the request", BASE_SIP
                    + ".5.4");

    /**
     * The server did not receive a timely response from an external server it
     * accessed in attempting to process the request. 408 (Request Timeout)
     * should be used instead if there was no response within the period
     * specified in the Expires header field from the upstream server.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.5.5">SIP RFC
     *      - 21.5.5 504 Server Time-out</a>
     */
    public static final Status SERVER_ERROR_SERVER_TIMEOUT = new Status(
            504,
            "Server Time-out",
            "The server did not receive a timely response from an external server",
            BASE_SIP + ".5.5");

    /**
     * The server does not support, or refuses to support, the SIP protocol
     * version that was used in the request. The server is indicating that it is
     * unable or unwilling to complete the request using the same major version
     * as the client, other than with this error message.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.5.6">SIP RFC
     *      - 21.5.6 505 Version Not Supported</a>
     */
    public static final Status SERVER_ERROR_VERSION_NOT_SUPPORTED = new Status(
            505,
            "Version Not Supported",
            "The server does not support, or refuses to support, the SIP protocol version that was used",
            BASE_SIP + ".5.6");

    /**
     * The server was unable to process the request since the message length
     * exceeded its capabilities.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.5.7">SIP RFC
     *      - 21.5.7 513 Message Too Large</a>
     */
    public static final Status SERVER_ERROR_MESSAGE_TOO_LARGE = new Status(
            513,
            "Message Too Large",
            "The server was unable to process the request since the message length exceeded its capabilities",
            BASE_SIP + ".5.7");

    /**
     * The callee's end system was contacted successfully but the callee is busy
     * and does not wish to take the call at this time.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.6.1">SIP RFC
     *      - 21.6.1 600 Busy Everywhere</a>
     */
    public static final Status GLOBAL_ERROR_BUSY_EVERYWHERE = new Status(
            600,
            "Busy Everywhere",
            "The callee is busy and does not wish to take the call at this time",
            BASE_SIP + ".6.1");

    /**
     * The callee's machine was successfully contacted but the user explicitly
     * does not wish to or cannot participate.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.6.2">SIP RFC
     *      - 21.6.2 603 Decline</a>
     */
    public static final Status GLOBAL_ERROR_DECLINE = new Status(603,
            "Decline",
            "The user explicitly does not wish to or cannot participate",
            BASE_SIP + ".6.2");

    /**
     * The server has authoritative information that the user indicated in the
     * Request-URI does not exist anywhere.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.6.3">SIP RFC
     *      - 21.6.3 604 Does Not Exist Anywhere</a>
     */
    public static final Status GLOBAL_ERROR_DOESNT_EXIST_ANYWHERE = new Status(
            604, "Does Not Exist Anywhere",
            "The user indicated in the Request-URI does not exist anywhere",
            BASE_SIP + ".6.3");

    /**
     * The user's agent was contacted successfully but some aspects of the
     * session description such as the requested media, bandwidth, or addressing
     * style were not acceptable.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-21.6.4">SIP RFC
     *      - 21.6.4 606 Not Acceptable</a>
     */
    public static final Status GLOBAL_ERROR_NOT_ACCEPTABLE = new Status(606,
            "Not Acceptable",
            "Some aspects of the session description were not acceptable",
            BASE_SIP + ".6.4");

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
            result = INFO_TRYING;
            break;
        case 180:
            result = INFO_RINGING;
            break;
        case 181:
            result = INFO_CALL_IS_BEING_FORWARDED;
            break;
        case 182:
            result = INFO_QUEUED;
            break;
        case 183:
            result = INFO_SESSION_PROGRESS;
            break;

        case 200:
            result = SUCCESS_OK;
            break;

        case 300:
            result = REDIRECTION_MULTIPLE_CHOICES;
            break;
        case 301:
            result = REDIRECTION_MOVED_PERMANENTLY;
            break;
        case 302:
            result = REDIRECTION_MOVED_TEMPORARILY;
            break;
        case 305:
            result = REDIRECTION_USE_PROXY;
            break;
        case 380:
            result = REDIRECTION_ALTERNATIVE_SERVICE;
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
        case 410:
            result = CLIENT_ERROR_GONE;
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
            result = CLIENT_ERROR_UNSUPPORTED_URI_SCHEME;
            break;
        case 420:
            result = CLIENT_ERROR_BAD_EXTENSION;
            break;
        case 421:
            result = CLIENT_ERROR_EXTENSION_REQUIRED;
            break;
        case 423:
            result = CLIENT_ERROR_INTERVAL_TOO_BRIEF;
            break;
        case 480:
            result = CLIENT_ERROR_TEMPORARILY_UNAVAILABLE;
            break;
        case 481:
            result = CLIENT_ERROR_CALL_DOESNT_EXIST;
            break;
        case 482:
            result = CLIENT_ERROR_LOOP_DETECTED;
            break;
        case 483:
            result = CLIENT_ERROR_TOO_MANY_HOPS;
            break;
        case 484:
            result = CLIENT_ERROR_ADDRESS_INCOMPLETE;
            break;
        case 485:
            result = CLIENT_ERROR_AMBIGUOUS;
            break;
        case 486:
            result = CLIENT_ERROR_BUSY_HERE;
            break;
        case 487:
            result = CLIENT_ERROR_REQUEST_TERMINATED;
            break;
        case 488:
            result = CLIENT_ERROR_NOT_ACCEPTABLE_HERE;
            break;
        case 491:
            result = CLIENT_ERROR_REQUEST_PENDING;
            break;
        case 493:
            result = CLIENT_ERROR_REQUEST_UNDECIPHERABLE;
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
            result = SERVER_ERROR_SERVER_TIMEOUT;
            break;
        case 505:
            result = SERVER_ERROR_VERSION_NOT_SUPPORTED;
            break;
        case 513:
            result = SERVER_ERROR_MESSAGE_TOO_LARGE;
            break;

        case 600:
            result = GLOBAL_ERROR_BUSY_EVERYWHERE;
            break;
        case 603:
            result = GLOBAL_ERROR_DECLINE;
            break;
        case 604:
            result = GLOBAL_ERROR_DOESNT_EXIST_ANYWHERE;
            break;
        case 606:
            result = GLOBAL_ERROR_NOT_ACCEPTABLE;
            break;

        default:
            result = Status.valueOf(code);
        }

        return result;
    }

}
