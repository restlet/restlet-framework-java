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
import org.restlet.test.jaxrs.services.car.CarListResource;
import org.restlet.test.jaxrs.services.car.CarResource;


public class CarTest extends JaxRsTestCase
{
	@Override
	protected Collection<Class<?>> createRootResourceColl()
	{
		return (Collection)Collections.singleton(CarListResource.class);
	}

	private static final boolean ONLY_ONE_CAR = true;
	private static final boolean ONLY_OFFERS = false;
	
	public static void testGetPlainText() throws Exception
	{
		if(ONLY_ONE_CAR || ONLY_OFFERS) return;
		Response response = accessServer(CarListResource.class, Method.GET, MediaType.TEXT_PLAIN);
		assertTrue(response.getStatus().isSuccess());
		Representation representation = response.getEntity();
		assertEquals(CarListResource.DUMMY_CAR_LIST, representation.getText());
		assertEqualMediaType(MediaType.TEXT_PLAIN, representation.getMediaType());
	}

	public static void testGetHtmlText() throws Exception
	{
		if(ONLY_ONE_CAR || ONLY_OFFERS) return;
		Response response = accessServer(CarListResource.class, Method.GET, MediaType.TEXT_HTML);
		assertTrue(response.getStatus().isClientError());
		assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
	}

	public static void testGetOffers() throws Exception
	{
		if(ONLY_ONE_CAR) return;
		Response response = accessServer(CarListResource.class, "offers", Method.GET, null);
		Representation representation = response.getEntity();
		if(response.getStatus().isError())
			System.out.println(representation.getText());
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals(CarListResource.OFFERS, representation.getText());
		assertEqualMediaType(MediaType.APPLICATION_OCTET_STREAM, representation.getMediaType()); // vorläufig
	}

	public static void testGetCar() throws Exception
	{
		if(ONLY_OFFERS) return;
		String carNumber = "5";
		Response response = accessServer(CarListResource.class, carNumber, Method.GET, null);
		Representation representation = response.getEntity();
		if(response.getStatus().isError())
			System.out.println(representation.getText());
		assertEquals(Status.SUCCESS_OK, response.getStatus());
		assertEquals(CarResource.createTextRepr(carNumber), representation.getText());
		assertEqualMediaType(MediaType.TEXT_PLAIN, representation.getMediaType()); // vorläufig
	}
	
	public static void testDelete() throws Exception
	{
		if(ONLY_ONE_CAR || ONLY_OFFERS) return;
		Response response = accessServer(CarListResource.class, Method.DELETE);
		assertTrue(response.getStatus().isClientError());
		assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response.getStatus());
	}
}