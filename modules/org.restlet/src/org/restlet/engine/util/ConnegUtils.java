/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.util;

import java.util.List;

import org.restlet.data.ClientInfo;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

/**
 * Content negotiation utilities.
 * 
 * @author Jerome Louvel
 */
public class ConnegUtils {

    /**
     * Returns the preferred metadata taking into account both metadata
     * supported by the server and client preferences.
     * 
     * @param supported
     *            The metadata supported by the server.
     * @param preferences
     *            The client preferences.
     * @return The preferred metadata.
     */
    public static <T extends Metadata> T getPreferredMetadata(
            List<T> supported, List<Preference<T>> preferences) {
        T result = null;
        float maxQuality = 0;

        if (supported != null) {
            for (Preference<T> pref : preferences) {
                for (T metadata : supported) {
                    if (pref.getMetadata().isCompatible(metadata)
                            && (pref.getQuality() > maxQuality)) {
                        result = metadata;
                        maxQuality = pref.getQuality();
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the best variant representation for a given resource according
     * the the client preferences.<br>
     * A default language is provided in case the variants don't match the
     * client preferences.
     * 
     * @param clientInfo
     *            The client preferences.
     * @param variants
     *            The list of variants to compare.
     * @param metadataService
     *            The metadata service.
     * @return The preferred variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public static Variant getPreferredVariant(ClientInfo clientInfo,
            List<? extends Variant> variants, MetadataService metadataService) {
        return new Conneg(clientInfo, metadataService)
                .getPreferredVariant(variants);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private ConnegUtils() {
    }
}
