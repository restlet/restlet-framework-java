/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.service;

import java.util.logging.LogManager;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.engine.log.LogFilter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Filter;

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
 * @see <a
 *      href="http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/331-restlet/201-restlet.html">User
 *      Guide - Access logging</a>
 * @see <a
 *      href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/logging/package-summary.html">java.util.logging</a>
 * @author Jerome Louvel
 */
public class LogService extends Service {
    /** The access logger name. */
    private volatile String loggerName;

    /** The log entry format. */
    private volatile String logFormat;

    /** The URI reference of the log properties. */
    private volatile Reference logPropertiesRef;

    /** Indicates if the identity check (as specified by RFC1413) is enabled. */
    private volatile boolean identityCheck;

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
        this.loggerName = null;
        this.logFormat = null;
        this.logPropertiesRef = null;
        this.identityCheck = false;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new LogFilter(context, this);
    }

    /**
     * Returns the format used.
     * 
     * @return The format used, or null if the default one is used.
     * @see org.restlet.routing.Template for format syntax and variables.
     */
    public String getLogFormat() {
        return this.logFormat;
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
     * Indicates if the identity check (as specified by RFC1413) is enabled.
     * Default value is false.
     * 
     * @return True if the identity check is enabled.
     */
    public boolean isIdentityCheck() {
        return this.identityCheck;
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
     * Sets the format to use when logging calls. The default format matches the
     * one of IIS 6.
     * 
     * @param format
     *            The format to use when loggin calls.
     * @see org.restlet.routing.Template for format syntax and variables.
     */
    public void setLogFormat(String format) {
        this.logFormat = format;
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
     * Starts the log service by attempting to read the log properties if the
     * {@link #getLogPropertiesRef()} returns a non null URI reference.
     */
    @Override
    public synchronized void start() throws Exception {
        super.start();

        if (getLogPropertiesRef() != null) {
            Representation logProperties = new ClientResource(getContext(),
                    getLogPropertiesRef()).get();

            if (logProperties != null) {
                LogManager.getLogManager().readConfiguration(
                        logProperties.getStream());
            }
        }
    }
}
