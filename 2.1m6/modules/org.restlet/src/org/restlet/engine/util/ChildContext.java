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

package org.restlet.engine.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.engine.log.LogUtils;

/**
 * Context based on a parent component's context but dedicated to a child
 * Restlet, typically to an application.
 * 
 * @author Jerome Louvel
 */
public class ChildContext extends Context {

    /** The child delegate, typically an application. */
    private volatile Restlet child;

    /** The parent context. */
    private volatile Context parentContext;

    /**
     * Constructor.
     * 
     * @param parentContext
     *            The parent context.
     */
    public ChildContext(Context parentContext) {
        this.child = null;
        this.parentContext = parentContext;
        setClientDispatcher(new ChildClientDispatcher(this));
        setServerDispatcher((parentContext != null) ? getParentContext()
                .getServerDispatcher() : null);
    }

    /**
     * Returns the child.
     * 
     * @return the child.
     */
    public Restlet getChild() {
        return this.child;
    }

    /**
     * Returns the parent context.
     * 
     * @return The parent context.
     */
    protected Context getParentContext() {
        return this.parentContext;
    }

    /**
     * Sets the child.
     * 
     * @param child
     *            The child.
     */
    public void setChild(Restlet child) {
        this.child = child;
        setLogger(LogUtils.getLoggerName(this.parentContext.getLogger()
                .getName(), child));
    }

}
