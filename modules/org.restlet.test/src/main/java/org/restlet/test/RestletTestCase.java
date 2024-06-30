/**
 * Copyright 2005-2024 Qlik
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
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.restlet.engine.Engine;
import org.restlet.representation.ObjectRepresentation;

/**
 * Marker class. All Restlet tests should be derived from this class.
 *
 * @author Lars Heuer (heuer[at]semagia.com)
 * @author Jerome Louvel
 */
public abstract class RestletTestCase {

    public static final int DEFAULT_TEST_PORT = 1337;

    protected static final String PROPERTY_TEST_PORT = "org.restlet.test.port";

    public static int TEST_PORT = getTestPort();

    private static int getTestPort() {
        if (System.getProperties().containsKey(PROPERTY_TEST_PORT)) {
            return Integer.parseInt(System.getProperty(PROPERTY_TEST_PORT));
        }

        return DEFAULT_TEST_PORT;
    }

    @BeforeEach
    protected void setUp() throws Exception {
        setUpEngine();
    }

    protected void setUpEngine() {
        Engine.clearThreadLocalVariables();

        // Restore a clean engine
        org.restlet.engine.Engine.register();

        // Prefer the internal connectors
        Engine.getInstance()
                .getRegisteredServers()
                .add(0, new org.restlet.engine.connector.HttpServerHelper(null));
        // FIXME turn on the internal connector.
        Engine.getInstance().getRegisteredClients()
                .add(0, new org.restlet.ext.httpclient.HttpClientHelper(null));

        // Enable object serialization
        ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED = true;
        ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED = true;
    }

    @AfterEach
    protected void tearDown() throws Exception {
        tearDownEngine();
    }

    protected void tearDownEngine() {
        Engine.clearThreadLocalVariables();
    }
}
