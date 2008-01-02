/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.test.jaxrs.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.ext.jaxrs.util.Util;

public class UtilTests extends TestCase {

    public void testConvertMetadataList1() {
        Collection<Preference<MediaType>> preferences = new ArrayList();
        List<Collection<MediaType>> sorted = (List) Util
                .sortMetadataList((Collection) preferences);
        assertEquals(0, sorted.size());

        preferences.add(new Preference<MediaType>(MediaType.TEXT_HTML));
        sorted = (List) Util.sortMetadataList((Collection) preferences);
        assertEquals(1, sorted.size());
        assertEquals(1, sorted.get(0).size());
        assertEquals(MediaType.TEXT_HTML, sorted.get(0).iterator().next());

        preferences.add(new Preference<MediaType>(MediaType.TEXT_PLAIN));
        sorted = (List) Util.sortMetadataList((Collection) preferences);
        assertEquals(1, sorted.size());
        assertEquals(2, sorted.get(0).size());
        assertTrue(sorted.get(0).contains(MediaType.TEXT_HTML));
        assertTrue(sorted.get(0).contains(MediaType.TEXT_PLAIN));
    }

    public void testConvertMetadataList2() {
        Collection<Preference<MediaType>> preferences = new ArrayList();
        preferences.add(new Preference<MediaType>(MediaType.TEXT_HTML, 0.7f));
        List<Collection<MediaType>> sorted = (List) Util
                .sortMetadataList((Collection) preferences);
        assertEquals(1, sorted.size());
        assertEquals(1, sorted.get(0).size());
        assertEquals(MediaType.TEXT_HTML, sorted.get(0).iterator().next());

        preferences.add(new Preference<MediaType>(MediaType.TEXT_PLAIN, 0.5f));
        sorted = (List) Util.sortMetadataList((Collection) preferences);
        assertEquals(2, sorted.size());
        assertEquals(1, sorted.get(0).size());
        assertEquals(MediaType.TEXT_HTML, sorted.get(0).iterator().next());
        assertEquals(1, sorted.get(1).size());
        assertEquals(MediaType.TEXT_PLAIN, sorted.get(1).iterator().next());
    }

    public void testConvertMetadataList3() {
        Collection<Preference<MediaType>> preferences = new ArrayList();
        preferences.add(new Preference<MediaType>(MediaType.TEXT_HTML, 0.5f));
        List<Collection<MediaType>> sorted = (List) Util
                .sortMetadataList((Collection) preferences);
        assertEquals(1, sorted.size());
        assertEquals(1, sorted.get(0).size());
        assertEquals(MediaType.TEXT_HTML, sorted.get(0).iterator().next());

        preferences.add(new Preference<MediaType>(MediaType.TEXT_PLAIN, 0.7f));
        sorted = (List) Util.sortMetadataList((Collection) preferences);
        assertEquals(2, sorted.size());
        assertEquals(1, sorted.get(0).size());
        assertEquals(MediaType.TEXT_PLAIN, sorted.get(0).iterator().next());
        assertEquals(1, sorted.get(1).size());
        assertEquals(MediaType.TEXT_HTML, sorted.get(1).iterator().next());
    }

    public void testConvertMetadataList4() {
        Collection<Preference<MediaType>> preferences = new ArrayList();
        preferences.add(new Preference<MediaType>(MediaType.TEXT_HTML, 0.5f));
        List<Collection<MediaType>> sorted = (List) Util
                .sortMetadataList((Collection) preferences);
        assertEquals(1, sorted.size());
        assertEquals(1, sorted.get(0).size());
        assertEquals(MediaType.TEXT_HTML, sorted.get(0).iterator().next());

        preferences.add(new Preference<MediaType>(MediaType.TEXT_PLAIN, 0.7f));
        preferences.add(new Preference<MediaType>(MediaType.TEXT_CSS, 0.5f));
        sorted = (List) Util.sortMetadataList((Collection) preferences);
        assertEquals(2, sorted.size());
        assertEquals(1, sorted.get(0).size());
        assertEquals(MediaType.TEXT_PLAIN, sorted.get(0).iterator().next());
        assertEquals(2, sorted.get(1).size());
        assertTrue(sorted.get(1).contains(MediaType.TEXT_HTML));
        assertTrue(sorted.get(1).contains(MediaType.TEXT_CSS));
    }
}