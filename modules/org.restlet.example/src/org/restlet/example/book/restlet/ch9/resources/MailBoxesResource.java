package org.restlet.example.book.restlet.ch9.resources;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a list of mailboxes.
 * 
 */
public class MailBoxesResource extends BaseResource {

    public MailBoxesResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public void acceptRepresentation(Representation entity)
            throws ResourceException {
        // TODO Auto-generated method stub
        super.acceptRepresentation(entity);
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        // TODO Auto-generated method stub
        return super.represent(variant);
    }

}
