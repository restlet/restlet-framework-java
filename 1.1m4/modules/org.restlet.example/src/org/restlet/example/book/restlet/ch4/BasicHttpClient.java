package org.restlet.example.book.restlet.ch4;
import org.restlet.Client;
import org.restlet.data.Protocol;

public class BasicHttpClient {
    public static void main(String[] args) {
        new Client(Protocol.HTTP).get("http://www.w3c.org");
    }
}
