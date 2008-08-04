/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.util;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.internal.util.RemainingPath;

/**
 * @author Stephan Koops
 * @see RemainingPath
 */
@SuppressWarnings("all")
public class RemainingPathTests extends TestCase {

    /**
     * assertEqualsAfterRemove
     * 
     * @param expected
     * @param actual
     */
    public void aear(String expected, String actual) {
        assertEquals(expected, RemainingPath.removeMatrixParams(actual));
    }

    public void testRemoveParams1() {
        aear("sdhfk", "sdhfk;sdf");
        aear("sdhfk", "sdhfk;sdf=1");
        aear("sdhfk", "sdhfk;sdf=1?");
        aear("sdhfk", "sdhfk;sdf=1?x");
        aear("sdhfk", "sdhfk;sdf=1?x&");
        aear("sdhfk", "sdhfk;sdf=1?x&;");
        aear("sdhfk", "sdhfk;sdf=1?x&;/");
        aear("sdhfk", "sdhfk;sdf=1?x&;c/");
        aear("sdhfk", "sdhfk;sdf=1?x&;c/sdf");
    }

    public void testRemoveParams11() {
        aear("/ddf", ";/ddf");
        aear("/ddf", ";sdf/ddf");
        aear("/ddf", ";sdf=/ddf");
        aear("/ddf", ";sdf=sfsd/ddf");
        aear("/ddf", ";sdf=sfsd;/ddf");
        aear("/ddf", ";sdf=sfsd;sdf/ddf");
    }

    public void testRemoveParams3() {
        aear("sdhfk/gkjj", "sdhfk;sdf/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;ff/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;ff=2/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;ff=2/gkjj?");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;ff=2/gkjj?f");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;ff=2/gkjj?f=");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;ff=2/gkjj?f=5");
    }

    public void testRemoveParams5() {
        aear("sdhfk/gkjj", "sdhfk/gkjj");
        aear("sdhfk/gkjj", "sdhfk;/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;ff/gkjj");
        aear("sdhfk/gkjj", "sdhfk;sdf=1;ff=2/gkjj");
    }

    public void testRemoveParams7() {
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj/");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj;/");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj;;/");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj;dd/");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj;dd=/");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj;dd=we/");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj;dd=we/;d");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj;dd=we/;d=f");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj;dd=we/;d=f;");
    }

    public void testRemoveParams9() {
        aear("sdhfk/gkjj/a", "sdhfk;sdf=1;ff=2/gkjj/a");
    }

    public void testRemoveParamsEmptyResult1() {
        aear("", ";");
        aear("", ";df");
        aear("", ";df=");
        aear("", ";df=sdfsdf");
        aear("", ";df=sdfsdf?");
        aear("", ";df=sdfsdf?sdf");
        aear("", ";df=sdfsdf?sdf=");
        aear("", ";df=sdfsdf?sdf=sdffs");
    }

    public void testRemoveParamsEmptyResult2() {
        aear("", "?");
        aear("", "?df");
        aear("", "?df=");
        aear("", "?df=sdfsdf");
        aear("", "?df=sdfsdf&");
        aear("", "?df=sdfsdf&sdf");
        aear("", "?df=sdfsdf&sdf=");
        aear("", "?df=sdfsdf&sdf=sdffs");
        aear("", "?df=sdfsdf?sdf=sdffs");
    }
}