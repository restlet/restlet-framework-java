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

package org.restlet.ext.oauth;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.data.CacheDirective;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.TokenGenerator;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Base class for common resources used by the OAuth server side.
 * 
 * @author Kristoffer Gronowski
 */
public abstract class OAuthServerResource extends ServerResource {

    public static final String ACCESS_TOKEN = "access_token";

    public static final String ASSERTION = "assertion";

    public static final String ASSERTION_TYPE = "assertion_type";

    public static final String AUTONOMOUS_USER = "__autonomous";

    public static final String CLIENT_ID = "client_id";

    public static final String CLIENT_SECRET = "client_secret";

    public static final String ClientCookieID = "_cid";

    public static final String CODE = "code";

    // public static final String OWNER = "owner"; //OAE Extension
    public static final String ERROR = "error";

    public static final String ERROR_DESC = "error_description";

    public static final String ERROR_URI = "error_uri";

    public static final String EXPIRES_IN = "expires_in";

    public static final String GRANT_TYPE = "grant_type";

    protected final static List<CacheDirective> noCache;

    protected final static List<CacheDirective> noStore;

    public static final String OAUTH_TOKEN = "oauth_token";

    public static final String PASSWORD = "password";

    public static final String REDIR_URI = "redirect_uri";

    public static final String REFRESH_TOKEN = "refresh_token";

    /**
     * MandatoryClient Request Authorization parameters.
     */
    public static final String RESPONSE_TYPE = "response_type";

    public static final String SCOPE = "scope";

    public static final String STATE = "state";

    public static final String TOKEN_SERVER_MAX_TIME_SEC = "_token_server_max_time_sec";

    public static final String TOKEN_SERVER_TIME_SEC = "_token_server_time_sec";

    public static final String USERNAME = "username";

    static {
        noStore = new ArrayList<CacheDirective>();
        noStore.add(CacheDirective.noStore());
        noCache = new ArrayList<CacheDirective>();
        noCache.add(CacheDirective.noCache());
    }

    protected volatile ClientStore<?> clients;

    protected volatile TokenGenerator generator;

    protected volatile long tokenMaxTimeSec = Token.UNLIMITED;

    protected volatile long tokenTimeSec = Token.UNLIMITED;

    /**
     * Default constructor.
     */
    public OAuthServerResource() {
        super();
    }

    /**
     * Completes the given {@link StringBuilder} with the authentication
     * attributes.
     * 
     * @param location
     *            The {@link StringBuilder} to complete.
     */
    private void appendState(StringBuilder location) {
        String sessionId = (String) getRequest().getAttributes().get(
                ClientCookieID);
        if (sessionId == null)
            sessionId = getCookies().getFirstValue(ClientCookieID);
        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        AuthSession session = (AuthSession) attribs.get(sessionId);
        String state = session.getState();
        if (state != null && state.length() > 0) {
            location.append("&state=");
            location.append(state);
        }
        session.reset();
    }

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();
        Context ctx = getContext();
        ConcurrentMap<String, Object> attribs = ctx.getAttributes();
        clients = ClientStoreFactory.getInstance();

        // NOT NEEDED I THINK:
        /*
         * clients = (ClientStore<?>) attribs.get(ClientStore.class
         * .getCanonicalName());
         */
        getLogger().fine("Found client store = " + clients);

        generator = clients.getTokenGenerator();
        getLogger().fine("Found token generator = " + generator);

        if (attribs.containsKey(TOKEN_SERVER_TIME_SEC)) {
            tokenTimeSec = (Long) attribs.get(TOKEN_SERVER_TIME_SEC);
        }

        if (attribs.containsKey(TOKEN_SERVER_MAX_TIME_SEC)) {
            tokenMaxTimeSec = (Long) attribs.get(TOKEN_SERVER_MAX_TIME_SEC);
        }
        generator.setMaxTokenTime(tokenMaxTimeSec);
    }

    /**
     * Returns the agent token for the given user, client and redirection URI.
     * 
     * @param userId
     *            The identifier of the user.
     * @param client
     *            The oAuth client.
     * @param redirURL
     *            The redirection URI.
     * @return The agent token for the given user, client and redirection URI.
     */
    protected String generateAgentToken(String userId, Client client,
            String redirURL) {
        AuthenticatedUser user = null;
        if (client.containsUser(userId)) {
            user = client.findUser(userId);
        } else {
            user = client.createUser(userId);
        }

        // TODO generate token and keep for a while.
        Token token = generator.generateToken(user, tokenTimeSec);
        StringBuilder location = new StringBuilder(redirURL);
        location.append("#access_token=").append(token.getToken());

        // TODO add expires
        appendState(location);

        // Sets the no-store Cache-Control header
        getResponse().setCacheDirectives(noStore);

        getLogger().fine("Redirecting to -> " + location.toString());
        // TODO add state to request string
        return location.toString();
    }

    /**
     * Returns the code for the given user, client and redirection URI.
     * 
     * @param userId
     *            The identifier of the user.
     * @param client
     *            The oAuth client.
     * @param redirURL
     *            The redirection URI.
     * @return The code for the given user, client and redirection URI.
     */
    protected String generateCode(String userId, Client client, String redirURL) {
        AuthenticatedUser user = null;
        if (client.containsUser(userId)) {
            user = client.findUser(userId);
        } else {
            user = client.createUser(userId);
        }

        // TODO generate code and keep for a while.
        String code = generator.generateCode(user);
        StringBuilder location = new StringBuilder(redirURL);
        String c = (location.indexOf("?") == -1) ? "?code=" : "&code=";
        location.append(c).append(code);
        appendState(location);

        // Sets the no-store Cache-Control header
        getResponse().setCacheDirectives(noStore);

        getLogger().fine("Redirecting to -> " + location.toString());
        // TODO add state to request string
        return location.toString();
    }

    /**
     * Returns the value of the first parameter found with the given name.
     * 
     * @param parameter
     *            The parameter name.
     * @param defaultValue
     *            The default value to return if no matching parameter found or
     *            if the parameter has a null value.
     * @return The value of the first parameter found with the given name or the
     *         default value.
     */
    protected String getParameter(String parameter, String defaultValue) {
        String val = (String) this.getContext().getAttributes().get(parameter);
        return val != null ? val : defaultValue;
    }

    /**
     * Parses a " " separated list of scopes into an array.
     * 
     * @param scopes
     *            The " " separated list of scopes.
     * @return The corresponding list of string of characters.
     */
    protected String[] parseScope(String scopes) {
        if (scopes != null && scopes.length() > 0) {
            StringTokenizer st = new StringTokenizer(scopes, " ");
            String[] scope = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++)
                scope[i] = st.nextToken();
            return scope;
        }
        return new String[0];
    }
}
