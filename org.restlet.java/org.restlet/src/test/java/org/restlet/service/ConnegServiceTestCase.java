/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.service;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Variant;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the content negotiation service.
 * 
 * @author Jerome Louvel
 */
public class ConnegServiceTestCase {

    @Test
    public void testStrict() {
        List<Variant> variants = new ArrayList<>();
        Variant variant = new Variant(MediaType.APPLICATION_XML);
        variants.add(variant);

        Request request = new Request();
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<>(MediaType.APPLICATION_JSON));

        MetadataService metadataService = new MetadataService();
        ConnegService connegService = new ConnegService();

        // Flexible algorithm
        Variant preferedVariant = connegService.getPreferredVariant(variants,
                request, metadataService);
        assertNotNull(preferedVariant);
        assertEquals(MediaType.APPLICATION_XML, preferedVariant.getMediaType());

        // Strict algorithm
        connegService.setStrict(true);
        preferedVariant = connegService.getPreferredVariant(variants, request,
                metadataService);
        assertNull(preferedVariant);

        // Add a variant to match the strict preferences
        variant = new Variant(MediaType.APPLICATION_JSON);
        variants.add(variant);
        preferedVariant = connegService.getPreferredVariant(variants, request,
                metadataService);
        assertNotNull(preferedVariant);
        assertEquals(MediaType.APPLICATION_JSON, preferedVariant.getMediaType());

    }
}
