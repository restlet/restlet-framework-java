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

package org.restlet.test;

import org.restlet.test.jaxrs.AllJaxRsTests;
import org.restlet.test.spring.AllSpringTests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite of unit tests for the Restlet RI.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class RestletTestSuite extends TestSuite {
    /** Constructor. */
    @SuppressWarnings("deprecation")
    public RestletTestSuite() {
        addTestSuite(ByteUtilsTestCase.class);
        addTestSuite(CallTestCase.class);
        addTestSuite(CookieTestCase.class);
        addTestSuite(ComponentXmlTestCase.class);
        addTestSuite(DirectoryTestCase.class);
        addTestSuite(FileClientTestCase.class);
        addTestSuite(FileReferenceTestCase.class);
        addTestSuite(FileRepresentationTestCase.class);
        addTestSuite(FilterTestCase.class);
        addTestSuite(FreeMarkerTestCase.class);
        addTestSuite(HttpBasicTestCase.class);
        addTestSuite(MediaTypeTestCase.class);
        addTestSuite(RedirectTestCase.class);
        addTestSuite(ReferenceTestCase.class);
        addTestSuite(ResolvingTransformerTestCase.class);
        addTestSuite(ResourceTestCase.class);
        addTestSuite(RestartTestCase.class);
        addTestSuite(RiapTestCase.class);
        addTestSuite(RouteListTestCase.class);
        addTestSuite(SpringTestCase.class);
        addTestSuite(StatusTestCase.class);
        addTestSuite(TemplateTestCase.class);
        addTestSuite(TransformerTestCase.class);
        addTestSuite(VelocityTestCase.class);
        addTestSuite(WadlTestCase.class);

        addTest(AllJaxRsTests.suite());
        addTest(AllSpringTests.suite());
    }

    /**
     * JUnit constructor.
     * 
     * @return The unit test.
     */
    public static Test suite() {
        return new RestletTestSuite();
    }

}
