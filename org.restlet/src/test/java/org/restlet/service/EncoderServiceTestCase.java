/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.engine.application.Encoder;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;

/** Unit tests for {@link EncoderService}. */
class EncoderServiceTestCase {

    @Test
    void defaultConstructor_isEnabledWithDefaultMinimumSize() {
        EncoderService service = new EncoderService();

        assertTrue(service.isEnabled());
        assertEquals(EncoderService.DEFAULT_MINIMUM_SIZE, service.getMinimumSize());
        assertFalse(service.getAcceptedMediaTypes().isEmpty());
        assertFalse(service.getIgnoredMediaTypes().isEmpty());
    }

    @Test
    void constructorWithFlag_setsEnabled() {
        EncoderService service = new EncoderService(false);
        assertFalse(service.isEnabled());
    }

    @Test
    void canEncode_withLargeAcceptedRepresentation_returnsTrue() {
        EncoderService service = new EncoderService();
        service.setMinimumSize(EncoderService.ANY_SIZE);
        StringRepresentation representation =
                new StringRepresentation("some text", MediaType.TEXT_PLAIN);

        assertTrue(service.canEncode(representation));
    }

    @Test
    void canEncode_withTooSmallRepresentation_returnsFalse() {
        EncoderService service = new EncoderService();
        service.setMinimumSize(1_000_000);
        StringRepresentation representation =
                new StringRepresentation("small", MediaType.TEXT_PLAIN);

        assertFalse(service.canEncode(representation));
    }

    @Test
    void canEncode_withIgnoredMediaType_returnsFalse() {
        EncoderService service = new EncoderService();
        service.setMinimumSize(EncoderService.ANY_SIZE);
        StringRepresentation representation = new StringRepresentation("data", MediaType.IMAGE_PNG);

        assertFalse(service.canEncode(representation));
    }

    @Test
    void canEncode_withNullRepresentation_returnsFalse() {
        EncoderService service = new EncoderService();

        assertFalse(service.canEncode(null));
    }

    @Test
    void getMinimumSizeAndSetMinimumSize_roundTrip() {
        EncoderService service = new EncoderService();

        service.setMinimumSize(2048);

        assertEquals(2048, service.getMinimumSize());
    }

    @Test
    void createInboundFilter_returnsEncoder() {
        EncoderService service = new EncoderService();

        Filter filter = service.createInboundFilter(new Context());

        assertNotNull(filter);
        assertInstanceOf(Encoder.class, filter);
    }

    @Test
    void createOutboundFilter_returnsEncoder() {
        EncoderService service = new EncoderService();

        Filter filter = service.createOutboundFilter(new Context());

        assertNotNull(filter);
        assertInstanceOf(Encoder.class, filter);
    }

    @Test
    void getDefaultAcceptedMediaTypes_containsAll() {
        assertTrue(EncoderService.getDefaultAcceptedMediaTypes().contains(MediaType.ALL));
    }

    @Test
    void getDefaultIgnoredMediaTypes_containsImages() {
        assertTrue(EncoderService.getDefaultIgnoredMediaTypes().contains(MediaType.IMAGE_ALL));
    }
}
