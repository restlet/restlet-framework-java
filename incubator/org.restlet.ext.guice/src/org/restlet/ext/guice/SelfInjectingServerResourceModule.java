package org.restlet.ext.guice;

import static org.restlet.ext.guice.SelfInjectingServerResource.MembersInjector;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

/**
 * Install this module to arrange for SelfInjectingServerResource
 * instances to have their members injected (idempotently) by the
 * doInit method (which is called automatically after construction).
 */
public class SelfInjectingServerResourceModule extends AbstractModule {

    @Override protected final void configure() {
        requestStaticInjection(SelfInjectingServerResource.class);
    }

    @Provides MembersInjector membersInjector(final Injector injector) {
        return new MembersInjector() {
            public void injectMembers(Object object) {
                injector.injectMembers(object);
            }
        };
    }
}
