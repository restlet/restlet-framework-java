package org.restlet.example.book.restlet.misc;

import org.restlet.representation.DigesterRepresentation;
import org.restlet.resource.ClientResource;

public class VerificationClient {
    public static void main(String[] args) throws Exception {
        ClientResource resource = new ClientResource(
                "http://localhost:8182/");
        // The Digester helps computing the digest while reading or writing the
        // representation's content.
        DigesterRepresentation rep = resource.get().getDigester();
        rep.write(System.out);
        if (rep.checkDigest()) {
            System.out.println("\nContent checked.");
        } else {
            System.out.println("\nContent not checked.");
        }
    }
}
