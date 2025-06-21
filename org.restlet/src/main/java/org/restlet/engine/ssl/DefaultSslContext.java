/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.ssl;

import javax.net.ssl.SSLContext;

/**
 * Default SSL context that delegates calls to {@link WrapperSslContextSpi}
 * 
 * @author Jerome Louvel
 */
public class DefaultSslContext extends SSLContext {

	/**
	 * Creates a SSL context SPI capable or setting additional properties on the
	 * created SSL engines and socket factories.
	 * 
	 * @param contextFactory The parent SSL context factory.
	 * @param wrappedContext The wrapped SSL context.
	 * @return The created SSL context SPI.
	 */
	private static WrapperSslContextSpi createContextSpi(DefaultSslContextFactory contextFactory,
			SSLContext wrappedContext) {
		return new WrapperSslContextSpi(contextFactory, wrappedContext);
	}

	/**
	 * Constructor.
	 * 
	 * @param contextFactory The parent SSL context factory.
	 * @param wrappedContext The wrapped SSL context.
	 * 
	 */
	public DefaultSslContext(DefaultSslContextFactory contextFactory, SSLContext wrappedContext) {
		super(createContextSpi(contextFactory, wrappedContext), wrappedContext.getProvider(),
				wrappedContext.getProtocol());
	}

}
