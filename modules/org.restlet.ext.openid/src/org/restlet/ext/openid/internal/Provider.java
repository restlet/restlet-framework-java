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

package org.restlet.ext.openid.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.server.ServerManager;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.Engine;
import org.restlet.ext.openid.AttributeExchange;
import org.restlet.ext.openid.internal.ProviderResult.OPR;

/**
 * Describes the OpenID provider, also known as an OP.
 * 
 * @author Martin Svensson
 */
public class Provider {

    public enum OpenIdMode {
        associate, check_authentication, checkid_immediate, checkid_setup, errorMode;
    }

    public static final String OPENID_MODE = "openid.mode";

    public static final String OPENID_REALM = "openid.realm";

    public static final String OPENID_RETURNTO = "openid.return_to";

    private final Map<String, UserSession> sessions = new HashMap<String, UserSession>();

    public Message fetchAttributes(ParameterList pl) throws Exception {
        if (pl == null)
            return null;
        Message m = Message.createMessage(pl);
        if (m.hasExtension(AxMessage.OPENID_NS_AX)) {
            return m;
        }
        return null;
    }

    public Message fetchAttributes(UserSession us) throws Exception {
        return fetchAttributes(us.getParameterList());
    }

    public Set<AttributeExchange> getAttributes(ParameterList pl,
            boolean required) throws Exception {
        Message m = fetchAttributes(pl);
        if (m == null)
            return null;
        MessageExtension me = m.getExtension(AxMessage.OPENID_NS_AX);
        if (me instanceof FetchRequest) {
            FetchRequest fr = (FetchRequest) me;
            Map<?, ?> attrs = fr.getAttributes(required);
            Set<AttributeExchange> toRet = new TreeSet<AttributeExchange>();
            for (Object key : attrs.keySet()) {
                String type = (String) attrs.get(key);
                AttributeExchange ax = AttributeExchange.valueOfType(type);
                if (ax != null)
                    toRet.add(ax);
            }
            return toRet;
        }
        return null;
    }

    public Logger getLogger() {
        Logger result = null;

        Context context = Context.getCurrent();

        if (context != null) {
            result = context.getLogger();
        }

        if (result == null) {
            result = Engine.getLogger(this, "org.restlet.ext.openid.OP");
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    public Map getOptionalAttributes(Message m) throws Exception {
        FetchRequest req = (FetchRequest) m
                .getExtension(AxMessage.OPENID_NS_AX);
        return req.getAttributes(false);
    }

    public Set<AttributeExchange> getOptionalAttributes(ParameterList pl)
            throws Exception {
        return getAttributes(pl, false);
    }

    public Set<AttributeExchange> getOptionalAttributes(UserSession us)
            throws Exception {
        return getAttributes(us.getParameterList(), false);
    }

    @SuppressWarnings("rawtypes")
    public Map getRequiredAttributes(Message m) throws Exception {
        FetchRequest req = (FetchRequest) m
                .getExtension(AxMessage.OPENID_NS_AX);
        return req.getAttributes(true);
    }

    public Set<AttributeExchange> getRequiredAttributes(ParameterList pl)
            throws Exception {
        return getAttributes(pl, true);
    }

    public Set<AttributeExchange> getRequiredAttributes(UserSession us)
            throws Exception {
        return getAttributes(us.getParameterList(), true);
    }

    public UserSession getSession(String sessionId) {
        return this.sessions.get(sessionId);
    }

    public ProviderResult processOPRequest(ServerManager sm, ParameterList pl,
            Request req, Response res, UserSession us) {
        String modeParam = null;// pl.getParameterValue(OPENID_MODE);
        OpenIdMode mode = null;
        Message response;

        if (pl == null && us != null) {
            pl = us.getParameterList();

        }
        try {
            mode = OpenIdMode.valueOf(pl.getParameterValue(OPENID_MODE));
        } catch (Exception e) {
            Engine.getAnonymousLogger().warning(
                    "Unknown openid.mode: " + modeParam);
            mode = OpenIdMode.errorMode;
        }
        Engine.getAnonymousLogger().info("processRequest: " + mode);
        switch (mode) {
        case associate:
            response = sm.associationResponse(pl);
            return new ProviderResult(OPR.OK, response.keyValueFormEncoding());
        case checkid_setup:
        case checkid_immediate:
            if (us == null || us.getUser() == null) { // this means no
                                                      // authorization
                // has taken place yet
                String session = UUID.randomUUID().toString();
                this.sessions.put(session, new UserSession(pl));
                return new ProviderResult(OPR.GET_USER, session);
            }
            OpenIdUser user = us.getUser();
            response = sm.authResponse(pl, user.getClaimedId(),
                    user.getClaimedId(), user.getApproved());
            // add any attributes:

            if (response instanceof DirectError) {
                return new ProviderResult(OPR.OK,
                        response.keyValueFormEncoding());
            }
            if (us.getUser().attributes() != null
                    && us.getUser().attributes().size() > 0) {
                FetchResponse fr = null;
                fr = FetchResponse.createFetchResponse();
                for (AttributeExchange attr : us.getUser().attributes()) {
                    String val = us.getUser().getAXValue(attr);
                    if (val != null) {
                        try {
                            fr.addAttribute(attr.getName(), attr.getSchema(),
                                    val);
                        } catch (MessageException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
                if (fr.getAttributes().size() > 0) {
                    try {
                        response.addExtension(fr);
                        sm.sign((AuthSuccess) response);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
            res.redirectSeeOther(response.getDestinationUrl(true));
            return new ProviderResult(OPR.OK, "");
        case check_authentication:
            response = sm.verify(pl);
            return new ProviderResult(OPR.OK, response.keyValueFormEncoding());
        case errorMode:
            response = DirectError.createDirectError("Unknown request");
            return new ProviderResult(OPR.OK, response.keyValueFormEncoding());
        }
        return null;
    }

}
