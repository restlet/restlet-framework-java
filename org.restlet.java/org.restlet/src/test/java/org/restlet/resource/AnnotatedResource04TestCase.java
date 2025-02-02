/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.resource;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test the annotated resources, client and server sides.
 * 
 * @author Jerome Louvel
 */
public class AnnotatedResource04TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyResource04.class);
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void testGet(final MediaType responseMediaType, final String responseBody) throws IOException, ResourceException {
        Representation result = clientResource.get(responseMediaType);
        assertNotNull(result);
        assertEquals(responseBody, result.getText());
        assertEquals(responseMediaType, result.getMediaType());
    }

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                Arguments.arguments(MediaType.TEXT_HTML, "<html><body>root</body></html>"),
                Arguments.arguments(MediaType.APPLICATION_JSON, "[\"root\"]"),
                Arguments.arguments(MediaType.APPLICATION_XML, "<root/>")
        );
    }

}
