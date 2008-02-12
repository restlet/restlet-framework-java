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
 * This abstract class support iterating over classes.
 * 
 * @author Stephan Koops
 */
abstract class AbstractClassIterator {

    /**
     * Loads the class with the given file name. <br />
     * Example: <code>loadClassByFileName("java/lang/String.class")</code>
     * will load the class {@link java.util.String}.
     * 
     * @param fileName
     *                Filename relativ to the package root.
     * @param throwOnExc
     *                if true, than a catched Exception is thrown, otherwise
     *                null will returned.
     * @param logger
     *                logger to log an {@link Exception} or {@link Error}.
     * @param logLevel
     *                Level to log occuring {@link Exception} or {@link Error}.
     *                If null, {@link Level#INFO} is used.
     * @return
     *                <ul>
     *                <li>Returns the loaded class, if it could be loaded.</li>
     *                <li>If the class could not be loaded and throwOnExc is
     *                <ul>
     *                <li><code>true</code>, the {@link Throwable} is
     *                thrown, wrapped in a WrappedClassLoadException.</li>
     *                <li><code>false</code>, null is returned. If a logger
     *                was given, the {@link Exception} or {@link Error} was
     *                logged.</li>
     *                </ul>
     *                </ul>
     * @throws WrappedClassLoadException
     *                 see return info.
     */
    public static Class<?> loadClassByFileName(String fileName,
            boolean throwOnExc, Logger logger, Level logLevel)
            throws WrappedClassLoadException {
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
            if (throwOnExc)
                throw new WrappedClassLoadException(classname, e);
            if (logger != null) {
                if (logLevel == null)
                    logLevel = Level.INFO;
                logger.log(logLevel, "Could not load class with name "
                        + classname, e);
            }
            return null;
        }
    }

    protected Logger logger;

    protected Level logLevel;

    protected Class<?> next;

    protected boolean throwOnExc;

    /**
     * Iterates over the classes in the given jar file.
     * 
     * @param directory
     *                the root directory for the packages
     * @param logger
     *                a logger to log {@link Exception}s or {@link Error}s;
     *                see
     *                {@link #loadClassByFileName(String, boolean, Logger, Level)}
     * @param logLevel
     *                the Log{@link Level} to use, must not be null; see
     *                {@link #loadClassByFileName(String, boolean, Logger, Level)}
     */
    protected AbstractClassIterator(boolean throwOnExc, Logger logger,
            Level logLevel) {
        if (logger != null && logLevel == null)
            throw new IllegalArgumentException(
                    "The logLevel must not be null if a Logger is given");
        this.logger = logger;
        this.logLevel = logLevel;
        this.throwOnExc = throwOnExc;
    }

    protected void couldNotLoad(String classname, Throwable throwable)
            throws WrappedClassLoadException {
        if (throwOnExc)
            throw new WrappedClassLoadException(classname, throwable);
        if (logger != null)
            log("could not load class " + classname, throwable);
    }

    /**
     * @param message
     * @param throwable
     */
    protected void log(String message, Throwable throwable) {
        if (logger != null) {
            logger.log(logLevel, message, throwable);
        }
    }

    /**
     * @return
     * @throws WrappedClassLoadException
     *                 Will be thrown, if this Iterator was ordered, to throw
     *                 exceptions, if a class could not be loaded. The reason is
     *                 found as cause in the thrown exception.
     */
    public abstract boolean hasNext() throws WrappedClassLoadException;

    /**
     * Loads the class with the given file name. <br />
     * Example: <code>loadClassByFileName("java/lang/String.class")</code>
     * will load the class {@link java.util.String}.
     * 
     * @param fileName
     *                Filename relativ to the package root.
     * @return
     *                <ul>
     *                <li>Returns the loaded class, if it could be loaded.</li>
     *                <li>If the class could not be loaded and throwOnExc is
     *                <ul>
     *                <li><code>true</code>, the {@link Throwable} is
     *                thrown, wrapped in a WrappedClassLoadException.</li>
     *                <li><code>false</code>, null is returned. If a logger
     *                was given, the {@link Exception} or {@link Error} was
     *                logged.</li>
     *                </ul>
     *                </ul>
     * @throws WrappedClassLoadException
     *                 see return info.
     */
    protected Class<?> loadClassByFileName(String fileName)
            throws WrappedClassLoadException {
        return loadClassByFileName(fileName, throwOnExc, logger, logLevel);
    }

    /**
     * @return
     * @throws NoSuchElementException
     * @see Iterator#next()
     */
    @SuppressWarnings("unchecked")
    public Class next() throws NoSuchElementException {
        if (!this.hasNext())
            throw new NoSuchElementException();
        Class<?> next = this.next;
        this.next = null;
        return next;
    }

    /**
     * This operation is typically not supported by this {@link Iterator}.
     * 
     * @throws UnsupportedOperationException
     */
    @Deprecated
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "This Operation is not supported.");
    }
}