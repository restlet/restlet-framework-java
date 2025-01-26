/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.guice;

import jakarta.inject.Inject;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import com.google.inject.MembersInjector;

/**
 * Application with support for creating Router instances that arrange for
 * member injection of resource instances.
 * 
 * @author Tim Peierls
 * @deprecated Will be removed in next major release.
 */
@Deprecated
public abstract class ResourceInjectingApplication extends Application {

    /**
     * Wraps a {@link Finder} returned by {@link #createFinder(Class)} to do
     * member injection using the passed in {@link MembersInjector}.
     * 
     * @param finder
     *            The finder.
     * @param membersInjector
     *            The instance of {@link MembersInjector}.
     * @return A wrapped {@link Finder}.
     */
    public static Finder wrapFinderWithMemberInjection(final Finder finder,
            final SelfInjectingServerResource.MembersInjector membersInjector) {
        return new Finder(finder.getContext(), finder.getTargetClass()) {
            @Override
            public ServerResource find(Request request, Response response) {
                ServerResource res = finder.find(request, response);
                if (res instanceof SelfInjectingServerResource) {
                    SelfInjectingServerResource tmp = (SelfInjectingServerResource) res;
                    tmp.ensureInjected(membersInjector);
                } else {
                    membersInjector.injectMembers(res);
                }
                return res;
            }
        };
    }

    /** The members injector. */
    @Inject
    private volatile SelfInjectingServerResource.MembersInjector membersInjector;

    @Override
    public Finder createFinder(Class<? extends ServerResource> targetClass) {
        Finder finder = super.createFinder(targetClass);
        return wrapFinderWithMemberInjection(finder, membersInjector);
    }

    /**
     * Returns a new instance of {@link Router} linked to this application.
     * 
     * @return A new instance of {@link Router}.
     */
    public Router newRouter() {
        final Application app = this;
        return new Router(getContext()) {
            @Override
            public Application getApplication() {
                return app;
            }
        };
    }
}
