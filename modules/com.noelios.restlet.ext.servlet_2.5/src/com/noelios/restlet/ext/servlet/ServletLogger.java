/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.servlet;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logger that wraps the logging methods of javax.servlet.ServletContext.
 * 
 * @author Jerome Louvel
 */
public class ServletLogger extends Logger {
    /** The Servlet context to use for logging. */
    private volatile javax.servlet.ServletContext context;

    /**
     * Constructor.
     * 
     * @param context
     *            The Servlet context to use.
     */
    public ServletLogger(javax.servlet.ServletContext context) {
        super(null, null);
        this.context = context;
    }

    /**
     * Returns the Servlet context to use for logging.
     * 
     * @return The Servlet context to use for logging.
     */
    private javax.servlet.ServletContext getContext() {
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
        getContext().log(record.getMessage(), record.getThrown());
    }

}
