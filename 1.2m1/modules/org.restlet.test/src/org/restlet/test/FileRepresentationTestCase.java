/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import java.io.File;
import java.io.FileOutputStream;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;

/**
 * Unit tests for the FileRepresentation class.
 * 
 * @author Kevin Conaway
 */
public class FileRepresentationTestCase extends RestletTestCase {

    private String uri;

    private Component component;

    private File file;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        uri = "http://localhost:" + getPort() + "/";

        component = new Component();
        component.getServers().add(Protocol.HTTP, getPort());
        component.start();

        file = new File(getClass().getName());
        FileOutputStream os = new FileOutputStream(file);
        os.write("abc".getBytes());
        os.close();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        component.stop();
    }

    public void testConstructors() {
        final File file = new File("test.txt");

        final FileRepresentation r = new FileRepresentation(file,
                MediaType.TEXT_PLAIN);

        assertEquals("test.txt", r.getDownloadName());
        assertEquals(MediaType.TEXT_PLAIN, r.getMediaType());
        assertNull(r.getExpirationDate());
    }

    public void testFileName() throws Exception {
        Application application = new Application() {
            @Override
            public Restlet createRoot() {
                return new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(new FileRepresentation(file,
                                MediaType.TEXT_PLAIN));
                        response.getEntity().setDownloadable(true);
                    }
                };
            }
        };

        component.getDefaultHost().attach(application);

        Client client = new Client(new Context(), Protocol.HTTP);
        Request request = new Request(Method.GET, uri);
        Response response = client.handle(request);
        Representation entity = response.getEntity();

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("abc", entity.getText());
        assertEquals(getClass().getName(), entity.getDownloadName());
    }

}
