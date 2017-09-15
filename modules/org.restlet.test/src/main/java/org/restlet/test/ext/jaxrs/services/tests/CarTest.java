/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
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

package org.restlet.test.ext.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.test.ext.jaxrs.services.car.CarListResource;
import org.restlet.test.ext.jaxrs.services.car.CarResource;
import org.restlet.test.ext.jaxrs.services.car.EngineResource;

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
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(CarListResource.class);
            }
        };
    }

    public void testDelete() throws Exception {
        final Response response = accessServer(Method.DELETE,
                CarListResource.class, null, null);
        assertTrue(
                "The status should be a client error, but was "
                        + response.getStatus(), response.getStatus()
                        .isClientError());
        assertEquals(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED,
                response.getStatus());
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
        assertTrue(
                "The status should be a client error, but was "
                        + response.getStatus(), response.getStatus()
                        .isClientError());
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
        assertEqualMediaType(MediaType.TEXT_PLAIN,
                representation.getMediaType());
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
