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

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.AuthenticationInfo;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.CookieSetting;
import org.restlet.data.Dimension;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

/**
 * Request wrapper. Useful for application developer who need to enrich the
 * request with application related properties and behavior.
 * 
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 * @author Jerome Louvel
 */
public class WrapperResponse extends Response {
    /** The wrapped response. */
    private final Response wrappedResponse;

    /**
     * Constructor.
     * 
     * @param wrappedResponse
     *            The wrapped response.
     */
    public WrapperResponse(Response wrappedResponse) {
        super((Request) null);
        this.wrappedResponse = wrappedResponse;
    }

    @Override
    public void abort() {
        wrappedResponse.abort();
    }

    @Override
    public void commit() {
        wrappedResponse.commit();
    }

    @Override
    public int getAge() {
        return wrappedResponse.getAge();
    }

    /**
     * Returns the set of methods allowed on the requested resource. This
     * property only has to be updated when a status
     * CLIENT_ERROR_METHOD_NOT_ALLOWED is set.
     * 
     * @return The list of allowed methods.
     */
    @Override
    public Set<Method> getAllowedMethods() {
        return getWrappedResponse().getAllowedMethods();
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
        return getWrappedResponse().getAttributes();
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo() {
        return wrappedResponse.getAuthenticationInfo();
    }

    /**
     * Returns the list of authentication requests sent by an origin server to a
     * client.
     * 
     * @return The list of authentication requests sent by an origin server to a
     *         client.
     */
    @Override
    public List<ChallengeRequest> getChallengeRequests() {
        return getWrappedResponse().getChallengeRequests();
    }

    /**
     * Returns the cookie settings provided by the server.
     * 
     * @return The cookie settings provided by the server.
     */
    @Override
    public Series<CookieSetting> getCookieSettings() {
        return getWrappedResponse().getCookieSettings();
    }

    /**
     * Returns the set of selecting dimensions on which the response entity may
     * vary. If some server-side content negotiation is done, this set should be
     * properly updated, other it can be left empty.
     * 
     * @return The set of dimensions on which the response entity may vary.
     */
    @Override
    public Set<Dimension> getDimensions() {
        return getWrappedResponse().getDimensions();
    }

    /**
     * Returns the entity representation.
     * 
     * @return The entity representation.
     */
    @Override
    public Representation getEntity() {
        return getWrappedResponse().getEntity();
    }

    /**
     * Returns the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @return The redirection reference.
     */
    @Override
    public Reference getLocationRef() {
        return getWrappedResponse().getLocationRef();
    }

    /**
     * Returns the list of authentication requests sent by a proxy to a client.
     * 
     * @return The list of authentication requests sent by a proxy to a client.
     */
    @Override
    public List<ChallengeRequest> getProxyChallengeRequests() {
        return getWrappedResponse().getProxyChallengeRequests();
    }

    /**
     * Returns the associated request
     * 
     * @return The associated request
     */
    @Override
    public Request getRequest() {
        return getWrappedResponse().getRequest();
    }

    @Override
    public Date getRetryAfter() {
        return wrappedResponse.getRetryAfter();
    }

    /**
     * Returns the server-specific information.
     * 
     * @return The server-specific information.
     */
    @Override
    public ServerInfo getServerInfo() {
        return getWrappedResponse().getServerInfo();
    }

    /**
     * Returns the status.
     * 
     * @return The status.
     */
    @Override
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

    @Override
    public boolean isAutoCommitting() {
        return wrappedResponse.isAutoCommitting();
    }

    @Override
    public boolean isCommitted() {
        return wrappedResponse.isCommitted();
    }

    /**
     * Indicates if the call came over a confidential channel such as an
     * SSL-secured connection.
     * 
     * @return True if the call came over a confidential channel.
     */
    @Override
    public boolean isConfidential() {
        return getWrappedResponse().isConfidential();
    }

    /**
     * Indicates if a content is available and can be sent. Several conditions
     * must be met: the content must exists and have some available data.
     * 
     * @return True if a content is available and can be sent.
     */
    @Override
    public boolean isEntityAvailable() {
        return getWrappedResponse().isEntityAvailable();
    }

    /**
     * Permanently redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.
     * 
     * @param targetRef
     *            The target URI reference.
     */
    @Override
    public void redirectPermanent(Reference targetRef) {
        getWrappedResponse().redirectPermanent(targetRef);
    }

    /**
     * Permanently redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.
     * 
     * @param targetUri
     *            The target URI.
     */
    @Override
    public void redirectPermanent(String targetUri) {
        getWrappedResponse().redirectPermanent(targetUri);
    }

    /**
     * Redirects the client to a different URI that SHOULD be retrieved using a
     * GET method on that resource. This method exists primarily to allow the
     * output of a POST-activated script to redirect the user agent to a
     * selected resource. The new URI is not a substitute reference for the
     * originally requested resource.
     * 
     * @param targetRef
     *            The target reference.
     */
    @Override
    public void redirectSeeOther(Reference targetRef) {
        getWrappedResponse().redirectSeeOther(targetRef);
    }

    /**
     * Redirects the client to a different URI that SHOULD be retrieved using a
     * GET method on that resource. This method exists primarily to allow the
     * output of a POST-activated script to redirect the user agent to a
     * selected resource. The new URI is not a substitute reference for the
     * originally requested resource.
     * 
     * @param targetUri
     *            The target URI.
     */
    @Override
    public void redirectSeeOther(String targetUri) {
        getWrappedResponse().redirectSeeOther(targetUri);
    }

    /**
     * Temporarily redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.
     * 
     * @param targetRef
     *            The target reference.
     */
    @Override
    public void redirectTemporary(Reference targetRef) {
        getWrappedResponse().redirectTemporary(targetRef);
    }

    /**
     * Temporarily redirects the client to a target URI. The client is expected
     * to reuse the same method for the new request.
     * 
     * @param targetUri
     *            The target URI.
     */
    @Override
    public void redirectTemporary(String targetUri) {
        getWrappedResponse().redirectTemporary(targetUri);
    }

    @Override
    public void setAge(int age) {
        wrappedResponse.setAge(age);
    }

    @Override
    public void setAllowedMethods(Set<Method> allowedMethods) {
        wrappedResponse.setAllowedMethods(allowedMethods);
    }

    @Override
    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        wrappedResponse.setAuthenticationInfo(authenticationInfo);
    }

    @Override
    public void setAutoCommitting(boolean autoCommitting) {
        wrappedResponse.setAutoCommitting(autoCommitting);
    }

    /**
     * Sets the list of authentication requests sent by an origin server to a
     * client.
     * 
     * @param requests
     *            The list of authentication requests sent by an origin server
     *            to a client.
     */
    @Override
    public void setChallengeRequests(List<ChallengeRequest> requests) {
        getWrappedResponse().setChallengeRequests(requests);
    }

    @Override
    public void setCommitted(boolean committed) {
        wrappedResponse.setCommitted(committed);
    }

    @Override
    public void setCookieSettings(Series<CookieSetting> cookieSettings) {
        wrappedResponse.setCookieSettings(cookieSettings);
    }

    @Override
    public void setDimensions(Set<Dimension> dimensions) {
        wrappedResponse.setDimensions(dimensions);
    }

    /**
     * Sets the entity representation.
     * 
     * @param entity
     *            The entity representation.
     */
    @Override
    public void setEntity(Representation entity) {
        getWrappedResponse().setEntity(entity);
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
        getWrappedResponse().setEntity(value, mediaType);
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param locationRef
     *            The reference to set.
     */
    @Override
    public void setLocationRef(Reference locationRef) {
        getWrappedResponse().setLocationRef(locationRef);
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param locationUri
     *            The URI to set.
     */
    @Override
    public void setLocationRef(String locationUri) {
        getWrappedResponse().setLocationRef(locationUri);
    }

    /**
     * Sets the list of authentication requests sent by a proxy to a client.
     * 
     * @param requests
     *            The list of authentication requests sent by a proxy to a
     *            client.
     */
    @Override
    public void setProxyChallengeRequests(List<ChallengeRequest> requests) {
        getWrappedResponse().setProxyChallengeRequests(requests);
    }

    /**
     * Sets the associated request.
     * 
     * @param request
     *            The associated request
     */
    @Override
    public void setRequest(Request request) {
        getWrappedResponse().setRequest(request);
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

    @Override
    public void setRetryAfter(Date retryAfter) {
        wrappedResponse.setRetryAfter(retryAfter);
    }

    @Override
    public void setServerInfo(ServerInfo serverInfo) {
        wrappedResponse.setServerInfo(serverInfo);
    }

    /**
     * Sets the status.
     * 
     * @param status
     *            The status to set.
     */
    @Override
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
    @Override
    public void setStatus(Status status, String message) {
        getWrappedResponse().setStatus(status, message);
    }

    @Override
    public void setStatus(Status status, Throwable throwable) {
        wrappedResponse.setStatus(status, throwable);
    }

    @Override
    public void setStatus(Status status, Throwable throwable, String message) {
        wrappedResponse.setStatus(status, throwable, message);
    }

    @Override
    public String toString() {
        return wrappedResponse.toString();
    }

}
