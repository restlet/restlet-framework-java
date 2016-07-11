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
     * @param request
     *            The request including client preferences.
     * @param metadataService
     *            The metadata service used to get default metadata values.
     */
    public Conneg(Request request, MetadataService metadataService) {
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
     * Returns the best variant representation for a given resource according
     * the the client preferences.<br>
     * A default language is provided in case the variants don't match the
     * client preferences.
     * 
     * @param variants
     *            The list of variants to compare.
     * @return The preferred variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public Variant getPreferredVariant(List<? extends Variant> variants) {
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
     * @param variant
     *            The variant to score.
     * @return The enriched client preferences.
     */
    public abstract float scoreVariant(Variant variant);

}
