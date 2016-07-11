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

import java.util.logging.Logger;

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
     * Returns an anonymous logger. By default it calls
     * {@link Logger#getAnonymousLogger()}. This method should be overridden by
     * subclasses.
     * 
     * @return The logger.
     */
    public Logger getAnonymousLogger() {
        // [ifndef gwt] instruction
        return Logger.getAnonymousLogger();
        // [ifdef gwt] instruction uncomment
        // return Logger.getLogger("");
    }

    /**
     * Returns a logger based on the class name of the given object. By default,
     * it calls {@link #getLogger(Class, String)} with a null default logger
     * name.
     * 
     * @param clazz
     *            The parent class.
     * @return The logger.
     */
    public final Logger getLogger(Class<?> clazz) {
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
    public final Logger getLogger(Class<?> clazz, String defaultLoggerName) {
        String loggerName = null;

        if (clazz != null) {
            // [ifndef gwt] instruction
            loggerName = clazz.getCanonicalName();
            // [ifdef gwt] instruction uncomment
            // loggerName = clazz.getName();
        }

        if (loggerName == null) {
            loggerName = defaultLoggerName;
        }

        if (loggerName != null) {
            return getLogger(loggerName);
        }

        return getAnonymousLogger();
    }

    /**
     * Returns a logger based on the class name of the given object. By default,
     * it calls {@link #getLogger(Class, String)} with the object's class as a
     * first parameter.
     * 
     * @param object
     *            The parent object.
     * @param defaultLoggerName
     *            The default logger name to use if no one can be inferred from
     *            the object class.
     * @return The logger.
     */
    public final Logger getLogger(Object object, String defaultLoggerName) {
        return getLogger(object.getClass(), defaultLoggerName);
    }

    /**
     * Returns a logger based on the given logger name. By default, it calls
     * {@link Logger#getLogger(String)}. This method should be overridden by
     * subclasses.
     * 
     * @param loggerName
     *            The logger name.
     * @return The logger.
     */
    public Logger getLogger(String loggerName) {
        return Logger.getLogger(loggerName);
    }

}
