package org.restlet.example.book.restlet.ch06.sec5;

import org.restlet.representation.DigesterRepresentation;
import org.restlet.resource.ClientResource;

public class VerificationClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8111/");
        // The Digester helps computing the digest while reading or writing the
        // representation's content.
        DigesterRepresentation rep = new DigesterRepresentation(resource.get());
        rep.write(System.out);
        if (rep.checkDigest()) {
            System.out.println("\nContent checked.");
        } else {
            System.out.println("\nContent not checked.");
        }
    }
}
