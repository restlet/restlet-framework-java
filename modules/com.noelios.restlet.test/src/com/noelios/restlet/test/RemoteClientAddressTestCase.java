/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.test;

import org.junit.Assert;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Test that the client address is available for all the connectors 
 * 
 * @author Kevin Conaway
 */
public class RemoteClientAddressTestCase extends BaseConnectorsTestCase {

    @Override
    protected Application createApplication(Component component) {
        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                Router router = new Router(getContext());
                router.attach("/test", RemoteClientAddressResource.class);
                return router;
            }
        };

        return application;
    }

    @Override
    protected void call() throws Exception {
        Request request = new Request(Method.GET, uri);
        Response r = new Client(Protocol.HTTP).handle(request);
        
        assertEquals(Status.SUCCESS_OK, r.getStatus());
    }

    public static class RemoteClientAddressResource extends Resource {

        public RemoteClientAddressResource(Context ctx, Request request, Response response) {
            super(ctx, request, response);
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation getRepresentation(Variant variant) {

            Assert.assertEquals("127.0.0.1", getRequest().getClientInfo().getAddress());
            Assert.assertTrue(getRequest().getClientInfo().getPort() > 0);
            
            return new StringRepresentation("OK");
        }
    }
}
