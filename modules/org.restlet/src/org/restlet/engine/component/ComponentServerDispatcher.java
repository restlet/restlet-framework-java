/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.component;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.TemplateDispatcher;

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
    /**
     * Constructor.
     * 
     * @param componentContext
     *            The component context.
     */
    public ComponentServerDispatcher(ComponentContext componentContext) {
        super(componentContext);
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
