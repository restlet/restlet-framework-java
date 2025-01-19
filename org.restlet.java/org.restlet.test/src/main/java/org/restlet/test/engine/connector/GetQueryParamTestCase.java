/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.engine.connector;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * Test that a simple get with query parameters works for all the connectors.
 * 
 * @author Kevin Conaway
 */
public class GetQueryParamTestCase extends BaseConnectorsTestCase {

    protected String getCallUri(String host) {
        return host + "/test?q1=a&q2=b";
    }

    @Override
    protected void doTestUri(String uri) throws Exception {
        final Client client = new Client(Protocol.HTTP);
        final Request request = new Request(Method.GET, uri);
        final Response response = client.handle(request);

        try {
            assertEquals(response.getStatus().getDescription(), Status.SUCCESS_OK,
                    response.getStatus());
            assertEquals("{q1=a, q2=b}", response.getEntity().getText());
        } finally {
            client.stop();
        }
    }

    @Override
    protected Application createApplication(Component component) {
       return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", GetTestResource.class);
                return router;
            }
        };
    }

    public static class GetTestResource extends ServerResource {
        @Get
        public String toString() {
            Form query = getQuery();
            Map<String, String> valuesMap = query.getValuesMap();
            SortedMap<String, String> sortedMap = new TreeMap<>(valuesMap);
            return sortedMap.toString();
        }
    }

}
