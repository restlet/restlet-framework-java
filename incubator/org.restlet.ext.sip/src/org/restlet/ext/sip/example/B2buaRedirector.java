package org.restlet.ext.sip.example;

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
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.ext.sip.Address;
import org.restlet.ext.sip.SipRecipientInfo;
import org.restlet.ext.sip.SipRequest;
import org.restlet.ext.sip.SipResponse;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;

/**
 * Redirector that implements the B2BUA scenario.
 */
public class B2buaRedirector extends Redirector {

    public static void main(String[] args) throws Exception {
        String[] arguments = new String[1];
        arguments[0] = "8182";
        // Start the origin server on port 8182
        // UacServerResource.main(arguments);

        Component c = new Component();
        Server server = new Server(Protocol.SIP);
        c.getServers().add(server);
        server.getContext().getParameters().add("tracing", "true");

        Client client = new Client(Protocol.SIP);
        c.getClients().add(client);
        client.getContext().getParameters().add("proxyHost", "localhost");
        client.getContext().getParameters().add("proxyPort", arguments[0]);
        client.getContext().getParameters().add("tracing", "true");
        client.getContext().getParameters().add("pipeliningConnections",
                "false");

        c.getDefaultHost().attachDefault(
                new B2buaRedirector(null, "sip:localhost:8182"));
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
        
        Address to = new Address();
        to.setReference(new Reference("127.0.0.1:8182"));
        to.setDisplayName("test");
        to.getParameters().add("tag", "aTag");
        r.setTo(to);
        
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
        //r.setResourceRef(targetRef);

        response.setAutoCommitting(false);
        next.handle(r, response);
    }

}
