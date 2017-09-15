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

package org.restlet.ext.sip;

import org.restlet.data.Method;

/**
 * Constants for SIP methods.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
public final class SipMethod {

    private static final String _BASE_SIP = "http://tools.ietf.org/html/rfc3261";

    private static final String _BASE_SIP_EVENT = "http://tools.ietf.org/html/rfc3903";

    private static final String _BASE_SIP_INFO = "http://tools.ietf.org/html/rfc2976";

    private static final String _BASE_SIP_NOTIFICATION = "http://tools.ietf.org/html/rfc3265";

    private static final String _BASE_SIP_REFER = "http://tools.ietf.org/html/rfc3515";

    /**
     * Confirms that the client has received a final response to an INVITE
     * request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-17.1.1.3">SIP
     *      RFC - 17.1.1.3 ACK</a>
     */
    public static final Method ACK = new Method("ACK",
            "Acknowledgment message", _BASE_SIP + "#section-17.1.1.3", false,
            false, false);

    /**
     * Terminates a call and can be sent by either the caller or the callee.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-15.1">SIP RFC -
     *      15.1 BYE</a>
     */
    public static final Method BYE = new Method("BYE", "Terminates a call",
            _BASE_SIP + "#section-15.1");

    /**
     * Cancels any pending request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-9">SIP RFC - 9
     *      CANCEL</a>
     */
    public static final Method CANCEL = new Method("CANCEL",
            "Cancels any pending request", _BASE_SIP + "#section-9");

    /**
     * Allow for the carrying of session related control information that is
     * generated during a session.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc2976">RFC 2976 - The SIP INFO
     *      Method</a>
     */
    public static final Method INFO = new Method("INFO", "Information message",
            _BASE_SIP_INFO);

    /**
     * Indicates a client is being invited to participate in a call session.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-17">SIP RFC - 17
     *      INVITE</a>
     */
    public static final Method INVITE = new Method("INVITE",
            "Call session invitation", _BASE_SIP + "#section-17");

    /**
     * Sent to inform subscribers of changes in state to which the subscriber
     * has a subscription.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3265#section-3.2">RFC 3265 -
     *      3.2. Description of NOTIFY Behavior</a>
     */
    public static final Method NOTIFY = new Method("NOTIFY",
            "Notification message", _BASE_SIP_NOTIFICATION + "#section-3.2");

    /**
     * Queries the capabilities of servers.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-11">SIP RFC - 11
     *      OPTIONS</a>
     */
    public static final Method OPTIONS = new Method("OPTIONS",
            "Capabilities query", _BASE_SIP + "#section-11");

    /**
     * Publishes event state.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3903#section-4">RFC 3903 - 4.
     *      Constructing PUBLISH Requests</a>
     */
    public static final Method PUBLISH = new Method("PUBLISH",
            "Publication message", _BASE_SIP_EVENT + "#section-4");

    /**
     * The recipient should refer to a resource provided in the Refer-To header
     * field of the request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3515#section-2">SIP REFER RFC
     *      - 2 REFER</a>
     */
    public static final Method REFER = new Method("REFER",
            "Contact a third-party", _BASE_SIP_REFER + "#section-2");

    /**
     * Registers the address listed in the To header field with a SIP server.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-10">SIP RFC - 10
     *      REGISTER</a>
     */
    public static final Method REGISTER = new Method("REGISTER",
            "User registration", _BASE_SIP + "#section-10");

    /**
     * Used to request current state and state updates from a remote node.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3265#section-3.1">RFC 3265 -
     *      3.1. Description of SUBSCRIBE Behavior</a>
     */
    public static final Method SUBSCRIBE = new Method("SUBSCRIBE",
            "Subscription message", _BASE_SIP_NOTIFICATION + "#section-3.1");

    /**
     * Returns the method associated to a given method name. If an existing
     * constant exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The method name.
     * @return The associated method.
     */
    public static Method valueOf(final String name) {
        Method result = null;

        if ((name != null) && !name.equals("")) {
            if (name.equalsIgnoreCase(ACK.getName())) {
                result = ACK;
            } else if (name.equalsIgnoreCase(BYE.getName())) {
                result = BYE;
            } else if (name.equalsIgnoreCase(CANCEL.getName())) {
                result = CANCEL;
            } else if (name.equalsIgnoreCase(INFO.getName())) {
                result = INFO;
            } else if (name.equalsIgnoreCase(INVITE.getName())) {
                result = INVITE;
            } else if (name.equalsIgnoreCase(NOTIFY.getName())) {
                result = NOTIFY;
            } else if (name.equalsIgnoreCase(OPTIONS.getName())) {
                result = OPTIONS;
            } else if (name.equalsIgnoreCase(PUBLISH.getName())) {
                result = PUBLISH;
            } else if (name.equalsIgnoreCase(REFER.getName())) {
                result = REFER;
            } else if (name.equalsIgnoreCase(REGISTER.getName())) {
                result = REGISTER;
            } else if (name.equalsIgnoreCase(SUBSCRIBE.getName())) {
                result = SUBSCRIBE;
            } else {
                result = new Method(name);
            }
        }

        return result;
    }
}
