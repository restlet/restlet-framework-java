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
public class DirectoryClassIterator extends AbstractClasspathIterator {

    private Stack<Iterator<File>> dirStack = new Stack<Iterator<File>>();

    private int rootDirnameLength;

    private Class<?> next;

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
    public DirectoryClassIterator(File directory, Logger logger, Level logLevel) {
        super(logger, logLevel);
        pushDir(directory);
        rootDirnameLength = directory.getPath().length() + 1;
    }

    /**
     * Iterates over the classes in the given jar file.
     * 
     * @param directory
     *                the root directory for the packages
     */
    public DirectoryClassIterator(File directory) {
        this(directory, null, null);
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

    public boolean hasNext() {
        if (this.next != null)
            return true;
        if(dirStack.isEmpty())
            return false;
        Iterator<File> iter = dirStack.peek();
        while (iter.hasNext()) {
            File entry = iter.next();
            if(entry.isDirectory())
            {
                pushDir(entry);
                return this.hasNext();
            }
            else
            {
                this.next = loadClass(entry);
                if(this.next != null)
                    return true;
            }
        }
        dirStack.pop();
        return this.hasNext();
    }

    private Class<?> loadClass(File file) {
        return loadClassByFileName(file.getPath().substring(
                rootDirnameLength));
    }
}