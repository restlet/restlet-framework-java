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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Range;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Test {@link org.restlet.data.Range}.
 * 
 * @author Jerome Louvel
 */
public class RangeTestCase extends TestCase {

    // Liste des tests à réaliser pour chaque couple connecteur client/serveur.
    // 1- un client fait des GET avec différents exemples de ranges (et liste de
    // ranges)
    // * vérifier que le serveur les récupère bien
    // * vérifier ensuite la réponse
    // 2- un client fait des PUT avec une représentation partielle

    /**
     * Internal class used for test purpose
     * 
     */
    private static class TestRangeApplication extends Application {

        @Override
        public Restlet createRoot() {
            Router router = new Router();
            router.attach("/test", new TestRangeRestlet());
            return router;
        }
    }

    /**
     * Internal class used for test purpose. It tests the list of ranges sent by
     * the request and compares it with the values sent into the query
     * 
     */
    private static class TestRangeRestlet extends Restlet {

        @Override
        public void handle(Request request, Response response) {
            Form form = request.getResourceRef().getQueryAsForm();
            List<Range> ranges = request.getRanges();

            boolean match = false;
            for (Parameter parameter : form) {
                long index = 0;
                long length = 0;
                String value = parameter.getValue();
                if (value.startsWith("-")) {
                    index = Range.INDEX_LAST;
                    length = Long.parseLong(value.substring(1));
                } else if (value.endsWith("-")) {
                    index = Long.parseLong(value.substring(0,
                            value.length() - 2));
                    length = Range.SIZE_MAX;
                } else {
                    String[] tab = value.split("-");
                    if (tab.length == 2) {
                        index = Long.parseLong(tab[0]);
                        length = index + Long.parseLong(tab[1]);
                    }
                }

                boolean found = false;
                for (Range range : ranges) {
                    found = (index == range.getIndex())
                            && (length == range.getSize());
                    if (found) {
                        break;
                    }
                }
                if (!found) {
                    break;
                } else {
                    match = true;
                }
            }
            if (match) {
                response.setStatus(Status.SUCCESS_OK);
            } else {
                response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
        }
    }

    /**
     * Tests ranges withour representations.
     */
    @Test
    public void testRanges() {

        try {
            Component component = new Component();
            component.getServers().add(Protocol.HTTP, 8182);
            component.getDefaultHost().attach(new TestRangeApplication());
            component.start();
            Client client = new Client(Protocol.HTTP);
            Request request = new Request(Method.GET,
                    "http://localhost:8182/test?range=0-500");
            request.setRanges(Arrays.asList(new Range(0, 500)));
            assertEquals(Status.SUCCESS_OK, client.handle(request).getStatus());

            request = new Request(Method.GET,
                    "http://localhost:8182/test?range=-500");
            request.setRanges(Arrays.asList(new Range(Range.INDEX_LAST, 500)));
            assertEquals(Status.SUCCESS_OK, client.handle(request).getStatus());

            request = new Request(Method.GET,
                    "http://localhost:8182/test?range=500-");
            request.setRanges(Arrays.asList(new Range(500, Range.SIZE_MAX)));
            assertEquals(Status.SUCCESS_OK, client.handle(request).getStatus());

            request = new Request(Method.GET,
                    "http://localhost:8182/test?range=500-1000");
            request.setRanges(Arrays.asList(new Range(500, 500)));
            assertEquals(Status.SUCCESS_OK, client.handle(request).getStatus());

            request = new Request(Method.GET,
                    "http://localhost:8182/test?range=500-1000&range=500-");
            request.setRanges(Arrays.asList(new Range(500, 500), new Range(500,
                    Range.SIZE_MAX)));
            assertEquals(Status.SUCCESS_OK, client.handle(request).getStatus());

            component.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
