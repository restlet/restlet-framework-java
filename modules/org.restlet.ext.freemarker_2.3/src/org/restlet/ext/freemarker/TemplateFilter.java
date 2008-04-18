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

import freemarker.template.Configuration;

/**
 * Filter response's entity and wrap it with a FreeMarker's template
 * representation.
 * 
 * @author Thierry Boileau (contact@noelios.com)
 */
public class TemplateFilter extends Filter {

    /** The FreeMarker configuration. */
    private volatile Configuration config;

    /** The template's data model. */
    private volatile Object dataModel;

    /**
     * Constructor.
     * 
     * @param config
     *                The FreeMarker configuration.
     * @param dataModel
     *                The template's data model.
     */
    public TemplateFilter(Configuration config, Object dataModel) {
        super();
        this.config = config;
        this.dataModel = dataModel;
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param config
     *                The FreeMarker configuration.
     * @param dataModel
     *                The template's data model.
     */
    public TemplateFilter(Context context, Configuration config,
            Object dataModel) {
        super(context);
        this.config = config;
        this.dataModel = dataModel;
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param next
     *                The next Restlet.
     * @param config
     *                The FreeMarker configuration.
     * @param dataModel
     *                The template's data model.
     */
    public TemplateFilter(Context context, Restlet next, Configuration config,
            Object dataModel) {
        super(context, next);
        this.config = config;
        this.dataModel = dataModel;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (response.isEntityAvailable()
                && response.getEntity().getEncodings().contains(
                        Encoding.FREEMARKER)) {
            response.setEntity(new TemplateRepresentation(response.getEntity(),
                    config, dataModel, response.getEntity().getMediaType()));
        }
    }

    /**
     * Returns the FreeMarker configuration.
     * 
     * @return The FreeMarker configuration.
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * Returns the template's data model.
     * 
     * @return The template's data model.
     */
    public Object getDataModel() {
        return dataModel;
    }

    /**
     * Sets the FreeMarker configuration.
     * 
     * @param config
     *                FreeMarker configuration.
     * @return The FreeMarker configuration.
     */
    public void setConfig(Configuration config) {
        this.config = config;
    }

    /**
     * Sets the template's data model.
     * 
     * @param dataModel
     *                The template's data model.
     * @return The template's data model.
     */
    public void setDataModel(Object dataModel) {
        this.dataModel = dataModel;
    }
}
