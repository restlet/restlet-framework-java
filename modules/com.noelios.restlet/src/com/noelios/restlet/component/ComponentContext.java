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

import org.restlet.Component;
import org.restlet.Context;

/**
 * Context allowing access to the component's connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentContext extends Context {
    /**
     * Returns a non-null logger name.
     * 
     * @param component
     *            The component.
     * @return The logger name.
     */
    private static String getLoggerName(Component component) {
        String result = component.getClass().getCanonicalName();
        if (result == null) {
            result = "org.restlet.component";
        }

        result += "#" + component.hashCode();

        return result;
    }

    /** The client dispatcher. */
    private volatile ComponentClientDispatcher clientDispatcher;

    /** The component helper. */
    private volatile ComponentHelper componentHelper;

    /** The server dispatcher. */
    private volatile ComponentServerDispatcher serverDispatcher;

    /**
     * Constructor.
     * 
     * @param componentHelper
     *            The component helper.
     * @param logger
     *            The logger instance of use.
     */
    public ComponentContext(ComponentHelper componentHelper) {
        super(getLoggerName(componentHelper.getHelped()));
        this.componentHelper = componentHelper;
        this.clientDispatcher = new ComponentClientDispatcher(this);
        this.serverDispatcher = new ComponentServerDispatcher(this);
    }

    @Override
    public Context createChildContext() {
        return new ChildContext(null, getComponentHelper().getHelped()
                .getContext());
    }

    @Override
    public ComponentClientDispatcher getClientDispatcher() {
        return this.clientDispatcher;
    }

    /**
     * Returns the component helper.
     * 
     * @return The component helper.
     */
    protected ComponentHelper getComponentHelper() {
        return this.componentHelper;
    }

    @Override
    public ComponentServerDispatcher getServerDispatcher() {
        return this.serverDispatcher;
    }

    /**
     * Sets the component helper.
     * 
     * @param componentHelper
     *            The component helper.
     */
    protected void setComponentHelper(ComponentHelper componentHelper) {
        this.componentHelper = componentHelper;
    }
}
