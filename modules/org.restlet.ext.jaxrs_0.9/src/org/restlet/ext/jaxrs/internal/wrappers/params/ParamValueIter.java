/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.ext.jaxrs.internal.wrappers.params;

import java.util.Iterator;

import org.restlet.data.Parameter;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;

class ParamValueIter implements Iterator<String> {

    private final Iterator<Parameter> paramIter;

    ParamValueIter(Iterable<Parameter> parameters) {
        this.paramIter = parameters.iterator();
    }

    /** @see java.util.Iterator#hasNext() */
    public boolean hasNext() {
        return this.paramIter.hasNext();
    }

    /** @see java.util.Iterator#next() */
    public String next() {
        return WrapperUtil.getValue(this.paramIter.next());
    }

    /** @see java.util.Iterator#remove() */
    public void remove() {
        this.paramIter.remove();
    }
}