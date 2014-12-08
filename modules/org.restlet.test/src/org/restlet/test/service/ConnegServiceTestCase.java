/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.service;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Request;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Variant;
import org.restlet.service.ConnegService;
import org.restlet.service.MetadataService;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the content negotiation service.
 * 
 * @author Jerome Louvel
 */
public class ConnegServiceTestCase extends RestletTestCase {

    public void testStrict() {
        List<Variant> variants = new ArrayList<Variant>();
        Variant variant = new Variant(MediaType.APPLICATION_XML);
        variants.add(variant);

        Request request = new Request();
        request.getClientInfo().getAcceptedMediaTypes()
                .add(new Preference<MediaType>(MediaType.APPLICATION_JSON));

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
