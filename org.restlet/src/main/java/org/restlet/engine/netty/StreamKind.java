/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

/**
 * The kind of channel carrying an HTTP exchange. It lets the shared code adapt to the protocol
 * without referencing the HTTP/2 or QUIC classes, which keeps the optional HTTP/3 dependencies
 * unloaded unless HTTP/3 is used.
 */
enum StreamKind {

    /** An HTTP/1.1 connection, carrying one exchange at a time. */
    HTTP1("HTTP/1.1"),

    /** An HTTP/2 stream, child of a TCP connection. */
    HTTP2("HTTP/2.0"),

    /** An HTTP/3 request stream, child of a QUIC connection. */
    HTTP3("HTTP/3.0");

    private final String version;

    StreamKind(String version) {
        this.version = version;
    }

    /**
     * Returns the protocol version, as reported to the application.
     *
     * @return The protocol version.
     */
    String version() {
        return version;
    }

    /**
     * Indicates whether the exchange is carried by a stream of a multiplexed connection.
     *
     * @return True for HTTP/2 and HTTP/3.
     */
    boolean isStream() {
        return this != HTTP1;
    }
}
