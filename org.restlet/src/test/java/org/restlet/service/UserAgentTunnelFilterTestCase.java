/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Status;
import org.restlet.routing.Router;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests cases for the tunneling of preferences based on user agent.
 */
public class UserAgentTunnelFilterTestCase {

    private Application application;

    /**
     * Creates a new Request object.
     *
     * @return A new Request object.
     */
    private Request createRequest() {
        final Request request = new Request();
        request.setMethod(Method.GET);
        request.getClientInfo().setAgent("msie/1.1");
        request.setResourceRef("/test");
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(MediaType.TEXT_XML));
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(MediaType.TEXT_HTML));

        return request;
    }

    @BeforeEach
    public void setUpEach() {
        this.application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                Router router = new Router(getContext());
                router.attachDefault(UserAgentTestResource.class);
                return router;
            }
        };
    }

    @Test
    public void testTunnelOff() {
        this.application.getTunnelService().setUserAgentTunnel(false);
        Request request = createRequest();
        Response response = new Response(request);
        this.application.handle(request, response);
        assertEquals(response.getStatus(), Status.SUCCESS_OK);
        assertEquals(MediaType.TEXT_XML, response.getEntity().getMediaType());
    }

    @Test
    public void testTunnelOn() {
        this.application.getTunnelService().setUserAgentTunnel(true);
        Request request = createRequest();
        Response response = new Response(request);
        this.application.handle(request, response);
        assertEquals(response.getStatus(), Status.SUCCESS_OK);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
    }
}
