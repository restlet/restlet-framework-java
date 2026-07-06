/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Test {@link org.restlet.data.Protocol}. */
class ProtocolTestCase {

    @Test
    void constructor_withSchemeNameOnly_derivesNameAndDescription() {
        Protocol protocol = new Protocol("myproto");
        assertEquals("myproto", protocol.getSchemeName());
        assertEquals("MYPROTO", protocol.getName());
        assertEquals("MYPROTO Protocol", protocol.getDescription());
        assertEquals(Protocol.UNKNOWN_PORT, protocol.getDefaultPort());
        assertFalse(protocol.isConfidential());
        assertNull(protocol.getVersion());
    }

    @Test
    void constructor_withDefaultPort() {
        Protocol protocol = new Protocol("proto", "PROTO", "description", 1234);
        assertEquals(1234, protocol.getDefaultPort());
        assertFalse(protocol.isConfidential());
    }

    @Test
    void constructor_withConfidentiality() {
        Protocol protocol = new Protocol("proto", "PROTO", "description", 1234, true);
        assertTrue(protocol.isConfidential());
    }

    @Test
    void constructor_withVersion() {
        Protocol protocol = new Protocol("proto", "PROTO", "description", 1234, true, "1.0");
        assertEquals("1.0", protocol.getVersion());
        assertEquals("PROTO", protocol.getTechnicalName());
    }

    @Test
    void constructor_withDefaultPortAndVersion() {
        Protocol protocol = new Protocol("proto", "PROTO", "description", 1234, "2.0");
        assertEquals("2.0", protocol.getVersion());
        assertFalse(protocol.isConfidential());
    }

    @Test
    void constructor_withDistinctTechnicalName() {
        Protocol protocol = new Protocol("https", "HTTPS", "HTTP", "description", 443, true, "1.1");
        assertEquals("HTTPS", protocol.getName());
        assertEquals("HTTP", protocol.getTechnicalName());
    }

    @Test
    void valueOf_knownScheme_returnsSharedConstant() {
        assertSame(Protocol.HTTP, Protocol.valueOf("http"));
        assertSame(Protocol.HTTPS, Protocol.valueOf("HTTPS"));
        assertSame(Protocol.FTP, Protocol.valueOf("ftp"));
        assertSame(Protocol.FILE, Protocol.valueOf("file"));
        assertSame(Protocol.CLAP, Protocol.valueOf("clap"));
        assertSame(Protocol.JAR, Protocol.valueOf("jar"));
        assertSame(Protocol.JDBC, Protocol.valueOf("jdbc"));
        assertSame(Protocol.RIAP, Protocol.valueOf("riap"));
        assertSame(Protocol.WAR, Protocol.valueOf("war"));
        assertSame(Protocol.ZIP, Protocol.valueOf("zip"));
    }

    @Test
    void valueOf_unknownScheme_createsNewProtocol() {
        Protocol protocol = Protocol.valueOf("custom");
        assertEquals("CUSTOM", protocol.getName());
    }

    @Test
    void valueOf_nullOrEmptyScheme_returnsNull() {
        assertNull(Protocol.valueOf(null));
        assertNull(Protocol.valueOf(""));
    }

    @Test
    void valueOf_withMatchingVersion_returnsSameConstant() {
        Protocol protocol = Protocol.valueOf("http", "1.1");
        assertSame(Protocol.HTTP, protocol);
    }

    @Test
    void valueOf_withDifferentVersion_createsNewInstanceWithVersion() {
        Protocol protocol = Protocol.valueOf("http", "2.0");
        assertNotSame(Protocol.HTTP, protocol);
        assertEquals("2.0", protocol.getVersion());
        assertEquals(Protocol.HTTP.getSchemeName(), protocol.getSchemeName());
        assertEquals(Protocol.HTTP.getDefaultPort(), protocol.getDefaultPort());
    }

    @Test
    void equals_isCaseInsensitiveOnName() {
        Protocol p1 = new Protocol("proto", "NAME", "d", 1, false);
        Protocol p2 = new Protocol("other", "name", "d2", 2, true);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    void equals_differentName_returnsFalse() {
        assertNotEquals(Protocol.HTTP, Protocol.HTTPS);
    }

    @Test
    void toString_withVersion_includesVersion() {
        assertEquals("HTTP/1.1", Protocol.HTTP.toString());
    }

    @Test
    void toString_withoutVersion_omitsSlash() {
        assertEquals("FTP", Protocol.FTP.toString());
    }
}
