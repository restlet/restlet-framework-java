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

package org.restlet.ext.freemarker;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.ext.freemarker.internal.ResolverHashModel;
import org.restlet.routing.Filter;
import org.restlet.util.Resolver;

import freemarker.template.Configuration;
import freemarker.template.TemplateHashModel;

/**
 * Filter response's entity and wrap it with a FreeMarker's template
 * representation. By default, the template representation provides a data model
 * based on the request and response objects. In order for the wrapping to
 * happen, the representations must have the {@link Encoding#FREEMARKER}
 * encoding set.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Thierry Boileau
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
     *            The context.
     */
    public TemplateFilter(Context context) {
        super(context);
        this.configuration = new Configuration();
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param next
     *            The next Restlet.
     */
    public TemplateFilter(Context context, Restlet next) {
        super(context, next);
        this.configuration = new Configuration();
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param next
     *            The next Restlet.
     * @param dataModel
     *            The filter's data model.
     */
    public TemplateFilter(Context context, Restlet next, Object dataModel) {
        this(context, next);
        this.dataModel = dataModel;
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param next
     *            The next Restlet.
     * @param dataModel
     *            The filter's data model.
     */
    public TemplateFilter(Context context, Restlet next,
            Resolver<Object> dataModel) {
        this(context, next);
        this.dataModel = dataModel;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (response.isEntityAvailable()
                && response.getEntity().getEncodings()
                        .contains(Encoding.FREEMARKER)) {
            TemplateRepresentation representation = new TemplateRepresentation(
                    response.getEntity(), this.configuration, response
                            .getEntity().getMediaType());
            representation.setDataModel(createDataModel(request, response));
            response.setEntity(representation);
        }
    }

    /**
     * Creates the FreeMarker data model for a given call. By default, it will
     * create a {@link TemplateHashModel} based on the result of
     * {@link Resolver#createResolver(Request, Response)}. If the
     * {@link #getDataModel()} method has a non null value, it will be used.
     * 
     * @param request
     *            The handled request.
     * @param response
     *            The handled response.
     * @return The FreeMarker data model for the given call.
     */
    protected Object createDataModel(Request request, Response response) {
        Object result = null;

        if (this.dataModel == null) {
            result = new ResolverHashModel(Resolver.createResolver(request,
                    response));
        } else {
            result = this.dataModel;
        }

        return result;
    }

    /**
     * Returns the FreeMarker configuration.
     * 
     * @return The FreeMarker configuration.
     */
    public Configuration getConfiguration() {
        return this.configuration;
    }

    /**
     * Returns the template data model common to all calls. If each call should
     * have a specific model, you should set this property to null.
     * 
     * @return The template data model common to all calls.
     */
    public Object getDataModel() {
        return dataModel;
    }

    /**
     * Sets the FreeMarker configuration.
     * 
     * @param config
     *            FreeMarker configuration.
     */
    public void setConfiguration(Configuration config) {
        this.configuration = config;
    }

    /**
     * Sets the template data model common to all calls. If each call should
     * have a specific model, you should set this property to null.
     * 
     * @param dataModel
     *            The template data model common to all calls.
     */
    public void setDataModel(Object dataModel) {
        this.dataModel = dataModel;
    }

}
