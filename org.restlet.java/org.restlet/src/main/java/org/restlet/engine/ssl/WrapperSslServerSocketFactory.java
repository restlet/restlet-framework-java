/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.ssl;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * SSL server socket factory that wraps the default one to do extra
 * initialization. Configures the cipher suites and the SSL certificate request.
 * 
 * @author Jerome Louvel
 */
public class WrapperSslServerSocketFactory extends SSLServerSocketFactory {

	/** The parent SSL context factory. */
	private final DefaultSslContextFactory contextFactory;

	/** The wrapped SSL server socket factory. */
	private final SSLServerSocketFactory wrappedSocketFactory;

	/**
	 * Constructor.
	 * 
	 * @param contextFactory       The parent SSL context factory.
	 * @param wrappedSocketFactory The wrapped SSL server socket factory.
	 */
	public WrapperSslServerSocketFactory(DefaultSslContextFactory contextFactory,
			SSLServerSocketFactory wrappedSocketFactory) {
		this.wrappedSocketFactory = wrappedSocketFactory;
		this.contextFactory = contextFactory;
	}

	@Override
	public ServerSocket createServerSocket() throws IOException {
		SSLServerSocket result = (SSLServerSocket) getWrappedSocketFactory().createServerSocket();
		return initSslServerSocket(result);
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		SSLServerSocket result = (SSLServerSocket) getWrappedSocketFactory().createServerSocket(port);
		return initSslServerSocket(result);
	}

	@Override
	public ServerSocket createServerSocket(int port, int backLog) throws IOException {
		SSLServerSocket result = (SSLServerSocket) getWrappedSocketFactory().createServerSocket(port, backLog);
		return initSslServerSocket(result);
	}

	@Override
	public ServerSocket createServerSocket(int port, int backLog, InetAddress ifAddress) throws IOException {
		SSLServerSocket result = (SSLServerSocket) getWrappedSocketFactory().createServerSocket(port, backLog,
				ifAddress);
		return initSslServerSocket(result);
	}

	/**
	 * Returns the parent SSL context factory.
	 * 
	 * @return The parent SSL context factory.
	 */
	public DefaultSslContextFactory getContextFactory() {
		return contextFactory;
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return getWrappedSocketFactory().getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return getWrappedSocketFactory().getSupportedCipherSuites();
	}

	/**
	 * Returns the wrapped SSL server socket factory.
	 * 
	 * @return The wrapped SSL server socket factory.
	 */
	public SSLServerSocketFactory getWrappedSocketFactory() {
		return wrappedSocketFactory;
	}

	/**
	 * Initializes the SSL server socket. Configures the certificate request (need
	 * or want) and the enabled cipher suites.
	 * 
	 * @param sslServerSocket The server socket to initialize.
	 * @return The initialized server socket.
	 */
	protected SSLServerSocket initSslServerSocket(SSLServerSocket sslServerSocket) {
		if (getContextFactory().isNeedClientAuthentication()) {
			sslServerSocket.setNeedClientAuth(true);
		} else if (getContextFactory().isWantClientAuthentication()) {
			sslServerSocket.setWantClientAuth(true);
		}

		if ((getContextFactory().getEnabledCipherSuites() != null)
				|| (getContextFactory().getDisabledCipherSuites() != null)) {
			sslServerSocket.setEnabledCipherSuites(
					getContextFactory().getSelectedCipherSuites(sslServerSocket.getSupportedCipherSuites()));
		}

		if ((getContextFactory().getEnabledProtocols() != null)
				|| (getContextFactory().getDisabledProtocols() != null)) {
			sslServerSocket.setEnabledProtocols(
					getContextFactory().getSelectedSslProtocols(sslServerSocket.getSupportedProtocols()));
		}

		return sslServerSocket;
	}

}
