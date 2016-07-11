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

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.restlet.resource.ServerResource;

/**
 * Base class for ServerResources that do their own member injection.
 * 
 * @author Tim Peierls
 */
public abstract class SelfInjectingServerResource extends ServerResource {

    /**
     * Implemented by DI framework-specific code. For example, with Guice, the
     * statically-injected MembersInjector just calls
     * {@code injector.injectMembers(object)}.
     */
    interface MembersInjector {
        void injectMembers(Object object);
    }

    /** Must be statically injected by DI framework. */
    @Inject
    private static volatile MembersInjector theMembersInjector;

    /**
     * Whether we've been injected yet. This protects against multiple injection
     * of a subclass that gets injected before {@link #doInit()} is called.
     */
    private final AtomicBoolean injected = new AtomicBoolean(false);

    /**
     * Subclasseses overriding this method must call {@code super.doInit()}
     * first.
     */
    protected void doInit() {
        ensureInjected(theMembersInjector);
    }

    void ensureInjected(MembersInjector membersInjector) {
        if (membersInjector != null && injected.compareAndSet(false, true)) {
            membersInjector.injectMembers(this);
        }
    }

    @Inject
    private void injected() { // NOPMD
        injected.set(true);
    }
}
