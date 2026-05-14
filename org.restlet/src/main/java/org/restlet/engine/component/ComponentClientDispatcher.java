/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
 * <p>Concurrency note: instances of this class or its subclasses can be invoked by several threads
 * at the same time and therefore must be thread-safe. You should be especially careful when storing
 * state as member variables.
 *
 * @author Jerome Louvel
 */
public class ComponentClientDispatcher extends TemplateDispatcher {
    /** The component context. */
    private final ComponentContext componentContext;

    /**
     * Constructor.
     *
     * @param componentContext The component context.
     */
    public ComponentClientDispatcher(ComponentContext componentContext) {
        this.componentContext = componentContext;
    }

    @Override
    protected int doHandle(Request request, Response response) {
        int result = CONTINUE;

        if (Protocol.RIAP.equals(request.getProtocol())) {
            result = doHandleRiapRequest(request, response);
        } else {
            getComponentContext().getComponentHelper().getClientRouter().handle(request, response);
        }

        return result;
    }

    private int doHandleRiapRequest(final Request request, final Response response) {
        Component component = getComponent();
        if (component == null) {
            getLogger().warning("No component is available to route the RIAP request.");
            return STOP;
        }

        // Let's dispatch it
        final int result;

        LocalReference cr = new LocalReference(request.getResourceRef());
        if (cr.getRiapAuthorityType() == LocalReference.RIAP_COMPONENT) {
            result = doHandleRiapComponentRequest(component, request, response);
        } else if (cr.getRiapAuthorityType() == LocalReference.RIAP_HOST) {
            result = doHandleRiapHostRequest(component, request, response);
        } else {
            getLogger()
                    .warning(
                            "Unknown RIAP authority. Only \"component\" and \"host\" are supported: "
                                    + cr.getRiapAuthorityType());
            result = STOP;
        }
        return result;
    }

    private int doHandleRiapHostRequest(
            final Component component, final Request request, final Response response) {
        int result = CONTINUE;

        VirtualHost host = null;
        VirtualHost currentHost;

        final Integer hostHashCode = VirtualHost.getCurrent();

        // Look up the virtual host
        for (final Iterator<VirtualHost> hostIter = component.getHosts().iterator();
                (host == null) && hostIter.hasNext(); ) {
            currentHost = hostIter.next();

            if (currentHost.hashCode() == hostHashCode) {
                host = currentHost;
            }
        }

        if ((host == null)
                && (component.getDefaultHost() != null)
                && component.getDefaultHost().hashCode() == hostHashCode) {
            host = component.getDefaultHost();
        }

        if (host != null) {
            // This causes the baseRef of the resource reference to
            // be set as if it had actually arrived from a server
            // connector.
            request.getResourceRef().setBaseRef(request.getResourceRef().getHostIdentifier());

            // Ask the virtual host to handle the call
            host.handle(request, response);
        } else {
            getLogger().warning("No virtual host is available to route the RIAP Host request.");
            result = STOP;
        }
        return result;
    }

    private static int doHandleRiapComponentRequest(
            final Component component, final Request request, final Response response) {
        // This causes the baseRef of the resource reference to be
        // set as if it had actually arrived from a server
        // connector.
        request.getResourceRef().setBaseRef(request.getResourceRef().getHostIdentifier());

        // Ask the private internal route to handle the call
        component.getInternalRouter().handle(request, response);

        return CONTINUE;
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
