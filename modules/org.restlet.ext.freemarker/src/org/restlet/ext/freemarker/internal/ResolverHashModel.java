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

package org.restlet.ext.freemarker.internal;

import org.restlet.util.Resolver;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Template Hash Model based on a Resolver instance.
 * 
 * @author Jerome Louvel
 */
public class ResolverHashModel implements TemplateHashModel {
    /** The inner resolver instance. */
    private final Resolver<? extends Object> resolver;

    /**
     * Constructor.
     * 
     * @param resolver
     *            The inner resolver.
     */
    public ResolverHashModel(Resolver<? extends Object> resolver) {
        super();
        this.resolver = resolver;
    }

    /**
     * Returns a scalar model based on the value returned by the resolver
     * according to the key.
     */
    public TemplateModel get(String key) throws TemplateModelException {
        Object value = this.resolver.resolve(key);
        if (value == null) {
            return null;
        } else if (value instanceof TemplateModel) {
            return (TemplateModel) value;
        }

        return new ScalarModel(value);
    }

    /**
     * Returns false.
     * 
     * @Return False.
     */
    public boolean isEmpty() throws TemplateModelException {
        return false;
    }
}