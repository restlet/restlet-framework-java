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

package org.restlet.test.jaxrs.services.tests;

import junit.framework.TestCase;

import org.restlet.ext.jaxrs.JaxRsRouter;
import org.restlet.test.jaxrs.services.DoublePath2;
import org.restlet.test.jaxrs.services.DoublePath1;

public class DoublePathTest extends TestCase {

    public static void testAttachDouble() throws Exception {
        JaxRsRouter router = new JaxRsRouter();
        router.attach(DoublePath1.class);
        router.attach(DoublePath1.class);
    }

    public static void _testAttachSamePathDouble() throws Exception {
        JaxRsRouter router = new JaxRsRouter();
        router.attach(DoublePath1.class);
        try {
            router.attach(DoublePath2.class);
            fail("Attach two root resource classes with the same @Path must raise an Excption");
        } catch (IllegalArgumentException e) {
            // wunderful, exception raised :-)
        }
    }
}