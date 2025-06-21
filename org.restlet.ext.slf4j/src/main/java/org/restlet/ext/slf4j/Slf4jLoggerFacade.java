/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.slf4j;

import java.util.logging.Logger;

import org.restlet.engine.log.LoggerFacade;
import org.slf4j.LoggerFactory;

/**
 * Restlet log facade for the SLF4J {@link LoggerFactory}. In order to use SLF4J
 * as the logging facade for Restlet, you need to set the
 * "org.restlet.engine.loggerFacadeClass" system property with the
 * "org.restlet.ext.slf4j.Slf4jLoggerFacade" value.
 * 
 * @see Slf4jLogger
 * @author Jerome Louvel
 */
public class Slf4jLoggerFacade extends LoggerFacade {

    /**
     * Returns an instance of {@link Slf4jLogger}, wrapping the result of
     * {@link LoggerFactory#getLogger(String)} where the logger name is "".
     * 
     * @return An anonymous logger.
     */
    @Override
    public Logger getAnonymousLogger() {
        return new Slf4jLogger(LoggerFactory.getLogger(""));
    }

    /**
     * Returns an instance of {@link Slf4jLogger}, wrapping the result of
     * {@link LoggerFactory#getLogger(String)} with the logger name.
     * 
     * @param loggerName
     *            The logger name.
     * @return An anonymous logger.
     */
    @Override
    public Logger getLogger(String loggerName) {
        return new Slf4jLogger(LoggerFactory.getLogger(loggerName));
    }

}
