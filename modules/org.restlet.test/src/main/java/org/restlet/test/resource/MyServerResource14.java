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

package org.restlet.test.resource;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * Sample server resource for testing annotated PUT methods.
 * 
 * @author Jerome Louvel
 */
public class MyServerResource14 extends ServerResource {

    @Put
    public Representation store1(Representation rep) {
        return new StringRepresentation("*", MediaType.TEXT_PLAIN);
    }

    @Put("xml")
    public Representation store2(Representation rep) {
        return new StringRepresentation("xml", MediaType.APPLICATION_XML);
    }

    @Put("xml:json")
    public Representation store3(Representation rep) {
        return new StringRepresentation("xml:json", MediaType.APPLICATION_JSON);
    }

    @Put("json")
    public Representation store4(Representation rep) {
        return new StringRepresentation("json", MediaType.APPLICATION_JSON);
    }
}
