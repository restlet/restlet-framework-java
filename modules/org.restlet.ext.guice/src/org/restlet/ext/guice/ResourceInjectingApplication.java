package org.restlet.ext.guice;

import javax.inject.Inject;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;


/**
 * Application with support for creating Router instances that
 * arrange for member injection of resource instances.
 */
public abstract class ResourceInjectingApplication extends Application {

    /**
     * Wraps a Finder returned by createFinder to do member injection using
     * the passed in MembersInjector.
     */
    public static Finder wrapFinderWithMemberInjection(
            final Finder finder,
            final SelfInjectingServerResource.MembersInjector membersInjector
    ) {
        return new Finder(finder.getContext(), finder.getTargetClass()) {
            @Override public ServerResource find(Request request, Response response) {
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

    public Router newRouter() {
        final Application app = this;
        return new Router(getContext()) {
            @Override public Application getApplication() {
                return app;
            }
        };
    }

    @Override public Finder createFinder(Class<? extends ServerResource> targetClass) {
        Finder finder = super.createFinder(targetClass);
        return wrapFinderWithMemberInjection(finder, membersInjector);
    }

    @Inject private volatile SelfInjectingServerResource.MembersInjector membersInjector;
}
