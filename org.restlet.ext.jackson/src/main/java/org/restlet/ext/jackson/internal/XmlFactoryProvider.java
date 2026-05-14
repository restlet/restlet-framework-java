/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jackson.internal;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import org.codehaus.stax2.osgi.Stax2InputFactoryProvider;
import org.codehaus.stax2.osgi.Stax2OutputFactoryProvider;
import org.restlet.engine.Edition;

/**
 * Provides {@link javax.xml.stream.XMLInputFactory} and {@link javax.xml.stream.XMLOutputFactory}
 * in an OSGI context.
 *
 * <p>In a non-OSGI context, the factories are retrieved with java service loader.
 *
 * @author Manuel Boillod
 */
public class XmlFactoryProvider {

    /**
     * Allow explicitly setting the Stax2InputFactory instance in OSGI context. In a no-OSGI
     * context, the factory is retrieved with a java service loader.
     *
     * <p>Note: Stax2 implementation is provided by woodstox library, which is a dependency of
     * Jackson.
     */
    public static Stax2InputFactoryProvider inputFactoryProvider = null;

    /**
     * Allow explicitly setting the Stax2OutputFactoryProvider instance in OSGI context. In a
     * no-OSGI context, the factory is retrieved with a java service loader.
     *
     * <p>Note: Stax2 implementation is provided by woodstox library, which is a dependency of
     * Jackson.
     */
    public static Stax2OutputFactoryProvider outputFactoryProvider = null;

    /**
     * Returns an instance of {@link javax.xml.stream.XMLInputFactory} according to the classpath.
     */
    public static XMLInputFactory newInputFactory() {
        if (inputFactoryProvider == null) {
            if (Edition.ANDROID.isCurrentEdition()) {
                inputFactoryProvider = new com.ctc.wstx.osgi.InputFactoryProviderImpl();
            } else {
                return XMLInputFactory.newFactory();
            }
        }
        return inputFactoryProvider.createInputFactory();
    }

    /**
     * Returns an instance of {@link javax.xml.stream.XMLInputFactory} according to the classpath.
     */
    public static XMLOutputFactory newOutputFactory() {
        if (outputFactoryProvider == null) {
            if (Edition.ANDROID.isCurrentEdition()) {
                outputFactoryProvider = new com.ctc.wstx.osgi.OutputFactoryProviderImpl();
            } else {
                return XMLOutputFactory.newFactory();
            }
        }

        return outputFactoryProvider.createOutputFactory();
    }
}
