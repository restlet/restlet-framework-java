package org.restlet.example.book.restlet.ch4;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

public class HelloWorldResource extends Resource {

    public HelloWorldResource(Context context, Request request,
            Response response) {
        super(context, request, response);
        // Declare all kind of representations supported by the resource
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = null;
        // Generate the right representation according to the variant.
        if (MediaType.TEXT_PLAIN.equals(variant.getMediaType())) {
            representation = new StringRepresentation("hello, world",
                    MediaType.TEXT_PLAIN);
        }
        return representation;
    }
}
