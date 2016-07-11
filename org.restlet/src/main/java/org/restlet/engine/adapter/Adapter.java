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

package org.restlet.engine.adapter;

import java.util.logging.Logger;

import org.restlet.Context;

/**
 * Converter between high-level and low-level HTTP calls.
 * 
 * @author Jerome Louvel
 */
public class Adapter {

    /** The context. */
    private volatile Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The context to use.
     */
    public Adapter(Context context) {
        this.context = context;
    }

    /**
     * Returns the context.
     * 
     * @return The context.
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        Logger result = (getContext() != null) ? getContext().getLogger()
                : null;
        return (result != null) ? result : Context.getCurrentLogger();
    }

}
