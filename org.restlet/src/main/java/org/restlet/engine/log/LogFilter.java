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

package org.restlet.engine.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.Engine;
import org.restlet.routing.Filter;
import org.restlet.service.LogService;

/**
 * Filter logging all calls after their handling by the target Restlet. The
 * current format is similar to IIS 6 logs. The logging is based on the
 * java.util.logging package.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class LogFilter extends Filter {
    /** The log service. */
    protected volatile LogService logService;

    /** The log service logger. */
    private volatile Logger logLogger;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param logService
     *            The log service descriptor.
     */
    public LogFilter(Context context, LogService logService) {
        super(context);
        this.logService = logService;

        if (logService != null) {
            if (logService.getLoggerName() != null) {
                this.logLogger = Engine.getLogger(logService.getLoggerName());
            } else if ((context != null)
                    && (context.getLogger().getParent() != null)) {
                this.logLogger = Engine.getLogger(context.getLogger()
                        .getParent().getName()
                        + "."
                        + LogUtils.getBestClassName(logService.getClass()));
            } else {
                this.logLogger = Engine.getLogger(LogUtils
                        .getBestClassName(logService.getClass()));
            }
        }
    }

    /**
     * Allows filtering after processing by the next Restlet. Logs the call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    protected void afterHandle(Request request, Response response) {
        try {
            if (request.isLoggable() && this.logLogger.isLoggable(Level.INFO)) {
                long startTime = (Long) request.getAttributes().get(
                        "org.restlet.startTime");
                int duration = (int) (System.currentTimeMillis() - startTime);
                this.logLogger.log(Level.INFO, this.logService
                        .getResponseLogMessage(response, duration));
            }
        } catch (Throwable e) {
            // Error while logging the call, cf issue #931
            getLogger().log(Level.SEVERE, "Cannot log call", e);
        }
    }

    /**
     * Allows filtering before processing by the next Restlet. Saves the start
     * time.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The continuation status.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        request.getAttributes().put("org.restlet.startTime",
                System.currentTimeMillis());

        // Set the log level for the given request
        request.setLoggable(this.logService.isLoggable(request));

        if (request.isLoggable() && this.logLogger.isLoggable(Level.FINE)) {
            this.logLogger.fine("Processing request to: \""
                    + ((request.getResourceRef() == null) ? "Unknown URI"
                            : request.getResourceRef().getTargetRef()
                                    .toString()) + "\"");
        }

        return CONTINUE;
    }

}
