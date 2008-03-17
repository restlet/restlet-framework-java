package org.restlet.example.book.restlet.ch9.resources;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource for a user's contact.
 * 
 */
public class ContactResource extends BaseResource {

    public ContactResource(Context context, Request request, Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    @Override
    public void removeRepresentations() throws ResourceException {
        // TODO Auto-generated method stub
        super.removeRepresentations();
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        // TODO Auto-generated method stub
        return super.represent(variant);
    }

    @Override
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        // TODO Auto-generated method stub
        super.storeRepresentation(entity);
    }

}
