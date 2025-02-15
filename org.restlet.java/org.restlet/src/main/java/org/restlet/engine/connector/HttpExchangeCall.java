/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Call that is used by the Basic HTTP server.
 * 
 * @author Jerome Louvel
 */
public class HttpExchangeCall extends ServerCall {

	/** The wrapped HTTP exchange. */
	private final HttpExchange exchange;

	/** Indicates if the request headers were parsed and added. */
	private volatile boolean requestHeadersAdded;

	/**
	 * Constructor.
	 *
	 * @param server   The parent server.
	 * @param exchange The wrapped {@link HttpExchange} instance.
	 */
	public HttpExchangeCall(Server server, HttpExchange exchange) {
		this(server, exchange, false);
	}

	/**
	 * Constructor.
	 *
	 * @param server       The parent server.
	 * @param exchange     The wrapped {@link HttpExchange} instance.
	 * @param confidential True if the confidentiality of the call is ensured (ex:
	 *                     via SSL)
	 */
	public HttpExchangeCall(Server server, HttpExchange exchange, boolean confidential) {
		super(server);
		this.exchange = exchange;
		setConfidential(confidential);
	}

	@Override
	public boolean abort() {
		this.exchange.close();
		return true;
	}

	@Override
	public void flushBuffers() throws IOException {
		this.exchange.getResponseBody().flush();
	}

	@Override
	public String getClientAddress() {
		return this.exchange.getRemoteAddress().getAddress().getHostAddress();
	}

	@Override
	public int getClientPort() {
		return this.exchange.getRemoteAddress().getPort();
	}

	@Override
	public String getMethod() {
		return this.exchange.getRequestMethod();
	}

	@Override
	public Series<Header> getRequestHeaders() {
		final Series<Header> result = super.getRequestHeaders();

		if (!this.requestHeadersAdded) {
			final Headers headers = this.exchange.getRequestHeaders();

			for (String name : headers.keySet()) {
				for (String value : headers.get(name)) {
					result.add(name, value);
				}
			}
			this.requestHeadersAdded = true;
		}

		return result;
	}

	@Override
	public InputStream getRequestEntityStream(long size) {
		return this.exchange.getRequestBody();
	}

	@Override
	public InputStream getRequestHeadStream() {
		return null;
	}

	@Override
	public String getRequestUri() {
		return this.exchange.getRequestURI().toString();
	}

	@Override
	public OutputStream getResponseEntityStream() {
		return this.exchange.getResponseBody();
	}

	@Override
	public void writeResponseHead(org.restlet.Response restletResponse) throws IOException {
		final Headers headers = this.exchange.getResponseHeaders();

		for (Header header : getResponseHeaders()) {
			headers.add(header.getName(), header.getValue());
		}

		// Send the headers
		Representation entity = restletResponse.getEntity();
		long responseLength = 0;

		if (entity == null || !entity.isAvailable()) {
			responseLength = -1;
		} else if (entity.getAvailableSize() != Representation.UNKNOWN_SIZE) {
			responseLength = entity.getAvailableSize();
		}

		this.exchange.sendResponseHeaders(getStatusCode(), responseLength);
	}

}
