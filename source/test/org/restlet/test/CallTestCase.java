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

import org.restlet.Call;
import org.restlet.DefaultCall;
import org.restlet.connector.ConnectorCall;
import org.restlet.data.Methods;
import org.restlet.data.Reference;
import org.restlet.data.Statuses;

/**
 * Test {@link org.restlet.Call}.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public class CallTestCase extends RestletTestCase
{

	public static void main(String[] args)
	{
		junit.awtui.TestRunner.run(CallTestCase.class);
	}

	/**
	 * Returns the call that is used for the tests.
	 *
	 * @return A call instance.
	 */
	protected Call getCall()
	{
		return new DefaultCall();
	}

	/**
	 * Returns a connector call.
	 *
	 * @return A connector call instance.
	 */
	protected ConnectorCall getConnectorCall()
	{
		return new DummyConnectorCall();
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
		Call call = getCall();
		call.setStatus(Statuses.SUCCESS_OK);
		assertEquals(Statuses.SUCCESS_OK, call.getStatus());
		call.setStatus(Statuses.CLIENT_ERROR_BAD_REQUEST);
		assertEquals(Statuses.CLIENT_ERROR_BAD_REQUEST, call.getStatus());
	}

	/**
	 * Tests client address getting/setting. 
	 */
	public void testClientAddress() throws Exception
	{
		Call call = getCall();
		String address = "127.0.0.1";
		call.setClientAddress(address);
		assertEquals(address, call.getClientAddress());
		assertEquals(1, call.getClientAddresses().size());
		assertEquals(address, call.getClientAddresses().get(0));
		address = "192.168.99.10";
		call.setClientAddress(address);
		assertEquals(address, call.getClientAddress());
		assertEquals(1, call.getClientAddresses().size());
		assertEquals(address, call.getClientAddresses().get(0));
	}

	/**
	 * Tests client addresses getting/setting. 
	 */
	public void testClientAddresses() throws Exception
	{
		Call call = getCall();
		String firstAddress = "127.0.0.1";
		String secondAddress = "192.168.99.10";
		List<String> addresses = Arrays.asList(new String[]{ firstAddress, secondAddress });
		call.getClientAddresses().addAll(addresses);
		assertEquals(addresses, call.getClientAddresses());
		assertEquals(firstAddress, call.getClientAddress());
		firstAddress = "192.168.99.20";
		call.setClientAddress(firstAddress);
		assertEquals(firstAddress, call.getClientAddress());
		assertEquals(firstAddress, call.getClientAddresses().get(0));
		assertEquals(secondAddress, call.getClientAddresses().get(1));
		firstAddress = "127.0.0.1";
		addresses = Arrays.asList(new String[] { firstAddress, secondAddress });
		call.getClientAddresses().clear();
		call.getClientAddresses().addAll(addresses);
		assertEquals(addresses, call.getClientAddresses());
		assertEquals(firstAddress, call.getClientAddress());
	}

	/**
	 * Tests client name getting/setting. 
	 */
	public void testClientName() throws Exception
	{
		Call call = getCall();
		String name = "Restlet";
		call.setClientName(name);
		assertEquals(name, call.getClientName());
		name = "Restlet Client";
		call.setClientName(name);
		assertEquals(name, call.getClientName());
	}

	/**
	 * Tests server address getting/setting. 
	 */
	public void testServerAddress() throws Exception
	{
		Call call = getCall();
		String address = "127.0.0.1";
		call.setServerAddress(address);
		assertEquals(address, call.getServerAddress());
		address = "192.168.99.10";
		call.setServerAddress(address);
		assertEquals(address, call.getServerAddress());
	}

	/**
	 * Tests server name getting/setting. 
	 */
	public void testServerName() throws Exception
	{
		Call call = getCall();
		String name = "Restlet";
		call.setServerName(name);
		assertEquals(name, call.getServerName());
		name = "Restlet Server";
		call.setServerName(name);
		assertEquals(name, call.getServerName());
	}

	/**
	 * Tests method getting/setting. 
	 */
	public void testMethod() throws Exception
	{
		Call call = getCall();
		call.setMethod(Methods.GET);
		assertEquals(Methods.GET, call.getMethod());
		call.setMethod(Methods.POST);
		assertEquals(Methods.POST, call.getMethod());
	}

	/**
	 * Tests redirection reference getting/setting. 
	 */
	public void testRedirectionRef() throws Exception
	{
		Call call = getCall();
		String uri = "http://www.restlet.org/";
		Reference reference = getReference(uri);
		call.setRedirectionRef(uri);
		assertEquals(reference, call.getRedirectionRef());
		uri = "http://www.restlet.org/something";
		reference = getReference(uri);
		call.setRedirectionRef(reference);
		assertEquals(reference, call.getRedirectionRef());
	}

	/**
	 * Tests referrer reference getting/setting. 
	 */
	public void testReferrerRef() throws Exception
	{
		Call call = getCall();
		String uri = "http://www.restlet.org/";
		Reference reference = getReference(uri);
		call.setReferrerRef(uri);
		assertEquals(reference, call.getReferrerRef());
		uri = "http://www.restlet.org/something";
		reference = getReference(uri);
		call.setReferrerRef(reference);
		assertEquals(reference, call.getReferrerRef());
	}

	/**
	 * Tests resource reference getting/setting. 
	 */
	public void testResourceRef() throws Exception
	{
		Call call = getCall();
		String uri = "http://www.restlet.org/";
		Reference reference = getReference(uri);
		call.setResourceRef(uri);
		assertEquals(reference, call.getResourceRef());
		uri = "http://www.restlet.org/something";
		reference = getReference(uri);
		call.setResourceRef(reference);
		assertEquals(reference, call.getResourceRef());
	}

	/**
	 * Tests context path getting/setting. 
	 */
	public void testContextPath() throws Exception
	{
		Call call = getCall();
		String resourceRefURI = "http://www.restlet.org/path/to/resource";
		Reference resourceRef = getReference(resourceRefURI);
		call.setResourceRef(resourceRefURI);
		assertEquals(resourceRef, call.getResourceRef());
		String uri = "http://www.restlet.org/path";
		Reference reference = getReference(uri);
		call.setContextPath(uri);
		assertEquals(uri, call.getContextPath());
		assertEquals(reference, call.getContextRef());
		uri = "http://www.restlet.org/path/to";
		reference = getReference(uri);
		call.setContextPath(uri);
		assertEquals(uri, call.getContextPath());
		assertEquals(reference, call.getContextRef());
	}

	/**
	 * Tests illegal context path setting. 
	 */
	public void testInvalidContextPath() throws Exception
	{
		Call call = getCall();
		String resourceRefURI = "http://www.restlet.org/something";
		Reference resourceRef = getReference(resourceRefURI);
		call.setResourceRef(resourceRefURI);
		assertEquals(resourceRef, call.getResourceRef());
		String uri = "http://www.restlet.org/cannot-match";
		try
		{
			call.setContextPath(uri);
			fail("Call accepts invalid context path. Resource path: '"
					+ resourceRefURI + "' Context path: '" + uri + "'");
		}
		catch (Exception ex)
		{
			// noop.
		}
	}

	/**
	 * Tests connector call getting/setting. 
	 */
	public void testConnectorCall() throws Exception
	{
		Call call = getCall();
		assertEquals("Unexpected initial value", null, call.getConnectorCall());
		ConnectorCall connCall = getConnectorCall();
		call.setConnectorCall(connCall);
		assertEquals(connCall, call.getConnectorCall());
	}

	/**
	 * Tests illegal connector call setting. 
	 */
	public void testIllegalConnectorCall() throws Exception
	{
		Call call = getCall();
		try
		{
			call.setConnectorCall(null);
			fail("Call accepts setting connector call to null");
		}
		catch (Exception ex)
		{
			// noop.
		}
	}

}
