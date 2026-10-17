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
 * Checks that the experimental HTTP/3 support can be used. This class doesn't reference the
 * optional Netty QUIC and HTTP/3 classes, so it can report their absence with a clear message
 * instead of a {@link NoClassDefFoundError}.
 */
final class Http3Availability {

    private Http3Availability() {}

    /**
     * Checks that the optional Netty HTTP/3 classes are present and that the native QUIC library
     * can be loaded.
     *
     * @throws IllegalStateException If HTTP/3 can't be used.
     */
    static void ensureAvailable() {
        try {
            Class.forName(
                    "io.netty.handler.codec.http3.Http3",
                    false,
                    Http3Availability.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalStateException(
                    "HTTP/3 requires the io.netty:netty-codec-http3 dependency", e);
        }
        Http3Support.ensureNativeLibrary();
    }
}
