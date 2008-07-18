package org.restlet.example.book.restlet.ch10;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DomResource extends Resource {

    public DomResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation rep = null;

        try {
            final Document d = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
            final Element r = d.createElement("mail");
            d.appendChild(r);
            final Element subject = d.createElement("subject");
            subject.appendChild(d
                    .createTextNode("This is the topic of the mail."));
            r.appendChild(subject);
            final Element from = d.createElement("from");
            from.appendChild(d.createTextNode("Me"));
            r.appendChild(from);
            d.normalizeDocument();
            rep = new DomRepresentation(MediaType.TEXT_XML, d);
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }

        return rep;
    }
}
