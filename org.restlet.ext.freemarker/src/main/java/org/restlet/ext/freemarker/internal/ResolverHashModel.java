/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.freemarker.internal;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.restlet.util.Resolver;

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
     * @param resolver The inner resolver.
     */
    public ResolverHashModel(Resolver<? extends Object> resolver) {
        super();
        this.resolver = resolver;
    }

    /** Returns a scalar model based on the value returned by the resolver according to the key. */
    public TemplateModel get(String key) throws TemplateModelException {
        Object value = this.resolver.resolve(key);
        if (value == null) {
            return null;
        } else if (value instanceof TemplateModel templateModel) {
            return templateModel;
        }

        return new ScalarModel(value);
    }

    /** Returns false. @Return False. */
    public boolean isEmpty() throws TemplateModelException {
        return false;
    }
}
