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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@link Iterator} iterates over the classes in a Jar file.
 * 
 * @see JarFile
 * @author Stephan Koops
 */
@SuppressWarnings("unchecked")
public class JarFileClassIterator extends AbstractClassIterator implements
        Iterator<Class<?>> {

    private Enumeration<? extends JarEntry> jarEntryEnum;

    /**
     * Iterates over the classes in the given jar file.
     * 
     * @param jarFile
     *                the jar file
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @param logger
     *                a logger to log {@link Exception}s or {@link Error}s;
     *                see {@link #loadClassByFileName(String, Logger, Level)}
     * @param logLevel
     *                the Log{@link Level} to use, must not be null; see
     *                {@link #loadClassByFileName(String, Logger, Level)}
     */
    public JarFileClassIterator(JarFile jarFile, boolean throwOnExc, Logger logger, Level logLevel) {
        super(throwOnExc, logger, logLevel);
        jarEntryEnum = jarFile.entries();
    }

    /**
     * Iterates over the classes in the given jar file.
     * 
     * @param jarFile
     *                the jar file
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @param logger
     *                a logger to log {@link Exception}s or {@link Error}s;
     *                see {@link #loadClassByFileName(String, Logger, Level)}
     * @param logLevel
     *                the Log{@link Level} to use, must not be null; see
     *                {@link #loadClassByFileName(String, Logger, Level)}
     * @throws IOException
     *                 if an I/O error has occurred while opening the Jar file.
     */
    public JarFileClassIterator(File jarFile, boolean throwOnExc, Logger logger, Level logLevel)
            throws IOException {
        this(new JarFile(jarFile), throwOnExc, logger, logLevel);
    }

    /**
     * Iterates over the classes in the given jar file. Exceptions and Errors
     * will not logged.
     * 
     * @param jarFile
     *                the jar file
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @see #JarFileClassIterator(JarFile, Logger, Level)
     */
    public JarFileClassIterator(JarFile jarFile, boolean throwOnExc) {
        this(jarFile, throwOnExc, null, null);
    }

    /**
     * Iterates over the classes in the given jar file.
     * 
     * @param jarFile
     *                the jar file
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @throws IOException
     *                 if an I/O error has occurred while opening the Jar file.
     * @throws SecurityException
     *                 if access to the file is denied by the SecurityManager
     * @see JarFile
     */
    public JarFileClassIterator(File jarFile, boolean throwOnExc) throws IOException {
        this(new JarFile(jarFile), throwOnExc, null, null);
    }

    public boolean hasNext() {
        if (this.next != null)
            return true;
        while (jarEntryEnum.hasMoreElements()) {
            JarEntry jarEntry = jarEntryEnum.nextElement();
            String fileName = jarEntry.getName();
            // TODO if the Jar contains Jars -> recursive
            this.next = loadClassByFileName(fileName);
            if (this.next != null)
                return true;
        }
        return false;
    }
}