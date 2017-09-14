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

package org.restlet.test.ext.jaxrs.util;

import java.lang.reflect.Method;

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
        try {
            Method removeMatrixParams = RemainingPath.class.getDeclaredMethod(
                    "removeMatrixParams", String.class);
            removeMatrixParams.setAccessible(true);
            String removed = (String) removeMatrixParams.invoke(null, actual);
            assertEquals(expected, removed);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testRemoveParams1() {
        aear("sdhfk/", "sdhfk;sdf");
        aear("sdhfk/", "sdhfk;sdf=1");
        aear("sdhfk/", "sdhfk;sdf=1?");
        aear("sdhfk/", "sdhfk;sdf=1?x");
        aear("sdhfk/", "sdhfk;sdf=1?x&");
        aear("sdhfk/", "sdhfk;sdf=1?x&;");
        aear("sdhfk/", "sdhfk;sdf=1?x&;/");
        aear("sdhfk/", "sdhfk;sdf=1?x&;c/");
        aear("sdhfk/", "sdhfk;sdf=1?x&;c/sdf");
    }

    public void testRemoveParams11() {
        aear("/ddf/", ";/ddf");
        aear("/ddf/", ";sdf/ddf");
        aear("/ddf/", ";sdf=/ddf");
        aear("/ddf/", ";sdf=sfsd/ddf");
        aear("/ddf/", ";sdf=sfsd;/ddf");
        aear("/ddf/", ";sdf=sfsd;sdf/ddf");
    }

    public void testRemoveParams3() {
        aear("sdhfk/gkjj/", "sdhfk;sdf/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj?");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj?f");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj?f=");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj?f=5");
    }

    public void testRemoveParams5() {
        aear("sdhfk/gkjj/", "sdhfk/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff/gkjj");
        aear("sdhfk/gkjj/", "sdhfk;sdf=1;ff=2/gkjj");
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
        aear("sdhfk/gkjj/a/", "sdhfk;sdf=1;ff=2/gkjj/a");
    }

    public void testRemoveParamsEmptyResult1() {
        aear("/", ";");
        aear("/", ";df");
        aear("/", ";df=");
        aear("/", ";df=sdfsdf");
        aear("/", ";df=sdfsdf?");
        aear("/", ";df=sdfsdf?sdf");
        aear("/", ";df=sdfsdf?sdf=");
        aear("/", ";df=sdfsdf?sdf=sdffs");
    }

    public void testRemoveParamsEmptyResult2() {
        aear("/", "?");
        aear("/", "?df");
        aear("/", "?df=");
        aear("/", "?df=sdfsdf");
        aear("/", "?df=sdfsdf&");
        aear("/", "?df=sdfsdf&sdf");
        aear("/", "?df=sdfsdf&sdf=");
        aear("/", "?df=sdfsdf&sdf=sdffs");
        aear("/", "?df=sdfsdf?sdf=sdffs");
    }
}
