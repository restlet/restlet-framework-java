package org.restlet.example.book.restlet.ch05.sec2.sub3;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Resource corresponding to a mail received or sent with the parent mail
 * account.
 */
public class MailServerResource extends ServerResource {

    @Override
    protected Representation get() throws ResourceException {
        SaxRepresentation result;

        // Create a new DOM representation
        result = new SaxRepresentation() {

            public void write(org.restlet.ext.xml.XmlWriter writer)
                    throws IOException {
                try {
                    // Start document
                    writer.startDocument();

                    // Append the root node
                    writer.startElement("mail");

                    // Append the child nodes and set their text content
                    writer.startElement("status");
                    writer.characters("received");
                    writer.endElement("status");

                    writer.startElement("subject");
                    writer.characters("Message to self");
                    writer.endElement("subject");

                    writer.startElement("content");
                    writer.characters("Doh!");
                    writer.endElement("content");

                    writer.startElement("accountRef");
                    writer.characters(new Reference(getReference(), "..")
                            .getTargetRef().toString());
                    writer.endElement("accountRef");

                    // End the root node
                    writer.endElement("mail");

                    // End the document
                    writer.endDocument();
                } catch (SAXException e) {
                    throw new IOException(e.getMessage());
                }
            };
        };

        return result;
    }

    @Override
    protected Representation put(Representation representation)
            throws ResourceException {
        // Wraps the XML representation in a DOM representation
        DomRepresentation mailRep = new DomRepresentation(representation);

        // Parses and normalizes the DOM document
        Document doc;
        try {
            doc = mailRep.getDocument();

            Element mailElt = doc.getDocumentElement();
            Element statusElt = (Element) mailElt
                    .getElementsByTagName("status").item(0);
            Element subjectElt = (Element) mailElt.getElementsByTagName(
                    "subject").item(0);
            Element contentElt = (Element) mailElt.getElementsByTagName(
                    "content").item(0);
            Element accountRefElt = (Element) mailElt.getElementsByTagName(
                    "accountRef").item(0);

            // Output the XML element values
            System.out.println("Status: " + statusElt.getTextContent());
            System.out.println("Subject: " + subjectElt.getTextContent());
            System.out.println("Content: " + contentElt.getTextContent());
            System.out
                    .println("Account URI: " + accountRefElt.getTextContent());
        } catch (IOException e) {
            throw new ResourceException(e);
        }

        return null;
    }
}
