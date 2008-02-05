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

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.car.CarListResource;
import org.restlet.test.jaxrs.services.car.CarResource;

public class CarTest extends JaxRsTestCase {
    private static final boolean ONLY_OFFERS = false;

    private static final boolean ONLY_ONE_CAR = false;

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Class<?>> createRootResourceColl() {
        return (Collection) Collections.singleton(CarListResource.class);
    }

    public void testDelete() throws Exception {
        if (ONLY_ONE_CAR || ONLY_OFFERS)
            return;
        Response response = accessServer(Method.DELETE, CarListResource.class);
        assertTrue(response.getStatus().isClientError());
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response
                .getStatus());
    }

    @SuppressWarnings("null")
    public void testGetCar() throws Exception {
        if (ONLY_OFFERS)
            return;
        String carNumber = "57";

        Response response = get(CarListResource.class, carNumber);
        Representation entity = response.getEntity();
        if (response.getStatus().isError())
            System.out.println(entity != null ? entity.getText()
                    : "[no entity]");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(CarResource.createTextRepr(carNumber), entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());

        if (true) // TODO wait for issue 435
            return;
        carNumber = "5%20%2B7";
        response = get(CarListResource.class, carNumber);
        entity = response.getEntity();
        if (response.getStatus().isError())
            System.out.println(entity != null ? entity.getText()
                    : "[no entity]");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(CarResource.createTextRepr(carNumber), entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }

    public void testGetHtmlText() throws Exception {
        if (ONLY_ONE_CAR || ONLY_OFFERS)
            return;
        Response response = accessServer(Method.GET, CarListResource.class,
                MediaType.TEXT_HTML);
        assertTrue(response.getStatus().isClientError());
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    @SuppressWarnings("null")
    public void testGetOffers() throws Exception {
        if (ONLY_ONE_CAR)
            return;
        Response response = accessServer(Method.GET, CarListResource.class,
                "offers", (MediaType) null);
        Representation representation = response.getEntity();
        if (response.getStatus().isError())
            System.out.println(representation != null ? representation
                    .getText() : "[no representation]");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(CarListResource.OFFERS, representation.getText());
        MediaType actualMediaType = representation.getMediaType();
        assertEqualMediaType(MediaType.TEXT_PLAIN, actualMediaType);
    }

    public void testGetPlainText() throws Exception {
        if (ONLY_ONE_CAR || ONLY_OFFERS)
            return;
        Response response = accessServer(Method.GET, CarListResource.class,
                MediaType.TEXT_PLAIN);
        Status status = response.getStatus();
        assertTrue("Status should be 2xx, but is " + status, status.isSuccess());
        Representation representation = response.getEntity();
        assertEquals(CarListResource.DUMMY_CAR_LIST, representation.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, representation
                .getMediaType());
    }

    public void testOptions() throws Exception {
        Response response = accessServer(Method.OPTIONS, CarListResource.class);
        assertAllowedMethod(response, Method.GET);

        response = accessServer(Method.OPTIONS, CarListResource.class, "offers");
        assertAllowedMethod(response, Method.GET, Method.POST);

        response = accessServer(Method.OPTIONS, CarListResource.class, "53");
        assertAllowedMethod(response, Method.GET);
    }
}