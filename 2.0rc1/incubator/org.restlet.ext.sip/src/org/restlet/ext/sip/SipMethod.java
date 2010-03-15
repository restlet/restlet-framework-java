/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.sip;

import org.restlet.data.Method;

/**
 * Constants for SIP methods.
 * 
 * @author Jerome Louvel
 */
public final class SipMethod {

    private static final String BASE_SIP = "http://tools.ietf.org/html/rfc3261";

    /**
     * Confirms that the client has received a final response to an INVITE
     * request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-17.1.1.3">SIP
     *      RFC - 17.1.1.3 ACK</a>
     */
    public static final Method ACK = new Method("ACK",
            "Acknowledgment message", BASE_SIP + "#section-17.1.1.3", false,
            false, false);

    /**
     * Terminates a call and can be sent by either the caller or the callee.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-15.1">SIP RFC -
     *      15.1 BYE</a>
     */
    public static final Method BYE = new Method("BYE", "Terminates a call",
            BASE_SIP + "#section-15.1");

    /**
     * Cancels any pending request.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-9">SIP RFC - 9
     *      CANCEL</a>
     */
    public static final Method CANCEL = new Method("CANCEL",
            "Cancels any pending request", BASE_SIP + "#section-9");

    /**
     * Indicates a client is being invited to participate in a call session.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-17">SIP RFC - 17
     *      INVITE</a>
     */
    public static final Method INVITE = new Method("INVITE",
            "Call session invitation", BASE_SIP + "#section-17");

    /**
     * Queries the capabilities of servers.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-11">SIP RFC - 11
     *      OPTIONS</a>
     */
    public static final Method OPTIONS = new Method("OPTIONS",
            "Capabilities query", BASE_SIP + "#section-11");

    /**
     * Registers the address listed in the To header field with a SIP server.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc3261#section-10">SIP RFC - 10
     *      REGISTER</a>
     */
    public static final Method REGISTER = new Method("REGISTER",
            "User registration", BASE_SIP + "#section-10");

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
            } else if (name.equalsIgnoreCase(INVITE.getName())) {
                result = INVITE;
            } else if (name.equalsIgnoreCase(OPTIONS.getName())) {
                result = OPTIONS;
            } else if (name.equalsIgnoreCase(REGISTER.getName())) {
                result = REGISTER;
            } else {
                result = new Method(name);
            }
        }

        return result;
    }
}
