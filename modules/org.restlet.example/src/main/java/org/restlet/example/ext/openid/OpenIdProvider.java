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

package org.restlet.example.ext.openid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;

/**
 * OpenID provider representation implementing an open identity IdP. At the
 * moment it is only used for testing.
 * 
 * User backend is not specified and if is only returning one user 'foo'
 * 
 * @author Kristoffer Gronowski
 */
// TODO yadisResolver.discoverRP(realm);
public class OpenIdProvider extends ServerResource {

    public OpenIdProvider() {
        getLogger().info("OpenID CREATED NEW PROVIDER");
    }

    @Post("form")
    public Representation represent(Representation input) {
        Form f = new Form(input);
        return handle(new ParameterList(f.getValuesMap()));
    }

    @Get("form")
    public Representation represent() {

        return handle(new ParameterList(getQuery().getValuesMap()));
    }

    private Representation handle(ParameterList request) {
        Logger log = getLogger();
        log.info("Handle on OP");
        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        ServerManager manager = (ServerManager) attribs.get("openid_manager");
        log.info("OP endpoint = " + manager.getOPEndpointUrl());

        String mode = request.hasParameter("openid.mode") ? request
                .getParameterValue("openid.mode") : null;

        Message response;
        String responseText;

        if ("associate".equals(mode)) {
            // --- process an association request ---
            response = manager.associationResponse(request);
            responseText = response.keyValueFormEncoding();
        } else if ("checkid_setup".equals(mode)
                || "checkid_immediate".equals(mode)) {
            // interact with the user and obtain data needed to continue
            List<?> userData = userInteraction(request,
                    manager.getOPEndpointUrl());

            String userSelectedId = (String) userData.get(0);
            String userSelectedClaimedId = (String) userData.get(1);
            Boolean authenticatedAndApproved = (Boolean) userData.get(2);

            // --- process an authentication request ---
            response = manager.authResponse(request, userSelectedId,
                    userSelectedClaimedId,
                    authenticatedAndApproved.booleanValue());

            if (response instanceof DirectError) {
                Form f = new Form();
                @SuppressWarnings("unchecked")
                Map<String, String> m = (Map<String, String>) response
                        .getParameterMap();
                for (String key : m.keySet()) {
                    f.add(key, m.get(key));
                }
                return f.getWebRepresentation();
            } else {
                // caller will need to decide which of the following to use:

                // option1: GET HTTP-redirect to the return_to URL
                // return new
                // StringRepresentation(response.getDestinationUrl(true));
                redirectSeeOther(response.getDestinationUrl(true));
                return new EmptyRepresentation();

                // option2: HTML FORM Redirection
                // RequestDispatcher dispatcher =
                // getServletContext().getRequestDispatcher("formredirection.jsp");
                // httpReq.setAttribute("prameterMap",
                // response.getParameterMap());
                // httpReq.setAttribute("destinationUrl",
                // response.getDestinationUrl(false));
                // dispatcher.forward(request, response);
                // return null;
            }
        } else if ("check_authentication".equals(mode)) {
            // --- processing a verification request ---
            response = manager.verify(request);
            log.info("OpenID : " + response.keyValueFormEncoding());
            responseText = response.keyValueFormEncoding();
        } else if (Method.GET.equals(getMethod())) {
            // Could be a discovery request
            sendXRDSLocation();
            return new StringRepresentation("XRDS Discovery Information");
        } else {
            // --- error response ---
            response = DirectError.createDirectError("Unknown request");
            responseText = response.keyValueFormEncoding();
        }

        // return the result to the user
        return new StringRepresentation(responseText);
    }

    private void sendXRDSLocation() {
        ConcurrentMap<String, Object> attribs = getContext().getAttributes();
        String id = getQuery().getFirstValue("id");
        String xrds = attribs.get("xrds").toString();
        String location = (id != null) ? xrds + "?id=" + id : xrds;
        getLogger().info("XRDS endpoint = " + xrds);

        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) getResponse().getAttributes()
                .get(HeaderConstants.ATTRIBUTE_HEADERS);

        if (headers == null) {
            headers = new Series<Header>(Header.class);
            headers.add("X-XRDS-Location", location);
            getResponse().getAttributes().put(
                    HeaderConstants.ATTRIBUTE_HEADERS, headers);
        } else {
            headers.add("X-XRDS-Location", location);
        }

        getLogger().info("Sending empty representation.");
    }

    private List<Object> userInteraction(ParameterList request, String endpoint) {
        StringBuilder id = new StringBuilder();
        id.append(endpoint);
        id.append("?id=");
        id.append("foo");
        List<Object> result = new ArrayList<Object>();
        result.add(0, id.toString());
        result.add(1, id.toString());
        result.add(2, Boolean.TRUE);
        return result;
    }

}
