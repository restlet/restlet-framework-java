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

package org.restlet.example.book.restlet.ch07.sec4.sub4;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.restlet.data.CacheDirective;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;

/**
 * Resource setting cache control information.
 */
public class CachingServerResource extends ServerResource {

    @Get
    public Representation represent() {
        // Modification date (Fri, 17 Apr 2012 10:10:10 GMT) unchanged.
        Calendar cal = new GregorianCalendar(2012, 4, 17, 10, 10, 10);
        Representation result = new StringRepresentation("<a href="
                + getReference() + ">" + System.currentTimeMillis() + "</a>");
        result.setMediaType(MediaType.TEXT_HTML);
        result.setModificationDate(cal.getTime());

        // Expiration date (Fri, 17 Apr 2012 13:10:10 GMT) unchanged.
        cal.roll(Calendar.HOUR, 3);
        result.setExpirationDate(cal.getTime());

        // Setting E-Tag
        result.setTag(new Tag("xyz123"));

        // Setting a cache directive
        getResponse().getCacheDirectives().add(CacheDirective.publicInfo());

        return result;
    }

    @Put
    public void store(Representation entity) {
        System.out.println("Storing a new entity.");
    }
}
