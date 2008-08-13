/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.application;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Range;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.service.RangeService;

public class RangeFilter extends Filter {

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     */
    public RangeFilter(Context context) {
        super(context);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (getRangeService().isEnabled() && response.isEntityAvailable()) {
            if (request.getRanges().size() == 1) {
                // At this time, list of ranges are not supported.
                Range requestedRange = request.getRanges().get(0);
                if (!requestedRange.equals(response.getEntity().getRange())) {
                    response.setEntity(new RangeRepresentation(response
                            .getEntity(), requestedRange));
                }
            } else if (request.getRanges().size() > 1) {
                // Return a server error as this isn't supported yet
                response
                        .setStatus(Status.CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE);
                response.setEntity(null);

            }
        }
    }

    /**
     * Returns the Range service of the parent application.
     * 
     * @return The Range service of the parent application.
     */
    public RangeService getRangeService() {
        return getApplication().getRangeService();
    }

}
