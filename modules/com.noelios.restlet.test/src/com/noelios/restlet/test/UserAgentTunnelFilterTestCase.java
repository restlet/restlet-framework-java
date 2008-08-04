/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.test;

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Tests cases for the tunneling of preferences based on user agent.
 */
public class UserAgentTunnelFilterTestCase extends TestCase {

    /** . */
    private static final String URL = "http://localhost:8182/test";

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
        request.setResourceRef(URL);
        request.getClientInfo().getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_XML));
        request.getClientInfo().getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_HTML));

        return request;
    }

    @Override
    public void setUp() {
        this.application = new Application() {
            @Override
            public Restlet createRoot() {
                final Router router = new Router(getContext());
                router.attachDefault(UserAgentTestResource.class);
                return router;
            }
        };
    }

    public void testTunnelOff() {
        this.application.getTunnelService().setUserAgentTunnel(false);

        final Response response = this.application.handle(createRequest());
        assertEquals(response.getStatus(), Status.SUCCESS_OK);
        assertEquals(MediaType.TEXT_XML, response.getEntity().getMediaType());
    }

    public void testTunnelOn() {
        this.application.getTunnelService().setUserAgentTunnel(true);

        final Response response = this.application.handle(createRequest());
        assertEquals(response.getStatus(), Status.SUCCESS_OK);
        assertEquals(MediaType.TEXT_HTML, response.getEntity().getMediaType());
    }
}
