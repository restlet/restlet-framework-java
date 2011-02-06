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

import org.restlet.Context;

/**
 * Context allowing access to the component's connectors.
 * 
 * @author Jerome Louvel
 */
public class ComponentContext extends Context {

    /** The component helper. */
    private volatile ComponentHelper componentHelper;

    /**
     * Constructor.
     * 
     * @param componentHelper
     *            The component helper.
     */
    public ComponentContext(ComponentHelper componentHelper) {
        super(ChildContext.getLoggerName("org.restlet", componentHelper
                .getHelped()));
        this.componentHelper = componentHelper;
        setClientDispatcher(new ComponentClientDispatcher(this));
        setServerDispatcher(new ComponentServerDispatcher(this));
    }

    @Override
    public Context createChildContext() {
        return new ChildContext(getComponentHelper().getHelped().getContext());
    }

    /**
     * Returns the component helper.
     * 
     * @return The component helper.
     */
    protected ComponentHelper getComponentHelper() {
        return this.componentHelper;
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
