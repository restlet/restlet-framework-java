/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gae.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.restlet.gae.Context;
import org.restlet.gae.Restlet;
import org.restlet.gae.data.Form;
import org.restlet.gae.data.Parameter;
import org.restlet.gae.data.Request;
import org.restlet.gae.data.Response;
import org.restlet.gae.util.Series;

/**
 * Delegate used by API classes to get support from the implementation classes.
 * Note that this is an SPI class that is not intended for public usage.
 * 
 * @author Jerome Louvel
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
        this.attributes = new ConcurrentHashMap<String, Object>();
        this.helped = helped;
    }

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
     * Returns the helped Restlet logger.
     * 
     * @return The helped Restlet logger.
     */
    public Logger getLogger() {
        return (getHelped().getContext() != null) ? getHelped().getContext()
                .getLogger() : Context.getCurrentLogger();
    }

    /**
     * Returns the helped Restlet parameters.
     * 
     * @return The helped Restlet parameters.
     */
    public Series<Parameter> getHelpedParameters() {
        Series<Parameter> result = null;

        if ((getHelped() != null) && (getHelped().getContext() != null)) {
            result = getHelped().getContext().getParameters();
        } else {
            result = new Form();
        }

        return result;
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
        // Associate the response to the current thread
        Response.setCurrent(response);

        // Associate the context to the current thread
        if (getContext() != null) {
            Context.setCurrent(getContext());
        }
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
