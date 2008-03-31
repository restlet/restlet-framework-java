package org.restlet.example.book.restlet.ch4;
import org.restlet.Client;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

public class TestPost {

    public static void main(String[] args) throws Exception {

        Restlet restlet = new Restlet() {
            public void handle(Request request, Response response) {
                System.out.println(request.getMethod());
                System.out.println(request.isEntityAvailable());
                System.out.println(request.getEntity().getMediaType());
            }
        };
        Server httpServer = new Server(Protocol.HTTP, 8080, restlet);
        httpServer.start();

        // Instantiates a client according to a protocol
        Client client = new Client(Protocol.HTTP);
        // Instantiates a request with a method and the resource's URI
        Request request = new Request(Method.POST, "http://localhost:8080");
        request.setEntity(new StringRepresentation("lkkjlljllkj", MediaType.APPLICATION_FLASH));

        // Sends the request and gets the response
        Response response = client.handle(request);

        httpServer.stop();
    }
}