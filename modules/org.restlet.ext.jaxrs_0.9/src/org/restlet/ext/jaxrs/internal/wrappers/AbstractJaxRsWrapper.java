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
package org.restlet.ext.jaxrs.internal.wrappers;

import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;

/**
 * An abstract wrapper class. Contains some useful static methods.
 * 
 * @author Stephan Koops
 */
public abstract class AbstractJaxRsWrapper {

    private final PathRegExp pathRegExp;

    /**
     * Creates a new AbstractJaxRsWrapper without a path.
     */
    AbstractJaxRsWrapper() {
        this.pathRegExp = PathRegExp.EMPTY;
    }

    /**
     * Creates a new AbstractJaxRsWrapper with a given {@link PathRegExp}.
     * 
     * @param pathRegExp
     *            must not be null.
     */
    AbstractJaxRsWrapper(PathRegExp pathRegExp) throws ImplementationException {
        if (pathRegExp == null) {
            throw new ImplementationException("The PathRegExp must not be null");
        }
        this.pathRegExp = pathRegExp;
    }

    /**
     * @return Returns the regular expression for the URI template.
     */
    protected PathRegExp getPathRegExp() {
        return this.pathRegExp;
    }
}