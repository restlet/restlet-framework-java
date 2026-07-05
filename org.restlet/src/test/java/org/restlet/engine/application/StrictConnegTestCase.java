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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.representation.Variant;

class StrictConnegTestCase {

    private static StrictConneg newConneg(Request request) {
        return new StrictConneg(request, null);
    }

    @Test
    void scoreMediaType_nullMediaType_returnsZero() {
        StrictConneg conneg = newConneg(new Request(Method.GET, "http://localhost/test"));
        assertEquals(0.0F, conneg.scoreMediaType(null));
    }

    @Test
    void scoreMediaType_noMatchingPreference_returnsNegativeOne() {
        Request request = new Request(Method.GET, "http://localhost/test");
        request.getClientInfo().getAcceptedMediaTypes().add(new Preference<>(MediaType.TEXT_PLAIN));
        StrictConneg conneg = newConneg(request);

        assertEquals(-1.0F, conneg.scoreMediaType(MediaType.APPLICATION_JSON));
    }

    @Test
    void scoreMediaType_matchingPreference_returnsQuality() {
        Request request = new Request(Method.GET, "http://localhost/test");
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_PLAIN, 0.8F));
        StrictConneg conneg = newConneg(request);

        assertEquals(0.8F, conneg.scoreMediaType(MediaType.TEXT_PLAIN));
    }

    @Test
    void scoreCharacterSet_matchingPreference_returnsQuality() {
        Request request = new Request(Method.GET, "http://localhost/test");
        request.getClientInfo()
                .getAcceptedCharacterSets()
                .add(new Preference<>(CharacterSet.UTF_8, 0.9F));
        StrictConneg conneg = newConneg(request);

        assertEquals(0.9F, conneg.scoreCharacterSet(CharacterSet.UTF_8));
    }

    @Test
    void scoreEncodings_emptyList_returnsZero() {
        StrictConneg conneg = newConneg(new Request(Method.GET, "http://localhost/test"));
        assertEquals(0.0F, conneg.scoreEncodings(List.of()));
    }

    @Test
    void scoreLanguages_emptyList_returnsZero() {
        StrictConneg conneg = newConneg(new Request(Method.GET, "http://localhost/test"));
        assertEquals(0.0F, conneg.scoreLanguages(List.of()));
    }

    @Test
    void scoreVariant_noAcceptedMediaType_returnsNegativeOne() {
        Request request = new Request(Method.GET, "http://localhost/test");
        request.getClientInfo().getAcceptedMediaTypes().clear();
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_PLAIN, 1.0F));
        StrictConneg conneg = newConneg(request);

        Variant variant = new Variant(MediaType.APPLICATION_JSON);
        assertEquals(-1.0F, conneg.scoreVariant(variant));
    }

    @Test
    void scoreVariant_fullyAcceptedVariant_returnsPositiveScore() {
        Request request = new Request(Method.GET, "http://localhost/test");
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_PLAIN, 1.0F));
        request.getClientInfo()
                .getAcceptedCharacterSets()
                .add(new Preference<>(CharacterSet.UTF_8, 1.0F));
        request.getClientInfo()
                .getAcceptedLanguages()
                .add(new Preference<>(Language.ENGLISH, 1.0F));
        StrictConneg conneg = newConneg(request);

        Variant variant = new Variant(MediaType.TEXT_PLAIN);
        variant.setCharacterSet(CharacterSet.UTF_8);
        variant.setLanguages(Arrays.asList(Language.ENGLISH));

        float score = conneg.scoreVariant(variant);
        assertTrue(score >= 0.0F);
    }

    @Test
    void getPreferredVariant_choosesHighestScoringVariant() {
        Request request = new Request(Method.GET, "http://localhost/test");
        request.getClientInfo().getAcceptedMediaTypes().clear();
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.APPLICATION_JSON, 1.0F));
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.TEXT_PLAIN, 0.5F));
        StrictConneg conneg = newConneg(request);

        Variant jsonVariant = new Variant(MediaType.APPLICATION_JSON);
        Variant textVariant = new Variant(MediaType.TEXT_PLAIN);

        Variant preferred = conneg.getPreferredVariant(Arrays.asList(textVariant, jsonVariant));

        assertEquals(jsonVariant, preferred);
    }

    @Test
    void getPreferredVariant_emptyList_returnsNull() {
        StrictConneg conneg = newConneg(new Request(Method.GET, "http://localhost/test"));
        assertNull(conneg.getPreferredVariant(List.of()));
    }

    @Test
    void getPreferredVariant_nullList_returnsNull() {
        StrictConneg conneg = newConneg(new Request(Method.GET, "http://localhost/test"));
        assertNull(conneg.getPreferredVariant(null));
    }

    @Test
    void getRequest_returnsConstructorRequest() {
        Request request = new Request(Method.GET, "http://localhost/test");
        StrictConneg conneg = newConneg(request);
        assertEquals(request, conneg.getRequest());
    }
}
