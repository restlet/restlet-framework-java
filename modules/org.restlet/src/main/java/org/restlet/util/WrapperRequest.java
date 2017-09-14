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

package org.restlet.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * Request wrapper. Useful for application developer who need to enrich the
 * request with application related properties and behavior.
 * 
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 * @author Jerome Louvel
 */
public class WrapperRequest extends Request {
    /** The wrapped request. */
    private final Request wrappedRequest;

    /**
     * Constructor.
     * 
     * @param wrappedRequest
     *            The wrapped request.
     */
    public WrapperRequest(Request wrappedRequest) {
        this.wrappedRequest = wrappedRequest;
    }

    @Override
    public boolean abort() {
        return wrappedRequest.abort();
    }

    @Override
    public void commit(Response response) {
        wrappedRequest.commit(response);
    }

    /**
     * Returns a modifiable attributes map that can be used by developers to
     * save information relative to the message. This is an easier alternative
     * to the creation of a wrapper instance around the whole message.<br>
     * <br>
     * 
     * In addition, this map is a shared space between the developer and the
     * connectors. In this case, it is used to exchange information that is not
     * uniform across all protocols and couldn't therefore be directly included
     * in the API. For this purpose, all attribute names starting with
     * "org.restlet" are reserved. Currently the following attributes are used:
     * <table>
     * <tr>
     * <th>Attribute name</th>
     * <th>Class name</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>org.restlet.http.headers</td>
     * <td>org.restlet.data.Form</td>
     * <td>Server HTTP connectors must provide all request headers and client
     * HTTP connectors must provide all response headers, exactly as they were
     * received. In addition, developers can also use this attribute to specify
     * <b>non-standard</b> headers that should be added to the request or to the
     * response.</td>
     * </tr>
     * </table>
     * Adding standard HTTP headers is forbidden because it could conflict with
     * the connector's internal behavior, limit portability or prevent future
     * optimizations.</td>
     * 
     * @return The modifiable attributes map.
     */
    @Override
    public ConcurrentMap<String, Object> getAttributes() {
        return getWrappedRequest().getAttributes();
    }

    /**
     * Returns the authentication response sent by a client to an origin server.
     * 
     * @return The authentication response sent by a client to an origin server.
     */
    @Override
    public ChallengeResponse getChallengeResponse() {
        return getWrappedRequest().getChallengeResponse();
    }

    /**
     * Returns the client-specific information.
     * 
     * @return The client-specific information.
     */
    @Override
    public ClientInfo getClientInfo() {
        return getWrappedRequest().getClientInfo();
    }

    /**
     * Returns the conditions applying to this call.
     * 
     * @return The conditions applying to this call.
     */
    @Override
    public Conditions getConditions() {
        return getWrappedRequest().getConditions();
    }

    /**
     * Returns the cookies provided by the client.
     * 
     * @return The cookies provided by the client.
     */
    @Override
    public Series<Cookie> getCookies() {
        return getWrappedRequest().getCookies();
    }

    /**
     * Returns the entity representation.
     * 
     * @return The entity representation.
     */
    @Override
    public Representation getEntity() {
        return getWrappedRequest().getEntity();
    }

    /**
     * Returns the host reference. This may be different from the resourceRef's
     * host, for example for URNs and other URIs that don't contain host
     * information.
     * 
     * @return The host reference.
     */
    @Override
    public Reference getHostRef() {
        return getWrappedRequest().getHostRef();
    }

    @Override
    public int getMaxForwards() {
        return wrappedRequest.getMaxForwards();
    }

    /**
     * Returns the method.
     * 
     * @return The method.
     */
    @Override
    public Method getMethod() {
        return getWrappedRequest().getMethod();
    }

    @Override
    public Uniform getOnResponse() {
        return wrappedRequest.getOnResponse();
    }

    @Override
    public Reference getOriginalRef() {
        return wrappedRequest.getOriginalRef();
    }

    /**
     * Returns the protocol by first returning the baseRef.schemeProtocol
     * property if it is set, or the resourceRef.schemeProtocol property
     * otherwise.
     * 
     * @return The protocol or null if not available.
     */
    @Override
    public Protocol getProtocol() {
        return getWrappedRequest().getProtocol();
    }

    // [ifndef gwt] method
    /**
     * Returns the authentication response sent by a client to a proxy.
     * 
     * @return The authentication response sent by a client to a proxy.
     */
    @Override
    public ChallengeResponse getProxyChallengeResponse() {
        return getWrappedRequest().getProxyChallengeResponse();
    }

    @Override
    public List<Range> getRanges() {
        return wrappedRequest.getRanges();
    }

    /**
     * Returns the referrer reference if available.
     * 
     * @return The referrer reference.
     */
    @Override
    public Reference getReferrerRef() {
        return getWrappedRequest().getReferrerRef();
    }

    /**
     * Returns the reference of the target resource.
     * 
     * @return The reference of the target resource.
     */
    @Override
    public Reference getResourceRef() {
        return getWrappedRequest().getResourceRef();
    }

    /**
     * Returns the application root reference.
     * 
     * @return The application root reference.
     */
    @Override
    public Reference getRootRef() {
        return getWrappedRequest().getRootRef();
    }

    /**
     * Returns the wrapped request.
     * 
     * @return The wrapped request.
     */
    protected Request getWrappedRequest() {
        return this.wrappedRequest;
    }

    /**
     * Returns the access control request headers of the target resource.
     * 
     * @return The access control request headers of the target resource.
     */
    @Override
    public Set<String> getAccessControlRequestHeaders() {
        return wrappedRequest.getAccessControlRequestHeaders();
    }

    /**
     * Returns the access control request method of the target resource.
     * 
     * @return The access control request method of the target resource.
     */
    @Override
    public Method getAccessControlRequestMethod() {
        return wrappedRequest.getAccessControlRequestMethod();
    }

    @Override
    public boolean isAsynchronous() {
        return wrappedRequest.isAsynchronous();
    }

    /**
     * Indicates if the call came over a confidential channel such as an
     * SSL-secured connection.
     * 
     * @return True if the call came over a confidential channel.
     */
    @Override
    public boolean isConfidential() {
        return getWrappedRequest().isConfidential();
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
        return getWrappedRequest().isEntityAvailable();
    }

    @Override
    public boolean isExpectingResponse() {
        return wrappedRequest.isExpectingResponse();
    }

    @Override
    public boolean isSynchronous() {
        return wrappedRequest.isSynchronous();
    }

    /**
     * Sets the authentication response sent by a client to an origin server.
     * 
     * @param response
     *            The authentication response sent by a client to an origin
     *            server.
     */
    @Override
    public void setChallengeResponse(ChallengeResponse response) {
        getWrappedRequest().setChallengeResponse(response);
    }

    @Override
    public void setClientInfo(ClientInfo clientInfo) {
        wrappedRequest.setClientInfo(clientInfo);
    }

    @Override
    public void setConditions(Conditions conditions) {
        wrappedRequest.setConditions(conditions);
    }

    @Override
    public void setCookies(Series<Cookie> cookies) {
        wrappedRequest.setCookies(cookies);
    }

    /**
     * Sets the entity representation.
     * 
     * @param entity
     *            The entity representation.
     */
    @Override
    public void setEntity(Representation entity) {
        getWrappedRequest().setEntity(entity);
    }

    /**
     * Sets a textual entity.
     * 
     * @param value
     *            The represented string.
     * @param mediaType
     *            The representation's media type.
     */
    @Override
    public void setEntity(String value, MediaType mediaType) {
        getWrappedRequest().setEntity(value, mediaType);
    }

    /**
     * Sets the host reference.
     * 
     * @param hostRef
     *            The host reference.
     */
    @Override
    public void setHostRef(Reference hostRef) {
        getWrappedRequest().setHostRef(hostRef);
    }

    /**
     * Sets the host reference using an URI string.
     * 
     * @param hostUri
     *            The host URI.
     */
    @Override
    public void setHostRef(String hostUri) {
        getWrappedRequest().setHostRef(hostUri);
    }

    @Override
    public void setMaxForwards(int maxForwards) {
        wrappedRequest.setMaxForwards(maxForwards);
    }

    /**
     * Sets the method called.
     * 
     * @param method
     *            The method called.
     */
    @Override
    public void setMethod(Method method) {
        getWrappedRequest().setMethod(method);
    }

    @Override
    public void setOnResponse(Uniform onResponseCallback) {
        wrappedRequest.setOnResponse(onResponseCallback);
    }

    @Override
    public void setOriginalRef(Reference originalRef) {
        wrappedRequest.setOriginalRef(originalRef);
    }

    @Override
    public void setProtocol(Protocol protocol) {
        wrappedRequest.setProtocol(protocol);
    }

    // [ifndef gwt] method
    /**
     * Sets the authentication response sent by a client to a proxy.
     * 
     * @param response
     *            The authentication response sent by a client to a proxy.
     */
    @Override
    public void setProxyChallengeResponse(ChallengeResponse response) {
        getWrappedRequest().setProxyChallengeResponse(response);
    }

    @Override
    public void setRanges(List<Range> ranges) {
        wrappedRequest.setRanges(ranges);
    }

    /**
     * Sets the referrer reference if available.
     * 
     * @param referrerRef
     *            The referrer reference.
     */
    @Override
    public void setReferrerRef(Reference referrerRef) {
        getWrappedRequest().setReferrerRef(referrerRef);
    }

    /**
     * Sets the referrer reference if available using an URI string.
     * 
     * @param referrerUri
     *            The referrer URI.
     */
    @Override
    public void setReferrerRef(String referrerUri) {
        getWrappedRequest().setReferrerRef(referrerUri);
    }

    /**
     * Sets the target resource reference. If the reference is relative, it will
     * be resolved as an absolute reference. Also, the context's base reference
     * will be reset. Finally, the reference will be normalized to ensure a
     * consistent handling of the call.
     * 
     * @param resourceRef
     *            The resource reference.
     */
    @Override
    public void setResourceRef(Reference resourceRef) {
        getWrappedRequest().setResourceRef(resourceRef);
    }

    /**
     * Sets the target resource reference using an URI string. Note that the URI
     * can be either absolute or relative to the context's base reference.
     * 
     * @param resourceUri
     *            The resource URI.
     */
    @Override
    public void setResourceRef(String resourceUri) {
        getWrappedRequest().setResourceRef(resourceUri);
    }

    /**
     * Sets the application root reference.
     * 
     * @param rootRef
     *            The application root reference.
     */
    @Override
    public void setRootRef(Reference rootRef) {
        getWrappedRequest().setRootRef(rootRef);
    }

    /**
     * Sets the access control request headers of the target resource.
     * 
     * @param accessControlRequestHeaders
     *            The access control request headers of the target resource.
     */
    @Override
    public void setAccessControlRequestHeaders(
            Set<String> accessControlRequestHeaders) {
        super.setAccessControlRequestHeaders(accessControlRequestHeaders);
    }

    /**
     * Sets the access control request method of the target resource.
     * 
     * @param accessControlRequestMethod
     *            The access control request method of the target resource.
     */
    @Override
    public void setAccessControlRequestMethod(Method accessControlRequestMethod) {
        super.setAccessControlRequestMethod(accessControlRequestMethod);
    }

    @Override
    public String toString() {
        return wrappedRequest.toString();
    }

}
