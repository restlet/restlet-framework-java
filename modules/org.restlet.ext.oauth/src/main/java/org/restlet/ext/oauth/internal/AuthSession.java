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

package org.restlet.ext.oauth.internal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.ext.oauth.ResponseType;

/**
 * Helper class to establish an authentication session. The session is created
 * in the AuthorizationResource on initial OAuth request.
 * 
 * At the moment it is not being cleaned up on the server side.
 * 
 * The cookie that is set will get removed when the browser closes the window.
 * 
 * @author Kristoffer Gronowski
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class AuthSession {

    private static final String CALLBACK = "callback";

    private static final String CLIENT_ID = "client_id";

    public static final int DEFAULT_TIMEOUT_SEC = 600;

    private static final String FLOW = "flow";

    private static final String GRANTED_SCOPE = "granted_scope";

    private static final String ID = "id";

    private static final String LAST_ACTIVITY = "last_activity";

    private static final String OWNER = "owner";

    private static final String REQ_SCOPE = "requested_scope";

    private static final String STATE = "state";

    private static final String TIMEOUT_SEC = "timeout_sec";

    /**
     * Instantiate new authorization session.
     * 
     * @return a new authorization session.
     */
    public static AuthSession newAuthSession() {
        AuthSession session = new AuthSession();
        // XXX: Is UUID a non-guessable value? (10.12. Cross-Site Request
        // Forgery)
        String sessionId = UUID.randomUUID().toString();
        session.setAttribute(ID, sessionId);
        session.setAttribute(LAST_ACTIVITY, System.currentTimeMillis());
        session.setSessionTimeout(DEFAULT_TIMEOUT_SEC);
        return session;
    }

    public static AuthSession toAuthSession(Map<String, Object> attribs) {
        AuthSession session = new AuthSession();
        for (Object key : attribs.keySet()) {
            session.attribs.put(key.toString(), attribs.get(key));
        }
        return session;
    }

    // Normalized attributes for data storage.
    private final ConcurrentMap<String, Object> attribs;

    private AuthSession() {
        this.attribs = new ConcurrentHashMap<String, Object>();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AuthSession)) {
            return false;
        }
        AuthSession e = (AuthSession) obj;
        return this.attribs.equals(e.attribs);
    }

    // private only used for storage
    private Object getAttribute(String name) {
        return attribs.get(name);
    }

    /**
     * @return the flow in progress
     */
    public ResponseType getAuthFlow() {
        String name = (String) getAttribute(FLOW);
        if (name == null) {
            return null;
        }
        return ResponseType.valueOf(name);
    }

    /**
     * @return return the client that established the cookie
     */
    public String getClientId() {
        return (String) getAttribute(CLIENT_ID);
    }

    public String[] getGrantedScope() {
        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) getAttribute(GRANTED_SCOPE);

        if (list == null) {
            return null;
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    public String getId() {
        return (String) getAttribute(ID);
    }

    /**
     * 
     * @return the URL used in the initial authorization call
     */
    public RedirectionURI getRedirectionURI() {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) getAttribute(CALLBACK);

        if (map == null) {
            return null;
        }

        String uri = map.get("uri").toString();
        Boolean dynamic = (Boolean) map.get("dynamic");
        return new RedirectionURI(uri, dynamic);
    }

    /**
     * 
     * @return array of requested scopes
     */
    public String[] getRequestedScope() {
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) getAttribute(REQ_SCOPE);

        if (list == null) {
            return null;
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * 
     * @return identity of the authenticated user.
     */
    public String getScopeOwner() {
        return (String) getAttribute(OWNER);
    }

    /**
     * Setting only affects new or updated sessions.
     * 
     * @return current session timeout
     */
    public int getSessionTimeout() {
        return ((Number) getAttribute(TIMEOUT_SEC)).intValue();
    }

    /**
     * @return client oauth state parameter
     */
    public String getState() {
        return (String) getAttribute(STATE);
    }

    private Object removeAttribute(String name) {
        return attribs.remove(name);
    }

    /**
     * Store attribute for internal use. The value must be normalized.
     * 
     * @param name
     * @param value
     *            normalized value.
     */
    private void setAttribute(String name, Object value) {
        if (value == null) {
            removeAttribute(name);
        } else {
            attribs.put(name, value);
        }
    }

    /**
     * @param flow
     *            current executing flow
     */
    public void setAuthFlow(ResponseType flow) {
        // Normalize
        setAttribute(FLOW, flow.name());
    }

    /**
     * Set the client/application that created the cookie
     * 
     * @param clientId
     *            POJO representing a client_id/secret
     */
    public void setClientId(String clientId) {
        setAttribute(CLIENT_ID, clientId);
    }

    public void setGrantedScope(String[] scope) {
        setAttribute(GRANTED_SCOPE, Arrays.asList(scope));
    }

    public void setRedirectionURI(RedirectionURI uri) {
        // Normalize
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("uri", uri.getURI());
        map.put("dynamic", uri.isDynamicConfigured());
        setAttribute(CALLBACK, map);
    }

    /**
     * @param scope
     *            array of scopes requested but not yet approved
     */
    public void setRequestedScope(String[] scope) {
        setAttribute(REQ_SCOPE, Arrays.asList(scope));
    }

    /**
     * 
     * @param owner
     *            the identity of the user of this session (openid)
     */
    public void setScopeOwner(String owner) {
        setAttribute(OWNER, owner);
    }

    /**
     * Default is 600 sec = 10min
     * 
     * @param timeSeconds
     *            sets the session expiry time in seconds
     */
    public void setSessionTimeout(int timeSeconds) {
        setAttribute(TIMEOUT_SEC, timeSeconds);
    }

    /**
     * @param state
     *            to be save and returned with code
     */
    public void setState(String state) {
        setAttribute(STATE, state);
    }

    /**
     * Get the Map interface that suitable for the database.
     * 
     * @return
     */
    public Map<String, Object> toMap() {
        return attribs;
    }

    public void updateActivity() throws AuthSessionTimeoutException {
        long currentTime = System.currentTimeMillis();
        long lastActivity = ((Number) getAttribute(LAST_ACTIVITY)).longValue();
        long delta = currentTime - lastActivity;
        if ((delta / 1000) >= getSessionTimeout()) {
            throw new AuthSessionTimeoutException();
        }
        lastActivity = System.currentTimeMillis();
        setAttribute(LAST_ACTIVITY, lastActivity);
    }
}
