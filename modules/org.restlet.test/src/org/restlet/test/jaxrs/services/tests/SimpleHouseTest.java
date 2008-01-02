/*
 * Copyright 2005-2007 Noelios Consulting.
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

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.SimpleTrain;


public class SimpleHouseTest extends JaxRsTestCase
{
	@Override
	protected Collection<Class<?>> createRootResourceColl()
	{
		return (Collection)Collections.singleton(SimpleTrain.class);
	}

	public static void testGetHtmlText() throws Exception
	{
		Response response = JaxRsTestCase.accessServer(SimpleTrain.class, Method.GET, MediaType.TEXT_HTML);
		assertTrue(response.getStatus().isClientError());
		assertEquals(response.getStatus(), Status.CLIENT_ERROR_NOT_ACCEPTABLE);
	}

	public static void testGetPlainText() throws Exception
	{
		Response response = JaxRsTestCase.accessServer(SimpleTrain.class, Method.GET, MediaType.TEXT_PLAIN);
		assertTrue(response.getStatus().isSuccess());
		Representation representation = response.getEntity();
		assertEquals(SimpleTrain.RERP_PLAIN_TEXT, representation.getText());
		assertEqualMediaType(MediaType.TEXT_PLAIN, representation.getMediaType());
	}

	public static void testGetTextAll() throws Exception
	{
		Response response = JaxRsTestCase.accessServer(SimpleTrain.class, Method.GET, MediaType.TEXT_ALL);
		assertTrue(response.getStatus().isSuccess());
		Representation representation = response.getEntity();
		assertEquals("", representation.getText());
		assertEqualMediaType(MediaType.TEXT_PLAIN, representation.getMediaType());

		// response = JaxRsTester.accessServer(SimpleTrain.class, Method.GET, MediaType.TEXT_CALENDAR);
		// assertTrue(response.getStatus().isSuccess()); representation = response.getEntity();
		// assertEquals(SimpleTrain.RERP_ANY_TEXT, representation.getText()); 
		// TODO assertEquals(MediaType.TEXT_PLAIN, representation.getMediaType());
	}
}