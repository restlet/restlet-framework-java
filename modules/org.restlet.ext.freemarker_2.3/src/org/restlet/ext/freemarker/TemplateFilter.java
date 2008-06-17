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

/**
 * Filter response's entity and wrap it with a FreeMarker's template
 * representation.<br>
 * By default, the template representation provides a data model based on the
 * request and response objects.<br>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Thierry Boileau (contact@noelios.com)
 */
public class TemplateFilter extends Filter {

    /** The FreeMarker configuration. */
    private volatile Configuration configuration;

    /** The template's data model. */
    private volatile Object dataModel;

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

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param next
     *                The next Restlet.
     * @param dataModel
     *                The filter's data model.
     */
    public TemplateFilter(Context context, Restlet next, Object dataModel) {
        this(context, next);
        this.dataModel = dataModel;
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param next
     *                The next Restlet.
     * @param dataModel
     *                The filter's data model.
     */
    public TemplateFilter(Context context, Restlet next,
            Resolver<Object> dataModel) {
        this(context, next);
        this.dataModel = dataModel;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (response.isEntityAvailable()
                && response.getEntity().getEncodings().contains(
                        Encoding.FREEMARKER)) {
            TemplateRepresentation representation = new TemplateRepresentation(
                    response.getEntity(), configuration, response.getEntity()
                            .getMediaType());

            if (this.dataModel == null) {
                representation.setDataModel(request, response);
            } else {
                representation.setDataModel(dataModel);
            }

            response.setEntity(representation);
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
