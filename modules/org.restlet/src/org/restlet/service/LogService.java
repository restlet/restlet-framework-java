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

package org.restlet.service;

import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.log.LogFilter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Template;

/**
 * Service providing access logging service. The implementation is fully based
 * on the standard logging mechanism introduced in JDK 1.4.<br>
 * <br>
 * The default access log format follows the <a
 * href="http://www.w3.org/TR/WD-logfile.html"> W3C Extended Log File Format</a>
 * with the following fields used: <br>
 * <ol>
 * <li>Date (YYYY-MM-DD)</li>
 * <li>Time (HH:MM:SS)</li>
 * <li>Client address (IP)</li>
 * <li>Remote user identifier (see RFC 1413)</li>
 * <li>Server address (IP)</li>
 * <li>Server port</li>
 * <li>Method (GET|POST|...)</li>
 * <li>Resource reference path (including the leading slash)</li>
 * <li>Resource reference query (excluding the leading question mark)</li>
 * <li>Response status code</li>
 * <li>Number of bytes sent</li>
 * <li>Number of bytes received</li>
 * <li>Time to serve the request (in milliseconds)</li>
 * <li>Host reference</li>
 * <li>Client agent name</li>
 * <li>Referrer reference</li>
 * </ol>
 * <br>
 * If you use <a href="http://www.analog.cx">Analog</a> to generate your log
 * reports, and if you use the default log format, then you can simply specify
 * this string as a value of the LOGFORMAT command:
 * (%Y-%m-%d\t%h:%n:%j\t%S\t%u\t%j\t%j\t%j\t%r\t%q\t%c\t%b\t%j\t%T\t%v\t%B\t%f)<br>
 * <br>
 * For custom access log format, see the syntax to use and the list of available
 * variable names in {@link org.restlet.routing.Template}. <br>
 * 
 * @see <a href="http://wiki.restlet.org/docs_2.2/201-restlet.html">User Guide -
 *      Access logging</a>
 * @see <a
 *      href="http://download.oracle.com/javase/1.5.0/docs/api/java/util/logging/package-summary.html">java.util.logging</a>
 * @author Jerome Louvel
 */
public class LogService extends Service {

    /** Indicates if the debugging mode is enabled. */
    private volatile boolean debugging;

    /** Indicates if the identity check (as specified by RFC1413) is enabled. */
    private volatile boolean identityCheck;

    /** The URI template of loggable resource references. */
    private volatile Template loggableTemplate;

    /** The access logger name. */
    private volatile String loggerName;

    /** The URI reference of the log properties. */
    private volatile Reference logPropertiesRef;

    /** The response log entry format. */
    private volatile String responseLogFormat;

    /** The response log template to use. */
    protected volatile Template responseLogTemplate;

    /**
     * Constructor.
     */
    public LogService() {
        this(true);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public LogService(boolean enabled) {
        super(enabled);
        this.loggableTemplate = null;
        this.loggerName = null;
        this.responseLogFormat = null;
        this.logPropertiesRef = null;
        this.identityCheck = false;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new LogFilter(context, this);
    }

    /**
     * Format a log entry using the default IIS log format.
     * 
     * @param response
     *            The response to log.
     * @param duration
     *            The call duration (in milliseconds).
     * @return The formatted log entry.
     */
    protected String getDefaultResponseLogMessage(Response response,
            int duration) {
        StringBuilder sb = new StringBuilder();
        Request request = response.getRequest();
        long currentTime = System.currentTimeMillis();

        // Append the date of the request
        sb.append(String.format("%tF", currentTime));
        sb.append('\t');

        // Append the time of the request
        sb.append(String.format("%tT", currentTime));
        sb.append('\t');

        // Append the client IP address
        String clientAddress = request.getClientInfo().getUpstreamAddress();
        sb.append((clientAddress == null) ? "-" : clientAddress);
        sb.append('\t');

        // Append the user name (via IDENT protocol)
        if (isIdentityCheck()) {
            // [ifndef gae]
            org.restlet.engine.log.IdentClient ic = new org.restlet.engine.log.IdentClient(
                    request.getClientInfo().getUpstreamAddress(), request
                            .getClientInfo().getPort(), response
                            .getServerInfo().getPort());
            sb.append((ic.getUserIdentifier() == null) ? "-" : ic
                    .getUserIdentifier());
        } else if ((request.getChallengeResponse() != null)
                && (request.getChallengeResponse().getIdentifier() != null)) {
            sb.append(request.getChallengeResponse().getIdentifier());
        } else {
            // [enddef]
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

        if (!response.isEntityAvailable()
                || Status.REDIRECTION_NOT_MODIFIED.equals(response.getStatus())
                || Status.SUCCESS_NO_CONTENT.equals(response.getStatus())
                || Method.HEAD.equals(request.getMethod())) {
            sb.append('0');
        } else {
            sb.append((response.getEntity().getSize() == -1) ? "-" : Long
                    .toString(response.getEntity().getSize()));
        }

        // Append the received size
        sb.append('\t');

        try {
            if (request.getEntity() == null) {
                sb.append('0');
            } else {
                sb.append((request.getEntity().getSize() == -1) ? "-" : Long
                        .toString(request.getEntity().getSize()));
            }
        } catch (Throwable t) {
            // Error while getting the request's entity, cf issue #931
            Engine.getLogger(LogService.class).log(Level.SEVERE,
                    "Cannot retrieve size of request's entity", t);
            sb.append("-");
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

    /**
     * Returns the URI template of loggable resource references. Returns null by
     * default, meaning the all requests are loggable, independant of their
     * target resource URI reference.
     * 
     * @return The URI template of loggable resource references.
     * @see Request#getResourceRef()
     */
    public Template getLoggableTemplate() {
        return loggableTemplate;
    }

    /**
     * Returns the name of the JDK's logger to use when logging access calls.
     * The default name will follow this pattern:
     * "org.restlet.MyComponent.LogService", where "MyComponent" will correspond
     * to the simple class name of your component subclass or to the base
     * "Component" class.
     * 
     * @return The name of the JDK's logger to use when logging access calls.
     */
    public String getLoggerName() {
        return this.loggerName;
    }

    /**
     * Returns the URI reference of the log properties.
     * 
     * @return The URI reference of the log properties.
     */
    public Reference getLogPropertiesRef() {
        return logPropertiesRef;
    }

    /**
     * Returns the format used when logging responses.
     * 
     * @return The format used, or null if the default one is used.
     * @see org.restlet.routing.Template for format syntax and variables.
     */
    public String getResponseLogFormat() {
        return this.responseLogFormat;
    }

    /**
     * Format an access log entry. If the log template property isn't provided,
     * then a default IIS like format is used.
     * 
     * @param response
     *            The response to log.
     * @param duration
     *            The call duration.
     * @return The formatted log entry.
     */
    public String getResponseLogMessage(Response response, int duration) {
        String result = null;

        // Format the call into a log entry
        if (this.responseLogTemplate != null) {
            result = this.responseLogTemplate.format(response.getRequest(),
                    response);
        } else {
            result = getDefaultResponseLogMessage(response, duration);
        }

        return result;
    }

    /**
     * Indicates if the debugging mode is enabled. False by default.
     * 
     * @return True if the debugging mode is enabled.
     * @deprecated Rely on {@link Application#isDebugging()} instead.
     */
    @Deprecated
    protected boolean isDebugging() {
        return debugging;
    }

    /**
     * Indicates if the identity check (as specified by RFC1413) is enabled.
     * Default value is false.
     * 
     * @return True if the identity check is enabled.
     */
    public boolean isIdentityCheck() {
        return this.identityCheck;
    }

    /**
     * Indicates if the call should be logged during the processing chain. By
     * default, it tries to match the request URI with the
     * {@link #getLoggableTemplate()} URI template otherwise is returns true.
     * 
     * @param request
     *            The request to log.
     * @return True if the call should be logged during the processing chain.
     */
    public boolean isLoggable(Request request) {
        return (getLoggableTemplate() == null) ? true : getLoggableTemplate()
                .match(request.getResourceRef().getTargetRef().toString()) > 0;
    }

    /**
     * Indicates if the debugging mode is enabled.
     * 
     * @param debugging
     *            True if the debugging mode is enabled.
     * @deprecated Rely on {@link Application#setDebugging(boolean)} instead.
     */
    @Deprecated
    protected void setDebugging(boolean debugging) {
        this.debugging = debugging;
    }

    /**
     * Indicates if the identity check (as specified by RFC1413) is enabled.
     * 
     * @param identityCheck
     *            True if the identity check is enabled.
     */
    public void setIdentityCheck(boolean identityCheck) {
        this.identityCheck = identityCheck;
    }

    /**
     * Sets the URI template of loggable resource references.
     * 
     * @param loggableTemplateRef
     *            The URI template of loggable resource references.
     * @see #setLoggableTemplate(Template)
     */
    public void setLoggableTemplate(String loggableTemplateRef) {
        if (loggableTemplateRef != null) {
            this.loggableTemplate = new Template(loggableTemplateRef);
        } else {
            this.loggableTemplate = null;
        }
    }

    /**
     * Sets the URI template of loggable resource references.
     * 
     * @param loggableTemplate
     *            The URI template of loggable resource references.
     */
    public void setLoggableTemplate(Template loggableTemplate) {
        this.loggableTemplate = loggableTemplate;
    }

    /**
     * Sets the name of the JDK's logger to use when logging access calls.
     * 
     * @param name
     *            The name of the JDK's logger to use when logging access calls.
     */
    public void setLoggerName(String name) {
        this.loggerName = name;
    }

    /**
     * Sets the URI reference of the log properties.
     * 
     * @param logPropertiesRef
     *            The URI reference of the log properties.
     */
    public void setLogPropertiesRef(Reference logPropertiesRef) {
        this.logPropertiesRef = logPropertiesRef;
    }

    /**
     * Sets the URI reference of the log properties.
     * 
     * @param logPropertiesUri
     *            The URI reference of the log properties.
     */
    public void setLogPropertiesRef(String logPropertiesUri) {
        setLogPropertiesRef(new Reference(logPropertiesUri));
    }

    /**
     * Sets the format to use when logging responses. The default format matches
     * the one of IIS 6.
     * 
     * @param responseLogFormat
     *            The format to use when logging responses.
     * @see org.restlet.routing.Template for format syntax and variables.
     */
    public void setResponseLogFormat(String responseLogFormat) {
        this.responseLogFormat = responseLogFormat;
    }

    /**
     * Starts the log service by attempting to read the log properties if the
     * {@link #getLogPropertiesRef()} returns a non null URI reference.
     */
    @Override
    public synchronized void start() throws Exception {
        super.start();

        this.responseLogTemplate = (getResponseLogFormat() == null) ? null
                : new Template(getResponseLogFormat());
        // [ifndef gae]
        if (getLogPropertiesRef() != null) {
            Representation logProperties = new ClientResource(getContext(),
                    getLogPropertiesRef()).get();

            if (logProperties != null) {
                java.util.logging.LogManager.getLogManager().readConfiguration(
                        logProperties.getStream());
            }
        }
        // [enddef]
    }
}
