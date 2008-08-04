/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.component;

import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.TemplateDispatcher;

/**
 * Component server dispatcher.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state as member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentServerDispatcher extends TemplateDispatcher {
    /**
     * Constructor.
     * 
     * @param context
     *            The component context.
     */
    public ComponentServerDispatcher(ComponentContext context) {
        super(context);
    }

    @Override
    protected void doHandle(Request request, Response response) {
        super.doHandle(request, response);

        // This causes the baseRef of the resource reference to be set
        // as if it had actually arrived from a server connector.
        request.getResourceRef().setBaseRef(
                request.getResourceRef().getHostIdentifier());

        // Ask the server router to actually handle the call
        getComponentContext().getComponentHelper().getServerRouter().handle(
                request, response);
    }

    /**
     * Returns the component context.
     * 
     * @return The component context.
     */
    private ComponentContext getComponentContext() {
        return (ComponentContext) getContext();
    }

}
