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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
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
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Describes a relaying party, also known as a RP.
 * 
 * @author Martin Svensson
 */
public class RelayingParty {

    private static Representation getForm(AuthRequest authReq) {
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

    private final ConsumerManager cm;

    private final Map<String, DiscoveryInformation> sessions;

    public RelayingParty() {
        this(new ConsumerManager());
    }

    public RelayingParty(ConsumerManager cm) {
        this.cm = cm;
        this.sessions = new HashMap<String, DiscoveryInformation>();
    }

    public String authRequest(String identifier, boolean sessionAware,
            boolean addReturnTo, String returnTo,
            Set<AttributeExchange> optionalAttrs,
            Set<AttributeExchange> requiredAttrs, Request req, Response res)
            throws Exception {

        List<?> discoveries = cm.discover(identifier);
        DiscoveryInformation di = cm.associate(discoveries);

        // return
        String sessionId = null;
        if (sessionAware && di != null) {
            getLogger().info("save discovery information to session");
            sessionId = UUID.randomUUID().toString();
            this.sessions.put(sessionId, di);
            Reference ref = new Reference(returnTo);
            ref.addQueryParameter("sessionId", sessionId);
            returnTo = ref.toString();
        }
        if (addReturnTo) {
            Reference ref = new Reference(returnTo);
            ref.addQueryParameter("returnTo", "true");
            returnTo = ref.toString();
        }
        AuthRequest authReq = cm.authenticate(di, returnTo);
        FetchRequest fetch = null;

        // add attributes
        if (optionalAttrs != null) {
            fetch = FetchRequest.createFetchRequest();
            for (AttributeExchange o : optionalAttrs) {
                fetch.addAttribute(o.getName(), o.getSchema(), false);
            }
        }
        if (requiredAttrs != null) {
            if (fetch == null)
                fetch = FetchRequest.createFetchRequest();
            for (AttributeExchange r : requiredAttrs) {
                fetch.addAttribute(r.getName(), r.getSchema(), true);
            }
        }
        if (fetch != null)
            authReq.addExtension(fetch);

        if (di != null && di.isVersion2()) {
            getLogger().info("sending auth request using OpenId 2 form");
            res.setEntity(getForm(authReq));
        } else {
            getLogger().info(
                    "sending auth request using OpenId 1 query parameters");
            res.redirectTemporary(authReq.getDestinationUrl(true));
        }
        return sessionId;
    }

    public Logger getLogger() {
        Logger result = null;

        Context context = Context.getCurrent();

        if (context != null) {
            result = context.getLogger();
        }

        if (result == null) {
            result = Engine.getLogger(this, "org.restlet.ext.openid.RP");
        }

        return result;
    }

    public boolean hasReturnTo(Request request) {
        String val = request.getResourceRef().getQueryAsForm()
                .getFirstValue("returnTo");
        return "true".equals(val) == true ? true : false;
    }

    public Identifier verify(Map<AttributeExchange, String> axResp,
            Request req, boolean sessionAware) throws Exception {

        // TODO: Make sure it can handle form based returns as well!
        Form params = req.getResourceRef().getQueryAsForm();
        ParameterList response = new ParameterList(params.getValuesMap());

        String sessionId = sessionAware ? params.getFirstValue("sessionId")
                : null;

        // retrieve the previously stored discovery information
        DiscoveryInformation discovered = sessionId != null ? sessions
                .get(sessionId) : null;
        getLogger().info(
                "retrieved discovery information from session: (" + sessionId
                        + ") " + discovered);

        String received = req.getResourceRef().getHostIdentifier()
                + req.getResourceRef().getPath();
        if (req.getResourceRef().hasQuery()) {
            received += "?" + req.getResourceRef().getQuery();
        }
        VerificationResult verification = cm.verify(received, response,
                discovered);
        Identifier verified = verification.getVerifiedId();
        if (verified != null) {
            AuthSuccess authSuccess = (AuthSuccess) verification
                    .getAuthResponse();

            if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
                FetchResponse fetchResp = (FetchResponse) authSuccess
                        .getExtension(AxMessage.OPENID_NS_AX);

                MessageExtension ext = authSuccess
                        .getExtension(AxMessage.OPENID_NS_AX);
                if (ext instanceof FetchResponse) {
                    @SuppressWarnings("unchecked")
                    List<String> aliases = (List<String>) fetchResp
                            .getAttributeAliases();
                    for (String alias : aliases) {
                        String value = fetchResp.getAttributeValue(alias);
                        axResp.put(AttributeExchange.valueOf(alias), value);
                    }
                }
            }
        }
        return verified;
    }

}
