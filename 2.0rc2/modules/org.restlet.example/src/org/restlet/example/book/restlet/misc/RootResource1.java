package org.restlet.example.book.restlet.misc;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

public class RootResource1 extends ServerResource {

    @Override
    protected void doInit() throws ResourceException {
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException {
        Representation result = null;

        if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            result = new StringRepresentation("<root/>", 
                    MediaType.TEXT_XML);
        } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            result = new StringRepresentation("[\"root\"]",                     
                    MediaType.APPLICATION_JSON);
        } else if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
            result = new StringRepresentation("<html><body>root</body></html>",
                    MediaType.TEXT_HTML);
        }

        return result;
    }

}
