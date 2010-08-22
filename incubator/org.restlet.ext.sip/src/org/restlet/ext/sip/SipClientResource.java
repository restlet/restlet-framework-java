/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.ext.sip;

import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Uniform;
import org.restlet.data.ClientInfo;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.sip.internal.SipInboundRequest;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.UniformResource;

/**
 * Client-side resource for the sip protocol. Acts like a proxy of a target
 * resource.<br>
 * <br>
 * This class changes the semantics of the {@link UniformResource#getRequest()}
 * and {@link UniformResource#getResponse()} methods. Since a clientResource may
 * receive severals responses for a single request (in case of interim
 * response), the {@link #getResponse()} method returns the last received
 * response object. The Request object returned by the {@link #getRequest()} is
 * actually a prototype which is cloned (except the representation) just before
 * the {@link #handle()} method is called.<br>
 * <br>
 * Users must be aware that by most representations can only be read or written
 * once. Some others, such as {@link StringRepresentation} stored the entity in
 * memory which can be read several times but has the drawback to consume
 * memory.<br>
 * <br>
 * Concurrency note: instances of the class are not designed to be shared among
 * several threads. If thread-safety is necessary, consider using the
 * lower-level {@link Client} class instead.
 * 
 * @author Thierry Boileau, Jerome Louvel
 */
public class SipClientResource extends ClientResource {

    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls.
     * 
     * @param <T>
     * @param context
     *            The context.
     * @param reference
     *            The target reference.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(Context context, Reference reference,
            Class<? extends T> resourceInterface) {
        SipClientResource clientResource = new SipClientResource(context,
                reference);
        return clientResource.wrap(resourceInterface);
    }

    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls.
     * 
     * @param <T>
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(Reference reference,
            Class<? extends T> resourceInterface) {
        return create(null, reference, resourceInterface);
    }

    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls.
     * 
     * @param <T>
     * @param uri
     *            The target URI.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T create(String uri, Class<? extends T> resourceInterface) {
        return create(null, new Reference(uri), resourceInterface);
    }

    /**
     * Constructor.
     * 
     * @param resource
     *            The client resource to copy.
     */
    public SipClientResource(SipClientResource resource) {
        super();
        SipRequest request = new SipRequest(resource.getRequest());
        SipResponse response = new SipResponse(request);
        setNext(resource.getNext());
        setFollowingRedirects(resource.isFollowingRedirects());
        setRetryOnError(resource.isRetryOnError());
        setRetryDelay(resource.getRetryDelay());
        setRetryAttempts(resource.getRetryAttempts());
        init(resource.getContext(), request, response);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public SipClientResource(Context context, java.net.URI uri) {
        this(context, SipMethod.INVITE, uri);
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
    public SipClientResource(Context context, Method method, java.net.URI uri) {
        this(context, method, new Reference(uri));
    }

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
    public SipClientResource(Context context, Method method, Reference reference) {
        this(context, new SipRequest(method, reference), new SipResponse(null));
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
    public SipClientResource(Context context, Method method, String uri) {
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
    public SipClientResource(Context context, Reference reference) {
        this(context, SipMethod.INVITE, reference);
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
    public SipClientResource(Context context, SipRequest request,
            SipResponse response) {
        super(context, request, response);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param uri
     *            The target URI.
     */
    public SipClientResource(Context context, String uri) {
        this(context, SipMethod.INVITE, uri);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public SipClientResource(java.net.URI uri) {
        this(Context.getCurrent(), null, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param uri
     *            The target URI.
     */
    public SipClientResource(Method method, java.net.URI uri) {
        this(Context.getCurrent(), method, uri);
    }

    /**
     * Constructor.
     * 
     * @param method
     *            The method to call.
     * @param reference
     *            The target reference.
     */
    public SipClientResource(Method method, Reference reference) {
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
    public SipClientResource(Method method, String uri) {
        this(Context.getCurrent(), method, uri);
    }

    /**
     * Constructor.
     * 
     * @param reference
     *            The target reference.
     */
    public SipClientResource(Reference reference) {
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
    public SipClientResource(SipRequest request, SipResponse response) {
        this(Context.getCurrent(), request, response);
    }

    /**
     * Constructor.
     * 
     * @param uri
     *            The target URI.
     */
    public SipClientResource(String uri) {
        this(Context.getCurrent(), null, uri);
    }

    /**
     * Confirms that the client has received a final response to an INVITE
     * request.
     * 
     * @throws ResourceException
     * @see <a href="http://tools.ietf.org/html/rfc2543#section-4.2.2">ACK
     *      method</a>
     */
    public void ack() throws ResourceException {
        handle(SipMethod.ACK);
    }

    /**
     * Indicates to the server that the user agent wishes to release the call.
     * 
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2543#section-4.2.4">BYE method</a>
     */
    public Representation bye() throws ResourceException {
        return handle(SipMethod.BYE);
    }

    /**
     * Cancels a pending request with the same Call-ID, To, From and CSeq
     * (sequence number only) header field values.
     * 
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2543#section-4.2.5">CANCEL
     *      method</a>
     */
    public void cancel() throws ResourceException {
        handle(SipMethod.CANCEL);
    }

    @Override
    public SipRequest getRequest() {
        return (SipRequest) super.getRequest();
    }

    @Override
    public SipResponse getResponse() {
        return (SipResponse) super.getResponse();
    }

    /**
     * Handles the call by invoking the next handler. The prototype request is
     * retrieved via {@link #getRequest()} and cloned and the response is set as
     * the latest with {@link #setResponse(Response)}. If necessary the
     * {@link #setNext(Uniform)} method is called as well with a {@link Client}
     * instance matching the request protocol.
     * 
     * @return The optional response entity.
     * @see #getNext()
     */
    @Override
    public Representation handle() {
        // TODO refactor ClientResource.
        // if (getRequest() instanceof SipRequest) {
        // SipResponse response = handle(new SipRequest(
        // (SipRequest) getRequest()));
        // return (response == null) ? null : response.getEntity();
        // }
        return null;

    }

    /**
     * Handles the call by cloning the prototype request, setting the method and
     * entity.
     * 
     * @param method
     *            The request method to use.
     * @return The optional response entity.
     */
    private Representation handle(Method method) {
        // TODO refactor ClientResource.
        // return handle(method, (Representation) null, getClientInfo());
        return null;
    }

    /**
     * Handles the call by cloning the prototype request, setting the method and
     * entity.
     * 
     * @param method
     *            The request method to use.
     * @param entity
     *            The request entity to set.
     * @param clientInfo
     *            The client preferences.
     * @return The optional response entity.
     */
    private Representation handle(Method method, Representation entity,
            ClientInfo clientInfo) {
        Representation result = null;

        // Prepare the request by cloning the prototype request
        // TODO refactor ClientResource.
        // SipRequest request;
        // if (getRequest() instanceof SipRequest) {
        // request = new SipRequest((SipRequest) getRequest());
        // request.setMethod(method);
        // request.setEntity(entity);
        // request.setClientInfo(clientInfo);
        //
        // // Actually handle the call
        // Response response = handle(request);
        //
        // if (response.getStatus().isError()) {
        // throw new ResourceException(response.getStatus());
        // } else {
        // result = (response == null) ? null : response.getEntity();
        // }
        // }

        return result;
    }

    /**
     * Handles the call by invoking the next handler. Then a new response is
     * created and the {@link #handle(Request, Response)} method is invoked and
     * the response set as the latest response with
     * {@link #setResponse(Response)}.
     * 
     * @param request
     *            The request to handle.
     * @return The response created.
     * @see #getNext()
     */
    @SuppressWarnings("unused")
    private SipResponse handle(SipInboundRequest request) {
        // TODO refactor ClientResource.
        return null;
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
     * @param retryAttempt
     *            The number of remaining attempts.
     * @param next
     *            The next handler handling the call.
     */
    @SuppressWarnings("unused")
    private void handle(SipInboundRequest request, SipResponse response,
            List<Reference> references, int retryAttempt, Uniform next) {
        if (next != null) {
            // Actually handle the call
            next.handle(request, response);
        }
    }

    /**
     * Communicating mid-session signaling information along the signaling path
     * for the call.
     * 
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2976#section-2">INFO method</a>
     */
    public Representation info() throws ResourceException {
        return handle(SipMethod.INFO);
    }

    /**
     * Communicating mid-session signaling information along the signaling path
     * for the call.
     * 
     * @param representation
     *            An optional representation.
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2976#section-2">INFO method</a>
     */
    public Representation info(Representation representation)
            throws ResourceException {
        return handle(SipMethod.INFO, representation, getClientInfo());
    }

    /**
     * Indicates that the user or service is being invited to participate in a
     * session.
     * 
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2543#section-4.2.1">INVITE
     *      method</a>
     */
    public Representation invite() throws ResourceException {
        return handle(SipMethod.INVITE);
    }

    /**
     * Informs subscribers of changes in state to which the subscriber has a
     * subscription.
     * 
     * @param representation
     *            The notification representation.
     * @return The optional result entity.
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc3265#section-3.2">NOTIFY method</a>
     */
    public Representation notify(Representation representation)
            throws ResourceException {
        return handle(SipMethod.NOTIFY, representation, getClientInfo());
    }

    /**
     * Queries a SIP server as to its capabilities.
     * 
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2543#section-4.2.3">OPTIONS
     *      method</a>
     */
    public Representation options() throws ResourceException {
        return handle(SipMethod.OPTIONS);
    }

    /**
     * Creates, modifies, and removes event state associated with an
     * address-of-record.
     * 
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc3903#section-4">PUBLISH method</a>
     */
    public void publish() throws ResourceException {
        handle(SipMethod.PUBLISH);
    }

    /**
     * Creates, modifies, and removes event state associated with an
     * address-of-record.
     * 
     * @param representation
     *            The optional request entity.
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc3903#section-4">PUBLISH method</a>
     */
    public void publish(Representation representation) throws ResourceException {
        handle(SipMethod.PUBLISH, representation, getClientInfo());
    }

    /**
     * Indicates that the target recipient should contact a third party using
     * the contact information provided in the request.
     * 
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc3515#section-2">REFER method</a>
     */
    public void refer() throws ResourceException {
        handle(SipMethod.REFER);
    }

    /**
     * Registers the address listed in the To header field with a SIP server.
     * 
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2543#section-4.2.6">REGISTER
     *      method</a>
     */
    public void register() throws ResourceException {
        handle(SipMethod.REGISTER);
    }

    /**
     * Registers the address listed in the To header field with a SIP server.
     * 
     * @param to
     *            The To header field.
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2543#section-4.2.6">REGISTER
     *      method</a>
     */
    public void register(Address to) throws ResourceException {
        SipInboundRequest request = (SipInboundRequest) getRequest();
        request.setTo(to);
        handle(SipMethod.REGISTER);
    }

    @Override
    public void setRequest(Request request) {
        if (request instanceof SipRequest) {
            super.setRequest(request);
        } else {
            throw new IllegalArgumentException(
                    "Only SipRequest instances are allowed as parameter");
        }
    }

    @Override
    public void setResponse(Response response) {
        if (response instanceof SipResponse) {
            super.setResponse(response);
        } else {
            throw new IllegalArgumentException(
                    "Only SipResponse instances are allowed as parameter");
        }
    }

    /**
     * Requests current state and state updates from a remote node.
     * 
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc3265#section-3.1">SUBSCRIBE
     *      method</a>
     */
    public void subscribe() throws ResourceException {
        handle(SipMethod.SUBSCRIBE);
    }

    /**
     * Requests current state and state updates from a remote node.
     * 
     * @param representation
     *            The optional request entity.
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc3265#section-3.1">SUBSCRIBE
     *      method</a>
     */
    public void subscribe(Representation representation)
            throws ResourceException {
        handle(SipMethod.SUBSCRIBE, representation, getClientInfo());
    }

}
