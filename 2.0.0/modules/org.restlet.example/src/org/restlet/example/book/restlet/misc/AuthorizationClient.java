package org.restlet.example.book.restlet.misc;

import org.restlet.data.ChallengeScheme;
import org.restlet.resource.ClientResource;

public class AuthorizationClient {
    public static void main(String[] args) throws Exception {

        ClientResource resource = new ClientResource(
                "http://localhost:8182/");
        resource.get();

        System.out.println(resource.getStatus());

        // Add the client authentication to the call
        resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "scott",
                "tiger");

        // Get the resource's state
        resource.get();
        System.out.println(resource.getStatus());
    }
}
