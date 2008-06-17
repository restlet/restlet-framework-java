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

package org.restlet.ext.velocity;

import java.io.IOException;
import java.util.Map;

import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Encoding;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Resolver;

/**
 * Filter response's entity and wrap it with a Velocity's template
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
     *                The context.
     */
    public TemplateFilter(Context context) {
        super(context);
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
        this.mapDataModel = null;
        this.resolverDataModel = null;
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
            Map<String, Object> dataModel) {
        super(context, next);
        this.mapDataModel = dataModel;
        this.resolverDataModel = null;
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
        super(context, next);
        this.mapDataModel = null;
        this.resolverDataModel = dataModel;
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        if (response.isEntityAvailable()
                && response.getEntity().getEncodings().contains(
                        Encoding.VELOCITY)) {
            try {
                TemplateRepresentation representation = new TemplateRepresentation(
                        response.getEntity(), response.getEntity()
                                .getMediaType());

                if (this.mapDataModel == null && this.resolverDataModel == null) {
                    representation.setDataModel(request, response);
                } else {
                    if (this.mapDataModel == null) {
                        representation.setDataModel(resolverDataModel);
                    } else {
                        representation.setDataModel(mapDataModel);
                    }
                }

                response.setEntity(representation);
            } catch (ResourceNotFoundException e) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, e);
            } catch (ParseErrorException e) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
            } catch (IOException e) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
            }
        }
    }
}
