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

/**
 * Provides {@link javax.xml.stream.XMLInputFactory} and {@link javax.xml.stream.XMLOutputFactory}.
 * The factories are retrieved with the Java service loader.
 *
 * @author Manuel Boillod
 */
public class XmlFactoryProvider {

    /**
     * Returns an instance of {@link javax.xml.stream.XMLInputFactory} according to the classpath.
     */
    public static XMLInputFactory newInputFactory() {
        return XMLInputFactory.newFactory();
    }

    /**
     * Returns an instance of {@link javax.xml.stream.XMLOutputFactory} according to the classpath.
     */
    public static XMLOutputFactory newOutputFactory() {
        return XMLOutputFactory.newFactory();
    }
}
