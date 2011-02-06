/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.example.book.restlet.ch05.sec6.client;

import junit.framework.TestCase;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.example.book.restlet.ch04.sec3.server.MailServerComponent;

/**
 * Mail client JUnit test case.
 */
public class MailClientTestCase extends TestCase {

    /**
     * Unit test for virtual hosts.
     * 
     * @throws Exception
     */
    public void testVirtualHost() throws Exception {

        // Instantiate our Restlet component
        MailServerComponent component = new MailServerComponent();

        // Prepare a mock HTTP call
        Request request = new Request();
        request.setMethod(Method.GET);
        request.setResourceRef("http://www.rmep.org/accounts/");
        request.setHostRef("http://www.rmep.org");
        Response response = new Response(request);
        response.getServerInfo().setAddress("1.2.3.10");
        response.getServerInfo().setPort(80);
        component.handle(request, response);

        // Test if response was successful
        assertTrue(response.getStatus().isSuccess());
    }

}
