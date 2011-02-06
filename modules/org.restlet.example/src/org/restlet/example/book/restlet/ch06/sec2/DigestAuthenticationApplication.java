/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.example.book.restlet.ch06.sec2;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.ext.crypto.DigestAuthenticator;
import org.restlet.routing.Router;
import org.restlet.security.LocalVerifier;
import org.restlet.security.MapVerifier;

/**
 * @author Bruno Harbulot (bruno/distributedmatter.net)
 * 
 */
public class DigestAuthenticationApplication extends Application {
    private final LocalVerifier verifier;

    public DigestAuthenticationApplication() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("scott", "tiger".toCharArray());
        this.verifier = verifier;
    }

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());

        DigestAuthenticator authenticator = new DigestAuthenticator(
                getContext(), "Digest Test", "1234567890ABCDEF");
        authenticator.setWrappedVerifier(this.verifier);
        authenticator.setNext(router);
        return authenticator;
    }

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8111);

        component.getDefaultHost().attachDefault(
                new DigestAuthenticationApplication());
        component.start();
    }
}
