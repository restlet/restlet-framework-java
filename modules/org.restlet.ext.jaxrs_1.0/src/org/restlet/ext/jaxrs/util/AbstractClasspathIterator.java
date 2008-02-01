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
package org.restlet.ext.jaxrs.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@link Iterator} iterates over all classes under a given directory. It
 * must be started with the root directory for the packages.
 * 
 * @author Stephan Koops
 */
abstract class AbstractClasspathIterator implements Iterator<Class<?>> {

    private Logger logger;

    private Level logLevel;

    protected Class<?> next;

    /**
     * Iterates over the classes in the given jar file.
     * 
     * @param directory
     *                the root directory for the packages
     * @param logger
     *                a logger to log {@link Exception}s or {@link Error}s;
     *                see {@link #loadClassByFileName(String, Logger, Level)}
     * @param logLevel
     *                the Log{@link Level} to use, must not be null; see
     *                {@link #loadClassByFileName(String, Logger, Level)}
     */
    protected AbstractClasspathIterator(Logger logger, Level logLevel) {
        if (logger != null && logLevel == null)
            throw new IllegalArgumentException(
                    "The logLevel must not be null if a Logger is given");
        this.logger = logger;
        this.logLevel = logLevel;
    }

    /**
     * @see Iterator#next()
     */
    public final Class<?> next() throws NoSuchElementException {
        if (!this.hasNext())
            throw new NoSuchElementException();
        Class<?> next = this.next;
        this.next = null;
        return next;
    }

    @Deprecated
    public final void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "This Operation is not supported.");
    }

    /**
     * logs the message, if a logger is given.
     * 
     * @param message
     * @param throwable
     */
    protected void log(String message, Throwable throwable) {
        if (logger != null)
            logger.log(logLevel, message, throwable);
    }

    protected Class<?> loadClassByFileName(String fileName) {
        return loadClassByFileName(fileName, logger, logLevel);
    }

    /**
     * Loads the class with the given file name. <br />
     * Example: <code>loadClassByFileName("java/lang/String.class")</code>
     * will load the class {@link java.util.String}.
     * 
     * @param fileName
     *                Filename relativ to the package root.
     * @param logger
     *                logger to log an {@link Exception} or {@link Error}.
     * @param logLevel
     *                Level to log occuring {@link Exception} or {@link Error}.
     *                If null, {@link Level#INFO} is used.
     * @return Returns the loaded class or null, if the class could not be
     *         loaded, for whatever reason. If a logger was given, the
     *         {@link Exception} or {@link Error} was logged.
     */
    public static Class<?> loadClassByFileName(String fileName, Logger logger,
            Level logLevel) {
        String cn;
        if (fileName.endsWith(".class"))
            cn = fileName.substring(0, fileName.length() - 6);
        else if (fileName.endsWith(".java"))
            cn = fileName.substring(0, fileName.length() - 5);
        else
            return null;
        String classname = cn.replace('/', '.').replace('\\', '.');
        try {
            return Class.forName(classname);
        } catch (Throwable e) {
            if (logger != null) {
                if (logLevel == null)
                    logLevel = Level.INFO;
                logger.log(logLevel, "Could not load class with name "
                        + classname, e);
            }
            return null;
        }
    }
}