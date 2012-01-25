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

import java.util.HashMap;
import java.util.Map;

/**
 * Describes SIP agent capabilities. Used by the SIP "supported", "unsupported",
 * "proxy-require", "require" headers.
 * 
 * @author Thierry Boileau
 */
public class OptionTag {

    /**
     * The known option tags registered with {@link #register(String)},
     * retrievable using {@link #valueOf(String)}.<br>
     * Keep the underscore for the ordering.
     */
    private static volatile Map<String, OptionTag> _optionTags = null;

    /**
     * Indicates support of the "Answer mode" extension.
     * 
     * @see <a http://tools.ietf.org/html/rfc5373">Requesting Answering Modes
     *      for SIP</a>
     */
    public static final OptionTag ANSWER_MODE = register("answermode");

    /**
     * "Early session" content-disposition type.
     * 
     * @see <a http://tools.ietf.org/html/rfc3959">The Early Session Disposition
     *      Type for SIP</a>
     */
    public static final OptionTag EARLY_SESSION = register("early-session");

    /**
     * Extension for the registry to allow subscriptions to lists of resources.
     * 
     * @see <a http://tools.ietf.org/html/rfc4662">A Session Initiation Protocol
     *      (SIP) Event Notification Extension for Resource Lists</a>
     */
    public static final OptionTag EVENT_LIST = register("eventlist");

    /**
     * Used to indicate that a UA supports changes to URIs in From and To header
     * fields during a dialog.
     * 
     * @see <a http://tools.ietf.org/html/rfc4916">Connected Identity in the
     *      SIP</a>
     */
    public static final OptionTag FROM_CHANGE = register("from-change");

    /**
     * Used to identify the Globally Routable User Agent URI (GRUU) extension.
     * 
     * @see <a http://tools.ietf.org/html/rfc5627">Obtaining and Using Globally
     *      Routable User Agent URIs (GRUUs) in the SIP</a>
     */
    public static final OptionTag GRUU = register("gruu");

    /**
     * When used with the Supported header, indicates support for the History
     * Information to be captured for requests and returned in subsequent
     * responses.
     * 
     * @see <a http://tools.ietf.org/html/rfc4244">An Extension to the SIP for
     *      Request History Information </a>
     */
    public static final OptionTag HIST_INFO = register("histinfo");

    /**
     * Used to identify the Interactive Connectivity Establishment (ICE)
     * extension. When present in a Require header field, it indicates that ICE
     * is required by an agent.
     * 
     * @see <a http://tools.ietf.org/html/draft-ietf-sip-ice-option-tag-02">An
     *      Extension to the SIP for Request History Information</a>
     */
    public static final OptionTag ICE = register("ice");

    /**
     * Support for the SIP Join Header.
     * 
     * @see <a http://tools.ietf.org/html/rfc3911">The SIP "Join" Header</a>
     */
    public static final OptionTag JOIN = register("join");

    /**
     * Indicates support for REFER requests that contain a resource list
     * document describing multiple REFER targets.
     * 
     * @see <a http://tools.ietf.org/html/rfc5368">Referring to Multiple
     *      Resources in the SIP</a>
     */
    public static final OptionTag MULTIPLE_REFER = register("multiple-refer");

    /**
     * Specifies a User Agent ability of accepting a REFER request without
     * establishing an implicit subscription
     * 
     * @see <a http://tools.ietf.org/html/rfc4488">Suppression of SIP REFER
     *      Method Implicit Subscription</a>
     */
    public static final OptionTag NO_REFER_SUB = register("norefersub");

    /**
     * Used to identify UAs and Registrars which support extensions for Client
     * Initiated Connections
     * 
     * @see <a http://tools.ietf.org/html/rfc5626">Managing Client-Initiated
     *      Connections in the SIP</a>
     */
    public static final OptionTag OUTBOUND = register("outbound");

    /**
     * Indicates the support of the Path extension.
     * 
     * @see <a http://tools.ietf.org/html/rfc3327">SIP Extension Header Field
     *      for Registering Non-Adjacent Contacts</a>
     */
    public static final OptionTag PATH = register("path");

    /**
     * An offerer MUST include this tag in the Require header field if the offer
     * contains one or more "mandatory" strength-tags.
     * 
     * @see <a http://tools.ietf.org/html/rfc3312">Integration of Resource
     *      Management and SIP</a>
     */
    public static final OptionTag PRECONDITION = register("precondition");

    /**
     * Used to ensure that a server understands the callee capabilities
     * parameters used in the request.
     * 
     * @see <a http://tools.ietf.org/html/rfc3840">Indicating User Agent
     *      Capabilities in the SIP</a>
     */
    public static final OptionTag PREF = register("pref");

    /**
     * Indicates support for the Privacy mechanism.
     * 
     * @see <a http://tools.ietf.org/html/rfc3323">A Privacy Mechanism for the
     *      SIP</a>
     */
    public static final OptionTag PRIVACY = register("privacy");

    /**
     * The body contains a list of URIs that indicates the recipients of the SIP
     * INVITE request.
     * 
     * @see <a http://tools.ietf.org/html/rfc5366">Conference Establishment
     *      Using Request-Contained Lists in the SIP</a>
     */
    public static final OptionTag RECIPIENT_LIST_INVITE = register("recipient-list-invite");

    /**
     * The body contains a list of URIs that indicates the recipients of the SIP
     * MESSAGE request.
     * 
     * @see <a http://tools.ietf.org/html/rfc5365">Multiple-Recipient MESSAGE
     *      Requests in the SIP</a>
     */
    public static final OptionTag RECIPIENT_LIST_MESSAGE = register("recipient-list-message");

    /**
     * Used to ensurethat a server can process the recipient-list body used in a
     * SUBSCRIBE request.
     * 
     * @see <a http://tools.ietf.org/html/rfc5367">Subscriptions to
     *      Request-Contained Resource Lists</a>
     */
    public static final OptionTag RECIPIENT_LIST_SUBSCRIBE = register("recipient-list-subscribe");

    /**
     * Indicates support for the SIP Replaces header.
     * 
     * @see <a http://tools.ietf.org/html/rfc3891">the SIP "Replaces" Header</a>
     */
    public static final OptionTag REPLACES = register("replaces");

    /**
     * Indicates or requests support for the resource priority mechanism.
     * 
     * @see <a http://tools.ietf.org/html/rfc4412">Communications Resource
     *      Priority for the SIP</a>
     */
    public static final OptionTag RESOURCE_PRIORITY = register("resource-priority");

    /**
     * When used in a Supported header field it indicates the support of the
     * ANAT semantics.
     * 
     * @see <a http://tools.ietf.org/html/rfc4092">Usage of the Session
     *      Description Protocol (SDP) Alternative Network Address Types (ANAT)
     *      Semantics in the SIP</a>
     */
    public static final OptionTag SDP_ANAT = register("sdp-anat");

    /**
     * Indicates support for the Security Agreement mechanism.
     * 
     * @see <a http://tools.ietf.org/html/rfc3329">Security Mechanism Agreement
     *      for the SIP</a>
     */
    public static final OptionTag SEC_AGREE = register("sec-agree");

    /**
     * Used to identify the target dialog header field extension.
     * 
     * @see <a http://tools.ietf.org/html/rfc4538">Request Authorization through
     *      Dialog Identification in the SIP</a>
     */
    public static final OptionTag T_DIALOG = register("tdialog");

    /**
     * Used for reliability of provisional responses.
     * 
     * @see <a http://tools.ietf.org/html/rfc3262">Reliability of Provisional
     *      Responses in the SIP</a>
     */
    public static final OptionTag TAG_100REL = register("100rel");

    /**
     * Indicates the support of the session timer extension.
     * 
     * @see <a http://tools.ietf.org/html/rfc4028">Session Timers in the SIP</a>
     */
    public static final OptionTag TIMER = register("timer");

    /**
     * Returns the known option tags map.
     * 
     * @return the known option tags map.
     */
    private static Map<String, OptionTag> getOptionTags() {
        if (_optionTags == null) {
            _optionTags = new HashMap<String, OptionTag>();
        }
        return _optionTags;
    }

    /**
     * Register an option tag that can later be retrieved using
     * {@link #valueOf(String)}. If the option tag already exists, the existing
     * tag is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The registered option tag.
     */
    public static synchronized OptionTag register(String name) {
        if (!getOptionTags().containsKey(name)) {
            final OptionTag tag = new OptionTag(name);
            getOptionTags().put(name, tag);
        }

        return getOptionTags().get(name);
    }

    /**
     * Returns the option tag associated to a name. If an existing constant
     * exists then it is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The associated option tag.
     */
    public static OptionTag valueOf(String name) {
        OptionTag result = null;

        if ((name != null) && !name.equals("")) {
            result = getOptionTags().get(name);
            if (result == null) {
                result = new OptionTag(name);
            }
        }

        return result;
    }

    /** The tag value. */
    private String tag;

    /**
     * Constructor.
     * 
     * @param tag
     *            The tag value.
     */
    public OptionTag(String tag) {
        super();
        this.tag = tag;
    }

    /**
     * Returns the tag value.
     * 
     * @return The tag value.
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag value.
     * 
     * @param tag
     *            The tag value.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

}
