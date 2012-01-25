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

package org.restlet.ext.netty.internal;

import java.lang.reflect.Constructor;

import org.jboss.netty.buffer.ChannelBufferFactory;

/**
 * Helper class that convert from Restlet options to Netty channel options.
 * 
 * @author Gabriel Ciuloaica (gciuloaica@gmail.com)
 */
public enum NettyParams {
    keepAlive(Boolean.class, true), bufferFactoryClass(
            ChannelBufferFactory.class, true), connectTimeoutMillis(
            Integer.class, true), reuseAddress(Boolean.class, true), receiveBufferSize(
            Integer.class, true), sendBufferSize(Integer.class, true), trafficClass(
            Integer.class, true), sslContextFactory(String.class, false), keystorePath(
            String.class, false), keystorePassword(String.class, false), keyPassword(
            String.class, false), keystoreType(String.class, false), truststorePath(
            String.class, false), truststorePassword(String.class, false), certAlgorithm(
            String.class, false), sslProtocol(String.class, false), needClientAuthentication(
            String.class, false), wantClientAuthentication(String.class, false);

    /** The parameter type class. */
    private final Class<?> paramType;

    /** Is the parameter a channel configuration option? */
    private final Boolean isChannelOption;

    /**
     * Private constructor.
     * 
     * @param type
     *            The parameter type class.
     * @param isChannelParam
     *            The parameter is channel parameter or not.
     */
    private NettyParams(Class<?> type, Boolean isChannelParam) {
        this.paramType = type;
        this.isChannelOption = isChannelParam;
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

    /**
     * Is this a channel option or not?
     * 
     * @return true if the parameter is a channel option or false otherwise
     */
    public Boolean isChannelOption() {
        return isChannelOption;
    }

}
