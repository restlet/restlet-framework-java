/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.engine.Engine;

/**
 * Marker class. All Restlet tests should be derived from this class.
 * 
 * @author Lars Heuer (heuer[at]semagia.com)
 */
public abstract class RestletTestCase extends TestCase {

    public static final int DEFAULT_TEST_PORT = 1337;

    private static final String PROPERTY_TEST_PORT = "org.restlet.test.port";

    public static int TEST_PORT = getTestPort();

    private static int getTestPort() {
        if (System.getProperties().containsKey(PROPERTY_TEST_PORT)) {
            return Integer.parseInt(System.getProperty(PROPERTY_TEST_PORT));
        }

        return DEFAULT_TEST_PORT;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("Setting up test " + getClass().getName() + "#"
                + getName());
        Engine.clearThreadLocalVariables();

        // Restore a clean engine
        org.restlet.engine.Engine.register();

        // Prefer the internal connectors
        Engine.getInstance()
                .getRegisteredServers()
                .add(0, new org.restlet.engine.connector.HttpServerHelper(null));
        Engine.getInstance()
                .getRegisteredClients()
                .add(0, new org.restlet.engine.connector.HttpClientHelper(null));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Engine.clearThreadLocalVariables();
    }
}
