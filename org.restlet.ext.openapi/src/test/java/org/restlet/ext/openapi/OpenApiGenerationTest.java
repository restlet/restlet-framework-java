/**
 * Copyright 2005-2026 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open source license available at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.restlet.ext.openapi.OpenApiApplication.OPENAPI_SPECIFICATION_DEFAULT_PATH;

import org.junit.jupiter.api.Test;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.openapi.OpenApiSpecifications.ValidationResult.Invalid;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

public class OpenApiGenerationTest {

    @Test
    public void testLibraryApplicationOpenApi() throws Exception {
        var application = new Library.LibraryApplication();
        var component = new Component();

        var server = component.getServers().add(
            Protocol.HTTP,
            0 // 0 = let the OS find an ephemeral port
        );

        component.getDefaultHost().attach(application);
        component.start();

        int actualPort = server.getEphemeralPort();

        System.out.println("Server started on: http://localhost:" + actualPort);

        ClientResource clientResource = new ClientResource(
            "http://localhost:" + actualPort + OPENAPI_SPECIFICATION_DEFAULT_PATH
        );

        Representation representation = clientResource.get();

        if (!clientResource.getStatus().isSuccess()) {
            fail("Failed to retrieve OpenAPI specification: " + clientResource.getStatus());
        }

        String actualYamlResponse = representation.getText();
        String expectedYamlResponse = OpenApiSpecifications.readFromClasspath("/library-openapi.yaml");

        assertEquals(expectedYamlResponse, actualYamlResponse);

        var validationResult = OpenApiSpecifications.validate(actualYamlResponse);

        if (validationResult instanceof Invalid(var validationErrors)) {
            fail("Generated OpenAPI specification is invalid: " + validationErrors);
        }
    }
}
