package org.restlet.example.book.restlet.ch03.sect5.sub4;

import org.restlet.resource.ClientResource;

/**
 * Creating dynamic proxies based on annotated Java interfaces.
 */
public class MailClient {

    public static void main(String[] args) throws Exception {
        RootResource mailRoot = ClientResource.create(
                "http://localhost:8182/", RootResource.class);
        String result = mailRoot.represent();
        System.out.println(result);
    }

}
