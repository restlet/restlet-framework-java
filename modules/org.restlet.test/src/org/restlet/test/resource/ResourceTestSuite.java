package org.restlet.test.resource;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ResourceTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.restlet.test.resource");
        // $JUnit-BEGIN$
        suite.addTestSuite(DirectoryTestCase.class);
        suite.addTestSuite(AnnotatedResource2TestCase.class);
        suite.addTestSuite(AnnotatedResource1TestCase.class);
        suite.addTestSuite(ResourceTestCase.class);
        suite.addTestSuite(AnnotatedResource3TestCase.class);
        // $JUnit-END$
        
        // Tests based on HTTP client connectors are not supported by the GAE
        // edition.
        // [ifndef gae]
        suite.addTestSuite(FileRepresentationTestCase.class);
        // [enddef]
        
        return suite;
    }

}
