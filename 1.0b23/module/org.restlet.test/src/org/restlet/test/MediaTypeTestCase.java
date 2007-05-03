/*
 * Copyright 2005-2006 Noelios Consulting.
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
package org.restlet.test;

import org.restlet.data.MediaType;

/**
 * Test {@link org.restlet.data.MediaType}.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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

        mt1 = new MediaType("application/*");
        mt2 = MediaType.APPLICATION_ALL;
        assertTrue(mt1.equals(mt2));
        assertEquals(mt1, mt2);
    }

    /**
     * Test references that are unequal.
     */
    public void testUnEquals() throws Exception {
        MediaType mt1 = new MediaType("application/xml");
        MediaType mt2 = new MediaType("application/xml2");
        assertFalse(mt1.equals(mt2));

        mt1 = new MediaType("application/1");
        mt2 = MediaType.APPLICATION_ALL;
        assertFalse(mt1.equals(mt2));
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
    }
}
