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

package org.restlet.ext.openid;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.ext.openid.internal.OpenIdConsumer;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
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
 * 
 */
public class OpenIdVerifier implements Verifier {

    public enum AX {
        country, dob, email, first, friendly, fullname, gender, language, last, postcode, timezone;
    }

    public static final ConcurrentHashMap<AX, String> ax = new ConcurrentHashMap<AX, String>(
            9);

    private static final Discovery discovery = new Discovery();

    private static final ConcurrentHashMap<String, ConsumerManager> managers = new ConcurrentHashMap<String, ConsumerManager>();

    public static final String PROVIDER_FLICKR = "http://flickr.com";

    public static final String PROVIDER_GOOGLE = "https://www.google.com/accounts/o8/id";

    public static final String PROVIDER_MYOPENID = "https://www.myopenid.com/";

    public static final String PROVIDER_MYSPACE = "http://api.myspace.com/openid";

    public static final String PROVIDER_YAHOO = "http://me.yahoo.com";

    private static final ConcurrentHashMap<String, Object> session = new ConcurrentHashMap<String, Object>();

    static {
        ax.put(AX.friendly, "http://axschema.org/namePerson/friendly");
        ax.put(AX.email, "http://axschema.org/contact/email"); // "http://schema.openid.net/contact/email"
        ax.put(AX.first, "http://axschema.org/namePerson/first");
        ax.put(AX.last, "http://axschema.org/namePerson/last");
        ax.put(AX.fullname, "http://axschema.org/namePerson");
        ax.put(AX.dob, "http://axschema.org/birthDate");
        ax.put(AX.gender, "http://axschema.org/person/gender");
        ax.put(AX.postcode, "http://axschema.org/contact/postalCode/home");
        ax.put(AX.country, "http://axschema.org/contact/country/home");
        ax.put(AX.language, "http://axschema.org/pref/language");
        ax.put(AX.timezone, "http://axschema.org/pref/timezone");
    }

    private volatile String defaultProvider;

    private final Set<AX> optionalAttributes;

    private final Set<AX> requiredAttributes;

    private volatile boolean useDefault = false;

    /**
     * Default constructor.
     */
    public OpenIdVerifier() {
        optionalAttributes = new HashSet<AX>();
        requiredAttributes = new HashSet<AX>();
    }

    /**
     * Constructor with a default OpenIdProvider/Identifier.
     * 
     * @param defaultProvider
     *            The default OpenIdProvider/Identifier.
     */
    public OpenIdVerifier(String defaultProvider) {
        this();
        setDefaultProvider(defaultProvider);
    }

    /**
     * Adds required User attribute to retrieve during authentication.
     * 
     * @param attributeName
     *            The name of the attribute. See valid attributes.
     */
    public void addOptionalAttribute(AX attributeName) {
        this.optionalAttributes.add(attributeName);
    }

    /**
     * Adds an optional User attribute to retrieve during authentication
     * 
     * @param attributeName
     *            The name of the attribute. See valid attributes.
     */
    public void addRequiredAttribute(AX attributeName) {
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
     * Returns the representation of an authentication form, in this case, a
     * simple HTML form.
     * 
     * @param authReq
     *            The authentication request.
     * @return The representation of an authentication form.
     */
    private Representation generateForm(AuthRequest authReq) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>OpenID HTML FORM Redirection</title>");
        sb.append("</head>");
        sb.append("<body onload=\"document.forms['openid-form-redirection'].submit();\">");
        sb.append("<form name=\"openid-form-redirection\" action=\"");
        sb.append(authReq.getOPEndpoint());
        sb.append("\" method=\"post\" accept-charset=\"utf-8\">");
        for (Object key : authReq.getParameterMap().keySet()) {
            sb.append(" <input type=\"hidden\" name=\"");
            sb.append(key.toString());
            // ${parameter.key}
            sb.append("\" value=\"");
            sb.append(authReq.getParameterMap().get(key));
            sb.append("\"/>");
        }
        sb.append("</form>");
        sb.append("</body>");
        sb.append("</html>");
        return new StringRepresentation(sb.toString(), MediaType.TEXT_HTML);
    }

    /**
     * Returns the {@link ConsumerManager} corresponding to the given URI.
     * 
     * @param OPUri
     *            The OP endpoint URI.
     * @return The {@link ConsumerManager} corresponding to the given URI.
     */
    private ConsumerManager getManager(String OPUri) {
        Logger l = Context.getCurrentLogger();
        l.info("Getting consumer manager for - " + OPUri);
        if (!managers.containsKey(OPUri)) {
            // create a new manager
            l.info("Creating new consumer manager for - " + OPUri);
            try {
                ConsumerManager cm = new ConsumerManager();
                cm.setConnectTimeout(30000);
                cm.setSocketTimeout(30000);
                cm.setFailedAssocExpire(0); // sec 0 = disabled
                // cm.setMaxAssocAttempts(4); //default
                managers.put(OPUri, cm);
                return cm;
            } catch (ConsumerException e) {
                l.warning("Failed to create ConsumerManager for - " + OPUri);
            }
            return null;
        } else {
            return managers.get(OPUri);
        }
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
            Context.getCurrentLogger().info("no target or return specified");
        }
        return target;
    }

    /**
     * Handles the return of an OpenID authentication request. Basically, it
     * extracts the user identifier, and the attributes to be exchanged from the
     * response.
     * 
     * @param queryParams
     *            The query's parameters.
     * @param request
     *            The authentication request.
     * @param response
     *            The authentication response.
     * @return An instance of {@link JSONObject} that contains the user
     *         identifier, and the attributes to be exchanged.
     */
    private JSONObject handleReturn(Form queryParams, Request request,
            Response response) {
        JSONObject obj = null;
        Logger l = Context.getCurrentLogger();
        // Map<String, String> axRequired = new HashMap<String, String>();
        Map<AX, String> axResp = new HashMap<AX, String>();
        Identifier i = verifyResponse(queryParams, axResp, request, response);
        if (i == null) {
            l.info("Authentication Failed");
            return obj;
        }
        l.info("Identifier = " + i.getIdentifier());
        String id = i.getIdentifier();

        if (id != null) {
            // New Code, always return JSON and let filter handle any
            // callback.
            // TODO maybe move it to use Principal.
            obj = new JSONObject();
            try {
                obj.put("id", i.getIdentifier());
                for (AX s : axResp.keySet()) {
                    obj.put(s.toString(), axResp.get(s));
                }
            } catch (JSONException e) {
                l.log(Level.WARNING, "Failed to get the ID!", e);
            }

        }
        // cleanup of cookie
        response.getCookieSettings().remove(OpenIdConsumer.DESCRIPTOR_COOKIE);
        CookieSetting disc = new CookieSetting(
                OpenIdConsumer.DESCRIPTOR_COOKIE, "");
        disc.setMaxAge(0);
        response.getCookieSettings().add(disc);
        return obj;
    }

    /**
     * Creates and sends an authentication request.
     * 
     * @param target
     *            The target.
     * @param params
     *            The set of params (not used yet).
     * @param request
     *            The original authenticated request.
     * @param response
     *            The response to complete.
     * @throws Exception
     */
    private void handleTarget(String target, Form params, Request request,
            Response response) throws Exception {
        Logger l = Context.getCurrentLogger();

        // create return to url
        String redir = request.getResourceRef().getHostIdentifier()
                + request.getResourceRef().getPath() + "?return=true";

        List<?> discoveries = null;
        discoveries = discovery.discover(target);
        for (Object o : discoveries) {
            if (o instanceof DiscoveryInformation) {
                DiscoveryInformation di = (DiscoveryInformation) o;
                l.info("Found - " + di.getOPEndpoint());
                target = di.getOPEndpoint().toString();
            }
        }

        ConsumerManager manager = getManager(target);
        DiscoveryInformation discovered = manager.associate(discoveries);

        // store the discovery information in the user's session
        // getContext().getAttributes().put("openid-disc", discovered);
        String sessionId = String.valueOf(System.identityHashCode(discovered));
        session.put(sessionId, discovered);

        response.getCookieSettings().add(
                new CookieSetting(OpenIdConsumer.DESCRIPTOR_COOKIE, sessionId));
        l.info("Setting DESCRIPTOR COOKIE");

        // obtain a AuthRequest message to be sent to the OpenID provider
        AuthRequest authReq = manager.authenticate(discovered, redir); // TODO
                                                                       // maybe
                                                                       // add
                                                                       // TIMESTAMP?;
        String ref = request.getResourceRef().getBaseRef().toString();
        l.info("OpenID - REALM = " + ref);
        authReq.setRealm(ref);

        // Attribute Exchange - getting optional and required
        FetchRequest fetch = null;
        // String[] optional = params.getValuesArray("ax_optional", true);
        for (AX o : this.optionalAttributes) {
            if (fetch == null)
                fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute(o.toString(), ax.get(o), false);
        }

        // String[] required = params.getValuesArray("ax_required", true);
        for (AX r : this.requiredAttributes) {
            if (fetch == null)
                fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute(r.toString(), ax.get(r), true);
        }

        if (fetch != null) {
            authReq.addExtension(fetch);
        }

        if (!discovered.isVersion2()) {
            l.info("OpenId - Http Redirect");
            response.redirectTemporary(authReq.getDestinationUrl(true));

        } else {
            l.info("OpenId - HTML Form Redirect");
            Form msg = new Form();
            for (Object key : authReq.getParameterMap().keySet()) {
                msg.add(key.toString(),
                        authReq.getParameterValue(key.toString()));
                l.info("Adding to form - key " + key.toString() + " : value"
                        + authReq.getParameterValue(key.toString()));
            }
            response.setEntity(generateForm(authReq));
            // response.redirectTemporary(authReq.getDestinationUrl(true));
        }

    }

    /**
     * Checks if it looks like a response to an OpenID authentication request.
     * 
     * @param queryParams
     *            The list of parameters.
     * @return True if the parameters ensure that it looks like a response to an
     *         OpenID authentication request.
     */
    private boolean isResponse(Form queryParams) {
        String rc = queryParams.getFirstValue("return");
        Context.getCurrentLogger().info("isReturn - " + rc);
        if (rc != null)
            return true;
        return false;
    }

    /**
     * Updates the given user with the values of the specified list of
     * attributes taken from the given {@link JSONObject}.
     * 
     * @param attributeSet
     *            The set of attributes.
     * @param object
     *            The source object.
     * @param user
     *            The User instance to update.
     */
    private void setAttributes(Set<AX> attributeSet, JSONObject object,
            User user) {
        if (attributeSet.size() < 1)
            return;
        try {
            if (attributeSet.contains(AX.email)
                    && !object.isNull(AX.email.toString())) {
                user.setEmail(object.getString(AX.email.toString()));
            }
            if (attributeSet.contains(AX.first)
                    && !object.isNull(AX.first.toString())) {
                user.setFirstName(object.getString(AX.first.toString()));
            }
            if (attributeSet.contains(AX.last)
                    && !object.isNull(AX.last.toString())) {
                user.setLastName(object.getString(AX.last.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        if (this.isResponse(params)) {
            JSONObject obj = this.handleReturn(params, request, response);

            if (obj == null) {
                return Verifier.RESULT_INVALID;
            } else {
                if (!obj.isNull("id")) {
                    try {
                        Context.getCurrentLogger().info(obj.toString(2));
                        String id = obj.getString("id");
                        User u = new User();
                        u.setIdentifier(id);
                        request.getClientInfo().setUser(u);
                        // set any attributes
                        setAttributes(optionalAttributes, obj, u);
                        setAttributes(requiredAttributes, obj, u);
                        return Verifier.RESULT_VALID;

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                Context.getCurrentLogger().info("Could not find identifier");
                return Verifier.RESULT_INVALID;
            }
        }

        String target = this.getTarget(params, request);

        if (target != null) {
            try {
                this.handleTarget(target, params, request, response);
                return Verifier.RESULT_MISSING;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Context.getCurrentLogger()
                .info("No Target or Return - reporting error");
        return Verifier.RESULT_INVALID;
    }

    // --- processing the authentication response ---
    @SuppressWarnings("unchecked")
    public Identifier verifyResponse(Form params, Map<AX, String> axResp,
            Request request, Response resp) {
        Logger l = Context.getCurrentLogger();

        try {
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response = new ParameterList(params.getValuesMap());
            l.info("response = " + response);

            // retrieve the previously stored discovery information
            l.info("GET COOKIES");
            String openidDisc = request.getCookies().getFirstValue(
                    OpenIdConsumer.DESCRIPTOR_COOKIE);
            // String openidDisc =
            // getCookieSettings().getFirstValue(DESCRIPTOR_COOKIE);
            l.info("openIdDiscServer - "
                    + resp.getCookieSettings().getFirstValue(
                            "DESCRIPTOR_COOKIE"));
            l.info("openIdDiscServerLength -" + resp.getCookieSettings().size());
            l.info("openIdDiscClient - " + openidDisc);
            l.info("openIdDiscClientLength -" + request.getCookies().size());

            if (resp.getCookieSettings().size() > 0) {
                for (CookieSetting setting : resp.getCookieSettings()) {
                    l.info("CookieSetting: " + setting.getName()
                            + setting.getFirst());
                }
            }

            if (request.getCookies().size() > 0) {
                for (Cookie setting : request.getCookies()) {
                    l.info("Cookie: " + setting.getName() + setting.getFirst());
                }
            }

            DiscoveryInformation discovered = (DiscoveryInformation) session
                    .get(openidDisc); // TODO cleanup

            l.info("discovered = " + discovered);

            // extract the receiving URL from the HTTP request

            l.info("getOriginalRef = " + request.getOriginalRef());

            ConsumerManager manager = getManager(discovered.getOPEndpoint()
                    .toString());
            String redir = request.getResourceRef().getHostIdentifier()
                    + request.getResourceRef().getPath() + "?return=true";

            VerificationResult verification = manager.verify(redir, response,
                    discovered);

            // examine the verification result and extract the verified
            // identifier
            Identifier verified = verification.getVerifiedId();
            l.info("verified = " + verified);

            if (verified != null) {
                AuthSuccess authSuccess = (AuthSuccess) verification
                        .getAuthResponse();

                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    FetchResponse fetchResp = (FetchResponse) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);

                    MessageExtension ext = authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);

                    if (ext instanceof FetchResponse) {
                        List<String> aliases = fetchResp.getAttributeAliases();
                        for (String alias : aliases) {
                            String value = fetchResp.getAttributeValue(alias);
                            axResp.put(AX.valueOf(alias), value);
                        }
                    }
                }

                return verified; // success
            }
        } catch (OpenIDException e) {
            l.log(Level.INFO, "", e);
        }
        return null;
    }

}
