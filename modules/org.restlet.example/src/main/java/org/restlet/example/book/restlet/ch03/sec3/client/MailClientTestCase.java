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

package org.restlet.example.book.restlet.ch03.sec3.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.example.book.restlet.ch03.sec3.server.MailServerComponent;

/**
 * Mail client JUnit test case.
 */
public class MailClientTestCase {

    /**
     * Unit test for virtual hosts.
     * 
     * @throws Exception
     */
    @Test
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
