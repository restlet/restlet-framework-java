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
import java.io.FileFilter;
import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@link Iterator} iterates over all classes under a given directory. It
 * must be started with the root directory for the packages.
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("unchecked")
public class DirectoryClassIterator extends AbstractClassIterator implements
        Iterator<Class<?>> {

    private Stack<Iterator<File>> dirStack = new Stack<Iterator<File>>();

    private int rootDirnameLength;

    /**
     * Iterates over the classes in the given jar file.
     * 
     * @param directory
     *                the root directory for the packages
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @param logger
     *                a logger to log {@link Exception}s or {@link Error}s;
     *                see
     *                {@link #loadClassByFileName(String, boolean, Logger, Level)}
     * @param logLevel
     *                the Log{@link Level} to use, must not be null; see
     *                {@link #loadClassByFileName(String, boolean, Logger, Level)}
     */
    public DirectoryClassIterator(File directory, boolean throwOnExc,
            Logger logger, Level logLevel) {
        super(throwOnExc, logger, logLevel);
        pushDir(directory);
        rootDirnameLength = directory.getPath().length() + 1;
    }

    /**
     * Iterates over the classes in the given jar file.
     * 
     * @param directory
     *                the root directory for the packages
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     */
    public DirectoryClassIterator(File directory, boolean throwOnExc) {
        this(directory, throwOnExc, null, null);
    }

    /**
     * @param directory
     */
    private void pushDir(File directory) {
        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;
                String filename = file.getName();
                if (filename.endsWith(".class") || filename.endsWith(".java"))
                    return true;
                return false;
            }
        });
        dirStack.push(new ArrayIterator<File>(files));
    }

    @Override
    public boolean hasNext() {
        if (this.next != null)
            return true;
        if (dirStack.isEmpty())
            return false;
        Iterator<File> iter = dirStack.peek();
        while (iter.hasNext()) {
            File entry = iter.next();
            if (entry.isDirectory()) {
                pushDir(entry);
                return this.hasNext();
            } else {
                this.next = loadClass(entry);
                if (this.next != null)
                    return true;
            }
        }
        dirStack.pop();
        return this.hasNext();
    }

    /**
     * Loads the class with the given file name. <br />
     * Example: <code>loadClassByFileName("java/lang/String.class")</code>
     * will load the class {@link java.util.String}.
     * 
     * @param file
     *                file representing the class to load.
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
    private Class<?> loadClass(File file) throws WrappedClassLoadException {
        return loadClassByFileName(file.getPath().substring(rootDirnameLength));
    }
}