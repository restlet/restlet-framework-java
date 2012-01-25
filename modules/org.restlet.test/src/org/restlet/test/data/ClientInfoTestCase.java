/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.data;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Request;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.representation.Variant;
import org.restlet.service.ConnegService;
import org.restlet.service.MetadataService;
import org.restlet.test.RestletTestCase;

/**
 * Test {@link org.restlet.data.ClientInfo} for content negotiation.
 * 
 * @author Jerome Louvel
 */
public class ClientInfoTestCase extends RestletTestCase {

    MetadataService ms;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ms = new MetadataService();
    }

    @Override
    protected void tearDown() throws Exception {
        ms = null;
        super.tearDown();
    }

    /**
     * Conneg tests.
     */
    public void testConneg() throws Exception {
        ConnegService connegService = new ConnegService();
        Request request = new Request();
        ClientInfo ci = request.getClientInfo();
        ci.getAcceptedLanguages().add(
                new Preference<Language>(Language.ENGLISH_US, 1.0F));
        ci.getAcceptedLanguages().add(
                new Preference<Language>(Language.FRENCH_FRANCE, 0.9F));
        ci.getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.TEXT_XML, 1.0F));

        List<Variant> variants = new ArrayList<Variant>();
        variants.add(new Variant(MediaType.TEXT_XML, Language.ENGLISH_US));
        variants.add(new Variant(MediaType.TEXT_XML, Language.FRENCH_FRANCE));
        Variant pv = connegService.getPreferredVariant(variants, request, ms);

        assertEquals(MediaType.TEXT_XML, pv.getMediaType());
        assertEquals(Language.ENGLISH_US, pv.getLanguages().get(0));

        // Leveraging parent languages
        variants.clear();
        variants.add(new Variant(MediaType.TEXT_XML, Language.ENGLISH));
        variants.add(new Variant(MediaType.TEXT_XML, Language.FRENCH));
        pv = connegService.getPreferredVariant(variants, request, ms);

        assertEquals(MediaType.TEXT_XML, pv.getMediaType());
        assertEquals(Language.ENGLISH, pv.getLanguages().get(0));

        // Testing quality priority over parent metadata
        variants.clear();
        variants.add(new Variant(MediaType.TEXT_PLAIN, Language.ENGLISH));
        variants.add(new Variant(MediaType.TEXT_XML, Language.FRENCH_FRANCE));
        pv = connegService.getPreferredVariant(variants, request, ms);

        assertEquals(MediaType.TEXT_XML, pv.getMediaType());
        assertEquals(Language.FRENCH_FRANCE, pv.getLanguages().get(0));

        // Testing quality priority over parent metadata
        variants.clear();
        variants.add(new Variant(MediaType.APPLICATION_XML, Language.ENGLISH_US));
        variants.add(new Variant(MediaType.TEXT_XML, Language.FRENCH_FRANCE));
        pv = connegService.getPreferredVariant(variants, request, ms);

        assertEquals(MediaType.TEXT_XML, pv.getMediaType());
        assertEquals(Language.FRENCH_FRANCE, pv.getLanguages().get(0));

        // Leveraging parent media types
        variants.clear();
        variants.add(new Variant(MediaType.APPLICATION_XML, Language.ENGLISH_US));
        variants.add(new Variant(MediaType.APPLICATION_XML,
                Language.FRENCH_FRANCE));
        pv = connegService.getPreferredVariant(variants, request, ms);

        assertEquals(MediaType.APPLICATION_XML, pv.getMediaType());
        assertEquals(Language.ENGLISH_US, pv.getLanguages().get(0));

    }

    /**
     * Conneg tests for IE which accepts all media types.
     */
    public void testConnegIO() throws Exception {
        ClientInfo ci = new ClientInfo();
        ci.getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.ALL, 1.0F));

        List<MediaType> types = new ArrayList<MediaType>();
        types.add(MediaType.TEXT_XML);
        types.add(MediaType.APPLICATION_JSON);
        MediaType pmt = ci.getPreferredMediaType(types);

        assertEquals(MediaType.TEXT_XML, pmt);
    }
}
