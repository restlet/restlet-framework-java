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

package org.restlet.engine.component;

import java.util.Iterator;

import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.engine.util.TemplateDispatcher;
import org.restlet.routing.VirtualHost;

/**
 * Component client dispatcher.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel
 */
public class ComponentClientDispatcher extends TemplateDispatcher {
    /** The component context. */
    private ComponentContext componentContext;

    /**
     * Constructor.
     * 
     * @param componentContext
     *            The component context.
     */
    public ComponentClientDispatcher(ComponentContext componentContext) {
        this.componentContext = componentContext;
    }

    @Override
    protected int doHandle(Request request, Response response) {
        int result = CONTINUE;
        Protocol protocol = request.getProtocol();

        if (protocol.equals(Protocol.RIAP)) {
            // Let's dispatch it
            LocalReference cr = new LocalReference(request.getResourceRef());
            Component component = getComponent();

            if (component != null) {
                if (cr.getRiapAuthorityType() == LocalReference.RIAP_COMPONENT) {
                    // This causes the baseRef of the resource reference to be
                    // set as if it had actually arrived from a server
                    // connector.
                    request.getResourceRef().setBaseRef(
                            request.getResourceRef().getHostIdentifier());

                    // Ask the private internal route to handle the call
                    component.getInternalRouter().handle(request, response);
                } else if (cr.getRiapAuthorityType() == LocalReference.RIAP_HOST) {
                    VirtualHost host = null;
                    VirtualHost currentHost = null;
                    final Integer hostHashCode = VirtualHost.getCurrent();

                    // Lookup the virtual host
                    for (final Iterator<VirtualHost> hostIter = getComponent()
                            .getHosts().iterator(); (host == null)
                            && hostIter.hasNext();) {
                        currentHost = hostIter.next();

                        if (currentHost.hashCode() == hostHashCode) {
                            host = currentHost;
                        }
                    }

                    if ((host == null) && (component.getDefaultHost() != null)) {
                        if (component.getDefaultHost().hashCode() == hostHashCode) {
                            host = component.getDefaultHost();
                        }
                    }

                    if (host != null) {
                        // This causes the baseRef of the resource reference to
                        // be set as if it had actually arrived from a server
                        // connector.
                        request.getResourceRef().setBaseRef(
                                request.getResourceRef().getHostIdentifier());

                        // Ask the virtual host to handle the call
                        host.handle(request, response);
                    } else {
                        getLogger()
                                .warning(
                                        "No virtual host is available to route the RIAP Host request.");
                        result = STOP;
                    }
                } else {
                    getLogger()
                            .warning(
                                    "Unknown RIAP authority. Only \"component\" is supported.");
                    result = STOP;
                }
            } else {
                getLogger().warning(
                        "No component is available to route the RIAP request.");
                result = STOP;
            }
        } else {
            getComponentContext().getComponentHelper().getClientRouter()
                    .handle(request, response);
        }

        return result;
    }

    /**
     * Returns the parent component.
     * 
     * @return The parent component.
     */
    private Component getComponent() {
        Component result = null;

        if ((getComponentContext() != null)
                && (getComponentContext().getComponentHelper() != null)) {
            result = getComponentContext().getComponentHelper().getHelped();
        }

        return result;

    }

    /**
     * Returns the component context.
     * 
     * @return The component context.
     */
    private ComponentContext getComponentContext() {
        return componentContext;
    }
}
