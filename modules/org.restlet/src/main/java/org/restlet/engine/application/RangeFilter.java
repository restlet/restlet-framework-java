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

package org.restlet.engine.application;

import static org.restlet.data.Range.isBytesRange;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Range;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.restlet.service.RangeService;

// [excludes gwt]
/**
 * Filter that is in charge to check the responses to requests for partial
 * content.
 * 
 * @author Thierry Boileau
 */
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
        if (getRangeService().isEnabled()) {
            response.getServerInfo().setAcceptingRanges(true);

            if (request.getMethod().isSafe() && response.isEntityAvailable()) {
                Range responseRange = response.getEntity().getRange();
                boolean rangedEntity = responseRange != null && isBytesRange(responseRange);

                if (response.getStatus().isSuccess()) {
                    if (Status.SUCCESS_PARTIAL_CONTENT.equals(response.getStatus())) {
                        if (!rangedEntity) {
                            getLogger()
                                    .warning(
                                            "When returning a \"206 Partial content\" status, your response entity must be properly ranged.");
                        } else {
                            // We assume that the response entity has been
                            // properly ranged.
                        }
                    } else {
                        // At this time, list of ranges are not supported.
                        if (request.getRanges().size() == 1
                                && (!request.getConditions().hasSomeRange()
                                || request.getConditions().getRangeStatus(response.getEntity()).isSuccess())) {
                            Range requestedRange = request.getRanges().get(0);

                            if ((!response.getEntity().hasKnownSize())
                                    && ((requestedRange.getIndex() == Range.INDEX_LAST
                                    || requestedRange.getSize() == Range.SIZE_MAX)
                                    && !(requestedRange.getIndex() == Range.INDEX_LAST
                                    && requestedRange.getSize() == Range.SIZE_MAX))) {
                                // The end index cannot be properly computed
                                response.setStatus(Status.SERVER_ERROR_INTERNAL);
                                getLogger()
                                        .warning(
                                                "Unable to serve this range since at least the end index of the range cannot be computed.");
                                response.setEntity(null);
                            } else if (!requestedRange.equals(responseRange)) {
                                if (rangedEntity) {
                                    getLogger().info(
                                            "The range of the response entity is not equal to the requested one.");
                                }

                                if (response.getEntity().hasKnownSize()
                                        && requestedRange.getSize() > response.getEntity().getAvailableSize()) {
                                    requestedRange.setSize(Range.SIZE_MAX);
                                }

                                response.setEntity(new RangeRepresentation(response.getEntity(), requestedRange));
                                response.setStatus(Status.SUCCESS_PARTIAL_CONTENT);
                            }
                        } else if (request.getRanges().size() > 1) {
                            // Return a server error as this feature isn't supported yet
                            response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                            getLogger()
                                    .warning(
                                            "Multiple ranges are not supported at this time.");
                            response.setEntity(null);
                        }
                    }
                } else {
                    // Ignore error responses
                }
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
