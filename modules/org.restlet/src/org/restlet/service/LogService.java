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

package org.restlet.service;

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
 * <br>
 * If you use <a href="http://www.analog.cx">Analog</a> to generate your log
 * reports, and if you use the default log format, then you can simply specify
 * this string as a value of the LOGFORMAT command:
 * (%Y-%m-%d\t%h:%n:%j\t%S\t%u\t%j\t%j\t%j\t%r\t%q\t%c\t%b\t%j\t%T\t%v\t%B\t%f)<br>
 * <br>
 * For custom access log format, see the syntax to use and the list of available
 * variable names in {@link org.restlet.util.Template}. <br>
 * 
 * @see <a href="http://www.restlet.org/documentation/1.1/tutorial#part07">Tutorial: Access logging</a>
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/logging/package-summary.html">java.util.logging</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class LogService extends Service {
    /** The access logger name. */
    private volatile String loggerName;

    /** The log entry format. */
    private volatile String logFormat;

    /** Indicates if the identity check (as specified by RFC1413) is enabled. */
    private volatile boolean identityCheck;

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
        this.identityCheck = false;
    }

    /**
     * Returns the format used.
     * 
     * @return The format used, or null if the default one is used.
     * @see org.restlet.util.Template for format syntax and variables.
     */
    public String getLogFormat() {
        return this.logFormat;
    }

    /**
     * Returns the name of the JDK's logger to use when logging calls.
     * 
     * @return The name of the JDK's logger to use when logging calls.
     */
    public String getLoggerName() {
        return this.loggerName;
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
     * @see org.restlet.util.Template for format syntax and variables.
     */
    public void setLogFormat(String format) {
        this.logFormat = format;
    }

    /**
     * Sets the name of the JDK's logger to use when logging calls.
     * 
     * @param name
     *            The name of the JDK's logger to use when logging calls.
     */
    public void setLoggerName(String name) {
        this.loggerName = name;
    }

}
