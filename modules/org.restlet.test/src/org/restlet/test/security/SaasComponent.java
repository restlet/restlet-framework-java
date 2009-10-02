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

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.security.Group;
import org.restlet.security.MemoryRealm;
import org.restlet.security.User;
import org.restlet.test.RestletTestCase;

/**
 * Sample SAAS component with declared organizations.
 * 
 * @author Jerome Louvel
 */
public class SaasComponent extends Component {

    public SaasComponent() {
        Context context = getContext().createChildContext();
        SaasApplication app = new SaasApplication(context);

        MemoryRealm realm = new MemoryRealm();
        context.setEnroler(realm.getEnroler());
        context.setVerifier(realm.getVerifier());

        // Add users
        User stiger = new User("stiger", "pwd", "Scott", "Tiger",
                "scott.tiger@foobar.com");
        realm.getUsers().add(stiger);

        User larmstrong = new User("larmstrong", "pwd", "Louis", "Armstrong",
                "la@foobar.com");
        realm.getUsers().add(larmstrong);

        // Add groups
        Group employees = new Group("employees ", "All FooBar employees");
        employees.getMemberUsers().add(larmstrong);
        realm.getRootGroups().add(employees);

        Group contractors = new Group("contractors ", "All FooBar contractors");
        contractors.getMemberUsers().add(stiger);
        realm.getRootGroups().add(contractors);

        Group managers = new Group("managers", "All FooBar managers");
        realm.getRootGroups().add(managers);

        Group directors = new Group("directors ", "Top-level directors");
        directors.getMemberUsers().add(larmstrong);
        managers.getMemberGroups().add(directors);

        Group developers = new Group("developers", "All FooBar developers");
        realm.getRootGroups().add(developers);

        Group engineers = new Group("engineers", "All FooBar engineers");
        engineers.getMemberUsers().add(stiger);
        developers.getMemberGroups().add(engineers);

        // realm.map(customer1, app.getRole("user"));
        realm.map(managers, app.getRole("admin"));

        getDefaultHost().attach(app);
        getServers().add(Protocol.HTTP, RestletTestCase.TEST_PORT);
    }
}
