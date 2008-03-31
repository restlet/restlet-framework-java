package org.restlet.example.book.restlet.ch10;

import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;

public class NonStandardMethodsResource extends Resource {

    /**
     * Does this resource handle "TEST" requests?
     * 
     * @return true.
     */
    public boolean allowTest() {
        return true;
    }

    /**
     * Handles "TEST" requests.
     */
    public void handleTest() {
        if (getRequest().isEntityAvailable()) {
            getResponse().setEntity(getRequest().getEntity());
            getResponse().setStatus(Status.SUCCESS_OK);
        } else {
            getResponse().setEntity(
                    new StringRepresentation("The entity was not available."));
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

}
