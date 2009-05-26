package org.restlet.example.firstSteps;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Resource which has only one representation.
 * 
 */
public class HelloWorldResource extends Resource {

    public HelloWorldResource(Context context, Request request,
            Response response) {
        super(context, request, response);

        // This representation has only one type of representation.
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = new StringRepresentation(
                "hello, world", MediaType.TEXT_PLAIN);
        return representation;
    }
}
