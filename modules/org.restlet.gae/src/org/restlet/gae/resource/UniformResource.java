/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gae.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.restlet.gae.Application;
import org.restlet.gae.Context;
import org.restlet.gae.Restlet;
import org.restlet.gae.data.ChallengeRequest;
import org.restlet.gae.data.ChallengeResponse;
import org.restlet.gae.data.ClientInfo;
import org.restlet.gae.data.Conditions;
import org.restlet.gae.data.Cookie;
import org.restlet.gae.data.CookieSetting;
import org.restlet.gae.data.Dimension;
import org.restlet.gae.data.Form;
import org.restlet.gae.data.Method;
import org.restlet.gae.data.Protocol;
import org.restlet.gae.data.Range;
import org.restlet.gae.data.Reference;
import org.restlet.gae.data.Request;
import org.restlet.gae.data.Response;
import org.restlet.gae.data.ServerInfo;
import org.restlet.gae.data.Status;
import org.restlet.gae.representation.DomRepresentation;
import org.restlet.gae.representation.Representation;
import org.restlet.gae.representation.SaxRepresentation;
import org.restlet.gae.util.Series;

/**
 * Base resource class exposing the uniform REST interface. Intended conceptual
 * target of a hypertext reference.<br>
 * <br>
 * "The central feature that distinguishes the REST architectural style from
 * other network-based styles is its emphasis on a uniform interface between
 * components. By applying the software engineering principle of generality to
 * the component interface, the overall system architecture is simplified and
 * the visibility of interactions is improved. Implementations are decoupled
 * from the services they provide, which encourages independent evolvability."
 * Roy T. Fielding<br>
 * <br>
 * Concurrency note: contrary to the {@link org.restlet.gae.Uniform} class and its
 * main {@link Restlet} subclass where a single instance can handle several
 * calls concurrently, one instance of {@link UniformResource} is created for
 * each call handled and accessed by only one thread at a time.<br>
 * <br>
 * Note: The current implementation isn't complete and doesn't support the full
 * syntax. This is work in progress and should only be used for experimentation.
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/rest_arch_style.htm#sec_5_1_5">Source
 *      dissertation</a>
 * @author Jerome Louvel
 */
public abstract class UniformResource {

    /** The current context. */
    private volatile Context context;

    /** The handled request. */
    private volatile Request request;

    /** The handled response. */
    private volatile Response response;

    /**
     * Set-up method that can be overridden in order to initialize the state of
     * the resource. By default it does nothing.
     * 
     * @see #init(Context, Request, Response)
     */
    protected void doInit() throws ResourceException {
    }

    /**
     * Clean-up method that can be overridden in order to release the state of
     * the resource. By default it does nothing.
     * 
     * @see #release()
     */
    protected void doRelease() throws ResourceException {
    }

    /**
     * Returns the set of methods allowed for the current client by the
     * resource. The result can vary based on the client's user agent,
     * authentication and authorization data provided by the client.
     * 
     * @return The set of allowed methods.
     */
    public Set<Method> getAllowedMethods() {
        return getResponse().getAllowedMethods();
    }

    /**
     * Returns the parent application if it exists, or null.
     * 
     * @return The parent application if it exists, or null.
     */
    public Application getApplication() {
        return Application.getCurrent();
    }

    /**
     * Returns the list of authentication requests sent by an origin server to a
     * client. If none is available, an empty list is returned.
     * 
     * @return The list of authentication requests.
     * @see Response#getChallengeRequests()
     */
    public List<ChallengeRequest> getChallengeRequests() {
        return getResponse().getChallengeRequests();
    }

    /**
     * Returns the authentication response sent by a client to an origin server.
     * 
     * @return The authentication response sent by a client to an origin server.
     * @see Request#getChallengeResponse()
     */
    public ChallengeResponse getChallengeResponse() {
        return getRequest().getChallengeResponse();
    }

    /**
     * Returns the client-specific information. Creates a new instance if no one
     * has been set.
     * 
     * @return The client-specific information.
     * @see Request#getClientInfo()
     */
    public ClientInfo getClientInfo() {
        return getRequest().getClientInfo();
    }

    /**
     * Returns the modifiable conditions applying to this request. Creates a new
     * instance if no one has been set.
     * 
     * @return The conditions applying to this call.
     * @see Request#getConditions()
     */
    public Conditions getConditions() {
        return getRequest().getConditions();
    }

    /**
     * Returns the current context.
     * 
     * @return The current context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Returns the modifiable series of cookies provided by the client. Creates
     * a new instance if no one has been set.
     * 
     * @return The cookies provided by the client.
     * @see Request#getCookies()
     */
    public Series<Cookie> getCookies() {
        return getRequest().getCookies();
    }

    /**
     * Returns the modifiable series of cookie settings provided by the server.
     * Creates a new instance if no one has been set.
     * 
     * @return The cookie settings provided by the server.
     * @see Response#getCookieSettings()
     */
    public Series<CookieSetting> getCookieSettings() {
        return getResponse().getCookieSettings();
    }

    /**
     * Returns the modifiable set of selecting dimensions on which the response
     * entity may vary. If some server-side content negotiation is done, this
     * set should be properly updated, other it can be left empty. Creates a new
     * instance if no one has been set.
     * 
     * @return The set of dimensions on which the response entity may vary.
     * @see Response#getDimensions()
     */
    public Set<Dimension> getDimensions() {
        return getResponse().getDimensions();
    }

    /**
     * Returns the host reference. This may be different from the resourceRef's
     * host, for example for URNs and other URIs that don't contain host
     * information.
     * 
     * @return The host reference.
     * @see Request#getHostRef()
     */
    public Reference getHostRef() {
        return getRequest().getHostRef();
    }

    /**
     * Returns the reference that the client should follow for redirections or
     * resource creations.
     * 
     * @return The redirection reference.
     * @see Response#getLocationRef()
     */
    public Reference getLocationRef() {
        return getResponse().getLocationRef();
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        return (getContext() != null) ? getContext().getLogger() : Context
                .getCurrentLogger();
    }

    /**
     * Returns the resource reference's optional matrix.
     * 
     * @return The resource reference's optional matrix.
     * @see Reference#getMatrixAsForm()
     */
    public Form getMatrix() {
        return getReference().getMatrixAsForm();
    }

    /**
     * Returns the method.
     * 
     * @return The method.
     * @see Request#getMethod()
     */
    public Method getMethod() {
        return getRequest().getMethod();
    }

    /**
     * Returns the original reference as requested by the client. Note that this
     * property is not used during request routing.
     * 
     * @return The original reference.
     * @see Request#getOriginalRef()
     */
    public Reference getOriginalRef() {
        return getRequest().getOriginalRef();
    }

    /**
     * Returns the protocol by first returning the resourceRef.schemeProtocol
     * property if it is set, or the baseRef.schemeProtocol property otherwise.
     * 
     * @return The protocol or null if not available.
     * @see Request#getProtocol()
     */
    public Protocol getProtocol() {
        return getRequest().getProtocol();
    }

    /**
     * Returns the resource reference's optional query.
     * 
     * @return The resource reference's optional query.
     * @see Reference#getQueryAsForm()
     */
    public Form getQuery() {
        return getReference().getQueryAsForm();
    }

    /**
     * Returns the ranges to return from the target resource's representation.
     * 
     * @return The ranges to return.
     * @see Request#getRanges()
     */
    public List<Range> getRanges() {
        return getRequest().getRanges();
    }

    /**
     * Returns the URI reference.
     * 
     * @return The URI reference.
     */
    public Reference getReference() {
        return getRequest() == null ? null : getRequest().getResourceRef();
    }

    /**
     * Returns the referrer reference if available.
     * 
     * @return The referrer reference.
     */
    public Reference getReferrerRef() {
        return getRequest().getReferrerRef();
    }

    /**
     * Returns the handled request.
     * 
     * @return The handled request.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Returns the request attributes.
     * 
     * @return The request attributes.
     * @see Request#getAttributes()
     */
    public Map<String, Object> getRequestAttributes() {
        return getRequest().getAttributes();
    }

    /**
     * Returns the request entity representation.
     * 
     * @return The request entity representation.
     */
    public Representation getRequestEntity() {
        return getRequest().getEntity();
    }

    /**
     * Returns the request entity as a DOM representation.<br>
     * This method can be called several times and will always return the same
     * representation instance. Note that if the entity is large this method can
     * result in important memory consumption. In this case, consider using a
     * SAX representation.
     * 
     * @return The entity as a DOM representation.
     */
    public DomRepresentation getRequestEntityAsDom() {
        return getRequest().getEntityAsDom();
    }

    /**
     * Returns the request entity as a form.<br>
     * This method can be called several times and will always return the same
     * form instance. Note that if the entity is large this method can result in
     * important memory consumption.
     * 
     * @return The entity as a form.
     */
    public Form getRequestEntityAsForm() {
        return getRequest().getEntityAsForm();
    }

    /**
     * Returns the request entity as a SAX representation.<br>
     * This method can be called several times and will always return the same
     * representation instance. Note that generally this type of representation
     * can only be parsed once. If you evaluate an XPath expression, it can also
     * only be done once. If you need to reuse the entity multiple times,
     * consider using the getEntityAsDom() method instead.
     * 
     * @return The entity as a SAX representation.
     */
    public SaxRepresentation getRequestEntityAsSax() {
        return getRequest().getEntityAsSax();
    }

    /**
     * Returns the request entity as text.<br>
     * This method can be called several times and will always return the same
     * text. Note that if the entity is large this method can result in
     * important memory consumption.
     * 
     * @return The entity as text.
     */
    public String getRequestEntityAsText() {
        return getRequest().getEntityAsText();
    }

    /**
     * Returns the handled response.
     * 
     * @return The handled response.
     */
    public Response getResponse() {
        return response;
    }

    /**
     * Returns the response attributes.
     * 
     * @return The response attributes.
     * @see Response#getAttributes()
     */
    public Map<String, Object> getResponseAttributes() {
        return getResponse().getAttributes();
    }

    /**
     * Returns the response entity representation.
     * 
     * @return The response entity representation.
     */
    public Representation getResponseEntity() {
        return getResponse().getEntity();
    }

    /**
     * Returns the response entity as a DOM representation.<br>
     * This method can be called several times and will always return the same
     * representation instance. Note that if the entity is large this method can
     * result in important memory consumption. In this case, consider using a
     * SAX representation.
     * 
     * @return The entity as a DOM representation.
     */
    public DomRepresentation getResponseEntityAsDom() {
        return getResponse().getEntityAsDom();
    }

    /**
     * Returns the response entity as a form.<br>
     * This method can be called several times and will always return the same
     * form instance. Note that if the entity is large this method can result in
     * important memory consumption.
     * 
     * @return The entity as a form.
     */
    public Form getResponseEntityAsForm() {
        return getResponse().getEntityAsForm();
    }

    /**
     * Returns the response entity as a SAX representation.<br>
     * This method can be called several times and will always return the same
     * representation instance. Note that generally this type of representation
     * can only be parsed once. If you evaluate an XPath expression, it can also
     * only be done once. If you need to reuse the entity multiple times,
     * consider using the getEntityAsDom() method instead.
     * 
     * @return The entity as a SAX representation.
     */
    public SaxRepresentation getResponseEntityAsSax() {
        return getResponse().getEntityAsSax();
    }

    /**
     * Returns the response entity as text.<br>
     * This method can be called several times and will always return the same
     * text. Note that if the entity is large this method can result in
     * important memory consumption.
     * 
     * @return The entity as text.
     */
    public String getResponseEntityAsText() {
        return getResponse().getEntityAsText();
    }

    /**
     * Returns the application root reference.
     * 
     * @return The application root reference.
     * @see Request#getRootRef()
     */
    public Reference getRootRef() {
        return getRequest().getRootRef();
    }

    /**
     * Returns the server-specific information. Creates a new instance if no one
     * has been set.
     * 
     * @return The server-specific information.
     * @see Response#getServerInfo()
     */
    public ServerInfo getServerInfo() {
        return getResponse().getServerInfo();
    }

    /**
     * Returns the status.
     * 
     * @return The status.
     * @see Response#getStatus()
     */
    public Status getStatus() {
        return getResponse().getStatus();
    }

    /**
     * Handles the call composed of the current context, request and response.
     * 
     * @return The optional response entity.
     */
    public abstract Representation handle();

    /**
     * Initialization method setting the environment of the current resource
     * instance. It the calls the {@link #doInit()} method that can be
     * overridden.
     * 
     * @param context
     *            The current context.
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public final void init(Context context, Request request, Response response) {
        this.context = context;
        this.request = request;
        this.response = response;

        try {
            doInit();
        } catch (ResourceException e) {
            if (getResponse() != null) {
                getResponse().setStatus(e.getStatus());
            }
        }
    }

    /**
     * Indicates if the message was or will be exchanged confidentially, for
     * example via a SSL-secured connection.
     * 
     * @return True if the message is confidential.
     * @see Request#isConfidential()
     */
    public boolean isConfidential() {
        return getRequest().isConfidential();
    }

    /**
     * Releases the resource. First calls the {@link #doRelease()} method then
     * {@link Request#release()} and finally {@link Response#release()}.
     * 
     * @see #doRelease()
     * @see Request#release()
     * @see Response#release()
     */
    public final void release() {
        try {
            doRelease();
        } catch (ResourceException e) {
            if (getResponse() != null) {
                getResponse().setStatus(e.getStatus());
            }
        }
    }

}
