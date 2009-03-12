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
        final TestSuite mySuite = new TestSuite();
        mySuite.setName("All service tests");
        mySuite.addTestSuite(AppPlusXmlTest.class);
        mySuite.addTestSuite(CarTest.class);
        mySuite.addTestSuite(ContextResolverTest.class);
        mySuite.addTestSuite(ContextsTest.class);
        mySuite.addTestSuite(CookieParamTest.class);
        mySuite.addTestSuite(DeterminingMediaTypeTest.class);
        mySuite.addTestSuite(ExcMapperTest.class);
        mySuite.addTestSuite(ExtendedUriBuilderByServiceTest.class);
        mySuite.addTestSuite(FormTest.class);
        mySuite.addTestSuite(GenericTypeTestCase.class);
        mySuite.addTestSuite(HeadOptionsTest.class);
        mySuite.addTestSuite(HttpHeaderTest.class);
        mySuite.addTestSuite(IllegalConstructorTest.class);
        mySuite.addTestSuite(IllegalThingsTest.class);
        mySuite.addTestSuite(InheritAnnotationTest.class);
        mySuite.addTestSuite(InjectionTest.class);
        mySuite.addTestSuite(Issue593Test.class);
        mySuite.addTestSuite(JsonTest.class);
        mySuite.addTestSuite(ListParamTest.class);
        mySuite.addTestSuite(MatchedTest.class);
        mySuite.addTestSuite(MatrixParamTest.class);
        mySuite.addTestSuite(MatrixParamTest2.class);
        mySuite.addTestSuite(MessageBodyWritersTest.class);
        mySuite.addTestSuite(MethodAheadLocatorTest.class);
        mySuite.addTestSuite(NoProviderTest.class);
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
        mySuite.addTestSuite(ThrowExceptionTest.class);
        mySuite.addTestSuite(ThrowWebAppExcProviderTest.class);
        mySuite.addTestSuite(UriBuilderByServiceTest.class);

        // at the end because it uses multiple of the previous classes
        mySuite.addTestSuite(MultipleResourcesTest.class);
        return mySuite;
    }
}
