/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
        final Response response = accessServer(Method.DELETE,
                CarListResource.class, null, null);
        assertTrue(response.getStatus().isClientError());
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED, response
                .getStatus());
    }

    /**
     * This tests, if a sub resource class of a sub resource class of a root
     * resource class is accessable.
     * 
     * @throws Exception
     */
    public void testEngine() throws Exception {
        final Response response = get("4711/engine");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals(EngineResource.getPlainRepr(4711), entity.getText());
    }

    public void testGetCar() throws Exception {
        final String carNumber = "57";

        final Response response = get(carNumber);
        final Representation entity = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(CarResource.createTextRepr(carNumber), entity.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
    }

    public void testGetHtmlText() throws Exception {
        final Response response = get(MediaType.TEXT_HTML);
        assertTrue(response.getStatus().isClientError());
        assertEquals(Status.CLIENT_ERROR_NOT_ACCEPTABLE, response.getStatus());
    }

    public void testGetOffers() throws Exception {
        final Response response = get("offers");
        final Representation representation = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals(CarListResource.OFFERS, representation.getText());
        final MediaType actualMediaType = representation.getMediaType();
        assertEqualMediaType(MediaType.TEXT_PLAIN, actualMediaType);
    }

    public void testGetPlainText() throws Exception {
        final Response response = get(MediaType.TEXT_PLAIN);
        final Status status = response.getStatus();
        assertTrue("Status should be 2xx, but is " + status, status.isSuccess());
        final Representation representation = response.getEntity();
        assertEquals(CarListResource.DUMMY_CAR_LIST, representation.getText());
        assertEqualMediaType(MediaType.TEXT_PLAIN, representation
                .getMediaType());
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