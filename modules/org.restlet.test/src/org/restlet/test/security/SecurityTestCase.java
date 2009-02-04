/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.security;

import org.junit.After;
import org.junit.Before;
import org.restlet.Component;
import org.restlet.test.RestletTestCase;

/**
 * Restlet unit tests for HTTP Basic authentication client/server. By default,
 * runs server on localhost on port {@value #DEFAULT_PORT}, which can be
 * overriden by setting system property {@value #RESTLET_TEST_PORT}
 * 
 * @author Stian Soiland
 */
public class SecurityTestCase extends RestletTestCase {

    private Component component;

    @Before
    public void makeServer() throws Exception {
    }

    @After
    public void stopServer() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }
    }

    public void testHttpBasic() {
        try {
            makeServer();
            stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
