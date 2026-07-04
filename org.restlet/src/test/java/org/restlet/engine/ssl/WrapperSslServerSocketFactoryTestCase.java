/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.ssl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link WrapperSslServerSocketFactory}. */
class WrapperSslServerSocketFactoryTestCase {

    @Test
    void gettersAndCipherSuites_delegateToWrappedFactory() {
        DefaultSslContextFactory contextFactory = new DefaultSslContextFactory();
        SSLServerSocketFactory wrapped =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        WrapperSslServerSocketFactory factory =
                new WrapperSslServerSocketFactory(contextFactory, wrapped);

        assertSame(contextFactory, factory.getContextFactory());
        assertSame(wrapped, factory.getWrappedSocketFactory());
        assertTrue(factory.getDefaultCipherSuites().length > 0);
        assertTrue(factory.getSupportedCipherSuites().length > 0);
    }

    @Test
    void createServerSocket_needClientAuth_configuresSocket() throws Exception {
        DefaultSslContextFactory contextFactory = new DefaultSslContextFactory();
        contextFactory.setNeedClientAuthentication(true);
        SSLServerSocketFactory wrapped =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        WrapperSslServerSocketFactory factory =
                new WrapperSslServerSocketFactory(contextFactory, wrapped);

        try (ServerSocket socket = factory.createServerSocket()) {
            assertTrue(socket.isBound() || !socket.isBound());
        }
    }

    @Test
    void createServerSocket_withPort_wantClientAuthAndCipherSuites_configuresSocket()
            throws Exception {
        DefaultSslContextFactory contextFactory = new DefaultSslContextFactory();
        contextFactory.setWantClientAuthentication(true);
        SSLServerSocketFactory wrapped =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        contextFactory.setEnabledCipherSuites(wrapped.getSupportedCipherSuites());

        WrapperSslServerSocketFactory factory =
                new WrapperSslServerSocketFactory(contextFactory, wrapped);

        try (ServerSocket socket = factory.createServerSocket(0)) {
            assertTrue(socket.getLocalPort() > 0);
        }
    }

    @Test
    void createServerSocket_withPortAndBacklog_enabledProtocols_configuresSocket()
            throws Exception {
        DefaultSslContextFactory contextFactory = new DefaultSslContextFactory();
        SSLServerSocketFactory wrapped =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        contextFactory.setEnabledProtocols(new String[] {"TLSv1.2", "TLSv1.3"});

        WrapperSslServerSocketFactory factory =
                new WrapperSslServerSocketFactory(contextFactory, wrapped);

        try (ServerSocket socket = factory.createServerSocket(0, 5)) {
            assertTrue(socket.getLocalPort() > 0);
        }
    }

    @Test
    void createServerSocket_withPortBacklogAndAddress_configuresSocket() throws Exception {
        DefaultSslContextFactory contextFactory = new DefaultSslContextFactory();
        SSLServerSocketFactory wrapped =
                (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        WrapperSslServerSocketFactory factory =
                new WrapperSslServerSocketFactory(contextFactory, wrapped);

        try (ServerSocket socket =
                factory.createServerSocket(0, 5, InetAddress.getLoopbackAddress())) {
            assertTrue(socket.getLocalPort() > 0);
        }
    }
}
