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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.test.engine;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.restlet.test.engine.application.CorsResponseFilterTestCase;
import org.restlet.test.engine.connector.ConnectorsTestSuite;
import org.restlet.test.engine.io.BioUtilsTestCase;
import org.restlet.test.engine.io.ReaderInputStreamTestCase;

/**
 * Suite of unit tests for the Restlet Framework.
 * 
 * @author Jerome Louvel
 */
public class EngineTestSuite extends TestSuite {

    /**
     * JUnit constructor.
     * 
     * @return The unit test.
     */
    public static Test suite() {
        return new EngineTestSuite();
    }

    /** Constructor. */
    public EngineTestSuite() {
        super("Engine package");
        addTestSuite(AlphaNumericComparatorTestCase.class);
        addTestSuite(AnnotationUtilsTestCase.class);
        addTestSuite(BioUtilsTestCase.class);
        addTestSuite(CookiesTestCase.class);
        addTestSuite(ContentTypeTestCase.class);
        addTestSuite(HeaderTestCase.class);
        addTestSuite(HttpCallTestCase.class);
        addTestSuite(ImmutableDateTestCase.class);
        addTestSuite(UnclosableInputStreamTestCase.class);
        addTestSuite(UnclosableOutputStreamTestCase.class);
        addTestSuite(PreferencesTestCase.class);
        addTestSuite(ReaderInputStreamTestCase.class);
        addTestSuite(CorsResponseFilterTestCase.class);

        // Tests based on HTTP client connectors are not supported by the GAE
        // edition.
        // [ifndef gae]
        addTestSuite(AuthenticationTestCase.class);
        addTestSuite(TunnelFilterTestCase.class);
        addTestSuite(UserAgentTunnelFilterTestCase.class);
        // [enddef]

        // [ifdef jse]
        addTest(ConnectorsTestSuite.suite());
        // [enddef]
    }
}
