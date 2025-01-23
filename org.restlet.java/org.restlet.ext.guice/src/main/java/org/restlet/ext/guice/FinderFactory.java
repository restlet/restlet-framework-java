/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.guice;

import java.lang.annotation.Annotation;

import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import com.google.inject.ProvisionException;

/**
 * Factory for dependency-injecting Finders.
 * 
 * @author Tim Peierls
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public interface FinderFactory {

    /**
     * Returns a {@link Finder} that will obtain a dependency-injected instance
     * of the ServerResource subtype bound to the type associated with the given
     * class.
     * 
     * @param cls
     *            The class to instantiate.
     * @return An instance of {@link Finder}.
     * @throws ProvisionException
     *             if {@code cls} is not bound to {@link ServerResource} or a
     *             subclass.
     */
    Finder finder(Class<?> cls);

    /**
     * Returns a {@link Finder} that will obtain a dependency-injected instance
     * of the ServerResource subtype bound to the type and qualifier associated
     * with the given class.
     * 
     * @param cls
     *            The class to instantiate.
     * @param qualifier
     *            The qualifier associated with the given class.
     * @return An instance of {@link Finder}.
     * @throws ProvisionException
     *             if {@code cls} qualified by {@code qualifier} is not bound to
     *             {@link ServerResource} or a subclass.
     */
    Finder finder(Class<?> cls, Class<? extends Annotation> qualifier);
}
