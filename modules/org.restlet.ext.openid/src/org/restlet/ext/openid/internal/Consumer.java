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

package org.restlet.ext.openid.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * OpenID consumer representation implementing an open identity RP.
 * 
 * @author Kristoffer Gronowski
 */
@Deprecated
public class Consumer extends ServerResource {

    public static final ConcurrentHashMap<String, String> ax = new ConcurrentHashMap<String, String>(
            9);

    public static final String DESCRIPTOR_COOKIE = "openid-disc";

    private static Discovery discovery = new Discovery();

    private static ConcurrentHashMap<String, ConsumerManager> managers = new ConcurrentHashMap<String, ConsumerManager>();

    private static ConcurrentHashMap<String, Object> session = new ConcurrentHashMap<String, Object>();

    static {
        ax.put("nickname", "http://axschema.org/namePerson/friendly");
        ax.put("email", "http://axschema.org/contact/email"); // "http://schema.openid.net/contact/email"
        ax.put("fullname", "http://axschema.org/namePerson");
        ax.put("dob", "http://axschema.org/birthDate");
        ax.put("gender", "http://axschema.org/person/gender");
        ax.put("postcode", "http://axschema.org/contact/postalCode/home");
        ax.put("country", "http://axschema.org/contact/country/home");
        ax.put("language", "http://axschema.org/pref/language");
        ax.put("timezone", "http://axschema.org/pref/timezone");
    }

    // public ConsumerManager manager;
    private Logger log;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        log = getLogger();

        // ConcurrentMap<String,Object> attribs = getContext().getAttributes();
        // manager = (ConsumerManager) attribs.get("consumer_manager");
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
        log.fine("Getting consumer manager for - " + OPUri);
        if (!managers.containsKey(OPUri)) {
            // create a new manager
            log.fine("Creating new consumer manager for - " + OPUri);
            // try {
            ConsumerManager cm = new ConsumerManager();
            cm.setConnectTimeout(30000);
            cm.setSocketTimeout(30000);
            cm.setFailedAssocExpire(0); // sec 0 = disabled
            // cm.setMaxAssocAttempts(4); //default
            managers.put(OPUri, cm);
            return cm;
            /*
             * } catch (ConsumerException e) {
             * log.warning("Failed to create ConsumerManager for - " + OPUri); }
             * return null;
             */
        } else {
            return managers.get(OPUri);
        }
    }

    // Used for RP discovery
    @Override
    protected Representation head() throws ResourceException {
        getLogger().fine("IN head() OpenIDResource");
        setXRDSHeader();
        getLogger().fine("Sending empty representation.");
        return new EmptyRepresentation();
    }

    @Get("html")
    public Representation represent() {
        Form params = getQuery();
        log.fine("OpenIDResource : " + params);

        String rc = params.getFirstValue("return");
        if (rc != null && rc.length() > 0) {
            Map<String, String> axRequired = new HashMap<String, String>();
            Map<String, String> axOptional = new HashMap<String, String>();
            Identifier i = verifyResponse(axRequired, axOptional);
            if (i == null) {
                log.warning("Authentication Failed");
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
                return new StringRepresentation("Authentication Failed");
            }
            log.fine("Identifier = " + i.getIdentifier());
            String id = i.getIdentifier();
            if (id != null) {
                // New Code, always return JSON and let filter handle any
                // callback.
                // TODO maybe move it to use Principal.
                JSONObject obj = new JSONObject();
                try {
                    obj.put("id", i.getIdentifier());
                    for (String s : axRequired.keySet()) {
                        obj.put(s, axRequired.get(s));
                    }
                    for (String s : axOptional.keySet()) {
                        obj.put(s, axOptional.get(s));
                    }
                } catch (JSONException e) {
                    log.log(Level.WARNING, "Failed to get the ID!", e);
                }

                getResponse().setEntity(new JsonRepresentation(obj));
            }
            // cleanup of cookie
            getResponse().getCookieSettings().remove(DESCRIPTOR_COOKIE);
            CookieSetting disc = new CookieSetting(DESCRIPTOR_COOKIE, "");
            disc.setMaxAge(0);
            getResponse().getCookieSettings().add(disc);
            // TODO save the identifier // send back to OAuth
            return getResponse().getEntity();
        }

        String target = params.getFirstValue("openid_identifier");
        if (target == null || target.length() == 0) {
            // No target - might be Yadis discovery
            String location = setXRDSHeader();
            StringBuilder html = new StringBuilder();
            html.append("<html><head><meta http-equiv=\"X-XRDS-Location\" content=\"");
            html.append(location);
            html.append("\"/></head></html>");
            return new StringRepresentation(html.toString(),
                    MediaType.TEXT_HTML);
        }

        try {
            StringBuilder returnToUrl = new StringBuilder();
            returnToUrl.append(getReference().getBaseRef());
            returnToUrl.append("?return=true");

            // --- Forward proxy setup (only if needed) ---
            // ProxyProperties proxyProps = new ProxyProperties();
            // proxyProps.setProxyName("proxy.example.com");
            // proxyProps.setProxyPort(8080);
            // HttpClientFactory.setProxyProperties(proxyProps);

            // perform discovery on the user-supplied identifier

            List<?> discoveries = null;
            discoveries = discovery.discover(target);
            for (Object o : discoveries) {
                if (o instanceof DiscoveryInformation) {
                    DiscoveryInformation di = (DiscoveryInformation) o;
                    log.fine("Found - " + di.getOPEndpoint());
                    target = di.getOPEndpoint().toString();
                }
            }

            ConsumerManager manager = getManager(target);
            // try {
            // discoveries = manager.discover(target);
            // } catch (YadisException e) {
            // log.info("Could not connect in time!!!!!!!!!!!!!!!!!!!!!!");
            // return new
            // StringRepresentation("Could not connect to Identity Server in time.",MediaType.TEXT_HTML);
            // }

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = manager.associate(discoveries);

            // store the discovery information in the user's session
            // getContext().getAttributes().put("openid-disc", discovered);
            String sessionId = String.valueOf(System
                    .identityHashCode(discovered));
            session.put(sessionId, discovered);

            getResponse().getCookieSettings().add(
                    new CookieSetting(DESCRIPTOR_COOKIE, sessionId));
            log.fine("Setting DESCRIPTOR COOKIE");

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = manager.authenticate(discovered,
                    returnToUrl.toString()); // TODO maybe add TIMESTAMP?
            // Domain wide realm add meta to main page
            // http://localhost:8080/oauth/xrds?returnTo=http://localhost:8080/oauth/openid_login\r\n
            // log.info("OpenID - REALM = " +
            // getReference().getHostIdentifier());
            // authReq.setRealm(getReference().getHostIdentifier().toString());
            log.fine("OpenID - REALM = " + getReference().getBaseRef());
            authReq.setRealm(getReference().getBaseRef().toString());

            // Attribute Exchange - getting optional and required
            FetchRequest fetch = null;
            String[] optional = params.getValuesArray("ax_optional", true);
            for (String o : optional) {
                if (!ax.containsKey(o)) {
                    log.warning("Not supported AX extension : " + o);
                    continue;
                }
                if (fetch == null)
                    fetch = FetchRequest.createFetchRequest();
                fetch.addAttribute(o, ax.get(o), false);
            }

            String[] required = params.getValuesArray("ax_required", true);
            for (String r : required) {
                if (!ax.containsKey(r)) {
                    log.warning("Not supported AX extension : " + r);
                    continue;
                }
                if (fetch == null)
                    fetch = FetchRequest.createFetchRequest();
                fetch.addAttribute(r, ax.get(r), true);
            }

            if (fetch != null) {
                authReq.addExtension(fetch);
            }

            if (!discovered.isVersion2()) {
                log.fine("REDIRECTING TEMPORARY");
                // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                // The only method supported in OpenID 1.x
                // redirect-URL usually limited ~2048 bytes
                redirectTemporary(authReq.getDestinationUrl(true));
                return null;
            } else {
                // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)

                Form msg = new Form();
                for (Object key : authReq.getParameterMap().keySet()) {
                    msg.add(key.toString(),
                            authReq.getParameterValue(key.toString()));
                    log.fine("Adding to form - key " + key.toString()
                            + " : value"
                            + authReq.getParameterValue(key.toString()));
                }

                // Redirector dispatcher = new Redirector(getContext(),
                // authReq.getOPEndpoint(),
                // Redirector.MODE_SERVER_OUTBOUND);
                // Request req = getRequest();
                // req.setEntity(msg.getWebRepresentation());
                // req.setMethod(Method.POST);
                // dispatcher.handle(getRequest(), getResponse());

                return generateForm(authReq);
            }
        } catch (DiscoveryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MessageException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConsumerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return getResponse().getEntity();
    }

    /**
     * Update the requests headers with OPENID dedicated HTTP headers. Returns
     * the capability document associated with the identity URI.
     * 
     * @return The capability document associated with the identity URI.
     */
    private String setXRDSHeader() {
        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        Reference xrds = new Reference(attribs.get("xrds").toString());
        if ("localhost".equals(xrds.getHostDomain())) {
            // make sure to use the same NIC as original request
            xrds.setHostDomain(getReference().getBaseRef().getHostDomain());
            xrds.setHostPort(getReference().getBaseRef().getHostPort());
        }
        String returnTo = getReference().getBaseRef().toString();
        String location = (returnTo != null) ? xrds.toString() + "?returnTo="
                + returnTo : xrds.toString();
        getLogger().fine("XRDS endpoint = " + xrds);
        Form headers = (Form) getResponse().getAttributes().get(
                "org.restlet.http.headers");
        if (headers == null) {
            headers = new Form();
            headers.add("X-XRDS-Location", location);
            getResponse().getAttributes().put("org.restlet.http.headers",
                    headers);
        } else {
            headers.add("X-XRDS-Location", location);
        }
        return location;
    }

    // --- processing the authentication response ---
    @SuppressWarnings("unchecked")
    public Identifier verifyResponse(Map<String, String> axRequired,
            Map<String, String> axOptional) {
        try {
            Logger.getLogger("").setLevel(Level.FINEST);
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response = new ParameterList(getQuery()
                    .getValuesMap());
            log.fine("response = " + response);

            // retrieve the previously stored discovery information
            log.fine("GET COOKIES");
            String openidDisc = getCookies().getFirstValue(DESCRIPTOR_COOKIE);
            // String openidDisc =
            // getCookieSettings().getFirstValue(DESCRIPTOR_COOKIE);
            log.fine("openIdDiscServer - "
                    + getCookieSettings().getFirstValue("DESCRIPTOR_COOKIE"));
            log.fine("openIdDiscServerLength -" + getCookieSettings().size());
            log.fine("openIdDiscClient - " + openidDisc);
            log.fine("openIdDiscClientLength -" + getCookies().size());

            if (getCookieSettings().size() > 0) {
                for (CookieSetting setting : getCookieSettings()) {
                    log.fine("CookieSetting: " + setting.getName()
                            + setting.getValue());
                }
            }
            if (getCookies().size() > 0) {
                for (Cookie setting : getCookies()) {
                    log.fine("Cookie: " + setting.getName()
                            + setting.getValue());
                }
            }

            DiscoveryInformation discovered = (DiscoveryInformation) session
                    .get(openidDisc); // TODO cleanup

            log.fine("discovered = " + discovered);

            // extract the receiving URL from the HTTP request

            log.fine("getOriginalRef = " + getOriginalRef());

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            // StringBuilder receivingURL = new StringBuilder();
            // receivingURL.append(getOriginalRef().getScheme(true));
            // receivingURL.append(':');
            // receivingURL.append(getOriginalRef().getSchemeSpecificPart(true));
            // log.info("receivingURL = "+receivingURL);

            log.fine("OpenID disc : " + discovered.getOPEndpoint());
            log.fine("OpenID orig ref : " + getOriginalRef());
            ConsumerManager manager = getManager(discovered.getOPEndpoint()
                    .toString());

            VerificationResult verification = manager.verify(getOriginalRef()
                    .toString(), response, discovered);
            log.fine("verification = " + verification);

            // examine the verification result and extract the verified
            // identifier
            Identifier verified = verification.getVerifiedId();
            log.fine("verified = " + verified);
            if (verified != null) {
                AuthSuccess authSuccess = (AuthSuccess) verification
                        .getAuthResponse();

                if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                    FetchRequest fetchResp = (FetchRequest) authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);

                    MessageExtension ext = authSuccess
                            .getExtension(AxMessage.OPENID_NS_AX);
                    if (ext instanceof FetchRequest) {
                        // FetchRequest fetchReq = (FetchRequest) ext;
                        Map<String, String> required = fetchResp
                                .getAttributes(true);
                        axRequired.putAll(required);
                        Map<String, String> optional = fetchResp
                                .getAttributes(false);
                        axOptional.putAll(optional);
                    }
                }

                return verified; // success
            }
        } catch (OpenIDException e) {
            log.log(Level.INFO, "", e);
        }
        log.setLevel(Level.INFO);
        return null;
    }

}
