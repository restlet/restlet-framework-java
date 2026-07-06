/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.restlet.data.Protocol;

/** Unit tests for {@link SpringServer}. */
class SpringServerTestCase {

    @Test
    void testConstructorWithProtocol() {
        SpringServer server = new SpringServer("HTTP");
        assertTrue(
                server.getProtocols().contains(Protocol.HTTP),
                "Server should support HTTP protocol");
    }

    @Test
    void testConstructorWithProtocolAndAddressAndPort() {
        SpringServer server = new SpringServer("HTTP", "127.0.0.1", 8112);
        assertTrue(
                server.getProtocols().contains(Protocol.HTTP),
                "Server should support HTTP protocol");
        assertEquals(8112, server.getPort(), "Wrong server port");
        assertEquals("127.0.0.1", server.getAddress(), "Wrong server address");
    }

    @Test
    void testConstructorWithProtocolAndPort() {
        SpringServer server = new SpringServer("HTTP", 8111);
        assertTrue(
                server.getProtocols().contains(Protocol.HTTP),
                "Server should support HTTP protocol");
        assertEquals(8111, server.getPort(), "Wrong server port");
    }

    @Test
    void testSetParametersAddsAllProperties() {
        SpringServer server = new SpringServer("HTTP");
        Properties parameters = new Properties();
        parameters.setProperty("key1", "value1");
        parameters.setProperty("key2", "value2");

        server.setParameters(parameters);

        assertEquals("value1", server.getContext().getParameters().getFirstValue("key1"));
        assertEquals("value2", server.getContext().getParameters().getFirstValue("key2"));
    }

    @Test
    void testSetParametersWithEmptyProperties() {
        SpringServer server = new SpringServer("HTTP");
        server.setParameters(new Properties());
        assertEquals(
                0,
                server.getContext().getParameters().subList("key1").size(),
                "No parameters should have been added");
    }
}
