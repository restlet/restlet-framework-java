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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Variant;

/**
 * Unit tests for the content negotiation service.
 *
 * @author Jerome Louvel
 */
class ConnegServiceTestCase {

    @Test
    void testStrict() {
        List<Variant> variants = new ArrayList<>();
        Variant variant = new Variant(MediaType.APPLICATION_XML);
        variants.add(variant);

        Request request = new Request();
        request.getClientInfo()
                .getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.APPLICATION_JSON));

        MetadataService metadataService = new MetadataService();
        ConnegService connegService = new ConnegService();

        // Flexible algorithm
        Variant preferedVariant =
                connegService.getPreferredVariant(variants, request, metadataService);
        assertNotNull(preferedVariant);
        assertEquals(MediaType.APPLICATION_XML, preferedVariant.getMediaType());

        // Strict algorithm
        connegService.setStrict(true);
        preferedVariant = connegService.getPreferredVariant(variants, request, metadataService);
        assertNull(preferedVariant);

        // Add a variant to match the strict preferences
        variant = new Variant(MediaType.APPLICATION_JSON);
        variants.add(variant);
        preferedVariant = connegService.getPreferredVariant(variants, request, metadataService);
        assertNotNull(preferedVariant);
        assertEquals(MediaType.APPLICATION_JSON, preferedVariant.getMediaType());
    }
}
