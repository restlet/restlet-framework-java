/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import org.restlet.engine.Engine;

/**
 * Flexible engine class loader. Uses the current class's class loader as its parent. Can also check
 * with the user class loader defined by {@link Engine#getUserClassLoader()} or with {@link
 * Thread#getContextClassLoader()} or with {@link Class#forName(String)}.
 *
 * @author Jerome Louvel
 */
public class EngineClassLoader extends ClassLoader {

    /** The parent Restlet engine. */
    private final Engine engine;

    /** Constructor. */
    public EngineClassLoader(Engine engine) {
        super(EngineClassLoader.class.getClassLoader());
        this.engine = engine;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> result = null;

        // First, try the user class loader
        ClassLoader cl = getEngine().getUserClassLoader();

        if (cl != null) {
            try {
                result = cl.loadClass(name);
            } catch (ClassNotFoundException ignored) {
                // Ignore
            }
        }

        // Then try the current thread's class loader
        if (result == null) {
            cl = Thread.currentThread().getContextClassLoader();

            if (cl != null) {
                try {
                    result = cl.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                    // Ignore
                }
            }
        }

        // Finally, try with this ultimate approach
        if (result == null) {
            try {
                result = Class.forName(name);
            } catch (ClassNotFoundException ignored) {
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

        // First, try the user class loader
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

        // First, try the user class loader
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
        final Enumeration<URL> allUrls = super.getResources(name);
        final ArrayList<URL> result = new ArrayList<>();

        if (allUrls != null) {
            URL url;
            while (allUrls.hasMoreElements()) {
                url = allUrls.nextElement();

                if (!result.contains(url)) {
                    result.add(url);
                }
            }
        }

        return new Enumeration<>() {
            private final Iterator<URL> iterator = result.iterator();

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public URL nextElement() {
                return iterator.next();
            }
        };
    }
}
