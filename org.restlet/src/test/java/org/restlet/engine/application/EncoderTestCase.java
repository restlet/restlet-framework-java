/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Filter;
import org.restlet.service.EncoderService;

/** Unit tests for {@link Encoder}. */
class EncoderTestCase {

    @Test
    void constructorAndGetters_exposeConfiguredFlagsAndService() {
        EncoderService service = new EncoderService();
        Encoder encoder = new Encoder(new Context(), true, false, service);

        assertTrue(encoder.isEncodingRequest());
        assertFalse(encoder.isEncodingResponse());
        assertSame(service, encoder.getEncoderService());
        assertTrue(encoder.getSupportedEncodings().contains(Encoding.GZIP));
    }

    @Test
    void getBestEncoding_selectsHighestQualityAcceptedEncoding() {
        Encoder encoder = new Encoder(new Context(), true, true, new EncoderService());
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.getAcceptedEncodings().add(new Preference<>(Encoding.DEFLATE, 0.5f));
        clientInfo.getAcceptedEncodings().add(new Preference<>(Encoding.GZIP, 0.9f));

        assertEquals(Encoding.GZIP, encoder.getBestEncoding(clientInfo));
    }

    @Test
    void getBestEncoding_noAcceptedEncodings_returnsNull() {
        Encoder encoder = new Encoder(new Context(), true, true, new EncoderService());
        assertNull(encoder.getBestEncoding(new ClientInfo()));
    }

    @Test
    void encode_withBestEncoding_wrapsInEncodeRepresentation() {
        Encoder encoder = new Encoder(new Context(), true, true, new EncoderService());
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.getAcceptedEncodings().add(new Preference<>(Encoding.GZIP, 1.0f));

        Representation source = new StringRepresentation("hello");
        Representation result = encoder.encode(clientInfo, source);

        assertInstanceOf(EncodeRepresentation.class, result);
    }

    @Test
    void encode_withoutBestEncoding_returnsOriginalRepresentation() {
        Encoder encoder = new Encoder(new Context(), true, true, new EncoderService());
        Representation source = new StringRepresentation("hello");

        assertSame(source, encoder.encode(new ClientInfo(), source));
    }

    @Test
    void beforeHandle_encodesRequestEntityWhenEnabled() {
        EncoderService service = new EncoderService();
        service.setMinimumSize(EncoderService.ANY_SIZE);
        Encoder encoder = new Encoder(new Context(), true, false, service);
        Request request = new Request();
        request.getClientInfo().getAcceptedEncodings().add(new Preference<>(Encoding.GZIP, 1.0f));
        request.setEntity(new StringRepresentation("hello", MediaType.TEXT_PLAIN));
        Response response = new Response(request);

        int result = encoder.beforeHandle(request, response);

        assertEquals(Filter.CONTINUE, result);
        assertInstanceOf(EncodeRepresentation.class, request.getEntity());
    }

    @Test
    void beforeHandle_disabled_doesNotEncode() {
        Encoder encoder = new Encoder(new Context(), false, false, new EncoderService());
        Request request = new Request();
        Representation entity = new StringRepresentation("hello", MediaType.TEXT_PLAIN);
        request.setEntity(entity);
        Response response = new Response(request);

        encoder.beforeHandle(request, response);

        assertSame(entity, request.getEntity());
    }

    @Test
    void afterHandle_encodesResponseEntityWhenEnabled() {
        EncoderService service = new EncoderService();
        service.setMinimumSize(EncoderService.ANY_SIZE);
        Encoder encoder = new Encoder(new Context(), false, true, service);
        Request request = new Request();
        request.getClientInfo().getAcceptedEncodings().add(new Preference<>(Encoding.GZIP, 1.0f));
        Response response = new Response(request);
        response.setEntity(new StringRepresentation("hello", MediaType.TEXT_PLAIN));

        encoder.afterHandle(request, response);

        assertInstanceOf(EncodeRepresentation.class, response.getEntity());
    }
}
