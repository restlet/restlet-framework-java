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
