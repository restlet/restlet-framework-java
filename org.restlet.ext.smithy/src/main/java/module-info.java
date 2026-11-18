/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
/*
 * The server classes use the smithy-java server, which splits a package in 1.8.0
 * (smithy-java#1437) and can't be required: they read it on the class path, with --add-reads
 * org.restlet.ext.smithy=ALL-UNNAMED, and its provider is only declared in META-INF/services.
 */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.smithy {
    requires transitive org.restlet;
    requires transitive software.amazon.smithy.java.client.core;
    requires transitive software.amazon.smithy.java.context;
    requires transitive software.amazon.smithy.java.http.api;
    requires software.amazon.smithy.java.client.http;
    requires software.amazon.smithy.java.core;
    requires software.amazon.smithy.java.io;

    exports org.restlet.ext.smithy;

    provides software.amazon.smithy.java.client.core.ClientTransportFactory with
            org.restlet.ext.smithy.internal.RestletTransportFactory;
}
