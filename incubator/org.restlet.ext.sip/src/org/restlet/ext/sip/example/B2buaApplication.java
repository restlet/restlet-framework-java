package org.restlet.ext.sip.example;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Uniform;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.ext.sip.SipRecipientInfo;
import org.restlet.ext.sip.SipRequest;
import org.restlet.ext.sip.SipResponse;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;

public class B2buaApplication {

    private static class B2buaRedirector extends Redirector {

        public B2buaRedirector(Context context, String targetTemplate) {
            super(context, targetTemplate);
        }

        public B2buaRedirector(Context context, String targetTemplate, int mode) {
            super(context, targetTemplate, mode);
        }

        @Override
        protected void outboundServerRedirect(Reference targetRef,
                Request request, Response response) {
            SipRequest r = (SipRequest) request;
            SipRecipientInfo sri = new SipRecipientInfo();
            sri.setProtocol(Protocol.SIP);
            sri.setName("b2bua");
            r.getSipRecipientsInfo().add(sri);

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
            // Update the request to cleanly go to the target URI
            request.setResourceRef(targetRef);
            request.getAttributes().remove(HeaderConstants.ATTRIBUTE_HEADERS);

            request.setOnResponse(new Uniform() {
                public void handle(Request req, Response resp) {
                    System.err.println(req.getMethod() + "resp.getStatus() " + resp.getStatus());
                    if (!resp.getStatus().isInformational()) {
                        // Allow for response rewriting and clean the headers
                        response.setEntity(rewrite(response.getEntity()));
                        request.getAttributes().remove(
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
                    } else {
                        SipResponse provisionalResponse = new SipResponse(req);
                        provisionalResponse.setStatus(resp.getStatus());
                        provisionalResponse.commit();
                    }
                }
            });

            next.handle(request, response);
        }

    }

    public static void main(String[] args) throws Exception {
        // Start the origin server on port 8182
        String[] arguments = new String[1];
        arguments[0] = "8182";
        UacServerResource.main(arguments);

        Component c = new Component();
        c.getServers().add(Protocol.SIP);
        Client client = new Client(new Context(), Protocol.SIP);
        client.getContext().getParameters().add("hostDomain", "localhost");
        client.getContext().getParameters().add("hostPort", arguments[0]);
        client.getContext().getParameters()
                .add("pipeliningConnections", "true");
        c.getClients().add(client);

        c.getDefaultHost().attachDefault(
                new B2buaRedirector(null, "sip:localhost:8182"));
        c.start();
    }
}
