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

package org.restlet.ext.openid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openid4java.discovery.Identifier;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.openid.internal.OpenIdUser;
import org.restlet.ext.openid.internal.XRDS;
import org.restlet.representation.Representation;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/**
 * Verifier that will do remote verification of using a provided
 * openid_identifier. The verifier will search for the openid_identifier in the
 * following three ways
 * <ol>
 * <li>Check the query for an openid_identifier
 * <li>Check the request attribute map for an openid_identifier
 * <li>Use the default openid_identifier if possible
 * </ol>
 * If an openid_identifier is found, it will do a temporary redirect (or return
 * a form) to the identifier to continue the authentication process. Upon
 * successful authentication the verifier will set the User.
 * <p>
 * The verifier can also try to request the following attributes to be returned
 * by the OpenIdProvider - setOptionalAttribute and setRequiredAttribute.
 * </p>
 * <ul>
 * <li>nickname
 * <li>email
 * <li>fullname
 * <li>dob
 * <li>gender
 * <li>postcode
 * <li>country
 * <li>language
 * <li>timezone
 * </ul>
 * 
 * @author Martin Svensson
 */
public class OpenIdVerifier implements Verifier {

    // Known OpenId Providers:
    public static final String PROVIDER_FLICKR = "http://flickr.com";

    public static final String PROVIDER_GOOGLE = "https://www.google.com/accounts/o8/id";

    public static final String PROVIDER_MYOPENID = "https://www.myopenid.com/";

    public static final String PROVIDER_MYSPACE = "http://api.myspace.com/openid";

    public static final String PROVIDER_YAHOO = "http://me.yahoo.com";

    private volatile String defaultProvider;

    private final Set<AttributeExchange> optionalAttributes;

    private final Set<AttributeExchange> requiredAttributes;

    private final RelayingParty rp;

    private volatile boolean useDefault = false;

    /**
     * Default constructor.
     */
    public OpenIdVerifier() {
        this(null, null);
    }

    /**
     * Constructor with a default OpenIdProvider/Identifier.
     * 
     * @param defaultProvider
     *            The default OpenIdProvider/Identifier.
     */
    public OpenIdVerifier(String defaultProvider) {
        this(defaultProvider, null);
    }

    public OpenIdVerifier(String defaultProvider, RelayingParty rp) {
        this.rp = (rp != null) ? rp : new RelayingParty();
        optionalAttributes = new HashSet<AttributeExchange>();
        requiredAttributes = new HashSet<AttributeExchange>();
        if (defaultProvider != null)
            setDefaultProvider(defaultProvider);
    }

    /**
     * Adds required User attribute to retrieve during authentication.
     * 
     * @param attributeName
     *            The name of the attribute. See valid attributes.
     */
    public void addOptionalAttribute(AttributeExchange attributeName) {
        this.optionalAttributes.add(attributeName);
    }

    /**
     * Adds an optional User attribute to retrieve during authentication
     * 
     * @param attributeName
     *            The name of the attribute. See valid attributes.
     */
    public void addRequiredAttribute(AttributeExchange attributeName) {
        this.requiredAttributes.add(attributeName);
    }

    /**
     * Clears the set of optional attributes to retrieve.
     */
    public void clearOptionalAttributes() {
        this.optionalAttributes.clear();
    }

    /**
     * Clears the set of required attributes to retrieve.
     */
    public void clearRequiredAttributes() {
        this.requiredAttributes.clear();
    }

    /**
     * Extracts the Target (openid identifier/provider) from a set of
     * parameters. First by looking at the query string, then in the request
     * attributes and finally if a default provider has been set
     * 
     * @param queryParams
     *            The set of parameters taken from the query string.
     * @param request
     *            The request.
     * @return The target (openid identifier/provider).
     */
    private String getTarget(Form queryParams, Request request) {
        String target = queryParams.getFirstValue("openid_identifier");
        if (target == null)
            target = (String) request.getAttributes().get("openid_identifier");
        if (target == null && useDefault)
            target = defaultProvider;
        if (target == null) {
            Context.getCurrentLogger().fine("no target or return specified");
        }
        return target;
    }

    /**
     * Sets the default provider. Will also set useDefaultProvider to true.
     */
    public void setDefaultProvider(String provider) {
        this.defaultProvider = provider;
        this.useDefault = true;
    }

    /**
     * Indicates if the defaultProvider must be used in case none is provided in
     * the request.
     * 
     * @param useDefault
     *            True if the defaultProvider must be used.
     */
    public void setUseDefaultProvider(boolean useDefault) {
        this.useDefault = useDefault;
    }

    /**
     * Verifies a request. The verifier will be called twice to verify a request
     * since verification is done remotely using callbacks. Also sets the user
     * object.
     * 
     * @return {@link Verifier#RESULT_INVALID} if it fails,
     *         {@link Verifier#RESULT_VALID} if success,
     *         {@link Verifier#RESULT_MISSING} while waiting for a callback
     *         response.
     */
    public int verify(Request request, Response response) {
        Form params = request.getResourceRef().getQueryAsForm();
        if (rp.hasReturnTo(request)) {
            Context.getCurrentLogger().info("handling return");
            Map<AttributeExchange, String> axResp = new HashMap<AttributeExchange, String>();
            try {
                Identifier identifier = rp.verify(axResp, request, true);
                // do some processing
                if (identifier != null && identifier.getIdentifier() != null) {
                    User u = new User(identifier.getIdentifier());
                    if (axResp.size() > 0) {
                        for (Map.Entry<AttributeExchange, String> entry : axResp
                                .entrySet()) {
                            OpenIdUser.setValueFromAX(entry.getKey(),
                                    entry.getValue(), u);
                        }
                    }
                    request.getClientInfo().setUser(u);
                    return RESULT_VALID;
                } else
                    return RESULT_INVALID;
            } catch (Exception e) { // assume rp discovery
                Reference ref = new Reference(request.getResourceRef()
                        .getHostIdentifier()
                        + request.getResourceRef().getPath());
                Context.getCurrentLogger().info("Generating XRDS Response");
                if (params.getFirst("sessionId") != null) {
                    ref.addQueryParameter("sessionId",
                            params.getFirstValue("sessionId"));
                    ref.addQueryParameter("return", "true");
                }
                try {
                    Representation rep = XRDS.returnToXrds(ref.toString());
                    response.setEntity(rep);
                    return RESULT_MISSING;
                } catch (Exception e1) {
                    // should not happen
                    e.printStackTrace();
                    return RESULT_UNKNOWN;
                }
            }

        } else {
            // generate a request
            String target = this.getTarget(params, request);
            Reference ref = new Reference(request.getResourceRef()
                    .getHostIdentifier() + request.getResourceRef().getPath());
            Context.getCurrentLogger().info(
                    "generating a authentication request");
            try {
                rp.authRequest(target, true, true, ref.toString(),
                        optionalAttributes, requiredAttributes, request,
                        response);
                return RESULT_MISSING;
            } catch (Exception e) {
                e.printStackTrace();
                return RESULT_INVALID;
            }
        }

    }

}
