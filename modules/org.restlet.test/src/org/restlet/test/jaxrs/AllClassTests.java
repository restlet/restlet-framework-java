/*
 * Copyright 2005-2008 Noelios Technologies.
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
import org.restlet.test.jaxrs.core.UriInfoTest;
import org.restlet.test.jaxrs.util.ConverterTests;
import org.restlet.test.jaxrs.util.EncodeOrCheckTests;
import org.restlet.test.jaxrs.util.PathRegExpTests;
import org.restlet.test.jaxrs.util.RemainingPathTests;
import org.restlet.test.jaxrs.util.UtilTests;
import org.restlet.test.jaxrs.wrappers.RootResourceClassTest;
import org.restlet.test.jaxrs.wrappers.WrapperClassesTests;

/**
 * Collect all class tests
 * 
 * @author Stephan Koops
 * @see #suite()
 */
public class AllClassTests extends TestCase {
    public static Test suite() {
        final TestSuite mySuite = new TestSuite();
        mySuite.setName("All class tests");
        // package .
        mySuite.addTestSuite(ExceptionMappersTest.class);
        // logs only: mySuite.addTestSuite(JaxRsApplicationTest.class);
        // package .core.
        mySuite.addTestSuite(JaxRsPathSegmentTest.class);
        mySuite.addTestSuite(JaxRsUriBuilderTest.class);
        mySuite.addTestSuite(UriInfoTest.class);
        // package .util.
        mySuite.addTestSuite(ConverterTests.class);
        mySuite.addTestSuite(EncodeOrCheckTests.class);
        mySuite.addTestSuite(PathRegExpTests.class);
        mySuite.addTestSuite(RemainingPathTests.class);
        mySuite.addTestSuite(UtilTests.class);
        // removed temporarily: mySuite.addTestSuite(UtilTests.class);
        // package .wrappers.
        mySuite.addTestSuite(RootResourceClassTest.class);
        mySuite.addTestSuite(WrapperClassesTests.class);
        return mySuite;
    }
}