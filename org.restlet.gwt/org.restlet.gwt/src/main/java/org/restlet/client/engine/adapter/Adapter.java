/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine.adapter;

import java.util.logging.Logger;

import org.restlet.client.Context;

/**
 * Converter between high-level and low-level HTTP calls.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
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
