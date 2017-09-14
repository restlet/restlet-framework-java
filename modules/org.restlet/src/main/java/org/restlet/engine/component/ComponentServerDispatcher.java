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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.util.TemplateDispatcher;

/**
 * Component server dispatcher.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel
 */
public class ComponentServerDispatcher extends TemplateDispatcher {

    /** The component context. */
    private ComponentContext componentContext;

    /**
     * Constructor.
     * 
     * @param componentContext
     *            The component context.
     */
    public ComponentServerDispatcher(ComponentContext componentContext) {
        this.componentContext = componentContext;
    }

    @Override
    public int beforeHandle(Request request, Response response) {
        int result = super.beforeHandle(request, response);

        // This causes the baseRef of the resource reference to be set
        // as if it had actually arrived from a server connector.
        request.getResourceRef().setBaseRef(
                request.getResourceRef().getHostIdentifier());

        return result;
    }

    @Override
    protected int doHandle(Request request, Response response) {
        int result = CONTINUE;
        // Ask the server router to actually handle the call
        getComponentContext().getComponentHelper().getServerRouter()
                .handle(request, response);

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
