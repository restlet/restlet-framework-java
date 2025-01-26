/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.restlet.engine.Engine;
import org.restlet.representation.ObjectRepresentation;

/**
 * Marker class. All Restlet tests should be derived from this class.
 *
 * @author Lars Heuer (heuer[at]semagia.com)
 * @author Jerome Louvel
 */
public abstract class RestletTestCase {

    protected static final int DEFAULT_TEST_PORT = 1337;

    protected static final String PROPERTY_TEST_PORT = "org.restlet.test.port";

    public static int TEST_PORT = getTestPort();

    protected static int getTestPort() {
        if (System.getProperties().containsKey(PROPERTY_TEST_PORT)) {
            return Integer.parseInt(System.getProperty(PROPERTY_TEST_PORT));
        }

        return DEFAULT_TEST_PORT;
    }

    @BeforeAll
    static void setUp() {
        setUpEngine();
    }

    /**
     * Clears thread local variables then sets-up the Restlet engine with
     * internal HTTP server and Jetty HTTP client connectors by default.
     */
    public static void setUpEngine() {
        Engine.clearThreadLocalVariables();

        // Restore a clean engine
        org.restlet.engine.Engine.register();

        // Prefer the internal connectors
        Engine.getInstance().getRegisteredServers().add(0,
                new org.restlet.engine.connector.HttpServerHelper(null));

        // FIXME turn on the internal connector.
        Engine.getInstance().getRegisteredClients().add(0,
                new org.restlet.ext.jetty.HttpClientHelper(null));

        // Enable object serialization
        ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED = true;
        ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED = true;
    }

    @AfterAll
    static void tearDown() {
        tearDownEngine();
    }

    /**
     * Clears thread local variables.
     */
    static void tearDownEngine() {
        Engine.clearThreadLocalVariables();
    }
}
