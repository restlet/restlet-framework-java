/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
                    // TODO EncodeOrCheck.all() is not the best solution, but
                    // works for now. Test it with %-encoding for "/"
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