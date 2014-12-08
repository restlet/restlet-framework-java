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

package org.restlet.test.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.header.PreferenceReader;
import org.restlet.engine.header.PreferenceWriter;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for the Preference related classes.
 * 
 * @author Jerome Louvel
 */
public class PreferencesTestCase extends RestletTestCase {
    /**
     * Tests the parsing of a single preference header.
     * 
     * @param headerValue
     *            The preference header.
     */
    private void testMediaType(String headerValue, boolean testEquals)
            throws IOException {
        PreferenceReader<MediaType> pr = new PreferenceReader<MediaType>(
                PreferenceReader.TYPE_MEDIA_TYPE, headerValue);
        List<Preference<MediaType>> prefs = new ArrayList<Preference<MediaType>>();
        pr.addValues(prefs);

        // Rewrite the header
        String newHeaderValue = PreferenceWriter.write(prefs);

        // Reread and rewrite the header (prevent formatting issues)
        pr = new PreferenceReader<MediaType>(PreferenceReader.TYPE_MEDIA_TYPE,
                headerValue);
        prefs = new ArrayList<Preference<MediaType>>();
        pr.addValues(prefs);
        String newHeaderValue2 = PreferenceWriter.write(prefs);

        if (testEquals) {
            // Compare initial and new headers
            assertEquals(newHeaderValue, newHeaderValue2);
        }
    }

    /**
     * Tests the preferences parsing.
     */
    public void testParsing() throws IOException {
        testMediaType(
                "text/*;q=0.3, text/html;q=0.7, text/html;level=1, text/html;LEVEL=2;q=0.4;ext1, */*;q=0.5",
                true);
        testMediaType(
                "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/*,,*/*;q=0.5",
                false);
    }
}
