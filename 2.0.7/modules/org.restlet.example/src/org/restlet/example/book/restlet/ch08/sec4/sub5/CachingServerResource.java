package org.restlet.example.book.restlet.ch08.sec4.sub5;

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

public class CachingServerResource extends ServerResource {

    @Get
    public Representation represent() {
        // Modification date (Fri, 17 Apr 2009 10:10:10 GMT) unchanged.
        Calendar cal = new GregorianCalendar(2009, 3, 17, 10, 10, 10);
        Representation result = new StringRepresentation("<a href="
                + getReference() + ">" + System.currentTimeMillis() + "</a>");
        result.setMediaType(MediaType.TEXT_HTML);
        result.setModificationDate(cal.getTime());

        // Expiration date (Fri, 17 Apr 2019 10:10:10 GMT) unchanged.
        cal = new GregorianCalendar(2019, 3, 17, 10, 10, 10);
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
