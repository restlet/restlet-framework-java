/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.test;

import junit.framework.TestCase;

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.DefaultCall;
import org.restlet.Restlet;
import org.restlet.component.RestletContainer;
import org.restlet.connector.DefaultServer;
import org.restlet.data.MediaTypes;
import org.restlet.data.Method;
import org.restlet.data.Methods;
import org.restlet.data.Protocols;

import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Unit tests for the RedirectRestlet.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class RedirectTestCase extends TestCase
{
	/**
	 * Tests the cookies parsing.
	 */
	public void testRedirect() throws Exception
	{
		// Create a new Restlet container
		RestletContainer myContainer = new RestletContainer();

		// Create the client connectors
		myContainer.getClients().put("TestClient", Protocols.HTTP);
		myContainer.getClients().put("ProxyClient", Protocols.HTTP);

		// Create the proxy Restlet
		String target = "http://localhost:9090${path}#[if query]?${query}#[end]";
		RedirectRestlet proxy = new RedirectRestlet(myContainer, target,
				RedirectRestlet.MODE_CONNECTOR);
		proxy.setConnectorName("ProxyClient");

		// Create a new Restlet that will display some path information.
		Restlet trace = new AbstractRestlet(myContainer)
		{
			public void handle(Call call)
			{
				// Print the requested URI path
				String output = "Resource URI:  " + call.getResourceRef() + '\n'
						+ "Context path:  " + call.getContextPath() + '\n'
						+ "Resource path: " + call.getResourcePath() + '\n'
						+ "Query string:  " + call.getResourceRef().getQuery() + '\n'
						+ "Method name:   " + call.getMethod() + '\n';
				call.setOutput(new StringRepresentation(output,
						MediaTypes.TEXT_PLAIN));
			}
		};

		// Create the server connectors
		myContainer.getServers().put("ProxyServer",
				new DefaultServer(Protocols.HTTP, proxy, 8080));
		myContainer.getServers().put("OriginServer",
				new DefaultServer(Protocols.HTTP, trace, 9090));

		// Now, let's start the container!
		myContainer.start();

		// Tests
		String uri = "http://localhost:8080/?foo=bar";
		testCall(myContainer, Methods.GET, uri);
		testCall(myContainer, Methods.POST, uri);
		testCall(myContainer, Methods.PUT, uri);
		testCall(myContainer, Methods.DELETE, uri);

		uri = "http://localhost:8080/abcd/efgh/ijkl?foo=bar&foo=beer";
		testCall(myContainer, Methods.GET, uri);
		testCall(myContainer, Methods.POST, uri);
		testCall(myContainer, Methods.PUT, uri);
		testCall(myContainer, Methods.DELETE, uri);

		uri = "http://localhost:8080/v1/client/kwse/CnJlNUQV9%252BNNqbUf7Lhs2BYEK2Y%253D/user/johnm/uVGYTDK4kK4zsu96VHGeTCzfwso%253D/";
		testCall(myContainer, Methods.GET, uri);

		// Stop the container
		myContainer.stop();
	}

	private void testCall(RestletContainer myContainer, Method method, String uri) throws Exception
	{
		Call call = new DefaultCall();
		call.setMethod(method);
		call.setResourceRef(uri);
		myContainer.callClient("TestClient", call);
		call.getOutput().write(System.out);
	}

}
