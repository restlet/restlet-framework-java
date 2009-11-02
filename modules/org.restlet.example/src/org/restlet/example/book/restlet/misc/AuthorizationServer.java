package org.restlet.example.book.restlet.misc;

import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch01.HelloResource;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;


public class AuthorizationServer {
    public static void main(String[] args) throws Exception {
        ChallengeAuthenticator guard = new ChallengeAuthenticator(
                new Context(), ChallengeScheme.HTTP_BASIC, "Tutorial");
        // Instantiate and populate the verifier
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("scott", "tiger".toCharArray());

        // Set the verifier
        guard.setVerifier(verifier);
        guard.setNext(HelloResource.class);

        // Instantiating the HTTP server and listening on port 8182
        new Server(Protocol.HTTP, 8182, guard).start();
    }
}
