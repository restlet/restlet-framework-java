/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.security;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Role;
import org.restlet.security.RoleAuthorizer;
import org.restlet.test.component.HelloWorldRestlet;

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

        Role admin = new Role(this, "admin", "Application administrators");
        getRoles().add(admin);

        Role user = new Role(this, "user", "Application users");
        getRoles().add(user);
    }

    @Override
    public Restlet createInboundRoot() {
        Router root = new Router();

        // Attach test 1
        ChallengeAuthenticator authenticator = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_BASIC, "saas");
        authenticator.setNext(new HelloWorldRestlet());
        root.attach("/httpBasicAuthenticator", authenticator);

        // Attach test 2
        Authorizer authorizer = Authorizer.ALWAYS;
        authorizer.setNext(new HelloWorldRestlet());
        root.attach("/alwaysAuthenticator", authorizer);

        // Attach test 3
        authorizer = Authorizer.NEVER;
        authorizer.setNext(new HelloWorldRestlet());
        root.attach("/neverAuthenticator", authorizer);

        // Attach test 4
        RoleAuthorizer roleAuthorizer = new RoleAuthorizer();
        roleAuthorizer.getAuthorizedRoles().add(getRole("admin"));
        roleAuthorizer.setNext(new HelloWorldRestlet());

        authenticator = new ChallengeAuthenticator(getContext(),
                ChallengeScheme.HTTP_BASIC, "saas");
        authenticator.setNext(roleAuthorizer);
        root.attach("/adminRoleAuthorizer", authenticator);

        // Attach test 5
        roleAuthorizer = new RoleAuthorizer();
        roleAuthorizer.getForbiddenRoles().add(getRole("admin"));
        roleAuthorizer.setNext(new HelloWorldRestlet());

        authenticator = new ChallengeAuthenticator(getContext(),
                ChallengeScheme.HTTP_BASIC, "saas");
        authenticator.setNext(roleAuthorizer);
        root.attach("/adminRoleForbiddenAuthorizer", authenticator);

        return root;
    }
}
