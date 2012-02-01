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

package org.restlet.ext.oauth.internal;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.restlet.ext.oauth.Client;
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
 */
public class AuthSession {

    private final ConcurrentMap<String, Object> attribs;

    // TODO: Remove?
    // private volatile long lastActivity = System.currentTimeMillis();

    private static final String ID = "id";

    private static final String CLIENT = "client";

    private static final String REQ_SCOPE = "requested_scope";

    private static final String FLOW = "flow";

    private static final String DYN_CALLBACK = "dynamic_callback";

    private static final String OWNER = "owner";

    private static final String STATE = "state";

    // If executor is set sessions will be removed.
    protected volatile ScheduledThreadPoolExecutor executor;

    protected volatile long timeoutMin = 3600;

    // TODO: Most likely not needed!
    // private final ConcurrentMap<String, Object> sessions;

    /**
     * 
     * @param sessions
     *            map where session will be stored based on their id
     * @param executor
     *            pool for handling session expiration.
     */
    public AuthSession(ConcurrentMap<String, Object> sessions,
            ScheduledThreadPoolExecutor executor) {
        this.attribs = new ConcurrentHashMap<String, Object>();
        String sessionId = UUID.randomUUID().toString();
        // setId(sessionId); // Generate a new ID
        setAttribute(ID, sessionId);
        // this.sessions = sessions;
        sessions.put(sessionId, this);
        // TODO start a timer...
    }

    // Only from constructor
    /*
     * private void setId(String id) { setAttribute(ID, id); }
     */

    /**
     * @return the session id for this object.
     */
    public String getId() {
        return (String) getAttribute(ID);
    }

    /**
     * Set the client/application that created the cookie
     * 
     * @param client
     *            POJO representing a client_id/secret
     */

    public void setClient(Client client) {
        setAttribute(CLIENT, client);
    }

    /**
     * @return return the client that established the cookie
     */
    public Client getClient() {
        return (Client) getAttribute(CLIENT);
    }

    /*
     * public void removeClient() { removeAttribute("client"); }
     */

    // public void setGrantedScope(String[] scope) {
    // setAttribute("grant_scope", scope);
    // }
    //
    // public String[] getGrantedScope() {
    // return (String[]) getAttribute("grant_scope");
    // }

    /**
     * @param scope
     *            array of scopes requested but not yet approved
     */

    public void setRequestedScope(String[] scope) {
        setAttribute(REQ_SCOPE, scope);
    }

    /**
     * 
     * @return array of requested scopes
     */

    public String[] getRequestedScope() {
        return (String[]) getAttribute(REQ_SCOPE);
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
     * 
     * @return identity of the authenticated user.
     */

    public String getScopeOwner() {
        return (String) getAttribute(OWNER);
    }

    /**
     * @param flow
     *            current executing flow
     */

    public void setAuthFlow(ResponseType flow) {
        setAttribute(FLOW, flow);
    }

    /**
     * @return the flow in progress
     */

    public ResponseType getAuthFlow() {
        return (ResponseType) getAttribute(FLOW);
    }

    /**
     * @param state
     *            to be save and returned with code
     */

    public void setState(String state) {
        setAttribute(STATE, state);
    }

    /**
     * @return client oauth state parameter
     */

    public String getState() {
        return (String) getAttribute(STATE);
    }

    /**
     * Allows for dynamic callback for redirection URI Only the base has to
     * match. The entire redirect_uri is preserved to use when returning.
     * 
     * Example https://example.com/myapp is the oficial CB URL stored in the
     * oauth authorization server.
     * 
     * A running client can use the following set of URL's
     * https://example.com/myapp?param=foo https://example.com/myapp/resource
     * 
     * @param uri
     */

    public void setDynamicCallbackURI(String uri) {
        setAttribute(DYN_CALLBACK, uri);
    }

    /**
     * 
     * @return the URL used in the initial authorization call
     */

    public String getDynamicCallbackURI() {
        return (String) getAttribute(DYN_CALLBACK);
    }

    /**
     * Resets all data except session id and authenticated user.
     * 
     * The rest of the parametes are just a cache from the time a client
     * requests authorization until the code or token is generated back.
     */
    public void reset() {
        removeAttribute(CLIENT);
        removeAttribute(REQ_SCOPE);
        removeAttribute(FLOW);
        // removeAttribute("state");
        removeAttribute(DYN_CALLBACK);
    }

    /**
     * Sets the timer pool if session are being cleaned up.
     * 
     * @param executor
     */
    public void setThreadPoolExecutor(ScheduledThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * 
     * @return the current executor in use or null if non was provided.
     */

    public ScheduledThreadPoolExecutor getThreadPoolExecutor() {
        return executor;
    }

    /**
     * Default is 3600 sec = 24h
     * 
     * @param timeMinutes
     *            sets the session expiry time in seconds
     */
    public void setSessionTimeout(long timeMinutes) {
        timeoutMin = timeMinutes;
    }

    /**
     * Setting only affects new or updated sessions.
     * 
     * @return current session timeout
     */

    public long getSessionTimeout() {
        return timeoutMin;
    }

    // private only used for storage

    private Object getAttribute(String name) {
        handleActivity();
        return attribs.get(name);
    }

    private void setAttribute(String name, Object value) {
        handleActivity();
        if (value == null) {
            removeAttribute(name);
        } else {
            attribs.put(name, value);
        }
    }

    private Object removeAttribute(String name) {
        handleActivity();
        return attribs.remove(name);
    }

    private void handleActivity() {
        // long currentTime = System.currentTimeMillis();
        // long delta = currentTime - lastActivity;
        // lastActivity = System.currentTimeMillis();
    }
}
