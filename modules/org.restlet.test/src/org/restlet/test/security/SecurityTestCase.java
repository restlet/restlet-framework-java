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

import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.test.RestletTestCase;

/**
 * Restlet unit tests for the security package.
 * 
 * @author Jerome Louvel
 */
public class SecurityTestCase extends RestletTestCase {

    private Component component;

    @Before
    public void startComponent() throws Exception {
        this.component = new SaasComponent();
    }

    @After
    public void stopServer() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }
    }

    public void testSecurity() {
        try {
            startComponent();

            String uri = "http://localhost:" + TEST_PORT;
            Client client = new Client(Protocol.HTTP);
            Response response = client.get(uri);

            assertEquals(Status.FORBIDDEN, response.getStatus());

            stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
