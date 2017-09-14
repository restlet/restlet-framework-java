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

package org.restlet.example.ext.sip;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.Uniform;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.sip.SipRecipientInfo;
import org.restlet.ext.sip.SipRequest;
import org.restlet.ext.sip.SipResponse;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;

/**
 * Redirector that implements the B2BUA scenario.
 */
@Deprecated
public class B2buaRedirector extends Redirector {

    public static void main(String[] args) throws Exception {
        String[] arguments = new String[1];
        arguments[0] = "8111";

        // Start the origin server on port 8111
        UacServerResource.main(arguments);

        Component c = new Component();
        Server server = new Server(Protocol.SIP);
        c.getServers().add(server);
        server.getContext().getParameters().add("tracing", "true");

        Client client = new Client(Protocol.SIP);
        c.getClients().add(client);
        client.getContext().getParameters().add("proxyHost", "localhost");
        client.getContext().getParameters().add("proxyPort", arguments[0]);
        client.getContext().getParameters().add("tracing", "false");
        client.getContext().getParameters()
                .add("pipeliningConnections", "false");

        c.getDefaultHost().attachDefault(
                new B2buaRedirector(null, "sip:localhost:8111"));
        c.start();
    }

    /**
     * Constructor for the client dispatcher mode.
     * 
     * @param context
     *            The context.
     * @param targetTemplate
     *            The template to build the target URI.
     */
    public B2buaRedirector(Context context, String targetTemplate) {
        super(context, targetTemplate);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param targetPattern
     *            The pattern to build the target URI (using StringTemplate
     *            syntax and the CallModel for variables).
     * @param mode
     *            The redirection mode.
     */
    public B2buaRedirector(Context context, String targetTemplate, int mode) {
        super(context, targetTemplate, mode);
    }

    @Override
    protected void outboundServerRedirect(Reference targetRef, Request request,
            Response response) {
        SipRequest r = (SipRequest) request;
        SipRecipientInfo sri = new SipRecipientInfo();
        sri.setProtocol(Protocol.SIP);
        sri.setTransport("TCP");
        sri.setName("127.0.0.1:5060");
        sri.getParameters().add("branch", "z9hG4bK-20369-1-0");
        r.getSipRecipientsInfo().add(0, sri);
        super.outboundServerRedirect(targetRef, r, response);
    };

    @Override
    protected void serverRedirect(Restlet next, Reference targetRef,
            final Request request, final Response response) {
        // Save the base URI if it exists as we might need it for
        // redirections
        final Reference resourceRef = request.getResourceRef();
        final Reference baseRef = resourceRef.getBaseRef();
        final String targetTemplate = getTargetTemplate();

        // Reset the protocol and let the dispatcher handle the protocol
        // request.setProtocol(null);
        request.getAttributes().remove(HeaderConstants.ATTRIBUTE_HEADERS);
        // Update the request to cleanly go to the target URI
        request.setOnResponse(new Uniform() {
            public void handle(Request req, Response resp) {
                SipResponse r = (SipResponse) resp;

                if (!resp.getStatus().isInformational()) {
                    // Allow for response rewriting and clean the headers
                    response.setEntity(rewrite(response.getEntity()));
                    response.getAttributes().remove(
                            HeaderConstants.ATTRIBUTE_HEADERS);
                    request.setResourceRef(resourceRef);

                    // In case of redirection, we may have to rewrite the
                    // redirect URI
                    if (response.getLocationRef() != null) {
                        Template rt = new Template(targetTemplate);
                        rt.setLogger(getLogger());
                        int matched = rt.parse(response.getLocationRef()
                                .toString(), request);

                        if (matched > 0) {
                            String remainingPart = (String) request
                                    .getAttributes().get("rr");

                            if (remainingPart != null) {
                                response.setLocationRef(baseRef.toString()
                                        + remainingPart);
                            }
                        }
                    }

                    if (!r.getSipRecipientsInfo().isEmpty()) {
                        r.getSipRecipientsInfo().remove(0);
                    }

                    resp.commit();
                } else {
                    SipResponse provisionalResponse = new SipResponse(request);
                    provisionalResponse.setStatus(resp.getStatus());
                    provisionalResponse.commit();
                }
            }
        });
        request.setResourceRef(targetRef);
        SipRequest r = new SipRequest((SipRequest) request);
        // r.setResourceRef(targetRef);

        response.setAutoCommitting(false);
        next.handle(r, response);
    }

}
