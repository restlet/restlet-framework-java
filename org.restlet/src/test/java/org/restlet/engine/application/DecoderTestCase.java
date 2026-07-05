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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

class DecoderTestCase {

    @Test
    void constructor_singleArg_decodesRequestOnly() {
        Decoder decoder = new Decoder(new Context());
        assertTrue(decoder.isDecodingRequest());
        assertFalse(decoder.isDecodingResponse());
    }

    @Test
    void constructor_explicitFlags_setsBoth() {
        Decoder decoder = new Decoder(new Context(), false, true);
        assertFalse(decoder.isDecodingRequest());
        assertTrue(decoder.isDecodingResponse());
    }

    @Test
    void canDecode_nullRepresentation_returnsFalse() {
        Decoder decoder = new Decoder(new Context());
        assertFalse(decoder.canDecode(null));
    }

    @Test
    void canDecode_noEncodings_returnsFalse() {
        Decoder decoder = new Decoder(new Context());
        Representation representation = new StringRepresentation("body");
        assertFalse(decoder.canDecode(representation));
    }

    @Test
    void canDecode_identityEncodingOnly_returnsFalse() {
        Decoder decoder = new Decoder(new Context());
        Representation representation = new StringRepresentation("body");
        representation.getEncodings().add(Encoding.IDENTITY);
        assertFalse(decoder.canDecode(representation));
    }

    @Test
    void canDecode_gzipEncoding_returnsTrue() {
        Decoder decoder = new Decoder(new Context());
        Representation representation = new StringRepresentation("body");
        representation.getEncodings().add(Encoding.GZIP);
        assertTrue(decoder.canDecode(representation));
    }

    @Test
    void decode_unsupportedEncoding_returnsOriginalRepresentation() {
        Decoder decoder = new Decoder(new Context());
        Representation representation = new StringRepresentation("body");
        representation.getEncodings().add(Encoding.COMPRESS);
        assertEquals(representation, decoder.decode(representation));
    }

    @Test
    void decode_identityEncodingOnly_returnsOriginalRepresentation() {
        Decoder decoder = new Decoder(new Context());
        Representation representation = new StringRepresentation("body");
        representation.getEncodings().add(Encoding.IDENTITY);
        assertEquals(representation, decoder.decode(representation));
    }

    @Test
    void decode_supportedEncoding_wrapsInDecodeRepresentation() {
        Decoder decoder = new Decoder(new Context());
        Representation representation = new StringRepresentation("body");
        representation.getEncodings().add(Encoding.GZIP);
        assertTrue(decoder.decode(representation) instanceof DecodeRepresentation);
    }

    @Test
    void beforeHandle_decodingRequestEnabled_decodesRequestEntity() {
        Decoder decoder = new Decoder(new Context(), true, false);
        Request request = new Request(Method.POST, "http://localhost/test");
        Representation entity = new StringRepresentation("body");
        entity.getEncodings().add(Encoding.GZIP);
        request.setEntity(entity);
        Response response = new Response(request);

        int result = decoder.beforeHandle(request, response);

        assertTrue(request.getEntity() instanceof DecodeRepresentation);
        assertEquals(org.restlet.routing.Filter.CONTINUE, result);
    }

    @Test
    void beforeHandle_decodingRequestDisabled_leavesRequestEntityUnchanged() {
        Decoder decoder = new Decoder(new Context(), false, false);
        Request request = new Request(Method.POST, "http://localhost/test");
        Representation entity = new StringRepresentation("body");
        entity.getEncodings().add(Encoding.GZIP);
        request.setEntity(entity);
        Response response = new Response(request);

        decoder.beforeHandle(request, response);

        assertEquals(entity, request.getEntity());
    }

    @Test
    void afterHandle_decodingResponseEnabled_decodesResponseEntity() {
        Decoder decoder = new Decoder(new Context(), false, true);
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        Representation entity = new StringRepresentation("body");
        entity.getEncodings().add(Encoding.GZIP);
        response.setEntity(entity);

        decoder.afterHandle(request, response);

        assertTrue(response.getEntity() instanceof DecodeRepresentation);
    }

    @Test
    void afterHandle_decodingResponseDisabled_leavesResponseEntityUnchanged() {
        Decoder decoder = new Decoder(new Context(), false, false);
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        Representation entity = new StringRepresentation("body");
        entity.getEncodings().add(Encoding.GZIP);
        response.setEntity(entity);

        decoder.afterHandle(request, response);

        assertEquals(entity, response.getEntity());
    }
}
