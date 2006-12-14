/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet;

import java.util.logging.Logger;

import org.restlet.data.ParameterList;

/**
 * Contextual data and services provided to a Restlet. The context is the means
 * by which a Restlet may access the software environment within the framework.
 * It is typically provided by the immediate parent Restlet (Component and
 * Application are the most common cases). The services provided are access to a
 * logger, access to configuration parameters and to a request dispatcher.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Context {
    /** The modifiable list of parameters. */
    private ParameterList parameters;

    /** The logger instance to use. */
    private Logger logger;

    /**
     * Constructor. Writes log messages to "org.restlet".
     */
    public Context() {
        this("org.restlet");
    }

    /**
     * Constructor.
     * 
     * @param logger
     *            The logger instance of use.
     */
    public Context(Logger logger) {
        this.logger = logger;
    }

    /**
     * Constructor.
     * 
     * @param loggerName
     *            The name of the logger to use.
     */
    public Context(String loggerName) {
        this(Logger.getLogger(loggerName));
    }

    /**
     * Returns a call dispatcher.
     * 
     * @return A call dispatcher.
     */
    public Dispatcher getDispatcher() {
        return null;
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Returns the modifiable list of parameters.
     * 
     * @return The modifiable list of parameters.
     */
    public ParameterList getParameters() {
        if (this.parameters == null)
            this.parameters = new ParameterList();
        return this.parameters;
    }
}