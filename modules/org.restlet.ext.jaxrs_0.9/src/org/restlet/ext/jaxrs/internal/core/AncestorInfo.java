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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.restlet.ext.jaxrs.internal.util.EncodeOrCheck;

class AncestorInfo {

    /**
     * 
     */
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * an unmodifiable list with the resource objects.
     */
    private final List<Object> resources;

    /**
     * an unmodifiable list with the decoded uris, or null, if not yet
     * requested.
     */
    private volatile List<String> urisDecoded;

    /**
     * an unmodifiable list with the encoded uris.
     */
    private final List<String> urisEncoded;

    /**
     * @param urisEncoded
     * @param resources
     */
    public AncestorInfo(List<String> urisEncoded, List<Object> resources) {
        this.urisEncoded = Arrays.asList(urisEncoded
                .toArray(EMPTY_STRING_ARRAY));
        this.resources = Arrays.asList(resources.toArray());
    }

    /**
     * Returns an unmodifiable List of resource class objects.
     * 
     * @return an unmodifiable List of resource class objects.
     */
    List<Object> getResources() {
        return this.resources;
    }

    /**
     * Returns an unmodifiable List of relative URI parts.
     * 
     * @return an unmodifiable List of relative URI parts.
     */
    List<String> getUris(boolean decode) {
        if (decode) {
            if (this.urisDecoded == null) {
                List<String> urisDecoded;
                urisDecoded = new ArrayList<String>(this.urisEncoded.size());
                for (final String uriEncoded : this.urisEncoded) {
                    urisDecoded.add(EncodeOrCheck.all(uriEncoded, false));
                }
                this.urisDecoded = Collections.unmodifiableList(urisDecoded);
            }
            return this.urisDecoded;
        } else {
            return this.urisEncoded;
        }
    }
}