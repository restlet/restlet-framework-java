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

import java.io.IOException;

/**
 * Log file handler that uses the {@link AccessLogFormatter} by default. Also
 * useful in configuration files to differentiate from the
 * {@link java.util.logging.FileHandler}.
 * 
 * @author Jerome Louvel
 */
public class AccessLogFileHandler extends java.util.logging.FileHandler {
    /**
     * Constructor.
     * 
     * @throws IOException
     * @throws SecurityException
     */
    public AccessLogFileHandler() throws IOException, SecurityException {
        super();
        init();
    }

    /**
     * Constructor.
     * 
     * @param pattern
     *            The name of the output file.
     * @throws IOException
     * @throws SecurityException
     */
    public AccessLogFileHandler(String pattern) throws IOException,
            SecurityException {
        super(pattern);
        init();
    }

    /**
     * Constructor.
     * 
     * @param pattern
     *            The name of the output file.
     * @param append
     *            Specifies append mode.
     * @throws IOException
     * @throws SecurityException
     */
    public AccessLogFileHandler(String pattern, boolean append)
            throws IOException, SecurityException {
        super(pattern, append);
        init();
    }

    /**
     * Constructor.
     * 
     * @param pattern
     *            The name of the output file.
     * @param limit
     *            The maximum number of bytes to write to any one file.
     * @param count
     *            The number of files to use.
     * @throws IOException
     * @throws SecurityException
     */
    public AccessLogFileHandler(String pattern, int limit, int count)
            throws IOException, SecurityException {
        super(pattern, limit, count);
        init();
    }

    /**
     * Constructor.
     * 
     * @param pattern
     *            The name of the output file.
     * @param limit
     *            The maximum number of bytes to write to any one file.
     * @param count
     *            The number of files to use.
     * @param append
     *            Specifies append mode.
     * @throws IOException
     * @throws SecurityException
     */
    public AccessLogFileHandler(String pattern, int limit, int count,
            boolean append) throws IOException, SecurityException {
        super(pattern, limit, count, append);
        init();
    }

    /**
     * Initialization code common to all constructors.
     */
    protected void init() {
        setFormatter(new AccessLogFormatter());
    }

}
