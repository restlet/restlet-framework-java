package org.restlet.example.book.restlet.ch10;

import java.io.File;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.TransformRepresentation;
import org.restlet.resource.Variant;

public class TransformerResource extends Resource {

    public TransformerResource(Context context, Request request,
            Response response) {
        super(context, request, response);
        // This resource is able to generate two kinds of representations.
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final File dir = new File(
                "D:\\alaska\\forge\\build\\swc\\nre\\trunk\\books\\apress\\manuscript\\sample");
        // Get the source XML
        final Representation source = new FileRepresentation(new File(dir,
                "mail.xml"), MediaType.APPLICATION_XML);

        Representation transformSheet = null;
        // Get the XSLT stylesheet
        if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
            transformSheet = new FileRepresentation(new File(dir,
                    "mail_html.xsl"), MediaType.TEXT_XML);
        } else {
            transformSheet = new FileRepresentation(new File(dir,
                    "mail_text.xsl"), MediaType.TEXT_XML);
        }

        // Instantiates the representation with both source and stylesheet.
        final Representation representation = new TransformRepresentation(
                getContext(), source, transformSheet);
        // Set the right media-type
        representation.setMediaType(variant.getMediaType());

        return representation;
    }
}
