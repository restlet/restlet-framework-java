/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Series;

/**
 * Client-side resource. Acts like a proxy of a target resource.
 * 
 * @author Jerome Louvel
 */
public class ClientResource extends UniformResource {

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
        this(null, method, reference);
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
        this(null, method, uri);
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
        this(null, method, uri);
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The target reference.
     */
    public ClientResource(Reference reference) {
        this(null, null, reference);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public ClientResource(String uri) {
        this(null, null, uri);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public ClientResource(URI uri) {
        this(null, null, uri);
    }

    /**
     * Deletes the target resource and all its representations.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The optional response entity.
     */
    @Override
    public Representation delete() throws ResourceException {
        getRequest().setMethod(Method.DELETE);
        return handle();
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
    @Override
    public Representation get() throws ResourceException {
        getRequest().setMethod(Method.GET);
        return handle();
    }

    /**
     * Represents the resource using a given variant.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param variant
     *            The variant representation to retrieve.
     * @return The representation matching the given variant.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.3">HTTP
     *      GET method</a>
     */
    @Override
    public Representation get(Variant variant) throws ResourceException {
        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        setClientInfo(variant.createClientInfo());
        Representation result = get();

        // Restore the current client info
        setClientInfo(currentClientInfo);
        return result;
    }

    /**
     * Retrieve the allowed methods on the target resource. This is done by
     * invoking {@link #head()}.
     */
    @Override
    public Set<Method> getAllowedMethods() {
        Set<Method> result = null;

        try {
            head();
            result = getResponse().getAllowedMethods();
        } catch (ResourceException e) {
            getLogger()
                    .log(
                            Level.INFO,
                            "Unable to determine the allowed methods. The HEAD call failed.",
                            e);
        }

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
     * Handles the call by invoking the next handler.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @return The optional response entity.
     * @throws ResourceException
     * @see #getNext()
     */
    @Override
    public Representation handle() throws ResourceException {
        Representation result = null;

        if (!hasNext()) {
            Protocol protocol = (getReference() == null) ? null
                    : getReference().getSchemeProtocol();

            if (protocol != null) {
                setNext(new Client(protocol));
            }
        }

        if (hasNext()) {
            getNext().handle(getRequest(), getResponse());
            result = getResponse().getEntity();

            if (!getResponse().getStatus().isSuccess()) {
                throw new ResourceException(getStatus());
            }
        } else {
            getLogger()
                    .warning(
                            "Unable to process the call for a client resource. No next Restlet has been provided.");
        }

        return result;
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
    @Override
    public Representation head() throws ResourceException {
        getRequest().setMethod(Method.HEAD);
        return handle();
    }

    /**
     * Represents the resource using a given variant. This method is identical
     * to {@link #get(Variant)} but doesn't return the actual content of the
     * representation, only its metadata.<br>
     * <br>
     * Note that the client preferences will be automatically adjusted, but only
     * for this request. If you want to change them once for all, you can use
     * the {@link #getClientInfo()} method.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param variant
     * @return The representation matching the given variant.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.4">HTTP
     *      HEAD method</a>
     */
    @Override
    public Representation head(Variant variant) throws ResourceException {
        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        setClientInfo(variant.createClientInfo());
        Representation result = head();

        // Restore the current client info
        setClientInfo(currentClientInfo);
        return result;
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
    @Override
    public Representation options() throws ResourceException {
        getRequest().setMethod(Method.HEAD);
        return handle();
    }

    /**
     * Describes the resource using a given variant.<br>
     * <br>
     * If a success status is not returned, then a resource exception is thrown.
     * 
     * @param variant
     *            The description variant to match.
     * @return The matched description or null.
     * @throws ResourceException
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9.2">HTTP
     *      OPTIONS method</a>
     */
    @Override
    public Representation options(Variant variant) throws ResourceException {
        // Save the current client info
        ClientInfo currentClientInfo = getClientInfo();

        // Create a fresh one for this request
        setClientInfo(variant.createClientInfo());
        Representation result = options();

        // Restore the current client info
        setClientInfo(currentClientInfo);
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
    @Override
    public Representation post(Representation entity) throws ResourceException {
        getRequest().setMethod(Method.POST);
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
    @Override
    public Representation put(Representation representation)
            throws ResourceException {
        getRequest().setMethod(Method.PUT);
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
