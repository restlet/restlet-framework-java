package com.noelios.restlet.test;

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
 * Simple resource that returns at least text/html and text/xml representations.
 */
public class UserAgentTestResource extends Resource {

    public UserAgentTestResource(Context context, Request request,
            Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        MediaType mediaType = variant.getMediaType();
        if (mediaType.equals(MediaType.TEXT_XML)) {
            return new StringRepresentation("<a>b</a>", mediaType);
        } else if (mediaType.equals(MediaType.TEXT_HTML)) {
            return new StringRepresentation("<html><body>a</body></html>",
                    mediaType);
        }

        return null;
    }
};
