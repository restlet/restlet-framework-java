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
 * @see <a
 *      href="http://www.restlet.org/documentation/1.1/tutorial#part07">Tutorial:
 *      Filters and call logging</a>
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
     *                The context.
     * @param logService
     *                The log service descriptor.
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
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    @Override
    protected void afterHandle(Request request, Response response) {
        long startTime = (Long) request.getAttributes().get(
                "org.restlet.startTime");
        int duration = (int) (System.currentTimeMillis() - startTime);

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
     *                The request to handle.
     * @param response
     *                The response to update.
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
     *                The request to log.
     * @param response
     *                The response to log.
     * @return The formatted log entry.
     */
    protected String format(Request request, Response response) {
        return this.logTemplate.format(request, response);
    }

    /**
     * Format a log entry using the default format.
     * 
     * @param request
     *                The request to log.
     * @param response
     *                The response to log.
     * @param duration
     *                The call duration (in milliseconds).
     * @return The formatted log entry.
     */
    protected String formatDefault(Request request, Response response,
            int duration) {
        StringBuilder sb = new StringBuilder();
        long currentTime = System.currentTimeMillis();

        // Append the date of the request
        sb.append(String.format("%tF", currentTime));
        sb.append('\t');

        // Append the time of the request
        sb.append(String.format("%tT", currentTime));
        sb.append('\t');

        // Append the client IP address
        String clientAddress = request.getClientInfo().getAddress();
        sb.append((clientAddress == null) ? "-" : clientAddress);
        sb.append('\t');

        // Append the user name (via IDENT protocol)
        if (this.logService.isIdentityCheck()) {
            IdentClient ic = new IdentClient(getLogger(), request
                    .getClientInfo().getAddress(), request.getClientInfo()
                    .getPort(), response.getServerInfo().getPort());
            sb.append((ic.getUserIdentifier() == null) ? "-" : ic
                    .getUserIdentifier());
        } else {
            sb.append('-');
        }
        sb.append('\t');

        // Append the server IP address
        String serverAddress = response.getServerInfo().getAddress();
        sb.append((serverAddress == null) ? "-" : serverAddress);
        sb.append('\t');

        // Append the server port
        Integer serverport = response.getServerInfo().getPort();
        sb.append((serverport == null) ? "-" : serverport.toString());
        sb.append('\t');

        // Append the method name
        String methodName = (request.getMethod() == null) ? "-" : request
                .getMethod().getName();
        sb.append((methodName == null) ? "-" : methodName);

        // Append the resource path
        sb.append('\t');
        String resourcePath = (request.getResourceRef() == null) ? "-"
                : request.getResourceRef().getPath();
        sb.append((resourcePath == null) ? "-" : resourcePath);

        // Append the resource query
        sb.append('\t');
        String resourceQuery = (request.getResourceRef() == null) ? "-"
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
        String agentName = request.getClientInfo().getAgent();
        sb.append((agentName == null) ? "-" : agentName);

        // Append the referrer
        sb.append('\t');
        sb.append((request.getReferrerRef() == null) ? "-" : request
                .getReferrerRef().getIdentifier());

        return sb.toString();
    }

}