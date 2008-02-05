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
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This {@link Iterator} iterates over the classes in a package
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("unchecked")
public class PackageIterator extends AbstractClassIterator implements
        Iterator<Class<?>> {

    private Enumeration<URL> dirEnum;

    private Iterator<Class<?>> currentIter;

    private String packageName;

    /**
     * Creates an Iterator over all classes in the classpath.
     * 
     * @param classLoader
     *                the {@link ClassLoader} that reaches the package.
     * @param packageName
     *                the name of the package
     * @param throwOnExc
     *                if true, ClassNotFoundExceptions are thrown (wrapped in a
     *                RuntimeException), if false, than they are logged.
     * @param logger
     *                a logger to log {@link Exception}s or {@link Error}s, or
     *                null, if Exceptions should not be logged.
     * @param logLevel
     *                the Log{@link Level} to use, must not be null; see
     *                {@link #loadClassByFileName(String, boolean, Logger, Level)}
     * @throws IOException
     *                 if the package resource could not be read.
     */
    public PackageIterator(ClassLoader classLoader, String packageName,
            boolean throwOnExc, Logger logger, Level logLevel)
            throws IOException {
        super(throwOnExc, logger, logLevel);
        dirEnum = classLoader.getResources(packageName.replace('.', '/'));
        this.packageName = packageName;
    }

    /**
     * Creates an Iterator over all classes in the classpath.
     * 
     * @param classLoader
     *                the {@link ClassLoader} that reaches the package.
     * @param packageName
     *                the name of the package
     * @throws IOException
     *                 if the package resource could not be read.
     */
    public PackageIterator(ClassLoader classLoader, String packageName)
            throws IOException {
        this(classLoader, packageName, false, null, null);
    }

    @Override
    public boolean hasNext() throws WrappedClassLoadException {
        // TODO Package.getPackages()
        if (this.next != null) {
            return true;
        }
        if (currentIter != null && currentIter.hasNext()) {
            this.next = currentIter.next();
            return this.hasNext();
        }
        while (this.dirEnum.hasMoreElements()) {
            URL dir = this.dirEnum.nextElement();
            File f = new File(dir.getPath());
            if (f.isDirectory()) {
                String path = f.getPath();
                File rootDir = new File(path.substring(0, path.length()
                        - packageName.length()));
                currentIter = new DirectoryClassIterator(f, rootDir, false,
                        throwOnExc, logger, logLevel);
                return this.hasNext();
            }
        }
        return false;
    }
}