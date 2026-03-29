/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.engine.log.LogUtils;

/**
 * Context based on a parent component's context but dedicated to a child Restlet, typically to an
 * application.
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
     * @param parentContext The parent context.
     */
    public ChildContext(Context parentContext) {
        this.child = null;
        this.parentContext = parentContext;
        setClientDispatcher(new ChildClientDispatcher(this));

        if (parentContext == null) {
            setServerDispatcher(null);
            setExecutorService(null);
        } else {
            setServerDispatcher(parentContext.getServerDispatcher());
            setExecutorService(
                    (parentContext.getExecutorService() != null)
                            ? new WrapperScheduledExecutorService(
                                    parentContext.getExecutorService())
                            : null);
        }
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
     * @param child The child.
     */
    public void setChild(Restlet child) {
        this.child = child;
        setLogger(LogUtils.getLoggerName(this.parentContext.getLogger().getName(), child));
    }
}
