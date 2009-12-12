/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.Server;
import org.restlet.data.Digest;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.Parameter;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.http.header.ContentType;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.http.header.RangeUtils;
import org.restlet.engine.util.Base64;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ReadableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

public abstract class ServerConnection extends Connection<Server> {

    public ServerConnection(ConnectorHelper<Server> helper, Socket socket)
            throws IOException {
        super(helper, socket);
    }

    /**
     * Returns the request entity if available.
     * 
     * @param headers
     *            The headers to use.
     * @return The request entity if available.
     */
    public Representation createRequestEntity(Series<Parameter> headers) {
        Representation result = null;
        long contentLength = HeaderUtils.getContentLength(headers);

        // Create the result representation
        InputStream requestStream = getRequestEntityStream(contentLength);
        ReadableByteChannel requestChannel = getRequestEntityChannel(contentLength);

        if (requestStream != null) {
            result = new InputRepresentation(requestStream, null, contentLength);
        } else if (requestChannel != null) {
            result = new ReadableRepresentation(requestChannel, null,
                    contentLength);
        }

        result.setSize(contentLength);

        // Extract some interesting header values
        for (Parameter header : headers) {
            if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_ENCODING)) {
                HeaderReader hr = new HeaderReader(header.getValue());
                String value = hr.readValue();

                while (value != null) {
                    Encoding encoding = Encoding.valueOf(value);

                    if (!encoding.equals(Encoding.IDENTITY)) {
                        result.getEncodings().add(encoding);
                    }
                    value = hr.readValue();
                }
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_LANGUAGE)) {
                HeaderReader hr = new HeaderReader(header.getValue());
                String value = hr.readValue();

                while (value != null) {
                    result.getLanguages().add(Language.valueOf(value));
                    value = hr.readValue();
                }
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_TYPE)) {
                ContentType contentType = new ContentType(header.getValue());
                result.setMediaType(contentType.getMediaType());
                result.setCharacterSet(contentType.getCharacterSet());
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_RANGE)) {
                RangeUtils.parseContentRange(header.getValue(), result);
            } else if (header.getName().equalsIgnoreCase(
                    HeaderConstants.HEADER_CONTENT_MD5)) {
                result.setDigest(new Digest(Digest.ALGORITHM_MD5, Base64
                        .decode(header.getValue())));
            }
        }

        return result;
    }

    /**
     * Returns the request entity channel if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The request entity channel if it exists.
     */
    public abstract ReadableByteChannel getRequestEntityChannel(long size);

    /**
     * Returns the request entity stream if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The request entity stream if it exists.
     */
    public abstract InputStream getRequestEntityStream(long size);

    /**
     * Returns the request head channel if it exists.
     * 
     * @return The request head channel if it exists.
     */
    public abstract ReadableByteChannel getRequestHeadChannel();

    /**
     * Returns the request head stream if it exists.
     * 
     * @return The request head stream if it exists.
     */
    public abstract InputStream getRequestHeadStream();

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    public abstract WritableByteChannel getResponseEntityChannel();

    /**
     * Returns the response entity stream if it exists.
     * 
     * @return The response entity stream if it exists.
     */
    public abstract OutputStream getResponseEntityStream();

    /**
     * Reads the next request sent by the client if available. Note that the
     * optional entity is not fully read.
     * 
     * @return The next request sent by the client if available.
     * @throws IOException
     */
    public abstract InternalRequest readRequest() throws IOException;

}
