/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

/**
 * Adapts a Restlet-configured JDK {@link SSLContext} to a Netty {@link SslContext}.
 *
 * <p>The Restlet SSL context applies the enabled protocols and cipher suites to the engines it
 * creates. Netty would otherwise reset them to its own defaults, so they are read from a sample
 * engine and passed explicitly.
 *
 * <p>This class is internal to the Netty connectors.
 */
final class NettySsl {

    private NettySsl() {}

    /**
     * Creates a Netty SSL context.
     *
     * @param context The configured JDK SSL context.
     * @param client True for a client context.
     * @param alpn The application protocol negotiation configuration.
     * @param clientAuth The client authentication mode, for server contexts.
     * @return The Netty SSL context.
     */
    public static SslContext newContext(
            SSLContext context,
            boolean client,
            ApplicationProtocolConfig alpn,
            ClientAuth clientAuth) {
        SSLEngine sample = context.createSSLEngine();
        sample.setUseClientMode(client);
        List<String> ciphers = Arrays.asList(sample.getEnabledCipherSuites());
        String[] protocols = sample.getEnabledProtocols();

        return new JdkSslContext(
                context,
                client,
                ciphers,
                IdentityCipherSuiteFilter.INSTANCE,
                alpn,
                clientAuth,
                protocols,
                false);
    }

    /**
     * Creates an ALPN configuration advertising the given protocols, in preference order.
     *
     * @param protocols The application protocol names.
     * @return The ALPN configuration.
     */
    public static ApplicationProtocolConfig alpn(List<String> protocols) {
        return new ApplicationProtocolConfig(
                ApplicationProtocolConfig.Protocol.ALPN,
                ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                protocols);
    }
}
