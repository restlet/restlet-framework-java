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

package org.restlet.service;

import java.util.List;

import org.restlet.Request;
import org.restlet.engine.application.Conneg;
import org.restlet.engine.application.FlexibleConneg;
import org.restlet.engine.application.StrictConneg;
import org.restlet.representation.Variant;

/**
 * Application service negotiating the preferred resource variants. This service
 * is leveraged by server-side and client-side content negotiation, annotated
 * method dispatching, and so on.
 * 
 * @author Jerome Louvel
 */
public class ConnegService extends Service {

    /**
     * Indicates if the conneg algorithm should strictly respect client
     * preferences or be more flexible.
     */
    private volatile boolean strict;

    /**
     * Constructor.
     */
    public ConnegService() {
        this(true);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public ConnegService(boolean enabled) {
        super(enabled);
        this.strict = false;
    }

    /**
     * Returns the best variant representation for a given resource according
     * the the client preferences.<br>
     * A default language is provided in case the variants don't match the
     * client preferences.
     * 
     * @param variants
     *            The list of variants to compare.
     * @param request
     *            The request including client preferences.
     * @param metadataService
     *            The metadata service used to get default metadata values.
     * @return The preferred variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public Variant getPreferredVariant(List<? extends Variant> variants,
            Request request, MetadataService metadataService) {
        Conneg conneg = isStrict() ? new StrictConneg(request, metadataService)
                : new FlexibleConneg(request, metadataService);
        return conneg.getPreferredVariant(variants);
    }

    /**
     * Indicates if the conneg algorithm should strictly respect client
     * preferences or be more flexible. Value is false by default.
     * 
     * @return True if the conneg algorithm should strictly respect client
     *         preferences.
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * Indicates if the conneg algorithm should strictly respect client
     * preferences or be more flexible.
     * 
     * @param strict
     *            True if the conneg algorithm should strictly respect client
     *            preferences.
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

}
