/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.xml.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/** Unit tests for {@link AbstractXmlReader}. */
class AbstractXmlReaderTestCase {

    private static class TestXmlReader extends AbstractXmlReader {
        @Override
        public void parse(InputSource input) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void parse(String systemId) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    void newInstance_hasNullHandlersAndFalseFeatures() {
        TestXmlReader reader = new TestXmlReader();
        assertNull(reader.getContentHandler());
        assertNull(reader.getDTDHandler());
        assertNull(reader.getEntityResolver());
        assertNull(reader.getErrorHandler());
        assertFalse(reader.getFeature("any-feature"));
        assertNull(reader.getProperty("any-property"));
    }

    @Test
    void contentHandler_setterAndGetter_roundTrip() {
        TestXmlReader reader = new TestXmlReader();
        ContentHandler handler = new DefaultHandler();
        reader.setContentHandler(handler);
        assertSame(handler, reader.getContentHandler());
    }

    @Test
    void dtdHandler_setterAndGetter_roundTrip() {
        TestXmlReader reader = new TestXmlReader();
        DTDHandler handler = new DefaultHandler();
        reader.setDTDHandler(handler);
        assertSame(handler, reader.getDTDHandler());
    }

    @Test
    void entityResolver_setterAndGetter_roundTrip() {
        TestXmlReader reader = new TestXmlReader();
        EntityResolver resolver = new DefaultHandler();
        reader.setEntityResolver(resolver);
        assertSame(resolver, reader.getEntityResolver());
    }

    @Test
    void errorHandler_setterAndGetter_roundTrip() {
        TestXmlReader reader = new TestXmlReader();
        ErrorHandler handler = new DefaultHandler();
        reader.setErrorHandler(handler);
        assertSame(handler, reader.getErrorHandler());
    }

    @Test
    void feature_setterAndGetter_roundTrip() {
        TestXmlReader reader = new TestXmlReader();
        reader.setFeature("my-feature", true);
        assertTrue(reader.getFeature("my-feature"));
        assertFalse(reader.getFeature("other-feature"));
    }

    @Test
    void property_setterAndGetter_roundTrip() {
        TestXmlReader reader = new TestXmlReader();
        Object value = new Object();
        reader.setProperty("my-property", value);
        assertSame(value, reader.getProperty("my-property"));
    }
}
