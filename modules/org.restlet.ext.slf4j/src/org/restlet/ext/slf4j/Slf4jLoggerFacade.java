/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
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
