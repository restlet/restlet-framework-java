/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

import java.net.HttpURLConnection;

/**
 * Representation that wraps another representation and closes the parent
 * {@link HttpURLConnection} when the representation is released.
 * 
 * @author Kevin Conaway
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
class ConnectionClosingRepresentation extends WrapperRepresentation {

	/** The parent connection. */
	private final HttpURLConnection connection;

	/**
	 * Default constructor.
	 * 
	 * @param wrappedRepresentation The wrapped representation.
	 * @param connection            The parent connection.
	 */
	public ConnectionClosingRepresentation(Representation wrappedRepresentation, HttpURLConnection connection) {
		super(wrappedRepresentation);
		this.connection = connection;
	}

	@Override
	public void release() {
		this.connection.disconnect();
		super.release();
	}

}
