/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.test.resource;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ResourceTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Resource package");
        // $JUnit-BEGIN$
        // suite.addTestSuite(DirectoryTestCase.class);

        suite.addTestSuite(AnnotatedResource01TestCase.class);
        suite.addTestSuite(AnnotatedResource02TestCase.class);
        suite.addTestSuite(AnnotatedResource03TestCase.class);
        suite.addTestSuite(AnnotatedResource04TestCase.class);
        suite.addTestSuite(AnnotatedResource05TestCase.class);
        suite.addTestSuite(AnnotatedResource06TestCase.class);
        suite.addTestSuite(AnnotatedResource07TestCase.class);
        suite.addTestSuite(AnnotatedResource08TestCase.class);
        // FIXME the new internal connector based on HttpUrlConnection does not
        // support non-HTTP methods.
        // suite.addTestSuite(AnnotatedResource9TestCase.class);
        suite.addTestSuite(AnnotatedResource10TestCase.class);
        suite.addTestSuite(AnnotatedResource11TestCase.class);
        suite.addTestSuite(AnnotatedResource12TestCase.class);
        suite.addTestSuite(AnnotatedResource13TestCase.class);
        suite.addTestSuite(AnnotatedResource14TestCase.class);
        suite.addTestSuite(AnnotatedResource15TestCase.class);
        suite.addTestSuite(AnnotatedResource16TestCase.class);
        suite.addTestSuite(AnnotatedResource17TestCase.class);
        suite.addTestSuite(AnnotatedResource18TestCase.class);

        // Tests based on HTTP client connectors are not supported by the GAE
        // edition.
        // [ifndef gae]
        // suite.addTestSuite(FileRepresentationTestCase.class);
        // [enddef]
        // $JUnit-END$

        return suite;
    }

}
