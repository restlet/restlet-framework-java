package org.restlet.test.ext.oauth;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Suite of unit tests for the OData extension.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class OAuthTestSuite extends TestSuite {

    /**
     * JUnit constructor.
     * 
     * @return The unit test.
     */
    public static Test suite() {
        TestSuite result = new TestSuite("OAuth extension");

        return result;
    }

}
