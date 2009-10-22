/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.netty.internal;

import java.lang.reflect.Constructor;

import org.jboss.netty.buffer.ChannelBufferFactory;

/**
 * <p>
 * Helper class that convert from Restlet opstions to Netty channel options.
 * </p>
 * 
 * @author Gabriel Ciuloaica (gciuloaica@gmail.com)
 */
public enum NettyParams {
    keepAlive(Boolean.class), bufferFactoryClass(ChannelBufferFactory.class), connectTimeoutMillis(
            Integer.class), reuseAddress(Boolean.class), receiveBufferSize(
            Integer.class), sendBufferSize(Integer.class), trafficClass(
            Integer.class);

    /** The parameter type class. */
    private final Class<?> paramType;

    /**
     * Private constructor.
     * 
     * @param type
     *            The parameter type class.
     */
    private NettyParams(Class<?> type) {
        this.paramType = type;
    }

    /**
     * <p>
     * Get converted value from a string option value.
     * </p>
     * 
     * @param value
     *            - option value
     * @return Object instance
     */
    public Object getValue(String value) {
        Object ret = null;
        try {
            Constructor<?> constructor = paramType.getConstructor(String.class);
            ret = constructor.newInstance(value);
        } catch (Exception e) {

        }

        if (ret == null) {
            ret = instantiateClass(value);
        }

        return ret;

    }

    /**
     * <p>
     * Instantiate the class specified in the option value.
     * </p>
     * 
     * @param value
     *            - fully qualified class name.
     * @return class instance
     */
    private Object instantiateClass(String value) {
        Object ret = null;

        Class<?> factory = null;
        try {
            factory = getClass().getClassLoader().loadClass(value);

        } catch (ClassNotFoundException e) {

        }
        if (factory != null) {
            try {
                Constructor<?> constructor = factory.getConstructor();
                final Object ref = constructor.newInstance();
                if (ref instanceof ChannelBufferFactory) {
                    ret = ref;
                }
            } catch (Exception e) {
            }
        }

        return ret;
    }

}
