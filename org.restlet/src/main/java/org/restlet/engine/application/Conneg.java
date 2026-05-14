/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import java.util.List;
import org.restlet.Request;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

/**
 * Content negotiation algorithm.
 *
 * @author Jerome Louvel
 */
public abstract class Conneg {

    /** The request including client preferences. */
    private final Request request;

    /**
     * Constructor.
     *
     * @param request The request including client preferences.
     * @param metadataService The metadata service used to get default metadata values.
     */
    protected Conneg(Request request, MetadataService metadataService) {
        this.request = request;
    }

    /**
     * Returns the request including client preferences.
     *
     * @return The request including client preferences.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Returns the best variant representation for a given resource according the client
     * preferences.<br>
     * A default language is provided in case the variants don't match the client preferences.
     *
     * @param variants The list of variants to compare.
     * @return The preferred variant.
     * @see <a href= "http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *     content negotiation algorithm</a>
     */
    public Variant getPreferredVariant(final List<? extends Variant> variants) {
        Variant result = null;

        if ((variants != null) && !variants.isEmpty()) {
            float bestScore = -1.0F;
            float current;

            // Compute the score of each variant
            for (Variant variant : variants) {
                current = scoreVariant(variant);

                if (current > bestScore) {
                    bestScore = current;
                    result = variant;
                }
            }
        }

        return result;
    }

    /**
     * Scores a variant relatively to enriched client preferences.
     *
     * @param variant The variant to score.
     * @return The enriched client preferences.
     */
    public abstract float scoreVariant(Variant variant);
}
