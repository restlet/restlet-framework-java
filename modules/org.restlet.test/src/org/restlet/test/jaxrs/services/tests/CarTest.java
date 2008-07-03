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

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.test.jaxrs.services.car.CarListResource;
import org.restlet.test.jaxrs.services.car.CarResource;
import org.restlet.test.jaxrs.services.car.EngineResource;

/**
 * @author Stephan Koops
 * @see CarListResource
 * @see CarResource
 * @see EngineResource
 */
public class CarTest extends JaxRsTestCase {

    public static void main(String[] args) throws Exception {
        new CarTest().runServerUntilKeyPressed();
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return CarListResource.class;
    }

    public void testDelete() throws Exception {
        Response response = accessServer(Method.DELETE, CarListResource.class,
                null, null);
        assertTrue(response.getStatus().isClientError());
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response
                .getStatus());
    }

    public void testGetCar() throws Exception {
        String carNumber = "57";

        Response response = get(carNumber);
        Representation entity = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(CarResource.createTextRepr(carNumber), entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }

    public void testGetHtmlText() throws Exception {
        Response response = get(MediaType.TEXT_HTML);
        assertTrue(response.getStatus().isClientError());
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    public void testGetOffers() throws Exception {
        Response response = get("offers");
        Representation representation = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(CarListResource.OFFERS, representation.getText());
        MediaType actualMediaType = representation.getMediaType();
        assertEqualMediaType(MediaType.TEXT_PLAIN, actualMediaType);
    }

    public void testGetPlainText() throws Exception {
        Response response = get(MediaType.TEXT_PLAIN);
        Status status = response.getStatus();
        assertTrue("Status should be 2xx, but is " + status, status.isSuccess());
        Representation representation = response.getEntity();
        assertEquals(CarListResource.DUMMY_CAR_LIST, representation.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, representation
                .getMediaType());
    }

    /**
     * This tests, if a sub resource class of a sub resource class of a root
     * resource class is accessable.
     * 
     * @throws Exception
     */
    public void testEngine() throws Exception {
        Response response = get("4711/engine");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        Representation entity = response.getEntity();
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals(EngineResource.getPlainRepr(4711), entity.getText());
    }

    public void testOptions() throws Exception {
        Response response = options();
        assertAllowedMethod(response, Method.GET, Method.POST);

        response = options("offers");
        assertAllowedMethod(response, Method.GET, Method.POST);

        response = options("53");
        assertAllowedMethod(response, Method.GET);
    }
}