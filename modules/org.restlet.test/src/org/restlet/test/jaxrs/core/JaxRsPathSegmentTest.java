/*
 * Copyright 2005-2008 Noelios Consulting.
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
package org.restlet.test.jaxrs.core;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.ext.jaxrs.internal.core.JaxRsPathSegment;

import junit.framework.TestCase;

public class JaxRsPathSegmentTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParseMatrixParams() {
        MultivaluedMap<String, String> matrixParams = JaxRsPathSegment
                .parseMatrixParams("mpn1=mpv1;mpn1=mpv2;mpn3=mpv3", true, false);
        List<String> mpn1 = matrixParams.get("mpn1");
        assertEquals(2, mpn1.size());
        assertEquals("mpv1", mpn1.get(0));
        assertEquals("mpv2", mpn1.get(1));

        List<String> mpn3 = matrixParams.get("mpn3");
        assertEquals(1, mpn3.size());
        assertEquals("mpv3", mpn3.get(0));
    }

    public void testParseMatrixParamsTrueFalse() {
        MultivaluedMap<String, String> matrixParams = JaxRsPathSegment
                .parseMatrixParams("mpn1=mpv1%20;mpn1=mp%20v2;mp%20n3=%20mpv3", true, false);
        List<String> mpn1 = matrixParams.get("mpn1");
        assertEquals(2, mpn1.size());
        assertEquals("mpv1 ", mpn1.get(0));
        assertEquals("mp v2", mpn1.get(1));

        List<String> mpn3 = matrixParams.get("mp n3");
        assertEquals(1, mpn3.size());
        assertEquals(" mpv3", mpn3.get(0));
    }

    public void testParseMatrixParamsFalseFalse() {
        MultivaluedMap<String, String> matrixParams = JaxRsPathSegment
                .parseMatrixParams("mpn1=mpv1%20;mpn1=mp%20v2;mp%20n3=%20mpv3", false, false);
        List<String> mpn1 = matrixParams.get("mpn1");
        assertEquals(2, mpn1.size());
        assertEquals("mpv1%20", mpn1.get(0));
        assertEquals("mp%20v2", mpn1.get(1));

        List<String> mpn3 = matrixParams.get("mp%20n3");
        assertEquals(1, mpn3.size());
        assertEquals("%20mpv3", mpn3.get(0));
    }
}
