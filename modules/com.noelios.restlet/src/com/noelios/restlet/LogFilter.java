/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.service.LogService;
import org.restlet.util.Template;

import com.noelios.restlet.util.IdentClient;

/**
 * Filter logging all calls after their handling by the target Restlet. The
 * current format is similar to IIS 6 logs. The logging is based on the
 * java.util.logging package.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://www.restlet.org/documentation/1.1/tutorial#part07">Tutorial
 *      : Filters and call logging< /a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class LogFilter extends Filter {
    /** Obtain a suitable logger. */
    private volatile Logger logger;

    /** The log service. */
    protected volatile LogService logService;

    /** The log template to use. */
    protected volatile Template logTemplate;

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
            this.logger = Logger.getLogger(logService.getLoggerName());
            this.logTemplate = (logService.getLogFormat() == null) ? null
                    : new Template(getLogger(), logService.getLogFormat());
        }
    }

    /**
     * Allows filtering after processing by the next Restlet. Log the call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    protected void afterHandle(Request request, Response response) {
        final long startTime = (Long) request.getAttributes().get(
                "org.restlet.startTime");
        final int duration = (int) (System.currentTimeMillis() - startTime);

        // Format the call into a log entry
        if (this.logTemplate != null) {
            this.logger.log(Level.INFO, format(request, response));
        } else {
            this.logger.log(Level.INFO, formatDefault(request, response,
                    duration));
        }
    }

    /**
     * Allows filtering before processing by the next Restlet. Save the start
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

        return CONTINUE;
    }

    /**
     * Format a log entry.
     * 
     * @param request
     *            The request to log.
     * @param response
     *            The response to log.
     * @return The formatted log entry.
     */
    protected String format(Request request, Response response) {
        return this.logTemplate.format(request, response);
    }

    /**
     * Format a log entry using the default format.
     * 
     * @param request
     *            The request to log.
     * @param response
     *            The response to log.
     * @param duration
     *            The call duration (in milliseconds).
     * @return The formatted log entry.
     */
    protected String formatDefault(Request request, Response response,
            int duration) {
        final StringBuilder sb = new StringBuilder();
        final long currentTime = System.currentTimeMillis();

        // Append the date of the request
        sb.append(String.format("%tF", currentTime));
        sb.append('\t');

        // Append the time of the request
        sb.append(String.format("%tT", currentTime));
        sb.append('\t');

        // Append the client IP address
        final String clientAddress = request.getClientInfo().getAddress();
        sb.append((clientAddress == null) ? "-" : clientAddress);
        sb.append('\t');

        // Append the user name (via IDENT protocol)
        if (this.logService.isIdentityCheck()) {
            final IdentClient ic = new IdentClient(getLogger(), request
                    .getClientInfo().getAddress(), request.getClientInfo()
                    .getPort(), response.getServerInfo().getPort());
            sb.append((ic.getUserIdentifier() == null) ? "-" : ic
                    .getUserIdentifier());
        } else {
            sb.append('-');
        }
        sb.append('\t');

        // Append the server IP address
        final String serverAddress = response.getServerInfo().getAddress();
        sb.append((serverAddress == null) ? "-" : serverAddress);
        sb.append('\t');

        // Append the server port
        final Integer serverport = response.getServerInfo().getPort();
        sb.append((serverport == null) ? "-" : serverport.toString());
        sb.append('\t');

        // Append the method name
        final String methodName = (request.getMethod() == null) ? "-" : request
                .getMethod().getName();
        sb.append((methodName == null) ? "-" : methodName);

        // Append the resource path
        sb.append('\t');
        final String resourcePath = (request.getResourceRef() == null) ? "-"
                : request.getResourceRef().getPath();
        sb.append((resourcePath == null) ? "-" : resourcePath);

        // Append the resource query
        sb.append('\t');
        final String resourceQuery = (request.getResourceRef() == null) ? "-"
                : request.getResourceRef().getQuery();
        sb.append((resourceQuery == null) ? "-" : resourceQuery);

        // Append the status code
        sb.append('\t');
        sb.append((response.getStatus() == null) ? "-" : Integer
                .toString(response.getStatus().getCode()));

        // Append the returned size
        sb.append('\t');
        if (response.getEntity() == null) {
            sb.append('0');
        } else {
            sb.append((response.getEntity().getSize() == -1) ? "-" : Long
                    .toString(response.getEntity().getSize()));
        }

        // Append the received size
        sb.append('\t');
        if (request.getEntity() == null) {
            sb.append('0');
        } else {
            sb.append((request.getEntity().getSize() == -1) ? "-" : Long
                    .toString(request.getEntity().getSize()));
        }

        // Append the duration
        sb.append('\t');
        sb.append(duration);

        // Append the host reference
        sb.append('\t');
        sb.append((request.getHostRef() == null) ? "-" : request.getHostRef()
                .toString());

        // Append the agent name
        sb.append('\t');
        final String agentName = request.getClientInfo().getAgent();
        sb.append((agentName == null) ? "-" : agentName);

        // Append the referrer
        sb.append('\t');
        sb.append((request.getReferrerRef() == null) ? "-" : request
                .getReferrerRef().getIdentifier());

        return sb.toString();
    }

}