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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Collect all service tests. See method {@link #suite()}.
 * 
 * @author Stephan Koops
 */
public class AllServiceTests extends TestCase {
    public static Test suite() {
        TestSuite mySuite = new TestSuite();
        mySuite.setName("All service tests");
        mySuite.addTestSuite(AncestorTest.class);
        mySuite.addTestSuite(AppPlusXmlTest.class);
        mySuite.addTestSuite(CarTest.class);
        mySuite.addTestSuite(ContextResolverTest.class);
        mySuite.addTestSuite(ContextsTest.class);
        mySuite.addTestSuite(CookieParamTest.class);
        mySuite.addTestSuite(DeterminingMediaTypeTest.class);
        mySuite.addTestSuite(ExcMapperTest.class);
        mySuite.addTestSuite(HeadOptionsTest.class);
        mySuite.addTestSuite(HttpHeaderTest.class);
        mySuite.addTestSuite(IllegalConstructorTest.class);
        mySuite.addTestSuite(IllegalThingsTest.class);
        mySuite.addTestSuite(InheritAnnotationTest.class);
        mySuite.addTestSuite(InjectionTest.class);
        mySuite.addTestSuite(JsonTest.class);
        mySuite.addTestSuite(ListParamTest.class);
        mySuite.addTestSuite(MatrixParamTest.class);
        mySuite.addTestSuite(MatrixParamTest2.class);
        mySuite.addTestSuite(MessageBodyWorkersTest.class);
        mySuite.addTestSuite(MethodAheadLocatorTest.class);
        mySuite.addTestSuite(OwnProviderTest.class);
        mySuite.addTestSuite(PathParamTest.class);
        mySuite.addTestSuite(PathParamTest2.class);
        mySuite.addTestSuite(PersonsTest.class);
        mySuite.addTestSuite(PrimitiveWrapperEntityTest.class);
        mySuite.addTestSuite(ProviderTest.class);
        mySuite.addTestSuite(QueryParamTest.class);
        mySuite.addTestSuite(RecursiveTest.class);
        mySuite.addTestSuite(RepresentationTest.class);
        mySuite.addTestSuite(RequestTest.class);
        mySuite.addTestSuite(ResponseBuilderTest.class);
        mySuite.addTestSuite(SecurityContextTest.class);
        mySuite.addTestSuite(SimpleHouseTest.class);
        mySuite.addTestSuite(SimpleTrainTest.class);
        mySuite.addTestSuite(ThreadLocalContextTest.class);
        mySuite.addTestSuite(ThrowExceptionTest.class);
        mySuite.addTestSuite(ThrowWebAppExcProviderTest.class);
        mySuite.addTestSuite(UriBuilderByServiceTest.class);

        // at the end because it uses multiple of the previous classes
        mySuite.addTestSuite(MultipleResourcesTest.class);
        return mySuite;
    }
}
