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

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;

/**
 * Context based on a parent component's context but dedicated to a child
 * Restlet, typically to an application.
 * 
 * @author Jerome Louvel
 */
public class ChildContext extends Context {

    /**
     * Indicates that a Restlet's context has changed.
     * 
     * @param restlet
     *            The Restlet with a changed context.
     * @param context
     *            The new context.
     */
    public static void fireContextChanged(Restlet restlet, Context context) {
        if (context != null) {
            if (context instanceof ChildContext) {
                ChildContext childContext = (ChildContext) context;

                if (childContext.getChild() == null) {
                    childContext.setChild(restlet);
                }
            } else if (!(restlet instanceof Component)
                    && (context instanceof ComponentContext)) {
                context
                        .getLogger()
                        .severe(
                                "For security reasons, don't pass the component context to child Restlets anymore. Use the Context#createChildContext() method instead."
                                        + restlet.getClass());
            }
        }
    }

    /**
     * Return the best class name. If the class is anonymous, then it returns
     * the super class name.
     * 
     * @param clazz
     *            The class to name.
     * @return The class name.
     */
    public static String getBestClassName(Class<?> clazz) {
        String result = clazz.getSimpleName();

        if ((result == null) || (result.equals(""))) {
            result = getBestClassName(clazz.getSuperclass());
        }

        return result;
    }

    /**
     * Returns a non-null logger name. It is composed by the canonical class
     * name of the owner object suffixed by the owner's hash code.
     * 
     * @param baseName
     *            The base logger name to prepend, without a trailing dot.
     * @param owner
     *            The context owner.
     * @return The logger name.
     */
    public static String getLoggerName(String baseName, Object owner) {
        String result = baseName;

        if ((owner != null) && (owner.getClass().getSimpleName() != null)) {
            result += "." + getBestClassName(owner.getClass());
        }

        return result;
    }

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
        setServerDispatcher((getParentContext() != null) ? getParentContext()
                .getServerDispatcher() : null);
    }

    @Override
    public Context createChildContext() {
        return new ChildContext(this);
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
        setLogger(getLoggerName(this.parentContext.getLogger().getName(), child));
    }

}
