/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
/** Restlet Framework core: API, engine and connectors. */
module org.restlet {
    requires transitive java.desktop;
    requires transitive java.logging;
    requires transitive java.xml;

    // Netty is an implementation detail of the HTTP connectors: required, not re-exported.
    requires io.netty.buffer;
    requires io.netty.codec;
    // Needed by the HTTP content decompressor; netty-codec-http only requires it statically, so
    // it would not be resolved on the module path otherwise.
    requires io.netty.codec.compression;
    requires io.netty.codec.http;
    requires io.netty.codec.http2;
    requires io.netty.common;
    requires io.netty.handler;
    requires io.netty.handler.proxy;
    requires io.netty.resolver;
    requires io.netty.transport;

    // Experimental HTTP/3: optional at runtime, see org.restlet.engine.netty.Http3Support.
    requires static io.netty.codec.http3;
    requires static io.netty.codec.classes.quic;

    exports org.restlet;
    exports org.restlet.data;
    exports org.restlet.engine;
    exports org.restlet.engine.application;
    exports org.restlet.engine.component;
    exports org.restlet.engine.connector;
    exports org.restlet.engine.converter;
    exports org.restlet.engine.header;
    exports org.restlet.engine.io;
    exports org.restlet.engine.local;
    exports org.restlet.engine.log;
    exports org.restlet.engine.netty;
    exports org.restlet.engine.resource;
    exports org.restlet.engine.security;
    exports org.restlet.engine.ssl;
    exports org.restlet.engine.util;
    exports org.restlet.representation;
    exports org.restlet.resource;
    exports org.restlet.routing;
    exports org.restlet.security;
    exports org.restlet.service;
    exports org.restlet.util;
}
