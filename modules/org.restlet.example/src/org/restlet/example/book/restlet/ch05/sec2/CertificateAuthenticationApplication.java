/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.book.restlet.ch05.sec2;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.example.book.restlet.ch05.EchoPrincipalsResource;
import org.restlet.routing.Router;
import org.restlet.security.Authenticator;

/**
 * @author Bruno Harbulot (bruno/distributedmatter.net)
 * 
 */
public class CertificateAuthenticationApplication extends Application {
    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attachDefault(EchoPrincipalsResource.class);

        Authenticator authenticator = new ClientCertificateAuthenticator(
                getContext());

        authenticator.setNext(router);
        return authenticator;
    }

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        Server server = component.getServers().add(Protocol.HTTPS, 8183);

        server.getContext().getParameters().add("wantClientAuthentication",
                "true");

        component.getDefaultHost().attachDefault(
                new CertificateAuthenticationApplication());
        component.start();
    }
}
