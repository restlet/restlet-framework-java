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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stephan Koops
 * @param <T>
 *                The interface or (typically abstract) class, that should be
 *                implemented.
 */
@SuppressWarnings("unchecked")
public class ServiceProviderIterator<T> extends AbstractClassIterator implements
        Iterator<Class<T>> {

    private static final String META_INF_SERVICES = "META-INF/services/";

    private Class<T> interfacce;

    /**
     * Contains an {@link Iterator} with the class names. It is read in the
     * constructor to ensure, that {@link IOException} can only occur on
     * creating, but not while iterating.
     */
    private Iterator<String> classNameIter;

    private ClassLoader classLoader;

    /**
     * Creates a new ServiceProviderIterator.
     * 
     * @param interfacce
     *                The interface or (typically abstract) class, that should
     *                be implemented.
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @throws IOException
     *                 If the file could not be read.
     * @see {@link #ServiceProviderIterator(Class, ClassLoader, Logger, Level)}
     */
    public ServiceProviderIterator(Class<T> interfacce, boolean throwOnExc)
            throws IOException {
        this(interfacce, throwOnExc, null, null, null);
    }

    /**
     * Creates a new ServiceProviderIterator.
     * 
     * @param interfacce
     *                The interface or (typically abstract) class, that should
     *                be implemented.
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @param classLoader
     *                The classLoader which reaches the file. If null, the
     *                system class loader is used.
     * @throws IOException
     *                 If the file could not be read.
     * @see {@link #ServiceProviderIterator(Class, ClassLoader, Logger, Level)}
     */
    public ServiceProviderIterator(Class<T> interfacce, boolean throwOnExc,
            ClassLoader classLoader) throws IOException {
        this(interfacce, throwOnExc, classLoader, null, null);
    }

    /**
     * Creates a new ServiceProviderIterator.
     * 
     * @param interfacce
     *                The interface or (typically abstract) class, that should
     *                be implemented.
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @param classLoader
     *                The classLoader which reaches the file. If null, the
     *                system class loader is used.
     * @param logger
     *                a logger to log {@link Exception}s or {@link Error}s or
     *                null, if they should be ignored.
     * @param logLevel
     *                the Log{@link Level} to use, must not be null, if a
     *                logger is given.
     * @param logLevel
     * @throws IOException
     *                 If the file could not be read.
     * @throws IllegalArgumentException
     *                 if the interfacce is null or no resource with it's name
     *                 could be found.
     */
    public ServiceProviderIterator(Class<T> interfacce, boolean throwOnExc,
            ClassLoader classLoader, Logger logger, Level logLevel)
            throws IOException, IllegalArgumentException {
        super(throwOnExc, logger, logLevel);
        if (interfacce == null)
            throw new IllegalArgumentException(
                    "The given interface (or class) must not be null");
        this.interfacce = interfacce;
        if (classLoader != null)
            this.classLoader = classLoader;
        else
            this.classLoader = ClassLoader.getSystemClassLoader();

        String rName = META_INF_SERVICES + interfacce.getName();
        InputStream inputStream = this.classLoader.getResourceAsStream(rName);
        if (inputStream == null && throwOnExc)
            throw new IllegalArgumentException("file " + rName + " not found");
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        List<String> classNames = new ArrayList<String>();
        String className;
        while ((className = reader.readLine()) != null) {
            className = className.trim();
            if (className.startsWith("#"))
                continue; // line is comment
            classNames.add(className);
        }
        this.classNameIter = classNames.iterator();
    }

    @Override
    public boolean hasNext() {
        if (this.next != null)
            return true;
        while (classNameIter.hasNext()) {
            String className = classNameIter.next();
            try {
                Class<?> clazz = classLoader.loadClass(className);
                if (this.interfacce.isAssignableFrom(clazz)) {
                    this.next = clazz;
                    return true;
                }
            } catch (ClassNotFoundException e) {
                if (throwOnExc)
                    throw new WrappedClassLoadException(className, e);
                log("Class with name " + className + " not found", e);
            }
        }
        return false;
    }
}