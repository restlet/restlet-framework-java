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

package org.restlet.engine.util;

/**
 * {@link Runnable} That allows to define the context class loader.
 * 
 * @author Jerome Louvel
 * 
 */
public abstract class ContextualRunnable implements Runnable {
    /** The contextual class loader used at run time. */
    private ClassLoader contextClassLoader;

    /**
     * Constructor.
     */
    public ContextualRunnable() {
        this.contextClassLoader = Thread.currentThread()
                .getContextClassLoader();
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
     * @param contextClassLoader
     *            The runnable's context class loader.
     */
    public void setContextClassLoader(ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
    }

}
