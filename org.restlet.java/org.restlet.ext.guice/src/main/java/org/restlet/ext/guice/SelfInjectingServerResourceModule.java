/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

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
 * @deprecated Will be removed in next major release.
 */
@Deprecated
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
