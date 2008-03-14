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
package org.restlet.test.jaxrs.services.car;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author Stephan Koops
 * @see CarResource
 */
@Path(CarListResource.PATH)
public class CarListResource {

    public static final String DUMMY_CAR_LIST = "[1, 2, 5]";

    public static final String OFFERS = "This are test offers";

    public static final String PATH = "cars";

    public static final String OFFERS_PATH = "offers";

    /**
     * Konstruktoren von Root-Resourcen werden von der JAX-RS-Laufzeitumgebung
     * aufgerufen. Bei mind. 1 der vorhandenen Konstruktoren mussen alle
     * Parameter mit &#64;HttpContext, &#64;HeaderParam, &#64;MatrixParam,
     * &#64;QueryParam oder &#64;UriParam annotiert sein. Ein Konstruktor ohne
     * Parameter ist auch erlaubt.
     */
    public CarListResource() {
    }

    /**
     * <p>
     * Diese Methode ist ein <b>sub-resource-method</b>, weil sie mit einer
     * Http-Methoden annotiert ist. Sie behandelt die Anfrage selber.
     * </p>
     * <p>
     * Alle Parameter bis auf einen von Resource-Methods mussen &#64;{@link MatrixParam},
     * &#64;{@link QueryParam}, &#64;{@link UriParam}, &#64;{@link HttpContext}
     * oder &#64;{@link HeaderParam} annotiert sein. Ein ggf. nicht annotierte
     * Parameter bekommt die Entity ubergeben. Alle Parameterklassen auber &#64;{@link HttpContext}
     * mussen Strings verstehen konnen (Konstruktor oder static valueOf(String)
     * mit genau einem String-Parameter).<br>
     * Wenn &#64;HttpContext: Klasse muss {@link UriInfo}, PrecoditionEvaluator
     * (inzwischen umbenannt, zu Provider?) oder {@link HttpHeaders}
     * </p>
     * <p>
     * Ruckgabetypen:
     * <ul>
     * <li>void: leerer Entity-Body</li>
     * <li>instanceof {@link Response}</li>
     * verwendet. Dafur kann bspw. der {@link Response.Builder} verwendet werden</li>
     * <li>sonst: gemappt von ? (fruher EntityProvider, Abschnitt 3.1 der Spec)</li>
     * </ul>
     * </p>
     * 
     * @return
     * 
     * @throws WebApplicationException
     *                 Muss gefangen werden. Sie kann einen Request enthalten.
     */
    @GET
    @Path(OFFERS_PATH)
    @ProduceMime("text/plain")
    public String getOffers() throws WebApplicationException {
        return OFFERS;
    }

    @POST
    @Path(OFFERS_PATH)
    public Response newCar() {
        try {
            return Response.created(new URI("../5")).build();
        } catch (URISyntaxException e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * 
     * @return
     */
    @GET
    @ProduceMime("text/plain")
    public String getCarList() {
        // LATER use URIs in response entity.
        return DUMMY_CAR_LIST;
    }

    /**
     * Diese Methode ist ein <b>sub-resource-locator</b>, weil sie nicht mit
     * einer Http-Methoden annotiert ist. Anfragen werden von der
     * zuruckgegebenen Resource behandelt.
     * 
     * @param id
     * @return
     */
    @Path("{id}")
    @Encoded
    public CarResource findCar(@javax.ws.rs.PathParam("id") int id) {
        return new CarResource(id);
    }
}
