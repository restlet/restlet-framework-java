/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 *
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *  Utilities to serialize {@link Throwable}.
 *
 * @author Manuel Boillod
 */
public class ThrowableSerializer {

    /** BeanInfo cache. */
    private static final ConcurrentMap<Class<?>, BeanInfo> cache = new ConcurrentHashMap<Class<?>, BeanInfo>();

    /**
     * Serialize {@link Throwable} properties to a Map using reflection.
     * The properties of {@link Throwable} class are ignored except if
     * they are overriden.
     *
     * @param throwable
     *          {@link Throwable} instance to serialize.
     *
     * @return A map with the @link Throwable} subclasses properties.
     */
    public static Map<String, Object> serializeToMap(Throwable throwable) {
        try {
            BeanInfo beanInfo = getBeanInfo(throwable);
            Map<String, Object> properties = new HashMap<String, Object>();

            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                properties.put(
                        propertyDescriptor.getName(),
                        propertyDescriptor.getReadMethod().invoke(throwable));
            }
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("Could not serialize properties of class " + throwable.getCause(), e);
        }
    }

    /**
     * Get a BeanInfo from cache or create it.
     * @param throwable
     *          Throwable instance
     * @return BeanInfo of throwable class
     */
    private static BeanInfo getBeanInfo(Throwable throwable) throws IntrospectionException {
        BeanInfo result = cache.get(throwable.getClass());

        if (result == null) {
            // Inspect the class itself for annotations
            result = Introspector.getBeanInfo(throwable.getClass(), Throwable.class, Introspector.IGNORE_ALL_BEANINFO);

            // Put the list in the cache if no one was previously present
            BeanInfo prev = cache.putIfAbsent(throwable.getClass(), result);

            if (prev != null) {
                // Reuse the previous entry
                result = prev;
            }
        }

        return result;
    }

    ;
}
