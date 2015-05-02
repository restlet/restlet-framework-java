/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.authentication;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.ext.crypto.DigestAuthenticator;
import org.restlet.resource.Directory;
import org.restlet.security.MapVerifier;

public class HttpDigestAuthenticationApplication extends Application {

    public static void main(String[] args) throws Exception {
        Component c = new Component();
        // server listening on pport 8182
        c.getServers().add(Protocol.HTTP, 8182);
        // client connector required by the Directory.
        c.getClients().add(Protocol.FILE);
        c.getDefaultHost().attach(new HttpDigestAuthenticationApplication());

        c.start();
    }

    @Override
    public Restlet createInboundRoot() {
        // Create a simple password verifier
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("scott", "tiger".toCharArray());

        DigestAuthenticator guard = new DigestAuthenticator(getContext(),
                "TestRealm", "mySecretServerKey");
        MapVerifier mapVerifier = new MapVerifier();
        mapVerifier.getLocalSecrets().put("scott", "tiger".toCharArray());
        guard.setWrappedVerifier(mapVerifier);

        // Create a Directory able to return a deep hierarchy of files
        Directory directory = new Directory(getContext(), "file:///tmp");
        directory.setListingAllowed(true);
        guard.setNext(directory);

        return guard;
    }

}
