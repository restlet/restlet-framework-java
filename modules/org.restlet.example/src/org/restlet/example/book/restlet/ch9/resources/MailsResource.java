package org.restlet.example.book.restlet.ch9.resources;

import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

public class MailsResource extends Resource {
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
}
