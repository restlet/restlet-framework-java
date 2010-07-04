package org.restlet.example.book.restlet.ch07.sec2.client;

import org.restlet.resource.ClientResource;

/**
 * Mail client.
 */
public class MailClient {

    /**
     * Mail client interacting with the RESTful mail server.
     * 
     * @param args
     *            The optional arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        System.out.println("\n1) Set-up the service client resource\n");
        ClientResource service = new ClientResource("http://localhost:8182");

        System.out.println("\n2) Describe the application\n");
        System.out.println(service.options().getText());
    }

}
