package org.restlet.example.book.restlet.ch03.sect5.sub3;

import org.restlet.resource.ClientResource;

/**
 * Illustrating features of client resources.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        ClientResource mailRoot = new ClientResource("http://localhost:8111/");
        mailRoot.get().write(System.out);

        String result = mailRoot.get(String.class);
        System.out.println("\n" + result);
    }

}
