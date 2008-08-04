/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;

import com.noelios.restlet.http.PreferenceReader;
import com.noelios.restlet.http.PreferenceUtils;

/**
 * Unit tests for the Preference related classes.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class PreferencesTestCase extends TestCase {
    /**
     * Tests the parsing of a single preference header.
     * 
     * @param headerValue
     *            The preference header.
     */
    private void testMediaType(String headerValue, boolean testEquals)
            throws IOException {
        final PreferenceReader<MediaType> pr = new PreferenceReader<MediaType>(
                PreferenceReader.TYPE_MEDIA_TYPE, headerValue);
        final List<Preference<MediaType>> prefs = new ArrayList<Preference<MediaType>>();
        Preference<MediaType> pref = pr.readPreference();

        while (pref != null) {
            prefs.add(pref);
            pref = pr.readPreference();
        }

        // Rewrite the header
        final String newHeaderValue = PreferenceUtils.format(prefs);

        if (testEquals) {
            // Compare initial and new headers
            assertEquals(headerValue, newHeaderValue);
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
