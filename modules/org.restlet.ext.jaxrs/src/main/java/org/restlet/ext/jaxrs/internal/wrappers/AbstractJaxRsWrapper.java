/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jaxrs.internal.wrappers;

import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.util.PathRegExp;

/**
 * An abstract wrapper class. Contains some useful static methods.
 * 
 * @author Stephan Koops
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
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
