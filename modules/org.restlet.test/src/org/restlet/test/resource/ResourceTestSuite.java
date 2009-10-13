package org.restlet.test.resource;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ResourceTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.restlet.test.resource");
        // $JUnit-BEGIN$
        suite.addTestSuite(DirectoryTestCase.class);
        suite.addTestSuite(ResourceTestCase.class);

        // TODO fix the import order before uncommenting this test case
        // suite.addTestSuite(AnnotatedResource1TestCase.class);
        suite.addTestSuite(AnnotatedResource2TestCase.class);
        suite.addTestSuite(AnnotatedResource3TestCase.class);
        suite.addTestSuite(AnnotatedResource4TestCase.class);
        suite.addTestSuite(AnnotatedResource5TestCase.class);
        suite.addTestSuite(AnnotatedResource6TestCase.class);
        suite.addTestSuite(AnnotatedResource7TestCase.class);

        // Tests based on HTTP client connectors are not supported by the GAE
        // edition.
        // [ifndef gae]
        suite.addTestSuite(FileRepresentationTestCase.class);
        // [enddef]
        // $JUnit-END$

        return suite;
    }

}
