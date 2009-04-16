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

package org.restlet.resource;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.service.ConverterService;
import org.restlet.util.Series;

/**
 * Client-side resource. Acts like a proxy of a target resource.<br>
 * <br>
 * Concurrency note: instances of the class are not designed to be shared among
 * several threads. If thread-safety is necessary, consider using the
 * lower-level {@link Client} class instead.<br>
 * <br>
 * Note: The current implementation isn't complete and doesn't support the full
 * syntax. This is work in progress and should only be used for experimentation.
 * 
 * @author Jerome Louvel
 */
public class ClientResource extends UniformResource {

    /** Indicates if redirections are followed. */
    private volatile boolean followRedirects;

    /** The next Restlet. */
    private volatile Uniform next;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Context context, Method method, Reference reference) {
        Request request = new Request(method, reference);
        Response response = new Response(request);

        if (context == null) {
            context = Context.getCurrent();
        }

        if (context != null) {
            this.next = context.getClientDispatcher();
        }

        this.followRedirects = true;
        init(context, request, response);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, Method method, String uri) {
        this(context, method, new Reference(uri));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, Method method, URI uri) {
        this(context, method, new Reference(uri));
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Context context, Reference reference) {
        this(context, Method.GET, reference);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The current context.
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public ClientResource(Context context, Request request, Response response) {
        this.followRedirects = true;
        init(context, request, response);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, String uri) {
        this(context, Method.GET, uri);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Context context, URI uri) {
        this(context, Method.GET, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public ClientResource(Method method, Reference reference) {
        this(Context.getCurrent(), method, reference);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Method method, String uri) {
        this(Context.getCurrent(), method, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public ClientResource(Method method, URI uri) {
        this(Context.getCurrent(), method, uri);
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The target reference.
     */
    public ClientResource(Reference reference) {
        this(Context.getCurrent(), null, reference);
    }

    /**
     * Constructor.
     * 
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     */
    public ClientResource(Request request, Response response) {
        this(Context.getCurrent(), request, response);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public ClientResource(String uri) {
        this(Context.getCurrent(), null, uri);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public ClientResource(URI uri) {
        this(Context.getCurrent(), null, uri);
    }

    /**
     * Deletes the target resource and all its representations.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The optional response entity.
     */
    public Representation delete() throws ResourceException {
        setMethod(Method.DELETE);
        return handle();
    }

    /**
     * Calls the {@link #release()}.
     */
    @Override
    protected void finalize() throws Throwable {
        release();
    }

    /**
     * Represents the resource using content negotiation to select the best
     * variant based on the client preferences.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The best representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public Representation get() throws ResourceException {
        setMethod(Method.GET);
        return handle();
    }

    /**
     * Represents the resource using a given media type.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The representation matching the given media type.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    public Representation get(MediaType mediaType) throws ResourceException {
        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        ClientInfo newClientInfo = new ClientInfo();
        newClientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(mediaType));
        setClientInfo(newClientInfo);
        Representation result = get();

        // Restore the current client info
        setClientInfo(currentClientInfo);
        return result;
    }

    /**
     * Returns the next Restlet. By default, it is the client dispatcher if a
     * context is available.
     * 
     * @return The next Restlet or null.
     */
    public Uniform getNext() {
        return this.next;
    }

    /**
     * Handles the call by invoking the next handler.
     * 
     * @return The optional response entity.
     * @see #getNext()
     */
    @Override
    public Representation handle() {
        Representation result = null;

        if (!hasNext()) {
            Protocol protocol = (getReference() == null) ? null
                    : getReference().getSchemeProtocol();

            if (protocol != null) {
                setNext(new Client(protocol));
            }
        }

        if (hasNext()) {
            handle(getRequest(), getResponse(), null);
            result = getResponse().getEntity();
        } else {
            getLogger()
                    .warning(
                            "Unable to process the call for a client resource. No next Restlet has been provided.");
        }

        return result;
    }

    /**
     * Handle the call and follow redirection for safe methods.
     * 
     * @param request
     *            The request to send.
     * @param response
     *            The response to update.
     * @param references
     *            The references that caused a redirection to prevent infinite
     *            loops.
     */
    private void handle(Request request, Response response,
            List<Reference> references) {
        // Actually handle the call
        getNext().handle(request, response);

        // Check for redirections
        if (request.getMethod().isSafe()
                && response.getStatus().isRedirection()) {
            Reference newTargetRef = response.getLocationRef();

            if ((references != null) && references.contains(newTargetRef)) {
                getLogger().warning(
                        "Infinite redirection loop detected with URI: "
                                + newTargetRef);
            } else if (!request.isEntityAvailable()) {
                getLogger()
                        .warning(
                                "Unable to follow the redirection because the request entity isn't available anymore.");
            } else {
                if (references == null) {
                    references = new ArrayList<Reference>();
                }

                // Add to the list of redirection reference
                // to prevent infinite loops
                references.add(request.getResourceRef());
                request.setResourceRef(newTargetRef);
                handle(request, response, references);
            }
        }
    }

    /**
     * Indicates if there is a next Restlet.
     * 
     * @return True if there is a next Restlet.
     */
    public boolean hasNext() {
        return getNext() != null;
    }

    /**
     * Represents the resource using content negotiation to select the best
     * variant based on the client preferences. This method is identical to
     * {@link #get()} but doesn't return the actual content of the
     * representation, only its metadata.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The best representation.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
     */
    public Representation head() throws ResourceException {
        setMethod(Method.HEAD);
        return handle();
    }

    /**
     * Represents the resource using a given media type. This method is
     * identical to {@link #get(MediaType)} but doesn't return the actual
     * content of the representation, only its metadata.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The representation matching the given media type.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
     */
    public Representation head(MediaType mediaType) throws ResourceException {
        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        ClientInfo newClientInfo = new ClientInfo();
        newClientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(mediaType));
        setClientInfo(newClientInfo);
        Representation result = head();

        // Restore the current client info
        setClientInfo(currentClientInfo);
        return result;
    }

    /**
     * Indicates if redirections are followed.
     * 
     * @return True if redirections are followed.
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * Describes the resource using content negotiation to select the best
     * variant based on the client preferences.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The best description.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    public Representation options() throws ResourceException {
        setMethod(Method.HEAD);
        return handle();
    }

    /**
     * Describes the resource using a given media type.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param mediaType
     *            The media type of the representation to retrieve.
     * @return The matched description or null.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    public Representation options(MediaType mediaType) throws ResourceException {
        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        ClientInfo newClientInfo = new ClientInfo();
        newClientInfo.getAcceptedMediaTypes().add(
                new Preference<MediaType>(mediaType));
        setClientInfo(newClientInfo);
        Representation result = options();

        // Restore the current client info
        setClientInfo(currentClientInfo);
        return result;
    }

    /**
     * TODO
     * 
     * @param entity
     * @return
     * @throws ResourceException
     */
    public Object post(Object entity) throws ResourceException {
        Object result = null;
        ConverterService cs = null;

        if (getApplication() != null) {
            cs = getApplication().getConverterService();
        } else {
            cs = new ConverterService();
        }

        Representation requestEntity = cs.toRepresentation(entity);
        Representation responseEntity = post(requestEntity);

        if (responseEntity != null) {
            try {
                result = cs.toObject(responseEntity);
            } catch (IOException e) {
                throw new ResourceException(e);
            }
        }

        return result;
    }

    /**
     * Posts a representation to the resource at the target URI reference.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param entity
     *            The posted entity.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.5">HTTP
     *      POST method</a>
     */
    public Representation post(Representation entity) throws ResourceException {
        setMethod(Method.POST);
        getRequest().setEntity(entity);
        return handle();
    }

    /**
     * Creates or updates a resource with the given representation as new state
     * to be stored.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param representation
     *            The representation to store.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.6">HTTP
     *      PUT method</a>
     */
    public Representation put(Representation representation)
            throws ResourceException {
        setMethod(Method.PUT);
        getRequest().setEntity(representation);
        return handle();
    }

    /**
     * Sets the authentication response sent by a client to an origin server.
     * 
     * @param challengeResponse
     *            The authentication response sent by a client to an origin
     *            server.
     * @see Request#setChallengeResponse(ChallengeResponse)
     */
    public void setChallengeResponse(ChallengeResponse challengeResponse) {
        getRequest().setChallengeResponse(challengeResponse);
    }

    /**
     * Sets the client-specific information.
     * 
     * @param clientInfo
     *            The client-specific information.
     * @see Request#setClientInfo(ClientInfo)
     */
    public void setClientInfo(ClientInfo clientInfo) {
        getRequest().setClientInfo(clientInfo);
    }

    /**
     * Sets the conditions applying to this request.
     * 
     * @param conditions
     *            The conditions applying to this request.
     * @see Request#setConditions(Conditions)
     */
    public void setConditions(Conditions conditions) {
        getRequest().setConditions(conditions);
    }

    /**
     * Sets the cookies provided by the client.
     * 
     * @param cookies
     *            The cookies provided by the client.
     * @see Request#setCookies(Series)
     */
    public void setCookies(Series<Cookie> cookies) {
        getRequest().setCookies(cookies);
    }

    /**
     * Indicates if redirections are followed.
     * 
     * @param followRedirects
     *            True if redirections are followed.
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * Sets the host reference.
     * 
     * @param hostRef
     *            The host reference.
     * @see Request#setHostRef(Reference)
     */
    public void setHostRef(Reference hostRef) {
        getRequest().setHostRef(hostRef);
    }

    /**
     * Sets the host reference using an URI string.
     * 
     * @param hostUri
     *            The host URI.
     * @see Request#setHostRef(String)
     */
    public void setHostRef(String hostUri) {
        getRequest().setHostRef(hostUri);
    }

    /**
     * Sets the method called.
     * 
     * @param method
     *            The method called.
     * @see Request#setMethod(Method)
     */
    public void setMethod(Method method) {
        getRequest().setMethod(method);
    }

    /**
     * Sets the next handler such as a Restlet or a Filter.
     * 
     * In addition, this method will set the context of the next Restlet if it
     * is null by passing a reference to its own context.
     * 
     * @param next
     *            The next handler.
     */
    public void setNext(org.restlet.Uniform next) {
        if (next instanceof Restlet) {
            Restlet nextRestlet = (Restlet) next;

            if (nextRestlet.getContext() == null) {
                nextRestlet.setContext(getContext());
            }
        }

        this.next = next;
    }

    /**
     * Sets the original reference requested by the client.
     * 
     * @param originalRef
     *            The original reference.
     * @see Request#setOriginalRef(Reference)
     */
    public void setOriginalRef(Reference originalRef) {
        getRequest().setOriginalRef(originalRef);
    }

    /**
     * Sets the ranges to return from the target resource's representation.
     * 
     * @param ranges
     *            The ranges.
     * @see Request#setRanges(List)
     */
    public void setRanges(List<Range> ranges) {
        getRequest().setRanges(ranges);
    }

    /**
     * Sets the target resource reference. If the reference is relative, it will
     * be resolved as an absolute reference. Also, the context's base reference
     * will be reset. Finally, the reference will be normalized to ensure a
     * consistent handling of the call.
     * 
     * @param resourceRef
     *            The resource reference.
     * @see Request#setResourceRef(Reference)
     */
    public void setReference(Reference resourceRef) {
        getRequest().setResourceRef(resourceRef);
    }

    /**
     * Sets the referrer reference if available.
     * 
     * @param referrerRef
     *            The referrer reference.
     * @see Request#setReferrerRef(Reference)
     */
    public void setReferrerRef(Reference referrerRef) {
        getRequest().setReferrerRef(referrerRef);
    }

    /**
     * Sets the referrer reference if available using an URI string.
     * 
     * @param referrerUri
     *            The referrer URI.
     * @see Request#setReferrerRef(String)
     */
    public void setReferrerRef(String referrerUri) {
        getRequest().setReferrerRef(referrerUri);
    }

    /**
     * Sets the target resource reference using a Reference. Note that the
     * Reference can be either absolute or relative to the context's base
     * reference.
     * 
     * @param resourceRef
     *            The resource Reference.
     * @see Request#setResourceRef(Reference)
     */
    public void setResourceRef(Reference resourceRef) {
        getRequest().setResourceRef(resourceRef);
    }

    /**
     * Sets the target resource reference using an URI string. Note that the URI
     * can be either absolute or relative to the context's base reference.
     * 
     * @param resourceUri
     *            The resource URI.
     * @see Request#setResourceRef(String)
     */
    public void setResourceRef(String resourceUri) {
        getRequest().setResourceRef(resourceUri);
    }

}
