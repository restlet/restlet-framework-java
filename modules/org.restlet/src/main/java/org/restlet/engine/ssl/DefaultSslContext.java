/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
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
