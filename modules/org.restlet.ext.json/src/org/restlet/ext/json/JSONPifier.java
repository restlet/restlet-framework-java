package com.adeo.wikeo.util;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

/**
 * COPY FROM https://bitbucket.org/markkharitonov/restlet-jsonp-filter/wiki/Home
 */
public class JSONPifier extends Filter {
    public JSONPifier(Context context) {
        super(context);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        // Check the presence of the callback parameter
        String[] callback = request.getResourceRef().getQueryAsForm().getValuesArray("callback");
        if (callback.length == 0) {
            return;
        }
        Representation entity = response.getEntity();
        if (entity != null && ("text".equals(entity.getMediaType().getMainType()) || MediaType.APPLICATION_JSON.equals(entity.getMediaType()))) {
            response.setEntity(new JSONPRepresentation(callback[0], response.getStatus(), response.getEntity()));
            response.setStatus(Status.SUCCESS_OK);
        }
    }

}