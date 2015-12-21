/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.json;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;

/**
 * Filter that converts response entity of the JSON media type into a JSONP
 * callback document. Make sure that you properly pass a "callback" query
 * parameter in the URI query string with the name of your JavaScrip callback
 * method.
 * 
 * See {@link JsonpRepresentation} for the actual wrapper representation used
 * internally.
 * 
 * @author Mark Kharitonov
 */
public class JsonpFilter extends Filter {

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public JsonpFilter(Context context) {
        super(context);
    }

    /**
     * Assumes that there is a "callback" query parameter available in the URI
     * query string, containing the name of the JavaScript callback method.
     */
    @Override
    public void afterHandle(Request request, Response response) {
        // Check the presence of the callback parameter
        String callback = request.getResourceRef().getQueryAsForm()
                .getFirstValue("callback");

        if (callback != null) {
            Representation entity = response.getEntity();

            if (entity != null
                    && ("text".equals(entity.getMediaType().getMainType()) || MediaType.APPLICATION_JSON
                            .equals(entity.getMediaType()))) {
                response.setEntity(new JsonpRepresentation(callback, response
                        .getStatus(), response.getEntity()));
                response.setStatus(Status.SUCCESS_OK);
            }
        }
    }

}