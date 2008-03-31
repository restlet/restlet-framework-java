package org.restlet.example.book.restlet.ch10;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.SaxRepresentation;
import org.restlet.resource.Variant;
import org.restlet.util.XmlWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class SaxResource extends Resource {

    public SaxResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation rep = null;

            XmlWriter w = new XmlWriter();
            w.setDataFormat(true);
            w.setIndentStep(2);
            try {
                w.startDocument();
                w.startElement("Person");
                w.dataElement("name", "Jane Smith");
                w.dataElement("date-of-birth", "1965-05-23");
                w.dataElement("citizenship", "US");
                w.endElement("Person");
                w.endDocument();
                rep = new SaxRepresentation(MediaType.TEXT_XML);
            } catch (SAXException e) {
                e.printStackTrace();
            }

        return rep;
    }
}
