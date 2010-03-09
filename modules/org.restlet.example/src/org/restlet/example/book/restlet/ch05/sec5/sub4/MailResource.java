package org.restlet.example.book.restlet.ch05.sec5.sub4;

import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 *  
 */
public interface MailResource {

    @Get("xml")
    public XstreamRepresentation<Mail> toXml();

    @Get("json")
    public JacksonRepresentation<Mail> toJson();

    @Put("xml")
    public void fromXml(XstreamRepresentation<Mail> representation);

    @Put("json")
    public void fromJson(JacksonRepresentation<Mail> representation);

}
