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

package org.restlet.ext.guice;

import java.lang.annotation.Annotation;

import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import com.google.inject.ProvisionException;

/**
 * Factory for dependency-injecting Finders.
 * 
 * @author Tim Peierls
 */
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
