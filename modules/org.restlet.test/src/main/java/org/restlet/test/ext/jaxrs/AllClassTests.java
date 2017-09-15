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

package org.restlet.test.ext.jaxrs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.restlet.test.ext.jaxrs.core.PathSegmentImplTest;
import org.restlet.test.ext.jaxrs.core.UriBuilderImplTest;
import org.restlet.test.ext.jaxrs.core.UriInfoTest;
import org.restlet.test.ext.jaxrs.util.ConverterTests;
import org.restlet.test.ext.jaxrs.util.EncodeOrCheckTests;
import org.restlet.test.ext.jaxrs.util.OrderedMapTest;
import org.restlet.test.ext.jaxrs.util.PathRegExpTests;
import org.restlet.test.ext.jaxrs.util.RemainingPathTests;
import org.restlet.test.ext.jaxrs.util.SortedOrderedBagTest;
import org.restlet.test.ext.jaxrs.util.UtilTests;
import org.restlet.test.ext.jaxrs.wrappers.RootResourceClassTest;
import org.restlet.test.ext.jaxrs.wrappers.WrapperClassesTests;

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
        mySuite.addTestSuite(PathSegmentImplTest.class);
        mySuite.addTestSuite(UriBuilderImplTest.class);
        mySuite.addTestSuite(UriInfoTest.class);
        // package .util.
        mySuite.addTestSuite(ConverterTests.class);
        mySuite.addTestSuite(EncodeOrCheckTests.class);
        mySuite.addTestSuite(OrderedMapTest.class);
        mySuite.addTestSuite(PathRegExpTests.class);
        mySuite.addTestSuite(RemainingPathTests.class);
        mySuite.addTestSuite(SortedOrderedBagTest.class);
        mySuite.addTestSuite(UtilTests.class);
        // removed temporarily: mySuite.addTestSuite(UtilTests.class);
        // package .wrappers.
        mySuite.addTestSuite(RootResourceClassTest.class);
        mySuite.addTestSuite(WrapperClassesTests.class);
        return mySuite;
    }

}
