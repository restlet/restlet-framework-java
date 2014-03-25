package org.restlet.ext.guice;

import org.restlet.ext.guice.SelfInjectingServerResource.MembersInjector;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

/**
 * Install this module to arrange for {@link SelfInjectingServerResource}
 * instances to have their members injected (idempotently) by the
 * {@link SelfInjectingServerResource#doInit()} method (which is called
 * automatically after construction).
 * 
 * @author Tim Peierls
 */
public class SelfInjectingServerResourceModule extends AbstractModule {

    @Override
    protected final void configure() {
        requestStaticInjection(SelfInjectingServerResource.class);
    }

    @Provides
    MembersInjector membersInjector(final Injector injector) {
        return new MembersInjector() {
            public void injectMembers(Object object) {
                injector.injectMembers(object);
            }
        };
    }
}
