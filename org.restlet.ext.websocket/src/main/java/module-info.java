/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
/** Restlet Framework WebSocket extension (experimental). */
module org.restlet.ext.websocket {
    requires transitive org.restlet;

    // Netty is an implementation detail of the extension: required, not re-exported.
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.compression;
    requires io.netty.codec.http;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.transport;

    exports org.restlet.ext.websocket;
}
