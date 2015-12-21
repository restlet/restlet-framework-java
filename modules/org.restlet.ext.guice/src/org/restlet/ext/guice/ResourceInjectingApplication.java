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

import javax.inject.Inject;

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
 */
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
