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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

/**
 * Diese Klasse wird von WidgetList.findWidget zurückgegeben, wenn ein bestimmes
 * Widtget angefragt wird. Diese Klasse handelt dann die Anfragen auf diesem
 * Widget. <br>
 * In der JSR-311-Spec wird diese Klasse wird als Sub-Resource bezeichnet.
 * 
 * @author Stephan Koops
 * @see CarListResource
 */
public class CarResource {
    private String id;

    /**
     * 
     * @param id
     */
    public CarResource(String id) {
        this.id = id;
    }

    /**
     * 
     * @return
     */
    @GET
    @ProduceMime("text/plain")
    public String getText() {
        return createTextRepr(id);
    }

    /**
     * Können Sub-Resourcen können auch weiter Sub-Resourcen zurückgeben?
     * 
     * @param id
     * @return
     */
    @Path("engine")
    public EngineResource findEngine() {
        return new EngineResource(this);
    }

    /**
     * 
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param carNumber
     * @return
     */
    public static String createTextRepr(Object carNumber) {
        return "This is the car with id " + carNumber + ".";
    }
}
