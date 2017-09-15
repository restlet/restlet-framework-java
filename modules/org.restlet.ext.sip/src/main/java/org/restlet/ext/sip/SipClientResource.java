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

package org.restlet.ext.sip;

import java.util.List;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.sip.internal.SipInboundRequest;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

/**
 * Client-side resource for the sip protocol. Acts like a proxy of a target
 * resource.<br>
 * <br>
 * This class changes the semantics of the {@link Resource#getRequest()} and
 * {@link Resource#getResponse()} methods. Since a clientResource may receive
 * severals responses for a single request (in case of interim response), the
 * {@link #getResponse()} method returns the last received response object. The
 * Request object returned by the {@link #getRequest()} is actually a prototype
 * which is cloned (except the representation) just before the {@link #handle()}
 * method is called.<br>
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
 * @deprecated Will be removed to focus on Web APIs.
 */
@Deprecated
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
     * Confirms that the client has received a final response to an INVITE
     * request.
     * 
     * @param representation
     *            The entity to send.
     * @throws ResourceException
     * @see <a href="http://tools.ietf.org/html/rfc2543#section-4.2.2">ACK
     *      method</a>
     */
    public void ack(Representation representation) throws ResourceException {
        handle(SipMethod.ACK, representation);
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
    public Request createRequest() {
        return new SipRequest(getRequest());
    }

    @Override
    protected Response createResponse(Request request) {
        return new SipResponse(request);
    }

    /**
     * Returns the request's command sequence.
     * 
     * @return The request's command sequence.
     */
    public String getCommandSequence() {
        return getRequest().getCommandSequence();
    }

    /**
     * Returns the request initiator's address.
     * 
     * @return The request initiator's address.
     */
    public Address getFrom() {
        return getRequest().getFrom();
    }

    @Override
    public SipRequest getRequest() {
        return (SipRequest) super.getRequest();
    }

    /**
     * Returns the request's call ID.
     * 
     * @return The request's call ID.
     */
    public String getRequestCallId() {
        return getRequest().getCallId();
    }

    @Override
    public SipResponse getResponse() {
        return (SipResponse) super.getResponse();
    }

    /**
     * Returns the response's call ID.
     * 
     * @return The response's call ID.
     */
    public String getResponseCallId() {
        return getResponse().getCallId();
    }

    /**
     * Returns the request's list of Via entries.
     * 
     * @return The request's list of Via entries.
     */
    public List<SipRecipientInfo> getSipRequestRecipientsInfo() {
        return getRequest().getSipRecipientsInfo();
    }

    /**
     * Returns the response's list of Via entries.
     * 
     * @return The response's list of Via entries.
     */
    public List<SipRecipientInfo> getSipResponseRecipientsInfo() {
        return getResponse().getSipRecipientsInfo();
    }

    /**
     * Returns the request recipient's address.
     * 
     * @return The request recipient's address.
     */
    public Address getTo() {
        return getRequest().getTo();
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
        return handle(SipMethod.INFO, representation);
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
     * Indicates that the user or service is being invited to participate in a
     * session.
     * 
     * @param representation
     *            An optional representation.
     * @throws ResourceException
     * @see <a http://tools.ietf.org/html/rfc2543#section-4.2.1">INVITE
     *      method</a>
     */
    public Representation invite(Representation representation)
            throws ResourceException {
        return handle(SipMethod.INVITE, representation);
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
        return handle(SipMethod.NOTIFY, representation);
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
        handle(SipMethod.PUBLISH, representation);
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

    /**
     * Sets the identifier of the call.
     * 
     * @param callId
     *            The identifier of the call.
     */
    public void setCallId(String callId) {
        getRequest().setCallId(callId);
    }

    /**
     * Sets the identifier of the command.
     * 
     * @param commandSequence
     *            The identifier of the command.
     */
    public void setCommandSequence(String commandSequence) {
        getRequest().setCommandSequence(commandSequence);
    }

    /**
     * Sets the description of the request's initiator.
     * 
     * @param from
     *            The description of the request's initiator.
     */
    public void setFrom(Address from) {
        getRequest().setFrom(from);
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

    /**
     * Sets the request's call ID.
     * 
     * @param callId
     *            The call ID.
     */
    public void setRequestCallId(String callId) {
        getRequest().setCallId(callId);
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
     * Sets the logical recipient of the request.
     * 
     * @param to
     *            The logical recipient of the request.
     */
    public void setTo(Address to) {
        getRequest().setTo(to);
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
        handle(SipMethod.SUBSCRIBE, representation);
    }

}
