/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.test.security;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.ChallengeGuard;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;
import org.restlet.test.HelloWorldRestlet;

/**
 * Sample SAAS application with a Basic authenticator guarding a hello world
 * Restlet.
 * 
 * @author Jerome Louvel
 */
public class SaasApplication extends Application {

    public SaasApplication() {
        this(null);
    }

    public SaasApplication(Context context) {
        super(context);

        Role admin = new Role("admin", "Application administrators");
        getRoles().add(admin);

        Role user = new Role("user", "Application users");
        getRoles().add(user);
    }

    @Override
    public Restlet createRoot() {
        Router root = new Router();

        // Attach test 1
        ChallengeAuthenticator authenticator = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_BASIC, "saas");
        authenticator.setNext(new HelloWorldRestlet());
        root.attach("/test1", authenticator);

        // Attach test 2
        Authorizer authorizer = Authorizer.ALWAYS;
        authorizer.setNext(new HelloWorldRestlet());
        root.attach("/test2", authorizer);

        // Attach test 3
        authorizer = Authorizer.NEVER;
        authorizer.setNext(new HelloWorldRestlet());
        root.attach("/test3", authorizer);

        // Attach test 4
        RoleAuthorizer roleAuthorizer = new RoleAuthorizer();
        roleAuthorizer.getAuthorizedRoles().add(findRole("admin"));

        ChallengeGuard guard = new ChallengeGuard(getContext(),
                ChallengeScheme.HTTP_BASIC, "saas");
        guard.setAuthorizer(roleAuthorizer);
        guard.setNext(new HelloWorldRestlet());
        root.attach("/test4", guard);

        // Attach test 5
        roleAuthorizer = new RoleAuthorizer();
        roleAuthorizer.getForbiddenRoles().add(findRole("admin"));
        roleAuthorizer.setNext(new HelloWorldRestlet());

        authenticator = new ChallengeAuthenticator(getContext(),
                ChallengeScheme.HTTP_BASIC, "saas");
        authenticator.setNext(roleAuthorizer);
        root.attach("/test5", authenticator);

        return root;
    }
}
