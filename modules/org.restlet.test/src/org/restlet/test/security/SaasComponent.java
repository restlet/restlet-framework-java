/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.security.Group;
import org.restlet.security.Organization;
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

		Organization customer1 = createOrganization1(context, app);
		getOrganizations().add(customer1);

		Organization customer2 = createOrganization2(context, app);
		getOrganizations().add(customer2);

		getDefaultHost().attach(app);
		getServers().add(Protocol.HTTP, RestletTestCase.TEST_PORT);
	}

	private Organization createOrganization1(Context context, Application app) {
		// Declare the FooBar organization
		Organization customer1 = new Organization("FooBar Inc.",
				"Customer contract : #14680", "foobar.com");

		// Add users
		User stiger = new User("stiger", "pwd", "Scott", "Tiger",
				"scott.tiger@foobar.com");
		customer1.getUsers().add(stiger);

		User larmstrong = new User("larmstrong", "pwd", "Louis", "Armstrong",
				"la@foobar.com");
		customer1.getUsers().add(larmstrong);

		// Add groups
		Group employees = new Group("employees ", "All FooBar employees");
		employees.getMemberUsers().add(larmstrong);
		customer1.getRootGroups().add(employees);

		Group contractors = new Group("contractors ", "All FooBar contractors");
		contractors.getMemberUsers().add(stiger);
		customer1.getRootGroups().add(contractors);

		Group managers = new Group("managers", "All FooBar managers");
		customer1.getRootGroups().add(managers);

		Group directors = new Group("directors ", "Top-level directors");
		directors.getMemberUsers().add(larmstrong);
		managers.getMemberGroups().add(directors);

		Group developers = new Group("developers", "All FooBar developers");
		customer1.getRootGroups().add(developers);

		Group engineers = new Group("engineers", "All FooBar engineers");
		engineers.getMemberUsers().add(stiger);
		developers.getMemberGroups().add(engineers);

		context.map(customer1, app.findRole("user"));
		context.map(managers, app.findRole("admin"));
		context.bind(customer1);
		return customer1;
	}

	private Organization createOrganization2(Context context, Application app) {
		// Declare the FooBar organization
		Organization customer2 = new Organization("PetStory Inc.",
				"Customer contract : #13471", "petstory.com");

		// Add users
		User lbird = new User("lbird", "pwd", "Louis", "Bird",
				"lbird@gmail.com");
		customer2.getUsers().add(lbird);

		User glanglois = new User("glanglois", "pwd", "Gerard", "Langlois",
				"gl@yahoo.com");
		customer2.getUsers().add(glanglois);

		// Add groups
		Group sales = new Group("sales ", "Sales departement");
		sales.getMemberUsers().add(lbird);
		customer2.getRootGroups().add(sales);

		Group marketing = new Group("marketing", "Marketing department");
		marketing.getMemberUsers().add(glanglois);
		customer2.getRootGroups().add(marketing);

		// context.bind(customer2);
		return customer2;
	}
}
