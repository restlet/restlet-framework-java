package org.restlet.ext.guice;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.restlet.resource.ServerResource;


/**
 * Base class for ServerResources that do their own member injection.
 */
public abstract class SelfInjectingServerResource extends ServerResource {

    /**
     * Implemented by DI framework-specific code. For example, with
     * Guice, the statically-injected MembersInjector just calls
     * {@code injector.injectMembers(object)}.
     */
    public interface MembersInjector {
        void injectMembers(Object object);
    }

    /**
     * Subclasseses overriding this method must call
     * {@code super.doInit()} first.
     */
    protected void doInit() {
        ensureInjected(theMembersInjector);
    }

    @Inject private void injected() {   // NOPMD
        injected.set(true);
    }

    void ensureInjected(MembersInjector membersInjector) {
        if (injected.compareAndSet(false, true)) {
            membersInjector.injectMembers(this);
        }
    }

    /**
     * Whether we've been injected yet. This protects against
     * multiple injection of a subclass that gets injected
     * before doInit is called.
     */
    private final AtomicBoolean injected = new AtomicBoolean(false);

    /**
     * Must be statically injected by DI framework.
     */
    @Inject private static volatile MembersInjector theMembersInjector;
}
