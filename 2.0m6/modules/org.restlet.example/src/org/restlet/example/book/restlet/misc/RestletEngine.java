package org.restlet.example.book.restlet.misc;

import org.restlet.engine.ClientHelper;
import org.restlet.engine.Engine;
import org.restlet.engine.ServerHelper;
import org.restlet.engine.security.AuthenticatorHelper;

public class RestletEngine {

    public static void main(String[] args) throws Exception {
        // List all registered connectors.
        Engine engine = Engine.getInstance();
        System.out.println("Client connectors:");
        for (ClientHelper clientHelper : engine.getRegisteredClients()) {
            System.out.println(clientHelper.getProtocols());
        }
        System.out.println("Server connectors:");
        for (ServerHelper serverHelper : engine.getRegisteredServers()) {
            System.out.println(serverHelper.getProtocols());
        }
        System.out.println("Authentication schemes:");
        for (AuthenticatorHelper authenticationHelper : engine
                .getRegisteredAuthenticators()) {
            System.out.println(authenticationHelper.getChallengeScheme());
        }
    }
}
