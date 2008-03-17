package org.restlet.example.book.restlet.ch9.resources;

import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

public class ContactResource extends Resource {
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
    public void storeRepresentation(Representation entity)
            throws ResourceException {
        // TODO Auto-generated method stub
        super.storeRepresentation(entity);
    }

}
