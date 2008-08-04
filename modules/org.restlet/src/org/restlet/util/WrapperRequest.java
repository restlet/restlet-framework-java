/*
 * Copyright 2005-2008 Noelios Technologies.
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

import java.util.Map;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
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

    @Override
    public DomRepresentation getEntityAsDom() {
        return getWrappedRequest().getEntityAsDom();
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
        return getWrappedRequest().getEntityAsForm();
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
        return getWrappedRequest().getEntityAsObject();
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
        return getWrappedRequest().getEntityAsSax();
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

    /**
     * Returns the method.
     * 
     * @return The method.
     */
    @Override
    public Method getMethod() {
        return getWrappedRequest().getMethod();
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

    /**
     * Indicates if the call came over a confidential channel such as an
     * SSL-secured connection.
     * 
     * @param confidential
     *            True if the call came over a confidential channel.
     */
    @Override
    public void setConfidential(boolean confidential) {
        getWrappedRequest().setConfidential(confidential);
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
        getWrappedRequest().setEntity(object);
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

}
