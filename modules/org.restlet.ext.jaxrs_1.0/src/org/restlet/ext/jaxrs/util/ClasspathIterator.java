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
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@link Iterator} iterates over all classes resolvable by the classpath.
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("unchecked")
public class ClasspathIterator extends AbstractClassIterator implements
        Iterator<Class<?>> {

    private Iterator<String> classPathIter;

    private Iterator<Class<?>> currentIter;

    /**
     * Creates an Iterator over all classes in the classpath.
     * 
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @param logger
     *                a logger to log {@link Exception}s or {@link Error}s, or
     *                null, if Exceptions should not be logged.
     * @param logLevel
     *                the Log{@link Level} to use, must not be null; see
     *                {@link #loadClassByFileName(String, boolean, Logger, Level)}
     */
    public ClasspathIterator(boolean throwOnExc, Logger logger, Level logLevel) {
        super(throwOnExc, logger, logLevel);
        String[] classpath = System.getProperty("java.class.path").split(";");
        this.classPathIter = new ArrayIterator<String>(classpath);
    }

    /**
     * Creates an Iterator over all classes in the classpath.
     */
    public ClasspathIterator() {
        this(false, null, null);
    }

    @Override
    public boolean hasNext() throws WrappedClassLoadException {
        if (this.next != null) {
            return true;
        }
        if (currentIter != null && currentIter.hasNext()) {
            this.next = currentIter.next();
            return this.hasNext();
        }
        while (this.classPathIter.hasNext()) {
            String cpEntry = this.classPathIter.next();
            File f = new File(cpEntry);
            if (f.isDirectory()) {
                currentIter = new DirectoryClassIterator(f, throwOnExc);
                return this.hasNext();
            } else {
                try {
                    JarFile jarFile = new JarFile(f);
                    currentIter = new JarFileClassIterator(jarFile, throwOnExc);
                    return this.hasNext();
                } catch (IOException e) {
                    String message = "The file "
                            + f
                            + " Is not a directory and not a valid jar file. Don't know what to do with it. Will ignore entry.";
                    log(message, e);
                }
            }
        }
        return false;
    }
}