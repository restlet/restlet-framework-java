/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test.jaxrs.services.tests;

import java.util.Collection;
import java.util.Collections;

import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.SimpleHouse;

public class SimpleHouseTest extends JaxRsTestCase
{

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Class<?>> createRootResourceColl()
	{
		return (Collection) Collections.singleton(SimpleHouse.class);
	}

	public static void testGetHtmlText() throws Exception
	{
		Response response = JaxRsTestCase.accessServer(SimpleHouse.class, Method.GET, MediaType.TEXT_HTML);
		assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
	}

	public static void testGetPlainText() throws Exception
	{
		Response response = JaxRsTestCase.accessServer(SimpleHouse.class, Method.GET, MediaType.TEXT_PLAIN);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		Representation representation = response.getEntity();
		assertEquals(SimpleHouse.RERP_PLAIN_TEXT, representation.getText());
		assertEqualMediaType(MediaType.TEXT_PLAIN, representation.getMediaType());
	}

	public static void testGetTextAll() throws Exception
	{
		Response response = JaxRsTestCase.accessServer(SimpleHouse.class, Method.GET, MediaType.TEXT_ALL);
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		Representation representation = response.getEntity();
		assertEquals(SimpleHouse.RERP_PLAIN_TEXT, representation.getText());
		assertEqualMediaType(MediaType.TEXT_PLAIN, representation.getMediaType());
	}

	public void testHead1() throws Exception
	{
		Response responseGett = JaxRsTestCase.accessServer(SimpleHouse.class, "headTest1", Method.GET, MediaType.TEXT_HTML);
		Response responseHead = JaxRsTestCase.accessServer(SimpleHouse.class, "headTest1", Method.HEAD, MediaType.TEXT_HTML);
		if(responseGett.getStatus().isError())
			System.out.println(responseGett.getEntity().getText());
		assertEquals(Status.SUCCESS_OK, responseGett.getStatus());
		if(responseHead.getStatus().isError())
			System.out.println(responseHead.getEntity().getText());
		assertEquals(Status.SUCCESS_OK, responseHead.getStatus());
		Representation representationGett = responseGett.getEntity();
		Representation representationHead = responseHead.getEntity();
		assertEquals("4711", representationGett.getText());
		assertEquals("", representationHead.getText());
		assertEqualMediaType(MediaType.TEXT_HTML, representationGett.getMediaType());
		assertEqualMediaType(MediaType.TEXT_HTML, representationHead.getMediaType());
	}

	public void testHead2() throws Exception
	{
		Response responseGett = JaxRsTestCase.accessServer(SimpleHouse.class, "headTest2", Method.GET, MediaType.TEXT_HTML);
		Response responseHead = JaxRsTestCase.accessServer(SimpleHouse.class, "headTest2", Method.HEAD, MediaType.TEXT_HTML);
		if(responseGett.getStatus().isError())
			System.out.println(responseGett.getEntity().getText());
		assertEquals(Status.SUCCESS_OK, responseGett.getStatus());
		if(responseHead.getStatus().isError())
			System.out.println(responseHead.getEntity().getText());
		assertEquals(Status.SUCCESS_OK, responseHead.getStatus());
		Representation representationGett = responseGett.getEntity();
		Representation representationHead = responseHead.getEntity();
		assertEquals("4711", representationGett.getText());
		assertEquals("", representationHead.getText());
		assertEqualMediaType(MediaType.TEXT_HTML, representationGett.getMediaType());
		assertEqualMediaType(MediaType.TEXT_HTML, representationHead.getMediaType());
	}

	public void testHead2plain() throws Exception
	{
		Response responseGett = JaxRsTestCase.accessServer(SimpleHouse.class, "headTest2", Method.GET, MediaType.TEXT_PLAIN);
		Response responseHead = JaxRsTestCase.accessServer(SimpleHouse.class, "headTest2", Method.HEAD, MediaType.TEXT_PLAIN);
		if(responseGett.getStatus().isError())
			System.out.println(responseGett.getEntity().getText());
		assertEquals(Status.SUCCESS_OK, responseGett.getStatus());
		if(responseHead.getStatus().isError())
			System.out.println(responseHead.getEntity().getText());
		assertEquals(Status.SUCCESS_OK, responseHead.getStatus());
		Representation representationGett = responseGett.getEntity();
		Representation representationHead = responseHead.getEntity();
		assertEquals("4711", representationGett.getText());
		assertEquals("", representationHead.getText());
		assertEqualMediaType(MediaType.TEXT_PLAIN, representationGett.getMediaType());
		assertEqualMediaType(MediaType.TEXT_PLAIN, representationHead.getMediaType());
	}

	public static void main(String[] args) throws Exception
	{
		Component component = startServer(SimpleHouse.class);
		System.in.read();
		stopServer(component);
	}
}