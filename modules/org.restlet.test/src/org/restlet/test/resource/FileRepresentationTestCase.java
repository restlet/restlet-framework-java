/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.resource;

import java.io.File;
import java.io.FileOutputStream;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.io.BioUtils;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the FileRepresentation class.
 * 
 * @author Kevin Conaway
 */
public class FileRepresentationTestCase extends RestletTestCase {

    private Component component;

    private File file;

    private File testDir;

    private String uri;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        uri = "http://localhost:" + TEST_PORT + "/";

        component = new Component();
        component.getServers().add(Protocol.HTTP, TEST_PORT);
        component.start();

        // Create a temporary directory for the tests
        this.testDir = new File(System.getProperty("java.io.tmpdir"),
                "FileRepresentationTestCase");
        this.testDir.mkdirs();

        this.file = new File(this.testDir, getClass().getName());
        FileOutputStream os = new FileOutputStream(file);
        os.write("abc".getBytes());
        os.close();
    }

    @Override
    public void tearDown() throws Exception {
        component.stop();
        BioUtils.delete(this.file);
        BioUtils.delete(this.testDir, true);
        component = null;
        this.file = null;
        this.testDir = null;
        super.tearDown();
    }

    public void testConstructors() {
        final File file = new File("test.txt");

        final FileRepresentation r = new FileRepresentation(file,
                MediaType.TEXT_PLAIN);

        assertEquals("test.txt", r.getDisposition().getFilename());
        assertEquals(MediaType.TEXT_PLAIN, r.getMediaType());
        assertNull(r.getExpirationDate());
    }

    public void testFileName() throws Exception {
        Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                return new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(new FileRepresentation(file,
                                MediaType.TEXT_PLAIN));
                        response.getEntity().getDisposition().setType(
                                Disposition.TYPE_ATTACHMENT);
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
        assertEquals(getClass().getName(), entity.getDisposition()
                .getFilename());
        client.stop();
    }

}
