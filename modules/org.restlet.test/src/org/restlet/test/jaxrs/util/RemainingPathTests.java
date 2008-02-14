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

package org.restlet.test.jaxrs.util;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.util.RemainingPath;

@SuppressWarnings("unchecked")
public class RemainingPathTests extends TestCase {

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
    
    public void testRemoveParams11() {
        aear("/ddf", ";/ddf");
        aear("/ddf", ";sdf/ddf");
        aear("/ddf", ";sdf=/ddf");
        aear("/ddf", ";sdf=sfsd/ddf");
        aear("/ddf", ";sdf=sfsd;/ddf");
        aear("/ddf", ";sdf=sfsd;sdf/ddf");
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

    /**
     * assertEqualsAfterRemove
     * 
     * @param expected
     * @param actual
     */
    public void aear(String expected, String actual) {
        assertEquals(expected, RemainingPath.removeMatrixParams(actual));
    }
}