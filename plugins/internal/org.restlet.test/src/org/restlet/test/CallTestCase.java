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
package org.restlet.test;

import java.util.Arrays;
import java.util.List;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientData;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;

import com.noelios.restlet.impl.connector.HttpCall;

/**
 * Test {@link org.restlet.Call}.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public class CallTestCase extends RestletTestCase
{
	/**
	 * Returns a request. 
	 * @return Request instance.
	 */
	protected Request getRequest()
	{
		return new Request();
	}

	/**
	 * Returns a response. 
	 * @param request The associated request.
	 * @return Response instance.
	 */
	protected Response getResponse(Request request)
	{
		return new Response(request);
	}

	/**
	 * Returns a connector call.
	 *
	 * @return A connector call instance.
	 */
	protected HttpCall getHttpCall()
	{
		return new HttpCall();
	}

	/**
	 * Returns a reference with the specified URI.
	 *
	 * @param uri The URI.
	 * @return Reference instance.
	 */
	protected Reference getReference(String uri)
	{
		return new Reference(uri);
	}

	/**
	 * Tests status getting/setting. 
	 */
	public void testStatus() throws Exception
	{
		Request request = getRequest();
		Response response = getResponse(request);
		response.setStatus(Status.SUCCESS_OK);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());
	}

	/**
	 * Tests client address getting/setting. 
	 */
	public void testClientAddress() throws Exception
	{
		ClientData client = getRequest().getClient();
		String address = "127.0.0.1";
		client.setAddress(address);
		assertEquals(address, client.getAddress());
		assertEquals(1, client.getAddresses().size());
		assertEquals(address, client.getAddresses().get(0));
		address = "192.168.99.10";
		client.setAddress(address);
		assertEquals(address, client.getAddress());
		assertEquals(1, client.getAddresses().size());
		assertEquals(address, client.getAddresses().get(0));
	}

	/**
	 * Tests client addresses getting/setting. 
	 */
	public void testClientAddresses() throws Exception
	{
		ClientData client = getRequest().getClient();
		String firstAddress = "127.0.0.1";
		String secondAddress = "192.168.99.10";
		List<String> addresses = Arrays.asList(new String[]
		{ firstAddress, secondAddress });
		client.getAddresses().addAll(addresses);
		assertEquals(addresses, client.getAddresses());
		assertEquals(firstAddress, client.getAddress());
		firstAddress = "192.168.99.20";
		client.setAddress(firstAddress);
		assertEquals(firstAddress, client.getAddress());
		assertEquals(firstAddress, client.getAddresses().get(0));
		assertEquals(secondAddress, client.getAddresses().get(1));
		firstAddress = "127.0.0.1";
		addresses = Arrays.asList(new String[]
		{ firstAddress, secondAddress });
		client.getAddresses().clear();
		client.getAddresses().addAll(addresses);
		assertEquals(addresses, client.getAddresses());
		assertEquals(firstAddress, client.getAddress());
	}

	/**
	 * Tests client agent getting/setting. 
	 */
	public void testClientAgent() throws Exception
	{
		ClientData client = getRequest().getClient();
		String name = "Restlet";
		client.setAgent(name);
		assertEquals(name, client.getAgent());
		name = "Restlet Client";
		client.setAgent(name);
		assertEquals(name, client.getAgent());
	}

	/**
	 * Tests server address getting/setting. 
	 */
	public void testServerAddress() throws Exception
	{
		Request request = getRequest();
		Response response = getResponse(request);
		String address = "127.0.0.1";
		response.getServer().setAddress(address);
		assertEquals(address, response.getServer().getAddress());
		address = "192.168.99.10";
		response.getServer().setAddress(address);
		assertEquals(address, response.getServer().getAddress());
	}

	/**
	 * Tests server agent getting/setting. 
	 */
	public void testServerAgent() throws Exception
	{
		Request request = getRequest();
		Response response = getResponse(request);
		String name = "Restlet";
		response.getServer().setAgent(name);
		assertEquals(name, response.getServer().getAgent());
		name = "Restlet Server";
		response.getServer().setAgent(name);
		assertEquals(name, response.getServer().getAgent());
	}

	/**
	 * Tests method getting/setting. 
	 */
	public void testMethod() throws Exception
	{
		Request request = getRequest();
		request.setMethod(Method.GET);
		assertEquals(Method.GET, request.getMethod());
		request.setMethod(Method.POST);
		assertEquals(Method.POST, request.getMethod());
	}

	/**
	 * Tests redirection reference getting/setting. 
	 */
	public void testRedirectionRef() throws Exception
	{
		Request request = getRequest();
		Response response = getResponse(request);
		String uri = "http://www.restlet.org/";
		Reference reference = getReference(uri);
		response.setRedirectRef(uri);
		assertEquals(reference, response.getRedirectRef());
		uri = "http://www.restlet.org/something";
		reference = getReference(uri);
		response.setRedirectRef(reference);
		assertEquals(reference, response.getRedirectRef());
	}

	/**
	 * Tests referrer reference getting/setting. 
	 */
	public void testReferrerRef() throws Exception
	{
		Request request = getRequest();
		String uri = "http://www.restlet.org/";
		Reference reference = getReference(uri);
		request.setReferrerRef(uri);
		assertEquals(reference, request.getReferrerRef());
		uri = "http://www.restlet.org/something";
		reference = getReference(uri);
		request.setReferrerRef(reference);
		assertEquals(reference, request.getReferrerRef());
	}

	/**
	 * Tests resource reference getting/setting. 
	 */
	public void testResourceRef() throws Exception
	{
		Request request = getRequest();
		String uri = "http://www.restlet.org/";
		Reference reference = getReference(uri);
		request.setResourceRef(uri);
		assertEquals(reference, request.getResourceRef());
		uri = "http://www.restlet.org/something";
		reference = getReference(uri);
		request.setResourceRef(reference);
		assertEquals(reference, request.getResourceRef());
	}

	/**
	 * Tests context's base reference getting/setting. 
	 */
	public void testBaseRef() throws Exception
	{
		Request request = getRequest();
		String resourceRefURI = "http://www.restlet.org/path/to/resource";
		Reference resourceRef = getReference(resourceRefURI);
		request.setResourceRef(resourceRefURI);
		assertEquals(resourceRef, request.getResourceRef());
		String uri = "http://www.restlet.org/path";
		Reference reference = getReference(uri);
		request.setBaseRef(uri);
		assertEquals(uri, request.getBaseRef().toString());
		assertEquals(reference, request.getBaseRef());
		uri = "http://www.restlet.org/path/to";
		reference = getReference(uri);
		request.setBaseRef(uri);
		assertEquals(uri, request.getBaseRef().toString());
		assertEquals(reference, request.getBaseRef());
	}

	/**
	 * Tests illegal context path setting. 
	 */
	public void testInvalidContextPath() throws Exception
	{
		//		Call call = getCall();
		//		String resourceRefURI = "http://www.restlet.org/something";
		//		Reference resourceRef = getReference(resourceRefURI);
		//		call.setResourceRef(resourceRefURI);
		//		assertEquals(resourceRef, call.getResourceRef());
		//		String uri = "http://www.restlet.org/cannot-match";
		//		try
		//		{
		//			call.setContextPath(uri);
		//			fail("Call accepts invalid context path. Resource path: '"
		//					+ resourceRefURI + "' Context path: '" + uri + "'");
		//		}
		//		catch (Exception ex)
		//		{
		//			// noop.
		//		}
	}

}
