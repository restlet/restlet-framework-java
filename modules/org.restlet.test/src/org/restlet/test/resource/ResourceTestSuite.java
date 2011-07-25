/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.test.resource;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ResourceTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Resource package");
        // $JUnit-BEGIN$
        suite.addTestSuite(DirectoryTestCase.class);

        suite.addTestSuite(AnnotatedResource1TestCase.class);
        suite.addTestSuite(AnnotatedResource2TestCase.class);
        suite.addTestSuite(AnnotatedResource3TestCase.class);
        suite.addTestSuite(AnnotatedResource4TestCase.class);
        suite.addTestSuite(AnnotatedResource5TestCase.class);
        suite.addTestSuite(AnnotatedResource6TestCase.class);
        suite.addTestSuite(AnnotatedResource7TestCase.class);
        suite.addTestSuite(AnnotatedResource8TestCase.class);
        suite.addTestSuite(AnnotatedResource9TestCase.class);
        suite.addTestSuite(AnnotatedResource10TestCase.class);
        suite.addTestSuite(AnnotatedResource11TestCase.class);
        suite.addTestSuite(AnnotatedResource12TestCase.class);
        suite.addTestSuite(AnnotatedResource13TestCase.class);

        // Tests based on HTTP client connectors are not supported by the GAE
        // edition.
        // [ifndef gae]
        suite.addTestSuite(FileRepresentationTestCase.class);
        // [enddef]
        // $JUnit-END$

        return suite;
    }

}
