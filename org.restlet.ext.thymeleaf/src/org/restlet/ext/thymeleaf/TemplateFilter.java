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

package org.restlet.ext.thymeleaf;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.data.Status;
import org.restlet.routing.Filter;
import org.restlet.util.Resolver;

/**
 * Filters response's entity and wraps it with a Thymeleaf's template
 * representation. By default, the template representation provides a data model
 * based on the request and response objects. In order for the wrapping to
 * happen, the representations must have the {@link #THYMELEAF} encoding
 * set.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Grzegorz Godlewski
 */
public abstract class TemplateFilter extends Filter {

    private static final Encoding THYMELEAF = new Encoding("thymeleaf",
            "Thymeleaf templated representation");

    /** The template's data model as a map. */
    private volatile Map<String, Object> mapDataModel;

    /** The template's data model as a resolver. */
    private volatile Resolver<Object> resolverDataModel;

    /**
     * Constructor.
     */
    public TemplateFilter() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public TemplateFilter(Context context) {
        super(context);
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
        this.mapDataModel = null;
        this.resolverDataModel = null;
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
            Map<String, Object> dataModel) {
        super(context, next);
        this.mapDataModel = dataModel;
        this.resolverDataModel = null;
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
        super(context, next);
        this.mapDataModel = null;
        this.resolverDataModel = dataModel;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (response.isEntityAvailable()
                && response.getEntity().getEncodings().contains(THYMELEAF)) {
            try {
                final TemplateRepresentation representation = new TemplateRepresentation(
                        (TemplateRepresentation) response.getEntity(),
                        getLocale(), response.getEntity().getMediaType());

                if ((this.mapDataModel == null)
                        && (this.resolverDataModel == null)) {
                    representation.setDataModel(request, response);
                } else {
                    if (this.mapDataModel == null) {
                        representation.setDataModel(this.resolverDataModel);
                    } else {
                        representation.setDataModel(this.mapDataModel);
                    }
                }

                response.setEntity(representation);
            } catch (IOException e) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
            }
        }
    }

    /**
     * Overrides with {@link Locale} detection.
     * 
     * @return The default {@link Locale}.
     */
    public Locale getLocale() {
        return Locale.getDefault();
    }
}
