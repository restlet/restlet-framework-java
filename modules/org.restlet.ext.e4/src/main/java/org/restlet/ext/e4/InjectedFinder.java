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

package org.restlet.ext.e4;

import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.e4.internal.Activator;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * @author Bryan Hunt
 * @deprecated Usage of Guice or Spring extensions is recommended instead.
 */
@Deprecated
public class InjectedFinder extends Finder {

    private static ReentrantLock diLock = new ReentrantLock();

    private IEclipseContext serviceContext;

    public InjectedFinder(Context context,
            Class<? extends ServerResource> targetClass) {
        super(context, targetClass);
        serviceContext = EclipseContextFactory.getServiceContext(Activator
                .getContext());
    }

    @Override
    public ServerResource create(Class<? extends ServerResource> clazz,
            Request request, Response response) {
        IEclipseContext childContext = serviceContext
                .createChild("ResourceContext");
        diLock.lock(); // The lock is required because
                       // ContextInjectionFactory.make() is not thread safe

        try {
            InjectedResource serverResource = (InjectedResource) ContextInjectionFactory
                    .make(clazz, childContext);
            serverResource.setEclipseContext(childContext);
            return (ServerResource) serverResource;
        } finally {
            diLock.unlock();
        }
    }
}
