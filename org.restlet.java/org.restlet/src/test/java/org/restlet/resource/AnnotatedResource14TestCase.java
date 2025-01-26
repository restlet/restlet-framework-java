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
import org.restlet.representation.StringRepresentation;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.restlet.data.MediaType.*;

/**
 * Test the annotated resources, client and server sides.
 *
 * @author Jerome Louvel
 */
public class AnnotatedResource14TestCase extends AbstractAnnotatedResourceWithFinderTestCase {

    @Override
    void configureFinder(Finder finder) {
        finder.setTargetClass(MyServerResource14.class);
    }

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                Arguments.arguments(APPLICATION_JSON, APPLICATION_JSON, "json"),
                Arguments.arguments(APPLICATION_XML, APPLICATION_JSON, "xml:json"),
                Arguments.arguments(APPLICATION_XML, APPLICATION_XML, "xml"),
                Arguments.arguments(TEXT_PLAIN, null, "*"),
                Arguments.arguments(TEXT_PLAIN, TEXT_PLAIN, "*")
        );
    }

    @ParameterizedTest
    @MethodSource("argumentsProvider")
    public void testQuery(final MediaType requestEntityMediaType, final MediaType responseMediaTypePreference, final String responseEntityAsText) throws IOException {
        StringRepresentation requestEntity = new StringRepresentation("test", requestEntityMediaType);

        Representation rep = responseMediaTypePreference == null
                ? clientResource.put(requestEntity)
                : clientResource.put(requestEntity, responseMediaTypePreference);

        assertNotNull(rep);
        if (responseMediaTypePreference != null) {
            assertEquals(responseMediaTypePreference, rep.getMediaType());
        }
        assertEquals(responseEntityAsText, rep.getText());
    }

    /**
     * Sample server resource for testing annotated PUT methods.
     *
     * @author Jerome Louvel
     */
    public static class MyServerResource14 extends ServerResource {

        @Put
        public Representation store1(Representation rep) {
            return new StringRepresentation("*", MediaType.TEXT_PLAIN);
        }

        @Put("xml")
        public Representation store2(Representation rep) {
            return new StringRepresentation("xml", MediaType.APPLICATION_XML);
        }

        @Put("xml:json")
        public Representation store3(Representation rep) {
            return new StringRepresentation("xml:json", MediaType.APPLICATION_JSON);
        }

        @Put("json")
        public Representation store4(Representation rep) {
            return new StringRepresentation("json", MediaType.APPLICATION_JSON);
        }
    }
}
