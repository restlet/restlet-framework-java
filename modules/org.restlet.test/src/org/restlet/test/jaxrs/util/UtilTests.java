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

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.CookieParam;
import org.restlet.ext.jaxrs.HeaderParam;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * @author Stephan Koops
 * @see Util
 */
public class UtilTests extends TestCase {

    /**
     * @see #testIsAnnotationPresent1()
     */
    @HeaderParam("h1")
    String hpAnnotated;

    /**
     * @see #testIsAnnotationPresent2()
     */
    String notAnnotated;

    /**
     * @see #testIsAnnotationPresent3()
     */
    @CookieParam("c1")
    String cpAnnotated;

    /**
     * @see Util#isAnnotationPresentExt(java.lang.reflect.AnnotatedElement,
     *      Class)
     * @throws Exception
     */
    public void testIsAnnotationPresent1() throws Exception {
        Field hpAnnotated = UtilTests.class.getDeclaredField("hpAnnotated");

        assertTrue(Util.isAnnotationPresentExt(hpAnnotated, HeaderParam.class));
        assertTrue(Util.isAnnotationPresentExt(hpAnnotated,
                javax.ws.rs.HeaderParam.class));
        assertFalse(Util.isAnnotationPresentExt(hpAnnotated, CookieParam.class));
        assertFalse(Util.isAnnotationPresentExt(hpAnnotated,
                javax.ws.rs.CookieParam.class));
    }

    /**
     * @see Util#isAnnotationPresentExt(java.lang.reflect.AnnotatedElement,
     *      Class)
     * @throws Exception
     */
    public void testIsAnnotationPresent2() throws Exception {
        Field notAnnotated = UtilTests.class.getDeclaredField("notAnnotated");

        assertFalse(Util
                .isAnnotationPresentExt(notAnnotated, HeaderParam.class));
        assertFalse(Util.isAnnotationPresentExt(notAnnotated,
                javax.ws.rs.HeaderParam.class));
        assertFalse(Util
                .isAnnotationPresentExt(notAnnotated, CookieParam.class));
        assertFalse(Util.isAnnotationPresentExt(notAnnotated,
                javax.ws.rs.CookieParam.class));
    }

    /**
     * @see Util#isAnnotationPresentExt(java.lang.reflect.AnnotatedElement,
     *      Class)
     * @throws Exception
     */
    public void testIsAnnotationPresent3() throws Exception {
        Field cpAnnotated = UtilTests.class.getDeclaredField("cpAnnotated");

        assertFalse(Util.isAnnotationPresentExt(cpAnnotated, HeaderParam.class));
        assertFalse(Util.isAnnotationPresentExt(cpAnnotated,
                javax.ws.rs.HeaderParam.class));
        assertTrue(Util.isAnnotationPresentExt(cpAnnotated, CookieParam.class));
        assertTrue(Util.isAnnotationPresentExt(cpAnnotated,
                javax.ws.rs.CookieParam.class));
    }
}