/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.restlet.test.engine.EngineTestSuite;
import org.restlet.test.jaxrs.AllJaxRsTests;
import org.restlet.test.spring.AllSpringTests;

/**
 * Suite of unit tests for the Restlet RI.
 * 
 * @author Jerome Louvel
 */
public class RestletTestSuite extends TestSuite {
    /**
     * JUnit constructor.
     * 
     * @return The unit test.
     */
    public static Test suite() {
        return new RestletTestSuite();
    }

    /** Constructor. */
    public RestletTestSuite() {
        addTestSuite(AtomTestCase.class);
        addTestSuite(ByteUtilsTestCase.class);
        addTestSuite(CallTestCase.class);
        addTestSuite(ComponentXmlTestCase.class);
        addTestSuite(CookieTestCase.class);
        addTestSuite(DigestTestCase.class);
        addTestSuite(DirectoryTestCase.class);
        addTestSuite(FileClientTestCase.class);
        addTestSuite(FileReferenceTestCase.class);
        addTestSuite(FileRepresentationTestCase.class);
        addTestSuite(FilterTestCase.class);
        addTestSuite(FreeMarkerTestCase.class);
        addTestSuite(HeaderTestCase.class);
        addTestSuite(HttpBasicTestCase.class);
        addTestSuite(ImmutableDateTestCase.class);
        addTestSuite(LanguageTestCase.class);
        addTestSuite(MediaTypeTestCase.class);
        addTestSuite(ProductTokenTestCase.class);
        addTestSuite(RangeTestCase.class);
        addTestSuite(RedirectTestCase.class);
        addTestSuite(ReferenceTestCase.class);
        addTestSuite(ResolvingTransformerTestCase.class);
        addTestSuite(ResourceTestCase.class);
        addTestSuite(RestartTestCase.class);
        addTestSuite(RiapTestCase.class);
        addTestSuite(RouteListTestCase.class);
        addTestSuite(SpringTestCase.class);
        addTestSuite(StatusTestCase.class);
        addTestSuite(TemplateFilterTestCase.class);
        addTestSuite(TemplateTestCase.class);
        addTestSuite(TransformerTestCase.class);
        addTestSuite(VelocityTestCase.class);
        addTestSuite(WadlTestCase.class);

        addTest(EngineTestSuite.suite());
        addTest(AllJaxRsTests.suite());
        addTest(AllSpringTests.suite());
    }

}
