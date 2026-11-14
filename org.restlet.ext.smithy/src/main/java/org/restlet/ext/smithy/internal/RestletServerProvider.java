/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy.internal;

import org.restlet.ext.smithy.SmithyServerBuilder;
import software.amazon.smithy.java.server.ServerBuilder;
import software.amazon.smithy.java.server.ServerProvider;

/**
 * Provider discovered by {@code Server.builder()} on the class path, under the name {@code
 * restlet}. A named module can't declare it: the SDK server isn't in the module graph.
 */
public final class RestletServerProvider implements ServerProvider {
    @Override
    public String name() {
        return "restlet";
    }

    @Override
    public ServerBuilder<?> serverBuilder() {
        return new SmithyServerBuilder();
    }

    @Override
    public int priority() {
        // The highest priority is picked first: 1 comes before the SDK's Netty server (0).
        return 1;
    }
}
