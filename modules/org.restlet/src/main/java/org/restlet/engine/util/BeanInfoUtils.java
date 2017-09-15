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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utilities to get the {@link BeanInfo} of a class.
 * 
 * @author Manuel Boillod
 */
public class BeanInfoUtils {

    /** BeanInfo cache. */
    private static final ConcurrentMap<Class<?>, BeanInfo> cache = new ConcurrentHashMap<Class<?>, BeanInfo>();

    /**
     * Get a BeanInfo from cache or create it. Stop introspection to
     * {@link Object} or {@link Throwable} if the class is a subtype of
     * {@link Throwable}
     * 
     * @param clazz
     *            The class
     * @return BeanInfo of the class
     */
    public static BeanInfo getBeanInfo(Class<?> clazz) {
        BeanInfo result = cache.get(clazz);

        if (result == null) {
            // Inspect the class itself for annotations

            Class<?> stopClass = Throwable.class.isAssignableFrom(clazz) ? Throwable.class
                    : Object.class;
            try {
                result = Introspector.getBeanInfo(clazz, stopClass,
                        Introspector.IGNORE_ALL_BEANINFO);
            } catch (IntrospectionException e) {
                throw new RuntimeException("Could not get BeanInfo of class "
                        + clazz.getName(), e);
            }

            // Put the list in the cache if no one was previously present
            BeanInfo prev = cache.putIfAbsent(clazz, result);

            if (prev != null) {
                // Reuse the previous entry
                result = prev;
            }
        }

        return result;
    }
}
