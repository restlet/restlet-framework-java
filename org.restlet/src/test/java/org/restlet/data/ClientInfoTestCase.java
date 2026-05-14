/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.restlet.data.Language.ENGLISH;
import static org.restlet.data.Language.ENGLISH_US;
import static org.restlet.data.Language.FRENCH;
import static org.restlet.data.Language.FRENCH_FRANCE;
import static org.restlet.data.MediaType.APPLICATION_XML;
import static org.restlet.data.MediaType.TEXT_PLAIN;
import static org.restlet.data.MediaType.TEXT_XML;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.representation.Variant;
import org.restlet.service.ConnegService;
import org.restlet.service.MetadataService;

/**
 * Test {@link org.restlet.data.ClientInfo} for content negotiation.
 *
 * @author Jerome Louvel
 */
class ClientInfoTestCase {

    @Nested
    class MixLanguageMediaTypeContentNegotiationTextCase {
        private final MetadataService ms = new MetadataService();
        private final ConnegService connegService = new ConnegService();
        private Request request;

        @BeforeEach
        void setup() {
            request = new Request();
            ClientInfo ci = request.getClientInfo();
            ci.getAcceptedLanguages().add(new Preference<>(ENGLISH_US, 1.0F));
            ci.getAcceptedLanguages().add(new Preference<>(FRENCH_FRANCE, 0.9F));
            ci.getAcceptedMediaTypes().add(new Preference<>(TEXT_XML, 1.0F));
        }

        @Test
        void shouldReturnEnUsAndTextXml() {
            List<Variant> variants =
                    List.of(
                            new Variant(TEXT_XML, ENGLISH_US),
                            new Variant(TEXT_XML, FRENCH_FRANCE));
            Variant pv = connegService.getPreferredVariant(variants, request, ms);

            assertEquals(TEXT_XML, pv.getMediaType());
            assertEquals(ENGLISH_US, pv.getLanguages().getFirst());
        }

        @Test
        void shouldReturnEnAndTextXml() {
            List<Variant> variants =
                    List.of(new Variant(TEXT_XML, ENGLISH), new Variant(TEXT_XML, FRENCH));
            Variant pv = connegService.getPreferredVariant(variants, request, ms);

            assertEquals(TEXT_XML, pv.getMediaType());
            assertEquals(ENGLISH, pv.getLanguages().getFirst());
        }

        // Testing quality priority over parent metadata
        @Test
        void shouldReturnFrFrAndText() {
            List<Variant> variants =
                    List.of(new Variant(TEXT_PLAIN, ENGLISH), new Variant(TEXT_XML, FRENCH_FRANCE));
            Variant pv = connegService.getPreferredVariant(variants, request, ms);

            assertEquals(TEXT_XML, pv.getMediaType());
            assertEquals(FRENCH_FRANCE, pv.getLanguages().getFirst());
        }

        // Testing quality priority over parent metadata
        @Test
        void shouldReturnFrFrAndXml() {
            List<Variant> variants =
                    List.of(
                            new Variant(APPLICATION_XML, ENGLISH_US),
                            new Variant(TEXT_XML, FRENCH_FRANCE));
            Variant pv = connegService.getPreferredVariant(variants, request, ms);

            assertEquals(TEXT_XML, pv.getMediaType());
            assertEquals(FRENCH_FRANCE, pv.getLanguages().getFirst());
        }

        // Leveraging parent media types
        @Test
        void shouldPreferEnUsAndApplicationXml() {
            List<Variant> variants =
                    List.of(
                            new Variant(APPLICATION_XML, ENGLISH_US),
                            new Variant(APPLICATION_XML, FRENCH_FRANCE));
            Variant pv = connegService.getPreferredVariant(variants, request, ms);

            assertEquals(APPLICATION_XML, pv.getMediaType());
            assertEquals(ENGLISH_US, pv.getLanguages().getFirst());
        }
    }

    /** Conneg tests for IE which accepts all media types. */
    @Test
    void testConnegIe() {
        ClientInfo ci = new ClientInfo();
        Preference<MediaType> allMediaTypesPreference = new Preference<>(MediaType.ALL, 1.0F);
        ci.getAcceptedMediaTypes().add(allMediaTypesPreference);

        List<MediaType> types = List.of(TEXT_XML, MediaType.APPLICATION_JSON);
        MediaType pmt = ci.getPreferredMediaType(types);

        assertEquals(TEXT_XML, pmt);
    }
}
