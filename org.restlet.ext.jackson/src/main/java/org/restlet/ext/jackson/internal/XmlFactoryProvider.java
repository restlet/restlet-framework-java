/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jackson.internal;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.codehaus.stax2.osgi.Stax2InputFactoryProvider;
import org.codehaus.stax2.osgi.Stax2OutputFactoryProvider;

/**
 * Provides {@link javax.xml.stream.XMLInputFactory}
 * and {@link javax.xml.stream.XMLOutputFactory}
 * in an OSGI context.
 *
 * In a no-OSGI context, the factories are retrieved with java service loader.
 *
 * @author Manuel Boillod
 */
public class XmlFactoryProvider {

    /**
     * Allow to explicitly set the Stax2InputFactory instance in OSGI context.
     * In a no-OSGI context, the factory is retrieved with java service loader.
     *
     * <p>Note: Stax2 implementation is provided by woodstox library which
     * is a dependency of Jackson.</p>
     *
     * @see org.restlet.ext.jackson.internal.Activator
     */
    public static Stax2InputFactoryProvider inputFactoryProvider = null;

    /**
     * Allow to explicitly set the Stax2OutputFactoryProvider instance in OSGI context.
     * In a no-OSGI context, the factory is retrieved with java service loader.
     *
     * <p>Note: Stax2 implementation is provided by woodstox library which
     * is a dependency of Jackson.</p>
     *
     * @see org.restlet.ext.jackson.internal.Activator
     */
    public static Stax2OutputFactoryProvider outputFactoryProvider = null;


    /**
     * Returns an instance of {@link javax.xml.stream.XMLInputFactory}
     * according to the classpath.
     */
    public static XMLInputFactory newInputFactory() {
        return inputFactoryProvider != null ?
                inputFactoryProvider.createInputFactory() :
                XMLInputFactory.newFactory();
    }

    /**
     * Returns an instance of {@link javax.xml.stream.XMLInputFactory}
     * according to the classpath.
     */
    public static XMLOutputFactory newOutputFactory() {
        return outputFactoryProvider != null ?
                outputFactoryProvider.createOutputFactory() :
                XMLOutputFactory.newFactory();
    }
}
