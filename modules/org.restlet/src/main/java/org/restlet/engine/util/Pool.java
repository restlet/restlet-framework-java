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
     * Default constructor.
     */
    public Pool() {
        this.store = createStore();
    }

    /**
     * Constructor. Pre-creates the minimum number of objects if needed using
     * the {@link #preCreate(int)} method.
     * 
     * @param initialSize
     *            The initial number of objects in the pool.
     */
    public Pool(int initialSize) {
        this();
        preCreate(initialSize);
    }

    /**
     * Checks in an object into the pool.
     * 
     * @param object
     *            The object to check in.
     */
    public void checkin(T object) {
        if (object != null) {
            clear(object);
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
     * Clears the store of reusable objects.
     */
    public void clear() {
        getStore().clear();
    }

    /**
     * Clears the given object when it is checked in the pool. Does nothing by
     * default.
     * 
     * @param object
     *            The object to clear.
     */
    protected void clear(T object) {

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
     * Pre-creates the initial objects using the {@link #createObject()} method
     * and check them in the pool using the {@link #checkin(Object)} method.
     * 
     * @param initialSize
     *            The initial number of objects.
     */
    public void preCreate(int initialSize) {
        for (int i = 0; i < initialSize; i++) {
            checkin(createObject());
        }
    }

}
