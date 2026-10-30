/**
 * Copyright 2005-2026 Qlik
 *
 * <p>The content of this file is subject to the terms of the Apache 2.0 open source license
 * available at https://www.opensource.org/licenses/apache-2.0
 *
 * <p>Restlet is a registered trademark of QlikTech International AB.
 */
@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module org.restlet.ext.mcp {
    requires transitive org.restlet;
    requires transitive io.modelcontextprotocol.sdk.mcp.core;
    requires transitive reactor.core;
    requires transitive org.reactivestreams;
    requires org.slf4j;
    requires io.modelcontextprotocol.sdk.mcp.json.jackson2;
    requires com.networknt.schema;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;

    exports org.restlet.ext.mcp;
}
