/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite of unit tests for the Restlet RI.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class RestletTestSuite extends TestSuite {
    /** Constructor. */
    public RestletTestSuite() {
        addTestSuite(CallTestCase.class);
        addTestSuite(CookieTestCase.class);
        addTestSuite(DirectoryTestCase.class);
        addTestSuite(FileReferenceTestCase.class);
        addTestSuite(FilterTestCase.class);
        addTestSuite(FreeMarkerTestCase.class);
        addTestSuite(MediaTypeTestCase.class);
        addTestSuite(RedirectTestCase.class);
        addTestSuite(ReferenceTestCase.class);
        addTestSuite(RestartTestCase.class);
        addTestSuite(RouteListTestCase.class);
        addTestSuite(TransformerTestCase.class);
        addTestSuite(VelocityTestCase.class);
        addTestSuite(HTTPBasicTest.class);
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
