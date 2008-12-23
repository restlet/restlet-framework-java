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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Test {@link org.restlet.data.MediaType}.
 * 
 * @author Jerome Louvel
 */
public class MediaTypeTestCase extends RestletTestCase {
    protected final static String DEFAULT_SCHEME = "http";

    protected final static String DEFAULT_SCHEMEPART = "//";

    /**
     * Equality tests.
     */
    public void testEquals() throws Exception {
        MediaType mt1 = new MediaType("application/xml");
        MediaType mt2 = MediaType.APPLICATION_XML;
        assertTrue(mt1.equals(mt2));
        assertEquals(mt1, mt2);

        final Series<Parameter> mediaParams1 = new Form();
        mediaParams1.add(new Parameter("charset", "ISO-8859-1"));
        final MediaType mt1Bis = new MediaType("application/xml", mediaParams1);

        final Series<Parameter> mediaParams2 = new Form();
        mediaParams2.add(new Parameter("charset", "ISO-8859-1"));
        final MediaType mt2Bis = new MediaType("application/xml", mediaParams2);

        final Series<Parameter> mediaParams3 = new Form();
        mediaParams3.add(new Parameter("charset", "ISO-8859-15"));
        final MediaType mt3 = new MediaType("application/xml", mediaParams3);

        assertTrue(mt1Bis.equals(mt2Bis));
        assertEquals(mt1, mt2);
        assertTrue(mt1Bis.equals(mt1, true));
        assertTrue(mt1Bis.equals(mt2, true));
        assertTrue(mt1Bis.equals(mt3, true));

        mt1 = new MediaType("application/*");
        mt2 = MediaType.APPLICATION_ALL;
        assertTrue(mt1.equals(mt2));
        assertEquals(mt1, mt2);

    }

    /**
     * Test inclusion.
     */
    public void testIncludes() throws Exception {
        MediaType mt1 = MediaType.APPLICATION_ALL;
        MediaType mt2 = MediaType.APPLICATION_XML;
        assertTrue(mt1.includes(mt1));
        assertTrue(mt2.includes(mt2));
        assertTrue(mt1.includes(mt2));
        assertFalse(mt2.includes(mt1));

        if (false) { // TODO should "app/*+xml" includes "app/xml"? Is not yet.
            mt1 = MediaType.APPLICATION_ALL_XML;
            mt2 = MediaType.APPLICATION_XML;
            assertTrue(mt1.includes(mt1));
            assertTrue(mt2.includes(mt2));
            assertTrue(mt1.includes(mt2));
            assertFalse(mt2.includes(mt1));
        }

        mt1 = MediaType.APPLICATION_ALL_XML;
        mt2 = MediaType.APPLICATION_ATOMPUB_SERVICE;
        assertTrue(mt1.includes(mt1));
        assertTrue(mt2.includes(mt2));
        assertTrue(mt1.includes(mt2));
        assertFalse(mt2.includes(mt1));
    }

    public void testMostSpecificMediaType() {
        assertEquals(MediaType.TEXT_ALL, MediaType.getMostSpecific(
                MediaType.ALL, MediaType.TEXT_ALL));
        assertEquals(MediaType.TEXT_ALL, MediaType.getMostSpecific(
                MediaType.TEXT_ALL, MediaType.ALL));

        assertEquals(MediaType.TEXT_PLAIN, MediaType.getMostSpecific(
                MediaType.ALL, MediaType.TEXT_ALL, MediaType.TEXT_PLAIN));
        assertEquals(MediaType.TEXT_PLAIN, MediaType.getMostSpecific(
                MediaType.ALL, MediaType.TEXT_PLAIN, MediaType.TEXT_ALL));
        assertEquals(MediaType.TEXT_PLAIN, MediaType.getMostSpecific(
                MediaType.TEXT_ALL, MediaType.ALL, MediaType.TEXT_PLAIN));
        assertEquals(MediaType.TEXT_PLAIN, MediaType.getMostSpecific(
                MediaType.TEXT_ALL, MediaType.TEXT_PLAIN, MediaType.ALL));
        assertEquals(MediaType.TEXT_PLAIN, MediaType.getMostSpecific(
                MediaType.TEXT_PLAIN, MediaType.ALL, MediaType.TEXT_ALL));
        assertEquals(MediaType.TEXT_PLAIN, MediaType.getMostSpecific(
                MediaType.TEXT_PLAIN, MediaType.TEXT_ALL, MediaType.ALL));
    }

    /**
     * Test references that are unequal.
     */
    public void testUnEquals() throws Exception {
        MediaType mt1 = new MediaType("application/xml");
        MediaType mt2 = new MediaType("application/xml2");
        assertFalse(mt1.equals(mt2));

        final Series<Parameter> mediaParams1 = new Form();
        mediaParams1.add(new Parameter("charset", "ISO-8859-1"));
        final MediaType mt1Bis = new MediaType("application/xml", mediaParams1);

        final Series<Parameter> mediaParams3 = new Form();
        mediaParams3.add(new Parameter("charset", "ISO-8859-15"));
        final MediaType mt3 = new MediaType("application/xml", mediaParams3);

        assertFalse(mt1Bis.equals(mt1));
        assertFalse(mt1Bis.equals(mt3));

        mt1 = new MediaType("application/1");
        mt2 = MediaType.APPLICATION_ALL;
        assertFalse(mt1.equals(mt2));
    }

    /**
     * Testing {@link MediaType#valueOf(String)} and
     * {@link MediaType#register(String, String)}
     */
    public void testValueOf() {
        assertSame(MediaType.APPLICATION_XML, MediaType
                .valueOf("application/xml"));
        assertSame(MediaType.ALL, MediaType.valueOf("*/*"));
        final MediaType newType = MediaType
                .valueOf("application/x-restlet-test");
        assertEquals("application", newType.getMainType());
        assertEquals("x-restlet-test", newType.getSubType());
        assertEquals("application/x-restlet-test", newType.getName());

        // Should not have got registered by call to valueOf() alone
        assertNotSame(newType, MediaType.valueOf("application/x-restlet-test"));

        final MediaType registeredType = MediaType.register(
                "application/x-restlet-test", "Restlet testcase");
        assertNotSame(newType, registeredType); // didn't touch old value
        assertEquals("application/x-restlet-test", registeredType.getName());
        assertEquals("Restlet testcase", registeredType.getDescription());

        // Later valueOf calls always returns the registered type
        assertSame(registeredType, MediaType
                .valueOf("application/x-restlet-test"));
        assertSame(registeredType, MediaType
                .valueOf("application/x-restlet-test"));
    }
}
