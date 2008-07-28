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

package org.restlet.util;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.CookieSetting;
import org.restlet.data.Dimension;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.SaxRepresentation;

/**
 * Request wrapper. Useful for application developer who need to enrich the
 * request with application related properties and behavior.
 * 
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 * @author Jerome Louvel (contact@noelios.com)
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
    public Map<String, Object> getAttributes() {
        return getWrappedResponse().getAttributes();
    }

    /**
     * Returns the authentication request sent by an origin server to a client.
     * 
     * @return The authentication request sent by an origin server to a client.
     * @deprecated Use the {@link #getChallengeRequests()} instead.
     */
    @Deprecated
    @Override
    public ChallengeRequest getChallengeRequest() {
        return getWrappedResponse().getChallengeRequest();
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
     * Returns the entity as a DOM representation.<br>
     * Note that this triggers the parsing of the entity into a reusable DOM
     * document stored in memory.<br>
     * This method and the related getEntity*() methods can only be invoked
     * once.
     * 
     * @return The entity as a DOM representation.
     */
    @Override
    public DomRepresentation getEntityAsDom() {
        return getWrappedResponse().getEntityAsDom();
    }

    /**
     * Returns the entity as a DOM representation.<br>
     * Note that this triggers the parsing of the entity into a reusable DOM
     * document stored in memory.<br>
     * This method and the related getEntity*() methods can only be invoked
     * once.
     * 
     * @return The entity as a DOM representation.
     */
    @Override
    public Form getEntityAsForm() {
        return getWrappedResponse().getEntityAsForm();
    }

    /**
     * Returns the entity as a higher-level object. This object is created by
     * the Application's converter service. If you want to use this method to
     * facilitate the processing of request entities, you need to provide a
     * custom implementation of the ConverterService class, overriding the
     * toObject(Representation) method. <br>
     * Note that this triggers the parsing of the entity.<br>
     * This method and the related getEntity*() methods can only be invoked
     * once.
     * 
     * @return The entity as a higher-level object.
     * @deprecated Since 1.1, the ConverterService is deprecated, with no
     *             replacement as it doesn't fit well with content negotiation.
     *             Most users prefer to handle those conversion in Resource
     *             subclasses.
     */
    @Override
    @Deprecated
    public Object getEntityAsObject() {
        return getWrappedResponse().getEntityAsObject();
    }

    /**
     * Returns the entity as a SAX representation.<br>
     * Note that this kind of representation can only be parsed once. If you
     * evaluate an XPath expression, it can also only be done once. If you need
     * to reuse the entity multiple times, consider using the getEntityAsDom()
     * method instead.
     * 
     * @return The entity as a SAX representation.
     */
    @Override
    public SaxRepresentation getEntityAsSax() {
        return getWrappedResponse().getEntityAsSax();
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
     * Returns the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @return The redirection reference.
     * @deprecated Use the getLocationRef() method instead.
     */
    @Override
    @Deprecated
    public Reference getRedirectRef() {
        return getWrappedResponse().getRedirectRef();
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

    /**
     * Sets the authentication request sent by an origin server to a client.
     * 
     * @param request
     *            The authentication request sent by an origin server to a
     *            client.
     */
    @Override
    public void setChallengeRequest(ChallengeRequest request) {
        getWrappedResponse().setChallengeRequest(request);
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

    /**
     * Sets the entity from a higher-level object. This object is converted to a
     * representation using the Application's converter service. If you want to
     * use this method to facilitate the setting of entities, you need to
     * provide a custom implementation of the ConverterService class, overriding
     * the toRepresentation(Object) method.
     * 
     * @param object
     *            The higher-level object.
     * @deprecated Since 1.1, the ConverterService is deprecated, with no
     *             replacement as it doesn't fit well with content negotiation.
     *             Most users prefer to handle those conversion in Resource
     *             subclasses.
     */
    @Override
    @Deprecated
    public void setEntity(Object object) {
        getWrappedResponse().setEntity(object);
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
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param redirectRef
     *            The redirection reference.
     * @deprecated Use the setLocationRef() method instead.
     */
    @Override
    @Deprecated
    public void setRedirectRef(Reference redirectRef) {
        getWrappedResponse().setRedirectRef(redirectRef);
    }

    /**
     * Sets the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @param redirectUri
     *            The redirection URI.
     * @deprecated Use the setLocationRef() method instead.
     */
    @Override
    @Deprecated
    public void setRedirectRef(String redirectUri) {
        getWrappedResponse().setRedirectRef(redirectUri);
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

}
