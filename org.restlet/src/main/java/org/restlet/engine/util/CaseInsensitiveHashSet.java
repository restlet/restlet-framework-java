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

package org.restlet.engine.util;

import java.util.Collection;
import java.util.HashSet;

/**
 * Set implementation that is case insensitive.
 */
public class CaseInsensitiveHashSet extends HashSet<String> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor initializing the set with the given collection.
     * 
     * @param source
     *            The source collection to use for initialization.
     */
    public CaseInsensitiveHashSet(Collection<? extends String> source) {
        super(source);
    }

    @Override
    public boolean add(String element) {
        return super.add(element.toLowerCase());
    }

    /**
     * Verify containment by ignoring case.
     */
    public boolean contains(String element) {
        return super.contains(element.toLowerCase());
    }

    @Override
    public boolean contains(Object o) {
        return contains(o.toString());
    }
}
