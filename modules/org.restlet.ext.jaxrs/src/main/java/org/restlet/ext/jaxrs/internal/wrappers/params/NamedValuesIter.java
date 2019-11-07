/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.jaxrs.internal.wrappers.params;

import java.util.Iterator;

import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;
import org.restlet.util.NamedValue;

/**
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
class NamedValuesIter implements Iterator<String> {

    private final Iterator<? extends NamedValue<String>> namedValuesIter;

    NamedValuesIter(Iterable<? extends NamedValue<String>> namedValues) {
        this.namedValuesIter = namedValues.iterator();
    }

    /** @see java.util.Iterator#hasNext() */
    public boolean hasNext() {
        return this.namedValuesIter.hasNext();
    }

    /** @see java.util.Iterator#next() */
    public String next() {
        return WrapperUtil.getValue(this.namedValuesIter.next());
    }

    /** @see java.util.Iterator#remove() */
    public void remove() {
        this.namedValuesIter.remove();
    }
}
