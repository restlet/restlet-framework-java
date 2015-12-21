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

import org.restlet.Context;
import org.restlet.engine.log.LogUtils;
import org.restlet.engine.util.ChildContext;

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
        super(LogUtils
                .getLoggerName("org.restlet", componentHelper.getHelped()));
        this.componentHelper = componentHelper;
        setClientDispatcher(new ComponentClientDispatcher(this));
        setServerDispatcher(new ComponentServerDispatcher(this));
        // [ifndef gae] instruction
        setExecutorService(componentHelper.getHelped().getTaskService());
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
