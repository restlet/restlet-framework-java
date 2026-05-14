/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
     * @param context The context to use.
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
        Logger result = (getContext() != null) ? getContext().getLogger() : null;
        return (result != null) ? result : Context.getCurrentLogger();
    }
}
