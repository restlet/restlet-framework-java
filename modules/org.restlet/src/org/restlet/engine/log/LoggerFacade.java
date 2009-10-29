/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.log;

import java.util.logging.Logger;

import org.restlet.engine.Edition;

/**
 * Logger facade to the underlying logging framework used by the Restlet
 * Framework. By default, it relies on the JULI mechanism built in Java SE. You
 * can provide an alternate implementation by extending this class and
 * overriding the methods.
 * 
 * @author Jerome Louvel
 */
public class LoggerFacade {

    /**
     * Returns an anonymous logger.
     * 
     * @return The logger.
     */
    public Logger getAnonymousLogger() {
        return Logger.getAnonymousLogger();
    }

    /**
     * Returns a logger based on the class name of the given object.
     * 
     * @param clazz
     *            The parent class.
     * @return The logger.
     */
    public Logger getLogger(Class<?> clazz) {
        return getLogger(clazz, null);
    }

    /**
     * Returns a logger based on the class name of the given object.
     * 
     * @param clazz
     *            The parent class.
     * @param defaultLoggerName
     *            The default logger name to use if no one can be inferred from
     *            the class.
     * @return The logger.
     */
    public Logger getLogger(Class<?> clazz, String defaultLoggerName) {
        String loggerName = null;

        if (clazz != null) {
            if (Edition.CURRENT == Edition.GWT) {
                loggerName = clazz.getName();
            } else {
                loggerName = clazz.getCanonicalName();
            }
        }

        if (loggerName == null) {
            loggerName = defaultLoggerName;
        }

        if (loggerName != null) {
            return getLogger(loggerName);
        } else {
            return getAnonymousLogger();
        }
    }

    /**
     * Returns a logger based on the class name of the given object.
     * 
     * @param object
     *            The parent object.
     * @param defaultLoggerName
     *            The default logger name to use if no one can be inferred from
     *            the object class.
     * @return The logger.
     */
    public Logger getLogger(Object object, String defaultLoggerName) {
        return getLogger(object.getClass(), defaultLoggerName);
    }

    /**
     * Returns a logger based on the given logger name.
     * 
     * @param loggerName
     *            The logger name.
     * @return The logger.
     */
    public Logger getLogger(String loggerName) {
        return Logger.getLogger(loggerName);
    }

}
