package org.restlet.example.book.restlet.misc;

import java.net.HttpURLConnection;
import java.net.URL;


import org.restlet.Server;
import org.restlet.data.Protocol;


public class JdkClient {

    public static void main(String[] args) throws Exception {
        // Instantiating the HTTP server and listening on port 8182
        new Server(Protocol.HTTP, 8182, TaggedServerResource.class)
                .start();

        final URL url = new URL("http://localhost:8182/");
        HttpURLConnection connection = (HttpURLConnection) url
                .openConnection();
        connection.getContent();
        connection.disconnect();

        String etag = connection.getHeaderField("ETag");
        if (etag.startsWith("W/")) {
            etag = etag.substring(2);
        }

        System.out.println("Putting if tag has changed.");
        connection.addRequestProperty("If-None-Match", etag);
        connection.setRequestMethod("PUT");
        connection.getContent();
        connection.disconnect();

        System.out.println("Putting if tag is still the same.");
        connection.addRequestProperty("If-None-Match", null);
        connection.addRequestProperty("If-Match", etag);
        connection.getContent();
        connection.disconnect();
    }
}
