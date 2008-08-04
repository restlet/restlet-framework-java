/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.component;

import java.util.logging.Logger;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Uniform;

import com.noelios.restlet.TemplateDispatcher;

/**
 * Context allowing access to the component's connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentContext extends Context {
    /** The component helper. */
    private ComponentHelper componentHelper;

    /**
     * Constructor.
     * 
     * @param componentHelper
     *            The component helper.
     */
    public ComponentContext(ComponentHelper componentHelper) {
        this(componentHelper, Logger.getLogger(Component.class
                .getCanonicalName()));
    }

    /**
     * Constructor.
     * 
     * @param componentHelper
     *            The component helper.
     * @param logger
     *            The logger instance of use.
     */
    public ComponentContext(ComponentHelper componentHelper, Logger logger) {
        super(logger);
        this.componentHelper = componentHelper;
    }

    /**
     * Returns a call dispatcher.
     * 
     * @return A call dispatcher.
     */
	@Override
    public Uniform getDispatcher() {
        return new TemplateDispatcher(this, getComponentHelper().getClientRouter());
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
