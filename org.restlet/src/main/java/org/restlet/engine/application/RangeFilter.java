/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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

/**
 * Filter that is in charge to check the responses to requests for partial content.
 *
 * @author Thierry Boileau
 */
public class RangeFilter extends Filter {

    /**
     * Constructor.
     *
     * @param context The parent context.
     */
    public RangeFilter(Context context) {
        super(context);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        response.getServerInfo().setAcceptingRanges(getRangeService().isEnabled());

        if (!getRangeService().isEnabled()) {
            return;
        }
        if (!request.getMethod().isSafe() || !response.isEntityAvailable()) {
            return;
        }
        if (!response.getStatus().isSuccess()) {
            return;
        }

        Range responseRange = response.getEntity().getRange();
        boolean rangedEntity = responseRange != null && isBytesRange(responseRange);

        if (Status.SUCCESS_PARTIAL_CONTENT.equals(response.getStatus())) {
            if (!rangedEntity) {
                getLogger()
                        .warning(
                                "When returning a \"206 Partial content\" status, your response entity must be properly ranged.");
            }
        } else if (request.getRanges().size() > 1) {
            multipleRangesNotSupported(response);
        } else if (request.getRanges().size() == 1
                && (!request.getConditions().hasSomeRange()
                        || request.getConditions()
                                .getRangeStatus(response.getEntity())
                                .isSuccess())) {
            Range requestedRange = request.getRanges().getFirst();

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
                    getLogger()
                            .info(
                                    "The range of the response entity is not equal to the requested one.");
                }

                if (response.getEntity().hasKnownSize()
                        && requestedRange.getSize() > response.getEntity().getAvailableSize()) {
                    requestedRange.setSize(Range.SIZE_MAX);
                }

                response.setEntity(new RangeRepresentation(response.getEntity(), requestedRange));
                response.setStatus(Status.SUCCESS_PARTIAL_CONTENT);
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

    private void multipleRangesNotSupported(final Response response) {
        // At this time, lists of ranges are not supported.
        // Return a server error as this feature isn't supported yet
        getLogger().warning("Multiple ranges are not supported at this time.");
        response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
        response.setEntity(null);
    }
}
