/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package com.noelios.restlet.ext.asyncweb;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;

import org.restlet.Server;
import org.restlet.data.CharacterSet;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.util.Series;
import org.safehaus.asyncweb.http.HttpRequest;
import org.safehaus.asyncweb.http.HttpResponse;
import org.safehaus.asyncweb.http.ResponseStatus;
import org.safehaus.asyncweb.http.internal.HttpHeaders;
import org.safehaus.asyncweb.http.internal.Request;
import org.safehaus.asyncweb.http.internal.Response;

import com.noelios.restlet.http.HttpServerCall;

/**
 * HttpServerCall implementation used by the AsyncServer.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://www.semagia.com/">Semagia</a>
 */
public class AsyncWebServerCall extends HttpServerCall {
	/** AsyncWeb request. */
	private Request request;

	/** Indicates if the request headers were parsed and added. */
	private boolean requestHeadersAdded;

	/**
	 * AsyncWeb response.
	 */
	private Response response;

	/**
	 * Constructor.
	 * 
	 * @param server
	 *            The parent server connector.
	 * @param request
	 *            The AsyncWebRequest.
	 * @param response
	 *            The AsyncWebResponse.
	 * @param confidential
	 *            Indicates if the server is acting in HTTPS mode.
	 */
	public AsyncWebServerCall(Server server, HttpRequest request,
			HttpResponse response, boolean confidential) {
		super(server);
		this.request = (Request) request;
		this.requestHeadersAdded = false;
		this.response = (Response) response;
		setConfidential(confidential);
	}

	@Override
	public String getClientAddress() {
		return request.getRemoteAddress();
	}

	@Override
	public int getClientPort() {
		return request.getRemotePort();
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getRequestUri() {
		StringBuilder stringBuilder = new StringBuilder(request.getRequestURI());

		// The query seems to be automatically parsed and decoded by AsyncWeb.
		// Therefore, we should rebuild the entire query string in order to
		// generate a proper URI.
		if (request.getParameterNames().hasNext()) {
			stringBuilder.append("?");
			for (Iterator<String> iterName = request.getParameterNames(); iterName
					.hasNext();) {
				String name = (String) iterName.next();
				if (request.getParameterValues(name).hasNext()) {
					for (Iterator<String> iterValue = request
							.getParameterValues(name); iterValue.hasNext();) {
						String value = (String) iterValue.next();
						// As the query seems to be decoded in the Latin1
						// character set, we should encode it.
						stringBuilder
								.append(
										Reference.encode(name,
												CharacterSet.ISO_8859_1))
								.append("=").append(
										Reference.encode(value,
												CharacterSet.ISO_8859_1));
						if (iterValue.hasNext()) {
							stringBuilder.append("&");
						}
					}

				}
				if (iterName.hasNext()) {
					stringBuilder.append("&");
				}
			}
		}
		return stringBuilder.toString();
	}

	@Override
	public String getMethod() {
		return request.getMethod().getName();
	}

	@Override
	public Series<Parameter> getRequestHeaders() {
		Series<Parameter> result = super.getRequestHeaders();

		if (!this.requestHeadersAdded) {
			HttpHeaders headers = request.getHeaders();
			int headerCount = headers.getSize();
			for (int i = 0; i < headerCount; i++) {
				result.add(headers.getHeaderName(i).getValue(), headers
						.getHeaderValue(i).getValue());
			}

			this.requestHeadersAdded = true;
		}

		return result;
	}

	@Override
	public void writeResponseHead(org.restlet.data.Response restletResponse)
			throws IOException {
		response.setStatus(ResponseStatus.forId(getStatusCode()),
				getReasonPhrase());

		// Ensure that headers are empty
		response.getHeaders().dispose();
		for (Parameter header : super.getResponseHeaders()) {
			response.addHeader(header.getName(), header.getValue());
		}
	}

	@Override
	public ReadableByteChannel getRequestChannel() {
		// Unsupported.
		return null;
	}

	@Override
	public InputStream getRequestStream() {
		return request.getInputStream();
	}

	@Override
	public WritableByteChannel getResponseChannel() {
		// Unsupported.
		return null;
	}

	@Override
	public OutputStream getResponseStream() {
		return response.getOutputStream();
	}

}
