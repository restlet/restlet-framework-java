/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.oauth;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.ext.oauth.internal.MemClientStore;

/**
 * Factory for ClientStore. The Default is to create a memory based client store
 * This class uses reflection and even an non empty constructor is possible. By
 * default it will instantiate a memory backed client store with empty
 * constructor. Example :
 * 
 * <pre>
 * {
 *     &#064;code
 *     Object[] params = { &quot;http://www.restlet.org&quot; };
 *     ClientStoreFactory.setClientStoreImpl(MyClientStore.class, params);
 *     ClientStore clientStore = ClientStoreFactory.getInstance();
 * }
 * </pre>
 * 
 * In the example the class MyClientStore would have a public constructor that
 * accepts a String parameter.
 * 
 * @author Kristoffer Gronowski
 */
public abstract class ClientStoreFactory {

    private static volatile Class<? extends ClientStore<?>> defaultImpl = MemClientStore.class;

    private static volatile Object[] params = {};

    private static volatile ClientStore<?> store;

    private static volatile Logger log;

    private ClientStoreFactory() {
    }

    /**
     * Creates an instance of ClientStore. It is a singelton so multiple calls
     * would just retrieve an earlier created instance.
     * 
     * @return an implementation of a ClientStore
     */
    public synchronized static ClientStore<?> getInstance() {
        if (log == null) {
            log = Context.getCurrentLogger();
        }

        if (store == null) {

            Class<?>[] classTypes = new Class[params.length];

            int i = 0;

            for (Object o : params) {
                classTypes[i++] = o.getClass();
            }

            try {
                Constructor<? extends ClientStore<?>> c = defaultImpl
                        .getConstructor(classTypes);
                store = c.newInstance(params);

            } catch (SecurityException e) {
                log.log(Level.SEVERE,
                        "Failed to initialize OAuth Data backend!", e);
            } catch (NoSuchMethodException e) {
                log.log(Level.SEVERE,
                        "Failed to initialize OAuth Data backend!", e);
            } catch (IllegalArgumentException e) {
                log.log(Level.SEVERE,
                        "Failed to initialize OAuth Data backend!", e);
            } catch (InstantiationException e) {
                log.log(Level.SEVERE,
                        "Failed to initialize OAuth Data backend!", e);
            } catch (IllegalAccessException e) {
                log.log(Level.SEVERE,
                        "Failed to initialize OAuth Data backend!", e);
            } catch (InvocationTargetException e) {
                log.log(Level.SEVERE,
                        "Failed to initialize OAuth Data backend!", e);
            }
        }
        return store;
    }

    /**
     * This method sets up if the implementing class uses a no-arg public
     * constructor
     * 
     * @param impl
     *            class reference of a class implementing ClientStore
     */
    public static void setClientStoreImpl(Class<? extends ClientStore<?>> impl) {
        defaultImpl = impl;
        Object[] dummy = null;
        setClientStoreImpl(impl, dummy);
    }

    /**
     * @param impl
     *            class reference of a class implementing ClientStore
     * @param constructorParams
     *            array of constructor arguments.
     */
    public static void setClientStoreImpl(Class<? extends ClientStore<?>> impl,
            Object... constructorParams) {
        if (store != null && !store.getClass().equals(impl)) {
            throw new IllegalStateException(
                    "Can't change the type of store once it already initialized.");
        }

        defaultImpl = impl;
        params = constructorParams;
    }

}
