/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.servlet.internal;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jakarta.servlet.ServletContext;

/**
 * Logger that wraps the logging methods of jakarta.servlet.ServletContext.
 * 
 * @author Jerome Louvel
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public class ServletLogger extends Logger {
    /** The Servlet context to use for logging. */
    private volatile ServletContext context;

    /**
     * Constructor.
     * 
     * @param context
     *            The Servlet context to use.
     */
    public ServletLogger(jakarta.servlet.ServletContext context) {
        super(null, null);
        this.context = context;
    }

    /**
     * Returns the Servlet context to use for logging.
     * 
     * @return The Servlet context to use for logging.
     */
    protected ServletContext getContext() {
        return this.context;
    }

    /**
     * Log a LogRecord.
     * 
     * @param record
     *            The LogRecord to be published
     */
    @Override
    public void log(LogRecord record) {
        if (record.getThrown() != null) {
            getContext().log(record.getMessage(), record.getThrown());
        } else {
            getContext().log(record.getMessage());
        }
    }

}
