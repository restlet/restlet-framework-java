package org.restlet.example.book.restlet.ch11;

import java.io.File;
import java.io.IOException;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class BasicHttpsClient {
    public static void main(String[] args) {
        // Instantiates a client according to a protocol
        final Client client = new Client(Protocol.HTTPS);
        // Instantiates a request with a method and the resource's URI
        final Request request = new Request(Method.GET,
                "https://localhost:8182/helloWorld");

        final File keystoreFile = new File("d:\\temp\\certificats",
                "myClientKeystore");
        System.setProperty("javax.net.ssl.trustStore", keystoreFile
                .getAbsolutePath());

        // Sends the request and gets the response
        final Response response = client.handle(request);

        // Prints the status of the response
        System.out.println(response.getStatus());

        // Writes the response's entity content, if available
        if (response.isEntityAvailable()) {
            try {
                response.getEntity().write(System.out);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
}
