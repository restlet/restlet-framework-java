package org.restlet.test.bench;

import java.io.IOException;

import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

public class RestletClient {

    /**
     * @param args
     * @throws IOException
     * @throws ResourceException
     */
    public static void main(String[] args) throws ResourceException,
            IOException {
        for (int i = 0; i < 10; i++) {
            ClientResource cr = new ClientResource("http://www.restlet.org");
            cr.get().write(System.out);
        }
    }

}
