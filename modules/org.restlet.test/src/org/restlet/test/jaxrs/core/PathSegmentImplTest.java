/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.jaxrs.core;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.core.PathSegmentImpl;

/**
 * @author Stephan Koops
 * @see PathSegmentImpl
 * @see PathSegment
 */
@SuppressWarnings("all")
public class PathSegmentImplTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParseMatrixParams() {
        final MultivaluedMap<String, String> matrixParams = PathSegmentImpl
                .parseMatrixParams("mpn1=mpv1;mpn1=mpv2;mpn3=mpv3", true);
        final List<String> mpn1 = matrixParams.get("mpn1");
        assertEquals(2, mpn1.size());
        assertEquals("mpv1", mpn1.get(0));
        assertEquals("mpv2", mpn1.get(1));

        final List<String> mpn3 = matrixParams.get("mpn3");
        assertEquals(1, mpn3.size());
        assertEquals("mpv3", mpn3.get(0));
    }

    public void testParseMatrixParamsFalseFalse() {
        final MultivaluedMap<String, String> matrixParams = PathSegmentImpl
                .parseMatrixParams("mpn1=mpv1%20;mpn1=mp%20v2;mp%20n3=%20mpv3",
                        false);
        final List<String> mpn1 = matrixParams.get("mpn1");
        assertEquals(2, mpn1.size());
        assertEquals("mpv1%20", mpn1.get(0));
        assertEquals("mp%20v2", mpn1.get(1));

        final List<String> mpn3 = matrixParams.get("mp%20n3");
        assertEquals(1, mpn3.size());
        assertEquals("%20mpv3", mpn3.get(0));
    }

    public void testParseMatrixParamsTrueFalse() {
        final MultivaluedMap<String, String> matrixParams = PathSegmentImpl
                .parseMatrixParams("mpn1=mpv1%20;mpn1=mp%20v2;mp%20n3=%20mpv3",
                        true);
        final List<String> mpn1 = matrixParams.get("mpn1");
        assertEquals(2, mpn1.size());
        assertEquals("mpv1 ", mpn1.get(0));
        assertEquals("mp v2", mpn1.get(1));

        final List<String> mpn3 = matrixParams.get("mp n3");
        assertEquals(1, mpn3.size());
        assertEquals(" mpv3", mpn3.get(0));
    }
}
