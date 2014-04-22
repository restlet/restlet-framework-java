/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.info;

import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * Component that can configure itself given a APISpark document. First, it
 * creates the server connectors and the virtual hosts if needed, trying to
 * reuse existing ones if available. Then it creates a
 * {@link ApisparkApplication} using this
 * {@link ApisparkApplication#APISparkApplication(Representation)} constructor.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class ApisparkComponent extends Component {

	/**
	 * Main method capable of configuring and starting a whole Restlet Component
	 * based on a list of local APISpark documents URIs, for example
	 * "file:///C:/YahooSearch.apispark".<br>
	 * <br>
	 * The necessary client connectors are automatically created.
	 * 
	 * @param args
	 *            List of local APISpark document URIs.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// Create a new APISpark-aware component
		final ApisparkComponent component = new ApisparkComponent();

		// For each APISpark document URI attach a matching Application
		for (final String arg : args) {
			component.attach(arg);
		}

		// Start the component
		component.start();
	}

	/**
	 * Default constructor.
	 */
	public ApisparkComponent() {
	}

	/**
	 * Constructor loading a APISpark description document at a given URI.<br>
	 * <br>
	 * The necessary client connectors are automatically created.
	 * 
	 * @param apisparkRef
	 *            The URI reference to the APISpark description document.
	 */
	public ApisparkComponent(Reference apisparkRef) {
		attach(apisparkRef);
	}

	/**
	 * Constructor based on a given APISpark description document.
	 * 
	 * @param apispark
	 *            The APISpark description document.
	 */
	public ApisparkComponent(Representation apispark) {
		attach(apispark);
	}

	/**
	 * Constructor loading a APISpark description document at a given URI.<br>
	 * <br>
	 * The necessary client connectors are automatically created.
	 * 
	 * @param apisparkUri
	 *            The URI to the APISpark description document.
	 */
	public ApisparkComponent(String apisparkUri) {
		attach(apisparkUri);
	}

	/**
	 * Attaches an application created from a APISpark description document
	 * available at a given URI reference.
	 * 
	 * @param apisparkRef
	 *            The URI reference to the APISpark description document.
	 * @return The created APISpark application.
	 */
	public ApisparkApplication attach(Reference apisparkRef) {
		ApisparkApplication result = null;

		// Adds some common client connectors to load the APISpark documents
		if (!getClients().contains(apisparkRef.getSchemeProtocol())) {
			getClients().add(apisparkRef.getSchemeProtocol());
		}

		// Get the APISpark document
		final Response response = getContext().getClientDispatcher().handle(
				new Request(Method.GET, apisparkRef));

		if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
			result = attach(response.getEntity());
		}

		return result;
	}

	/**
	 * Attaches an application created from a APISpark description document to
	 * the component.
	 * 
	 * @param apispark
	 *            The APISpark description document.
	 * @return The created APISpark application.
	 */
	public ApisparkApplication attach(Representation apispark) {
		final ApisparkApplication result = new ApisparkApplication(getContext()
				.createChildContext(), apispark);
		result.attachToComponent(this);
		return result;
	}

	/**
	 * Attaches an application created from a APISpark description document
	 * available at a given URI.
	 * 
	 * @param apisparkUri
	 *            The URI to the APISpark description document.
	 * @return The created APISpark application.
	 */
	public ApisparkApplication attach(String apisparkUri) {
		return attach(new Reference(apisparkUri));
	}

}
