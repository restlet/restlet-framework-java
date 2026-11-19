/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.application;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Range;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.restlet.service.RangeService;

import static org.restlet.data.Range.isBytesRange;
import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_RANGE_COMPLETE_LENGTH;

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
     * @param context The parent context.
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
                            getLogger().warning(
                                    "When returning a \"206 Partial content\" status, your response entity must be properly ranged.");
                        } else {
                            // We assume that the response entity has been properly ranged.
                        }
                    } else if (request.getRanges().size() > 1) { // At this time, lists of ranges are not supported.
                        // Return a server error as this feature isn't supported yet
                        response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                        getLogger().warning("Multiple ranges are not supported at this time.");
                        response.setEntity(null);
                    } else if (request.getRanges().size() == 1 &&
                            (!request.getConditions().hasSomeRange()
                                    || request.getConditions().getRangeStatus(response.getEntity()).isSuccess())) {
                        Range requestedRange = request.getRanges().get(0);
                        boolean knownSize = response.getEntity().hasKnownSize();

                        if (!rangedEntity
                                && knownSize
                                && isNotSatisfiable(requestedRange, response.getEntity().getSize())) {
                            rangeNotSatisfiable(response, response.getEntity().getSize());
                        } else if ((!knownSize)
                                && ((requestedRange.getIndex() == Range.INDEX_LAST
                                || requestedRange.getSize() == Range.SIZE_MAX)
                                && !(requestedRange.getIndex() == Range.INDEX_LAST
                                && requestedRange.getSize() == Range.SIZE_MAX))) {
                            // The end index cannot be properly computed
                            response.setStatus(Status.SERVER_ERROR_INTERNAL);
                            getLogger().warning(
                                    "Unable to serve this range since at least the end index of the range cannot be computed.");
                            response.setEntity(null);
                        } else if (!requestedRange.equals(responseRange)) {
                            if (rangedEntity) {
                                getLogger().info(
                                        "The range of the response entity is not equal to the requested one.");
                            }

                            Range servedRange = requestedRange;

                            if (knownSize
                                    && endsPastTheEnd(requestedRange, response.getEntity().getAvailableSize())) {
                                // A range ending past the end of the entity is cut at its end, and a suffix
                                // longer than the entity selects all of it (RFC 9110 section 14.1.2). The
                                // requested range is left unchanged.
                                servedRange = new Range(
                                        (requestedRange.getIndex() == Range.INDEX_LAST) ? Range.INDEX_FIRST
                                                : requestedRange.getIndex(),
                                        Range.SIZE_MAX, requestedRange.getInstanceSize(),
                                        requestedRange.getUnitName());
                            }

                            response.setEntity(new RangeRepresentation(response.getEntity(), servedRange));
                            response.setStatus(Status.SUCCESS_PARTIAL_CONTENT);
                        }
                    }
                } else {
                    // Ignore error responses
                }
            }
        }
    }

    /**
     * Indicates if a byte range selects nothing of an entity of the given length (RFC 9110 section
     * 14.1.1): its first position is at or past the end, or it is a suffix of zero bytes.
     */
    private static boolean isNotSatisfiable(Range range, long entitySize) {
        if (!isBytesRange(range)) {
            return false;
        }

        return (range.getIndex() >= Range.INDEX_FIRST)
                ? range.getIndex() >= entitySize
                : range.getSize() == 0;
    }

    /**
     * Indicates if a range of known size ends after the last byte of an entity of the given length.
     */
    private static boolean endsPastTheEnd(Range range, long entitySize) {
        if (range.getSize() == Range.SIZE_MAX) {
            return false;
        }

        return (range.getIndex() == Range.INDEX_LAST)
                ? range.getSize() > entitySize
                : range.getSize() > entitySize - range.getIndex();
    }

    /**
     * Answers a 416 (Range Not Satisfiable) response, whose Content-Range header gives the complete
     * length of the entity (RFC 9110 sections 14.2 and 14.4).
     */
    private void rangeNotSatisfiable(Response response, long completeLength) {
        response.getEntity().release();
        response.setEntity(null);
        response.setStatus(Status.CLIENT_ERROR_REQUESTED_RANGE_NOT_SATISFIABLE);
        response.getAttributes().put(ATTRIBUTE_RANGE_COMPLETE_LENGTH, completeLength);
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
