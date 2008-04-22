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

package org.restlet.ext.freemarker;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Resolver;

import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * Filter response's entity and wrap it with a FreeMarker's template
 * representation.<br>
 * The data model is based on the Resolver model.<br>
 * 
 * @see org.restlet.util.Resolver
 * 
 * @author Thierry Boileau (contact@noelios.com)
 */
public class TemplateFilter extends Filter {

    /**
     * Hash Model based on a Resolver instance.
     */
    private class ResolverHashModel implements TemplateHashModel {
        private Resolver<String> resolver;

        public ResolverHashModel(Resolver<String> resolver) {
            super();
            this.resolver = resolver;
        }

        /**
         * Return a scalar model based on the value returned by the resolver
         * according to the key.
         */
        public TemplateModel get(String key) throws TemplateModelException {
            return new ScalarModel(resolver.resolve(key));
        }

        public boolean isEmpty() throws TemplateModelException {
            return false;
        }
    }

    /**
     * Data model that gives access to a String value.
     * 
     */
    private class ScalarModel implements TemplateScalarModel {
        private String value;

        public ScalarModel(String value) {
            super();
            this.value = value;
        }

        public String getAsString() throws TemplateModelException {
            return value;
        }
    }

    /** The FreeMarker configuration. */
    private volatile Configuration configuration;

    /**
     * Constructor.
     */
    public TemplateFilter() {
        super();
        this.configuration = new Configuration();
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     */
    public TemplateFilter(Context context) {
        super(context);
        this.configuration = new Configuration();
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param next
     *                The next Restlet.
     */
    public TemplateFilter(Context context, Restlet next) {
        super(context, next);
        this.configuration = new Configuration();
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (response.isEntityAvailable()
                && response.getEntity().getEncodings().contains(
                        Encoding.FREEMARKER)) {
            response.setEntity(new TemplateRepresentation(response.getEntity(),
                    configuration, new ResolverHashModel(Resolver
                            .createResolver(request, response)), response
                            .getEntity().getMediaType()));
        }
    }

    /**
     * Returns the FreeMarker configuration.
     * 
     * @return The FreeMarker configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the FreeMarker configuration.
     * 
     * @param config
     *                FreeMarker configuration.
     */
    public void setConfiguration(Configuration config) {
        this.configuration = config;
    }
}
