package org.restlet.engine.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *  Utilities to get the {@link BeanInfo} of a class.
 *
 * @author Manuel Boillod
 */
public class BeanInfoUtils {

    /** BeanInfo cache. */
    private static final ConcurrentMap<Class<?>, BeanInfo> cache = new ConcurrentHashMap<Class<?>, BeanInfo>();

    /**
     * Get a BeanInfo from cache or create it.
     * Stop introspection to {@link Object} or {@link Throwable} if the class
     * is a subtype of {@link Throwable}
     *
     * @param clazz
     *          The class
     * @return BeanInfo of the class
     */
    public static BeanInfo getBeanInfo(Class<?> clazz) {
        BeanInfo result = cache.get(clazz);

        if (result == null) {
            // Inspect the class itself for annotations

            Class<?> stopClass = Throwable.class.isAssignableFrom(clazz) ? Throwable.class : Object.class;
            try {
                result = Introspector.getBeanInfo(clazz, stopClass, Introspector.IGNORE_ALL_BEANINFO);
            } catch (IntrospectionException e) {
                throw new RuntimeException("Could not get BeanInfo of class " + clazz.getName(), e);
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
