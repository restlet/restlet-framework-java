/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
/** Restlet Framework GraphQL extension (experimental). */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.graphql {
    requires transitive org.restlet;

    // graphql-java types are the public model of the extension. graphql-java 26.1 has no module
    // descriptor: its automatic module name comes from its manifest.
    requires transitive com.graphqljava;
    requires transitive org.reactivestreams;

    // Optional: only loaded when GraphqlTransport.WEBSOCKET is enabled.
    requires static org.restlet.ext.websocket;

    // JSON is an implementation detail: required, not re-exported.
    requires tools.jackson.core;
    requires tools.jackson.databind;

    exports org.restlet.ext.graphql;
}
