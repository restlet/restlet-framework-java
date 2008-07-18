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

package org.restlet.gwt.util;

import java.util.HashMap;
import java.util.Map;

import org.restlet.gwt.Context;
import org.restlet.gwt.Restlet;
import org.restlet.gwt.data.Form;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.data.Request;
import org.restlet.gwt.data.Response;

/**
 * Delegate used by API classes to get support from the implementation classes.
 * Note that this is an SPI class that is not intended for public usage.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Helper<T extends Restlet> {

    /**
     * The map of attributes exchanged between the API and the Engine via this
     * helper.
     */
    private final Map<String, Object> attributes;

    /**
     * The helped Restlet.
     */
    private volatile T helped;

    /**
     * Constructor.
     * 
     * @param helped
     *            The helped Restlet.
     */
    public Helper(T helped) {
        this.attributes = new HashMap<String, Object>();
        this.helped = helped;
    }

    /**
     * Creates a new context.
     * 
     * @param loggerName
     *            The JDK's logger name to use for contextual logging.
     * @return The new context.
     */
    public abstract Context createContext(String loggerName);

    /**
     * Returns the map of attributes exchanged between the API and the Engine
     * via this helper.
     * 
     * @return The map of attributes.
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    /**
     * Returns the helped Restlet context.
     * 
     * @return The helped Restlet context.
     */
    public Context getContext() {
        return getHelped().getContext();
    }

    /**
     * Returns the helped Restlet.
     * 
     * @return The helped Restlet.
     */
    public T getHelped() {
        return this.helped;
    }

    /**
     * Returns the helped Restlet parameters.
     * 
     * @return The helped Restlet parameters.
     */
    public Series<Parameter> getParameters() {
        return (getHelped() != null) ? getHelped().getContext().getParameters()
                : new Form();
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {

    }

    /**
     * Sets the helped Restlet.
     * 
     * @param helpedRestlet
     *            The helped Restlet.
     */
    public void setHelped(T helpedRestlet) {
        this.helped = helpedRestlet;
    }

    /** Start callback. */
    public abstract void start() throws Exception;

    /** Stop callback. */
    public abstract void stop() throws Exception;

    /**
     * Update callback with less impact than a {@link #stop()} followed by a
     * {@link #start()}.
     */
    public abstract void update() throws Exception;
}
