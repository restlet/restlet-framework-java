package org.restlet.example.book.restlet.ch05.sec5.sub4;

import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 * Annotated mail resource interface
 */
public interface MailResource {

    @Get
    public Mail retrieve();

    @Put
    public void store(Mail mail);

}
