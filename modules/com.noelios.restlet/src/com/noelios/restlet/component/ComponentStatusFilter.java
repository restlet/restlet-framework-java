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

package com.noelios.restlet.component;

import org.restlet.Component;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import com.noelios.restlet.StatusFilter;

/**
 * Status filter that tries to obtain ouput representation from an application.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentStatusFilter extends StatusFilter {
    /** The component. */
    private volatile Component component;

    /**
     * Constructor.
     * 
     * @param component
     *            The component.
     */
    public ComponentStatusFilter(Component component) {
        super(component.getContext().createChildContext(), component
                .getStatusService().isOverwrite(), component.getStatusService()
                .getContactEmail(), "/");
        this.component = component;
    }

    /**
     * Returns the component.
     * 
     * @return The component.
     */
    public Component getComponent() {
        return this.component;
    }

    @Override
    public Representation getRepresentation(Status status, Request request,
            Response response) {
        Representation result = getComponent().getStatusService()
                .getRepresentation(status, request, response);
        if (result == null) {
            result = super.getRepresentation(status, request, response);
        }
        return result;
    }

    @Override
    public Status getStatus(Throwable throwable, Request request,
            Response response) {
        Status result = getComponent().getStatusService().getStatus(throwable,
                request, response);
        if (result == null) {
            result = super.getStatus(throwable, request, response);
        }
        return result;
    }
}
