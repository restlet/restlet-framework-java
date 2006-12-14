/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.container;

import java.util.logging.Logger;

import org.restlet.Container;
import org.restlet.Context;
import org.restlet.Dispatcher;

import com.noelios.restlet.TemplateDispatcher;

/**
 * Context allowing access to the container's connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ContainerContext extends Context {
    /** The container helper. */
    private ContainerHelper containerHelper;

    /**
     * Constructor.
     * 
     * @param containerHelper
     *            The container helper.
     */
    public ContainerContext(ContainerHelper containerHelper) {
        this(containerHelper, Logger.getLogger(Container.class
                .getCanonicalName()));
    }

    /**
     * Constructor.
     * 
     * @param containerHelper
     *            The container helper.
     * @param logger
     *            The logger instance of use.
     */
    public ContainerContext(ContainerHelper containerHelper, Logger logger) {
        super(logger);
        this.containerHelper = containerHelper;
    }

    /**
     * Returns a call dispatcher.
     * 
     * @return A call dispatcher.
     */
    public Dispatcher getDispatcher() {
        return new TemplateDispatcher(getContainerHelper().getClientRouter());
    }

    /**
     * Returns the container helper.
     * 
     * @return The container helper.
     */
    protected ContainerHelper getContainerHelper() {
        return this.containerHelper;
    }

    /**
     * Sets the container helper.
     * 
     * @param containerHelper
     *            The container helper.
     */
    protected void setContainerHelper(ContainerHelper containerHelper) {
        this.containerHelper = containerHelper;
    }
}
