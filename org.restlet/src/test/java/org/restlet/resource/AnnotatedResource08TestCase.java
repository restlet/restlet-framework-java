/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.restlet.data.MediaType.APPLICATION_JSON;
import static org.restlet.data.MediaType.APPLICATION_WWW_FORM;
import static org.restlet.data.MediaType.APPLICATION_XML;
import static org.restlet.data.MediaType.TEXT_HTML;

import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
class AnnotatedResource08TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyResource08.class);
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    void testPost(
            final MediaType requestBodyMediaType,
            final String requestBody,
            final MediaType responseBodyMediaType,
            final String responseBody)
            throws IOException, ResourceException {
        Representation input = new StringRepresentation(requestBody, requestBodyMediaType);
        Representation result = clientResource.post(input, responseBodyMediaType);
        assertNotNull(result);
        assertEquals(responseBody, result.getText());
        assertEquals(responseBodyMediaType, result.getMediaType());
    }

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                Arguments.arguments(APPLICATION_XML, "root", APPLICATION_XML, "root1"),
                Arguments.arguments(APPLICATION_XML, "root", APPLICATION_JSON, "root1"),
                Arguments.arguments(APPLICATION_JSON, "root", APPLICATION_XML, "root1"),
                Arguments.arguments(APPLICATION_WWW_FORM, "root", APPLICATION_WWW_FORM, "root2"),
                Arguments.arguments(APPLICATION_WWW_FORM, "root", TEXT_HTML, "root2"));
    }
}
