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

package org.restlet.test.jaxrs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.restlet.test.jaxrs.core.JaxRsPathSegmentTest;
import org.restlet.test.jaxrs.core.JaxRsUriBuilderTest;
import org.restlet.test.jaxrs.core.JaxRsUriInfoTest;
import org.restlet.test.jaxrs.util.EncodeOrCheckTests;
import org.restlet.test.jaxrs.util.PathRegExpTests;
import org.restlet.test.jaxrs.util.RemainingPathTests;
import org.restlet.test.jaxrs.wrappers.RootResourceClassTest;

public class AllClassTests extends TestCase {
    public static Test suite() {
        TestSuite mySuite = new TestSuite();
        mySuite.setName("All class tests");
        // package .
        mySuite.addTestSuite(JaxRsRouterTest.class);
        mySuite.addTestSuite(RootResourceClassTest.class);
        mySuite.addTestSuite(PathRegExpTests.class);
        // package .core.
        mySuite.addTestSuite(JaxRsPathSegmentTest.class);
        mySuite.addTestSuite(JaxRsUriInfoTest.class);
        mySuite.addTestSuite(JaxRsUriBuilderTest.class);
        // package .util.
        mySuite.addTestSuite(EncodeOrCheckTests.class);
        mySuite.addTestSuite(RemainingPathTests.class);
        return mySuite;
    }
}