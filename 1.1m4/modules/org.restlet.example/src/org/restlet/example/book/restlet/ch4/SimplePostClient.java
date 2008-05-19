package org.restlet.example.book.restlet.ch4;
import java.io.IOException;

import org.restlet.Client;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class SimplePostClient {
    // 
    // review=ljjll

    public static void main(String[] args) {
        // Instantiates a client according to a protocol
        Client client = new Client(Protocol.HTTP);
        // Instantiates a request with a method and the resource's URI
        Request request = new Request(Method.POST,
                "http://www.comp.leeds.ac.uk/cgi-bin/Perl/environment-example");
        Form form = new Form();
        form.set("review", "ljjll", false);
        request.setEntity(form.getWebRepresentation());

        // Sends the request and gets the response
        Response response = client.handle(request);

        // Prints the status of the response
        System.out.println(response.getStatus());

        // Writes the response's entity content, if available
        if (response.isEntityAvailable()) {
            try {
                response.getEntity().write(System.out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
