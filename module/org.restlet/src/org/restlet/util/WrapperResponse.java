/*
 * Copyright 2005-2006 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.util;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.CookieSetting;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.resource.Resource;

/**
 * Request wrapper. Useful for application developer who need to enrich the
 * request with application related properties and behavior.
 * 
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class WrapperResponse extends Response {
    /** The wrapped response. */
    private Response wrappedResponse;

    /**
     * Constructor.
     * 
     * @param wrappedResponse
     *            The wrapped response.
     */
    public WrapperResponse(Response wrappedResponse) {
        super(null);
        this.wrappedResponse = wrappedResponse;
    }

    /**
     * Returns the authentication request sent by an origin server to a client.
     * 
     * @return The authentication request sent by an origin server to a client.
     */
    public ChallengeRequest getChallengeRequest() {
        return getWrappedResponse().getChallengeRequest();
    }

    /**
     * Returns the cookie settings provided by the server.
     * 
     * @return The cookie settings provided by the server.
     */
    public Series<CookieSetting> getCookieSettings() {
        return getWrappedResponse().getCookieSettings();
    }

    /**
     * Returns the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @return The redirection reference.
     */
    public Reference getRedirectRef() {
        return getWrappedResponse().getRedirectRef();
    }

    /**
     * Returns the associated request
     * 
     * @return The associated request
     */
    public Request getRequest() {
        return getWrappedResponse().getRequest();
    }

    /**
     * Returns the server-specific information.
     * 
     * @return The server-specific information.
     */
    public ServerInfo getServerInfo() {
        return getWrappedResponse().getServerInfo();
    }

    /**
     * Returns the status.
     * 
     * @return The status.
     */
    public Status getStatus() {
        return getWrappedResponse().getStatus();
    }

    /**
     * Returns the wrapped response.
     * 
     * @return The wrapped response.
     */
    protected Response getWrappedResponse() {
        return this.wrappedResponse;
    }

    /**
     * Sets the authentication request sent by an origin server to a client.
     * 
     * @param request
     *            The authentication request sent by an origin server to a
     *            client.
     */
    public void setChallengeRequest(ChallengeRequest request) {
        getWrappedResponse().setChallengeRequest(request);
    }

    /**
     * Sets the entity with the best representation of a resource, according to
     * the client preferences. <br/> If no representation is found, sets the
     * status to "Not found".<br/> If no acceptable representation is
     * available, sets the status to "Not acceptable".<br/>
     * 
     * @param resource
     *            The resource for which the best representation needs to be
     *            set.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public void setEntity(Resource resource) {
        getWrappedResponse().setEntity(resource);
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param redirectRef
     *            The redirection reference.
     */
    public void setRedirectRef(Reference redirectRef) {
        getWrappedResponse().setRedirectRef(redirectRef);
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param redirectUri
     *            The redirection URI.
     */
    public void setRedirectRef(String redirectUri) {
        getWrappedResponse().setRedirectRef(redirectUri);
    }

    /**
     * Sets the associated request.
     * 
     * @param request
     *            The associated request
     */
    public void setRequest(WrapperRequest request) {
        getWrappedResponse().setRequest(request);
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     */
    public void setStatus(Status status) {
        getWrappedResponse().setStatus(status);
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     * @param message
     *            The status message.
     */
    public void setStatus(Status status, String message) {
        getWrappedResponse().setStatus(status, message);
    }

}
