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

package com.noelios.restlet.component;

import java.util.logging.Logger;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Dispatcher;

import com.noelios.restlet.TemplateDispatcher;

/**
 * Context allowing access to the component's connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentContext extends Context {
    /** The component helper. */
    private ComponentHelper componentHelper;

    /**
     * Constructor.
     * 
     * @param componentHelper
     *            The component helper.
     */
    public ComponentContext(ComponentHelper componentHelper) {
        this(componentHelper, Logger.getLogger(Component.class
                .getCanonicalName()));
    }

    /**
     * Constructor.
     * 
     * @param componentHelper
     *            The component helper.
     * @param logger
     *            The logger instance of use.
     */
    public ComponentContext(ComponentHelper componentHelper, Logger logger) {
        super(logger);
        this.componentHelper = componentHelper;
    }

    /**
     * Returns a call dispatcher.
     * 
     * @return A call dispatcher.
     */
    public Dispatcher getDispatcher() {
        return new TemplateDispatcher(this, getComponentHelper().getClientRouter());
    }

    /**
     * Returns the component helper.
     * 
     * @return The component helper.
     */
    protected ComponentHelper getComponentHelper() {
        return this.componentHelper;
    }

    /**
     * Sets the component helper.
     * 
     * @param componentHelper
     *            The component helper.
     */
    protected void setComponentHelper(ComponentHelper componentHelper) {
        this.componentHelper = componentHelper;
    }
}
