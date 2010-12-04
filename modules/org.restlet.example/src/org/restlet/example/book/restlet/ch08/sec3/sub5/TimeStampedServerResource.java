package org.restlet.example.book.restlet.ch08.sec3.sub5;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TimeStampedServerResource extends ServerResource {

    @Get
    public Representation represent() {
        Representation result = new StringRepresentation("hello, world");
        // Modification date (Fri, 17 Apr 2009 10:10:10 GMT) unchanged.
        Calendar cal = new GregorianCalendar(2009, 3, 17, 10, 10, 10);
        result.setModificationDate(cal.getTime());
        return result;
    }

}
