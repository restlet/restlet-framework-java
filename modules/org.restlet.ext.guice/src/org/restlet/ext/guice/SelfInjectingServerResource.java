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
