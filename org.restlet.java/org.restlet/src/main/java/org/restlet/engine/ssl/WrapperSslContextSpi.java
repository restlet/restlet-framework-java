/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.ssl;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.SecureRandom;

/**
 * Default SSL context SPI capable or setting additional properties on the
 * created SSL engines and socket factories.
 * 
 * @author Jerome Louvel
 */
public class WrapperSslContextSpi extends SSLContextSpi {

	/** The parent SSL context factory. */
	private final DefaultSslContextFactory contextFactory;

	/** The wrapped SSL context. */
	private final SSLContext wrappedContext;

	/**
	 * Constructor.
	 * 
	 * @param contextFactory The parent SSL context factory.
	 * @param wrappedContext The wrapped SSL context.
	 */
	public WrapperSslContextSpi(DefaultSslContextFactory contextFactory, SSLContext wrappedContext) {
		this.contextFactory = contextFactory;
		this.wrappedContext = wrappedContext;
	}

	@Override
	protected SSLEngine engineCreateSSLEngine() {
		SSLEngine result = getWrappedContext().createSSLEngine();
		initEngine(result);
		return result;
	}

	@Override
	protected SSLEngine engineCreateSSLEngine(String peerHost, int peerPort) {
		SSLEngine result = getWrappedContext().createSSLEngine(peerHost, peerPort);
		initEngine(result);
		return result;
	}

	@Override
	protected SSLSessionContext engineGetClientSessionContext() {
		return getWrappedContext().getClientSessionContext();
	}

	@Override
	protected SSLSessionContext engineGetServerSessionContext() {
		return getWrappedContext().getServerSessionContext();
	}

	@Override
	protected SSLServerSocketFactory engineGetServerSocketFactory() {
		return new WrapperSslServerSocketFactory(getContextFactory(), getWrappedContext().getServerSocketFactory());
	}

	@Override
	protected SSLSocketFactory engineGetSocketFactory() {
		return new WrapperSslSocketFactory(getContextFactory(), getWrappedContext().getSocketFactory());
	}

	@Override
	protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom random) throws KeyManagementException {
		getWrappedContext().init(km, tm, random);
	}

	/**
	 * Returns the parent SSL context factory.
	 * 
	 * @return The parent SSL context factory.
	 */
	protected DefaultSslContextFactory getContextFactory() {
		return contextFactory;
	}

	/**
	 * Returns the wrapped SSL context.
	 * 
	 * @return The wrapped SSL context.
	 */
	protected SSLContext getWrappedContext() {
		return wrappedContext;
	}

	/**
	 * Initializes the SSL engine with additional parameters from the SSL context
	 * factory.
	 * 
	 * @param sslEngine The SSL engine to initialize.
	 */
	protected void initEngine(SSLEngine sslEngine) {
		if (getContextFactory().isNeedClientAuthentication()) {
			sslEngine.setNeedClientAuth(true);
		} else if (getContextFactory().isWantClientAuthentication()) {
			sslEngine.setWantClientAuth(true);
		}

		if ((getContextFactory().getEnabledCipherSuites() != null)
				|| (getContextFactory().getDisabledCipherSuites() != null)) {
			sslEngine.setEnabledCipherSuites(
					getContextFactory().getSelectedCipherSuites(sslEngine.getSupportedCipherSuites()));
		}

		if ((getContextFactory().getEnabledProtocols() != null)
				|| (getContextFactory().getDisabledProtocols() != null)) {
			sslEngine.setEnabledProtocols(
					getContextFactory().getSelectedSslProtocols(sslEngine.getSupportedProtocols()));
		}
	}

}
