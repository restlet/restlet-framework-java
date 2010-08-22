/**
 * Copyright 2005-2010 Noelios Technologies.
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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Generic object pool.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 */
public abstract class Pool<T> {

    /** Store of reusable objects. */
    private final Queue<T> store;

    /**
     * Default constructor. Sets the minimum size to 0.
     */
    public Pool() {
        this(0);
    }

    /**
     * Constructor. Pre-creates the minimum number of objects if needed using
     * the {@link #createObject()} method.
     * 
     * @param initialSize
     *            The initial number of objects in the pool.
     */
    public Pool(int initialSize) {
        this.store = createStore();

        // Pre-create the initial number object and add them to the pool
        for (int i = 0; i < initialSize; i++) {
            checkin(createObject());
        }
    }

    /**
     * Checks in an object into the pool.
     * 
     * @param object
     *            The object to check in.
     */
    public void checkin(T object) {
        if (object != null) {
            recycle(object);
            this.store.offer(object);
        }
    }

    /**
     * Checks out an object from the pool. Creates a new one if the pool is
     * empty.
     * 
     * @return An object from the pool.
     */
    public T checkout() {
        T result;

        if ((result = this.store.poll()) == null) {
            result = createObject();
        }

        return result;
    }

    /**
     * Creates a new reusable object.
     * 
     * @return A new reusable object.
     */
    protected abstract T createObject();

    /**
     * Creates the store of reusable objects.
     * 
     * @return The store of reusable objects.
     */
    protected Queue<T> createStore() {
        return new ConcurrentLinkedQueue<T>();
    }

    /**
     * Returns the store containing the reusable objects.
     * 
     * @return The store containing the reusable objects.
     */
    protected Queue<T> getStore() {
        return store;
    }

    /**
     * Recycle the given object when it is checked in the pool. Does nothing by
     * default.
     * 
     * @param object
     *            The object to recycle.
     */
    protected void recycle(T object) {

    }

}
