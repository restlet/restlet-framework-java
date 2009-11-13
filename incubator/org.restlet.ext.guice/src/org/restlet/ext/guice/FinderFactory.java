package org.restlet.ext.guice;

import com.google.inject.Key;

import org.restlet.Context;
import org.restlet.resource.Finder;
import org.restlet.resource.Handler;
import org.restlet.resource.ServerResource;

/**
 * Produces a Finder instance, given a Guice key or a Handler class, that will
 * obtain a Handler/Resource for that key or class.
 */
public interface FinderFactory {

    /**
     * Returns a Finder that will obtain an instance of ServerResource bound to
     * the given Key.
     */
    Finder finderOf(Key<? extends ServerResource> key, Context context);

    /**
     * Returns a Finder that will obtain an instance of ServerResource bound to
     * the given class.
     */
    Finder finderOf(Class<? extends ServerResource> cls, Context context);

    /**
     * Returns a Finder that will obtain an instance of Handler bound to the
     * given Key.
     */
    Finder finderFor(Key<? extends Handler> key);

    /**
     * Returns a Finder that will obtain an instance of Handler bound to the
     * given class.
     */
    Finder finderFor(Class<? extends Handler> cls);
}
