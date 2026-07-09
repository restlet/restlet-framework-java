/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.openapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.restlet.ext.openapi.OpenApiApplication.OPENAPI_SPECIFICATION_DEFAULT_PATH;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.ext.openapi.OpenApiSpecifications.ValidationResult.Invalid;
import org.restlet.ext.openapi.example.BasicAuthenticationRootRouterExample;
import org.restlet.ext.openapi.example.BasicAuthenticationSubRouterExample;
import org.restlet.ext.openapi.example.LibraryExample;
import org.restlet.ext.openapi.example.SubRouterExample;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

class OpenApiGenerationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    Component component;
    Server server;

    @BeforeEach
    void setup() throws Exception {
        component = new Component();
        server =
                component
                        .getServers()
                        .add(
                                Protocol.HTTP, 0 // 0 = let the OS find an ephemeral port
                                );
        component.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        component.stop();
    }

    @ParameterizedTest(name = "test {0}")
    @MethodSource("openApiApplicationTestCases")
    void testOpenApi(
            final String testCase,
            final Application application,
            final String oas3File,
            final ChallengeResponse challengeResponse)
            throws Exception {
        component.getDefaultHost().attach(application);

        int actualPort = server.getEphemeralPort();

        System.out.println("Server started on: http://localhost:" + actualPort);

        ClientResource clientResource =
                new ClientResource(
                        "http://localhost:" + actualPort + OPENAPI_SPECIFICATION_DEFAULT_PATH);

        if (challengeResponse != null) {
            clientResource.setChallengeResponse(challengeResponse);
        }

        Representation representation = clientResource.get();

        if (!clientResource.getStatus().isSuccess()) {
            fail("Failed to retrieve OpenAPI specification: " + clientResource.getStatus());
        }

        String actualYamlResponse = parseAndFormatYaml(representation.getText());
        String expectedYamlResponse =
                parseAndFormatYaml(OpenApiSpecifications.readFromClasspath(oas3File));

        assertEquals(expectedYamlResponse, actualYamlResponse);

        var validationResult = OpenApiSpecifications.validate(actualYamlResponse);

        if (validationResult instanceof Invalid(var validationErrors)) {
            fail("Generated OpenAPI specification is invalid: " + validationErrors);
        }
    }

    public static Stream<Arguments> openApiApplicationTestCases() {
        return Stream.of(
                Arguments.of(
                        "library",
                        new LibraryExample.LibraryApplication(),
                        "/library-openapi.yaml",
                        null),
                Arguments.of(
                        "sub-router",
                        new SubRouterExample.SubRouterApplication(),
                        "/sub-router-openapi.yaml",
                        null),
                Arguments.of(
                        "basic-authentication-root-router",
                        new BasicAuthenticationRootRouterExample
                                .BasicAuthenticationRootRouterApplication(),
                        "/basic-authentication-root-router-openapi.yaml",
                        new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "login", "password")),
                Arguments.of(
                        "basic-authentication-subrouter",
                        new BasicAuthenticationSubRouterExample
                                .BasicAuthenticationSubRouterApplication(),
                        "/basic-authentication-subrouter-openapi.yaml",
                        new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "login", "password")));
    }

    private String parseAndFormatYaml(String yaml) {
        try {
            var tree = OBJECT_MAPPER.readTree(yaml);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
