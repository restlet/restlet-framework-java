/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.data;

import java.util.List;

import org.restlet.resource.Representation;
import org.restlet.util.Series;

/**
 * Generic request sent by client connectors. It is then received by server
 * connectors and processed by Restlets. This request can also be processed by a
 * chain of Restlets, on both client and server sides. Requests are uniform
 * across all types of connectors, protocols and components.
 * 
 * @see org.restlet.data.Response
 * @see org.restlet.Uniform
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Request extends Message {
    /**
     * Private cookie series.
     * 
     * @author Jerome Louvel (contact@noelios.com)
     */
    private static class CookieSeries extends Series<Cookie> {
        /**
         * Constructor.
         */
        public CookieSeries() {
            super();
        }

        /**
         * Constructor.
         * 
         * @param delegate
         *                The delegate list.
         */
        public CookieSeries(List<Cookie> delegate) {
            super(delegate);
        }

        @Override
        public Cookie createEntry(String name, String value) {
            return new Cookie(name, value);
        }

        @Override
        public Series<Cookie> createSeries(List<Cookie> delegate) {
            if (delegate != null)
                return new CookieSeries(delegate);
            else
                return new CookieSeries();
        }
    }

    /** The authentication response sent by a client to an origin server. */
    private ChallengeResponse challengeResponse;

    /** The client-specific information. */
    private ClientInfo clientInfo;

    /** The condition data. */
    private Conditions conditions;

    /** Indicates if the call came over a confidential channel. */
    private boolean confidential;

    /** The cookies provided by the client. */
    private Series<Cookie> cookies;

    /** The host reference. */
    private Reference hostRef;

    /** The method. */
    private Method method;

    /** The referrer reference. */
    private Reference referrerRef;

    /** The resource reference. */
    private Reference resourceRef;

    /** The application root reference. */
    private Reference rootRef;

    /**
     * Constructor.
     */
    public Request() {
        this.confidential = false;
    }

    /**
     * Constructor.
     * 
     * @param method
     *                The call's method.
     * @param resourceRef
     *                The resource reference.
     */
    public Request(Method method, Reference resourceRef) {
        this(method, resourceRef, null);
    }

    /**
     * Constructor.
     * 
     * @param method
     *                The call's method.
     * @param resourceRef
     *                The resource reference.
     * @param entity
     *                The entity.
     */
    public Request(Method method, Reference resourceRef, Representation entity) {
        super(entity);
        setMethod(method);
        setResourceRef(resourceRef);
    }

    /**
     * Constructor.
     * 
     * @param method
     *                The call's method.
     * @param resourceUri
     *                The resource URI.
     */
    public Request(Method method, String resourceUri) {
        this(method, new Reference(resourceUri));
    }

    /**
     * Constructor.
     * 
     * @param method
     *                The call's method.
     * @param resourceUri
     *                The resource URI.
     * @param entity
     *                The entity.
     */
    public Request(Method method, String resourceUri, Representation entity) {
        this(method, new Reference(resourceUri), entity);
    }

    /**
     * Returns the authentication response sent by a client to an origin server.
     * 
     * @return The authentication response sent by a client to an origin server.
     */
    public ChallengeResponse getChallengeResponse() {
        return this.challengeResponse;
    }

    /**
     * Returns the client-specific information. Creates a new instance if no one
     * has been set.
     * 
     * @return The client-specific information.
     */
    public ClientInfo getClientInfo() {
        if (this.clientInfo == null)
            this.clientInfo = new ClientInfo();
        return this.clientInfo;
    }

    /**
     * Returns the modifiable conditions applying to this request. Creates a new
     * instance if no one has been set.
     * 
     * @return The conditions applying to this call.
     */
    public Conditions getConditions() {
        if (this.conditions == null)
            this.conditions = new Conditions();
        return this.conditions;
    }

    /**
     * Returns the modifiable series of cookies provided by the client. Creates
     * a new instance if no one has been set.
     * 
     * @return The cookies provided by the client.
     */
    public Series<Cookie> getCookies() {
        if (this.cookies == null)
            this.cookies = new CookieSeries();
        return this.cookies;
    }

    /**
     * Returns the host reference. This may be different from the resourceRef's
     * host, for example for URNs and other URIs that don't contain host
     * information.
     * 
     * @return The host reference.
     */
    public Reference getHostRef() {
        return this.hostRef;
    }

    /**
     * Returns the method.
     * 
     * @return The method.
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Returns the protocol by first returning the resourceRef.schemeProtocol
     * property if it is set, or the baseRef.schemeProtocol property otherwise.
     * 
     * @return The protocol or null if not available.
     */
    public Protocol getProtocol() {
        // Attempt to guess the protocol to use
        // from the target reference scheme
        Protocol result = (getResourceRef() != null) ? getResourceRef()
                .getSchemeProtocol() : null;

        // Fallback: look at base reference scheme
        if (result == null) {
            result = (getResourceRef().getBaseRef() != null) ? getResourceRef()
                    .getBaseRef().getSchemeProtocol() : null;
        }

        return result;
    }

    /**
     * Returns the referrer reference if available.
     * 
     * @return The referrer reference.
     */
    public Reference getReferrerRef() {
        return this.referrerRef;
    }

    /**
     * Returns the reference of the target resource.
     * 
     * @return The reference of the target resource.
     */
    public Reference getResourceRef() {
        return this.resourceRef;
    }

    /**
     * Returns the application root reference.
     * 
     * @return The application root reference.
     */
    public Reference getRootRef() {
        return this.rootRef;
    }

    /**
     * Indicates if the call came over a confidential channel such as an
     * SSL-secured connection.
     * 
     * @return True if the call came over a confidential channel.
     */
    public boolean isConfidential() {
        return this.confidential;
    }

    /**
     * Indicates if a content is available and can be sent. Several conditions
     * must be met: the method must allow the sending of content, the content
     * must exists and have some available data.
     * 
     * @return True if a content is available and can be sent.
     */
    @Override
    public boolean isEntityAvailable() {
        if (getMethod().equals(Method.GET) || getMethod().equals(Method.HEAD)
                || getMethod().equals(Method.DELETE)) {
            return false;
        } else {
            return super.isEntityAvailable();
        }
    }

    /**
     * Sets the authentication response sent by a client to an origin server.
     * 
     * @param response
     *                The authentication response sent by a client to an origin
     *                server.
     */
    public void setChallengeResponse(ChallengeResponse response) {
        this.challengeResponse = response;
    }

    /**
     * Sets the client-specific information.
     * 
     * @param clientInfo
     *                The client-specific information.
     */
    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    /**
     * Sets the conditions applying to this request.
     * 
     * @param conditions
     *                The conditions applying to this request.
     */
    public void setConditions(Conditions conditions) {
        this.conditions = conditions;
    }

    /**
     * Indicates if the call came over a confidential channel such as an
     * SSL-secured connection.
     * 
     * @param confidential
     *                True if the call came over a confidential channel.
     */
    public void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    /**
     * Sets the cookies provided by the client.
     * 
     * @param cookies
     *                The cookies provided by the client.
     */
    public void setCookies(Series<Cookie> cookies) {
        this.cookies = cookies;
    }

    /**
     * Sets the host reference.
     * 
     * @param hostRef
     *                The host reference.
     */
    public void setHostRef(Reference hostRef) {
        this.hostRef = hostRef;
    }

    /**
     * Sets the host reference using an URI string.
     * 
     * @param hostUri
     *                The host URI.
     */
    public void setHostRef(String hostUri) {
        setHostRef(new Reference(hostUri));
    }

    /**
     * Sets the method called.
     * 
     * @param method
     *                The method called.
     */
    public void setMethod(Method method) {
        this.method = method;
    }

    /**
     * Sets the referrer reference if available.
     * 
     * @param referrerRef
     *                The referrer reference.
     */
    public void setReferrerRef(Reference referrerRef) {
        this.referrerRef = referrerRef;

        // A referrer reference must not include a fragment.
        if ((this.referrerRef != null)
                && (this.referrerRef.getFragment() != null))
            this.referrerRef.setFragment(null);
    }

    /**
     * Sets the referrer reference if available using an URI string.
     * 
     * @param referrerUri
     *                The referrer URI.
     */
    public void setReferrerRef(String referrerUri) {
        setReferrerRef(new Reference(referrerUri));
    }

    /**
     * Sets the target resource reference. If the reference is relative, it will
     * be resolved as an absolute reference. Also, the context's base reference
     * will be reset. Finally, the reference will be normalized to ensure a
     * consistent handling of the call.
     * 
     * @param resourceRef
     *                The resource reference.
     */
    public void setResourceRef(Reference resourceRef) {
        this.resourceRef = resourceRef;
    }

    /**
     * Sets the target resource reference using an URI string. Note that the URI
     * can be either absolute or relative to the context's base reference.
     * 
     * @param resourceUri
     *                The resource URI.
     */
    public void setResourceRef(String resourceUri) {
        if (getResourceRef() != null) {
            // Allow usage of URIs relative to the current base reference
            setResourceRef(new Reference(getResourceRef().getBaseRef(),
                    resourceUri));
        } else {
            setResourceRef(new Reference(resourceUri));
        }
    }

    /**
     * Sets the application root reference.
     * 
     * @param rootRef
     *                The application root reference.
     */
    public void setRootRef(Reference rootRef) {
        this.rootRef = rootRef;
    }

}
