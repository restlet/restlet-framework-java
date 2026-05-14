/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

/**
 * {@link Runnable} That allows defining the context class loader.
 *
 * @author Jerome Louvel
 */
public abstract class ContextualRunnable implements Runnable {
    /** The contextual class loader used at run time. */
    private ClassLoader contextClassLoader;

    /** Constructor. */
    protected ContextualRunnable() {
        this.contextClassLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * Returns the runnable's context class loader.
     *
     * @return The runnable's context class loader.
     */
    public ClassLoader getContextClassLoader() {
        return contextClassLoader;
    }

    /**
     * Sets the runnable's context class loader.
     *
     * @param contextClassLoader The runnable's context class loader.
     */
    public void setContextClassLoader(ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
    }
}
