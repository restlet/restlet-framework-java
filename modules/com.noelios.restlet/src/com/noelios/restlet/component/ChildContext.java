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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.component;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Uniform;

/**
 * Context based on a parent component's context but dedicated to a child
 * Restlet, typically to an application.
 * 
 * @author Jerome Louvel
 */
public class ChildContext extends Context {

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

    /** The child delegate, typically an application. */
    private volatile Restlet child;

    /** The client dispatcher. */
    private volatile ChildClientDispatcher clientDispatcher;

    /** The parent context. */
    private volatile Context parentContext;

    /** The server dispatcher. */
    private volatile Uniform serverDispatcher;

    /**
     * Constructor.
     * 
     * @param parentContext
     *            The parent context.
     */
    public ChildContext(Context parentContext) {
        this.child = null;
        this.parentContext = parentContext;
        this.clientDispatcher = new ChildClientDispatcher(this);
        this.serverDispatcher = (getParentContext() != null) ? getParentContext()
                .getServerDispatcher()
                : null;
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

    @Override
    public ChildClientDispatcher getClientDispatcher() {
        return this.clientDispatcher;
    }

    /**
     * Returns the parent context.
     * 
     * @return The parent context.
     */
    protected Context getParentContext() {
        return this.parentContext;
    }

    @Override
    public Uniform getServerDispatcher() {
        return this.serverDispatcher;
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
