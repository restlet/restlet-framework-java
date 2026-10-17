/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
/** Restlet Framework core: API, engine and connectors. */
@SuppressWarnings({"module", "requires-automatic", "requires-transitive-automatic"})
module org.restlet {
    requires transitive java.desktop;
    requires transitive java.logging;
    requires transitive java.xml;
    requires transitive org.eclipse.jetty.http2.client;
    requires transitive org.eclipse.jetty.http2.client.transport;
    requires transitive org.eclipse.jetty.http2.server;
    requires transitive org.eclipse.jetty.http3.client;
    requires transitive org.eclipse.jetty.http3.client.transport;
    requires transitive org.eclipse.jetty.http3.server;
    requires transitive org.eclipse.jetty.alpn.server;
    requires transitive org.eclipse.jetty.client;
    requires transitive org.eclipse.jetty.http;
    requires transitive org.eclipse.jetty.io;
    requires transitive org.eclipse.jetty.server;
    requires transitive org.eclipse.jetty.util;
    requires transitive org.eclipse.jetty.quic.client;
    requires transitive org.eclipse.jetty.quic.server;

    exports org.restlet;
    exports org.restlet.data;
    exports org.restlet.engine;
    exports org.restlet.engine.adapter;
    exports org.restlet.engine.application;
    exports org.restlet.engine.component;
    exports org.restlet.engine.connector;
    exports org.restlet.engine.converter;
    exports org.restlet.engine.header;
    exports org.restlet.engine.io;
    exports org.restlet.engine.local;
    exports org.restlet.engine.log;
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
