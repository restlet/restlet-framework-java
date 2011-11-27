package org.restlet.ext.guice;

import java.lang.annotation.Annotation;

import org.restlet.resource.Finder;

/**
 * Factory for dependency-injecting Finders.
 */
public interface FinderFactory {

    /**
     * Returns a Finder that will obtain a dependency-injected instance of
     * the ServerResource subtype bound to the type associated with the given class.
     * @throws ProvisionException if {@code cls} is not bound to ServerResource or a subclass.
     */
    Finder finder(Class<?> cls);

    /**
     * Returns a Finder that will obtain a dependency-injected instance of
     * the ServerResource subtype bound to the type and qualifier associated
     * with the given class.
     * @throws ProvisionException if {@code cls} qualified by {@code qualifier}
     * is not bound to ServerResource or a subclass.
     */
    Finder finder(Class<?> cls, Class<? extends Annotation> qualifier);
}
