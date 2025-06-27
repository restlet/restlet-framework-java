/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;

/**
 * Unit tests for the FileRepresentation class.
 *
 * @author Kevin Conaway
 */
public class FileRepresentationTestCase {

    private Component component;

    private Path filePath;

    private String uri;

    @BeforeEach
    protected void setUpEach() throws Exception {
        Engine.register(true);
        component = new Component();
        Server server = component.getServers().add(Protocol.HTTP, 0);
        component.start();
        uri = "http://localhost:" + server.getActualPort() + "/";

        // Create a temporary directory for the tests
        this.filePath = Files.createTempFile("FileRepresentationTestCase", "test.txt");
        Files.writeString(filePath, "abc");
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
        component.stop();
        component = null;
    }

    @Test
    public void testConstructors() {
        final File file = new File("test.txt");

        final FileRepresentation r = new FileRepresentation(file, MediaType.TEXT_PLAIN);

        assertEquals("test.txt", r.getDisposition().getFilename());
        assertEquals(MediaType.TEXT_PLAIN, r.getMediaType());
        assertNull(r.getExpirationDate());
    }

    @Test
    public void testFileName() throws Exception {
        Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                return new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity(new FileRepresentation(filePath.toFile(), MediaType.TEXT_PLAIN));
                        response.getEntity().getDisposition()
                                .setType(Disposition.TYPE_ATTACHMENT);
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
        assertEquals(filePath.toFile().getName(), entity.getDisposition().getFilename());
        client.stop();
    }

}
