/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
