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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.restlet.engine.Edition;
import org.restlet.engine.Engine;

/**
 * Flexible engine class loader. Uses the current class's class loader as its
 * parent. Can also check with the user class loader defined by
 * {@link Engine#getUserClassLoader()} or with
 * {@link Thread#getContextClassLoader()} or with {@link Class#forName(String)}.
 * 
 * @author Jerome Louvel
 */
public class EngineClassLoader extends ClassLoader {

    /** The parent Restlet engine. */
    private final Engine engine;

    /**
     * Constructor.
     */
    public EngineClassLoader(Engine engine) {
        super(EngineClassLoader.class.getClassLoader());
        this.engine = engine;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = null;

        // First try the user class loader
        ClassLoader cl = getEngine().getUserClassLoader();

        if (cl != null) {
            try {
                result = cl.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                // Ignore
            }
        }

        // Then try the current thread's class loader
        if (result == null) {
            cl = Thread.currentThread().getContextClassLoader();

            if (cl != null) {
                try {
                    result = cl.loadClass(name);
                } catch (ClassNotFoundException cnfe) {
                    // Ignore
                }
            }
        }

        // Finally try with this ultimate approach
        if (result == null) {
            try {
                result = Class.forName(name);
            } catch (ClassNotFoundException cnfe) {
                // Ignore
            }
        }

        // Otherwise throw an exception
        if (result == null) {
            throw new ClassNotFoundException(name);
        }

        return result;
    }

    @Override
    protected URL findResource(String name) {
        URL result = null;

        // First try the user class loader
        ClassLoader cl = getEngine().getUserClassLoader();

        if (cl != null) {
            result = cl.getResource(name);
        }

        // Then try the current thread's class loader
        if (result == null) {
            cl = Thread.currentThread().getContextClassLoader();

            if (cl != null) {
                result = cl.getResource(name);
            }
        }

        return result;
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> result = null;

        // First try the user class loader
        ClassLoader cl = getEngine().getUserClassLoader();

        if (cl != null) {
            result = cl.getResources(name);
        }

        // Then try the current thread's class loader
        if (result == null) {
            cl = Thread.currentThread().getContextClassLoader();

            if (cl != null) {
                result = cl.getResources(name);
            }
        }

        return result;
    }

    /**
     * Returns the parent Restlet engine.
     * 
     * @return The parent Restlet engine.
     */
    protected Engine getEngine() {
        return engine;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> allUrls = super.getResources(name);
        Vector<URL> result = new Vector<URL>();

        if (allUrls != null) {
            try {
                URL url;
                while (allUrls.hasMoreElements()) {
                    url = allUrls.nextElement();

                    if (result.indexOf(url) == -1) {
                        result.add(url);
                    }
                }
            } catch (NullPointerException e) {
                // At this time (June 2009) a NPE is thrown with Dalvik JVM.
                // Let's throw the NPE for the other editions.
                if (Edition.CURRENT != Edition.ANDROID) {
                    throw e;
                }
            }
        }

        return result.elements();
    }

}
