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
package org.restlet.example.book.restlet.ch06.sec3;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.example.book.restlet.ch06.EchoPrincipalsResource;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.RoleAuthorizer;

/**
 * @author Bruno Harbulot (bruno/distributedmatter.net)
 * 
 */
public class RoleAuthorizationApplication extends Application {
    public RoleAuthorizationApplication(Context context) {
        super(context);
    }

    @Override
    public synchronized Restlet createInboundRoot() {
        ChallengeAuthenticator authenticator = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_BASIC, "Basic Test");
        authenticator.setVerifier(getContext().getDefaultVerifier());

        RoleAuthorizer authorizer = new RoleAuthorizer();
        authorizer.getAuthorizedRoles().add(getRole("admin"));
        authorizer.setNext(EchoPrincipalsResource.class);

        Router router = new Router(getContext());
        router.attach("/admin", authorizer);
        router.attachDefault(EchoPrincipalsResource.class);

        authenticator.setNext(router);
        return authenticator;
    }
}
