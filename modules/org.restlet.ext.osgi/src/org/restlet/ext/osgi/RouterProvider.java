/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.osgi;

import java.util.HashSet;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;

/**
 * @author Bryan Hunt
 * 
 */
public class RouterProvider extends RestletProvider implements IRouterProvider {
    private IRestletProvider defaultRestletProvider;

    private HashSet<IDirectoryProvider> directoryProviders = new HashSet<IDirectoryProvider>();

    private HashSet<IResourceProvider> resourceProviders = new HashSet<IResourceProvider>();

    private Router router;

    private void attachDirectory(IDirectoryProvider directoryProvider) {
        router.attach(directoryProvider.getPath(),
                directoryProvider.getInboundRoot(router.getContext()));
    }

    private void attachResource(IResourceProvider resourceProvider) {
        for (String path : resourceProvider.getPaths()) {
        	TemplateRoute templateRoute = router.attach(path, resourceProvider.getInboundRoot(router.getContext()));
        	templateRoute.setMatchingMode(resourceProvider.getMatchingMode());
        }
    }

    public void bindDefaultResourceProvider(IResourceProvider resourceProvider) {
        defaultRestletProvider = resourceProvider;

        if (router != null)
            router.attachDefault(resourceProvider.getInboundRoot(router
                    .getContext()));
    }

    public void bindDefaultRouterProvider(IRouterProvider routerProvider) {
        defaultRestletProvider = routerProvider;

        if (router != null)
            router.attachDefault(routerProvider.getInboundRoot(router
                    .getContext()));
    }

    public void bindDirectoryProvider(IDirectoryProvider directoryProvider) {
        directoryProviders.add(directoryProvider);

        if (router != null)
            attachDirectory(directoryProvider);
    }

    public void bindResourceProvider(IResourceProvider resourceProvider) {
        resourceProviders.add(resourceProvider);

        if (router != null)
            attachResource(resourceProvider);
    }

    protected Router createRouter(Context context) {
        return new Router(context);
    }

    @Override
    protected Restlet getFilteredRestlet() {
        return router;
    }

    @Override
    public Restlet getInboundRoot(Context context) {
        if (router == null) {
            router = createRouter(context);

            for (IResourceProvider resourceProvider : resourceProviders)
                attachResource(resourceProvider);

            for (IDirectoryProvider directoryProvider : directoryProviders)
                attachDirectory(directoryProvider);

            if (defaultRestletProvider != null)
                router.attachDefault(defaultRestletProvider
                        .getInboundRoot(context));
        }

        Restlet inboundRoot = super.getInboundRoot(context);
        return inboundRoot != null ? inboundRoot : router;
    }

    public void unbindDefaultResourceProvider(IResourceProvider resourceProvider) {
        if (defaultRestletProvider == resourceProvider) {
            defaultRestletProvider = null;

            if (router != null)
                router.detach(resourceProvider.getInboundRoot(router
                        .getContext()));
        }
    }

    public void unbindDefaultRouterProvider(IRouterProvider routerProvider) {
        if (defaultRestletProvider == routerProvider) {
            defaultRestletProvider = routerProvider;

            if (router != null)
                router.detach(routerProvider.getInboundRoot(router.getContext()));
        }
    }

    public void unbindDirectoryProvider(IDirectoryProvider directoryProvider) {
        if (directoryProviders.remove(directoryProvider)) {
            if (router != null)
                router.detach(directoryProvider.getInboundRoot(router
                        .getContext()));
        }
    }

    public void unbindResourceProvider(IResourceProvider resourceProvider) {
        if (resourceProviders.remove(resourceProvider)) {
            if (router != null)
                router.detach(resourceProvider.getInboundRoot(router
                        .getContext()));
        }
    }
}
