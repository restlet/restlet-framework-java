/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import org.restlet.data.Encoding;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;
import org.restlet.util.WrapperRepresentation;

// [excludes gwt]
/**
 * Representation that decodes a wrapped representation if its encoding is
 * supported. If at least one encoding of the wrapped representation is not
 * supported, then the wrapped representation is not decoded.
 * 
 * @author Jerome Louvel
 */
public class DecodeRepresentation extends WrapperRepresentation {
    /**
     * Returns the list of supported encodings.
     * 
     * @return The list of supported encodings.
     */
    public static List<Encoding> getSupportedEncodings() {
        return Arrays.<Encoding> asList(Encoding.GZIP, Encoding.DEFLATE,
                Encoding.DEFLATE_NOWRAP, Encoding.ZIP, Encoding.IDENTITY);
    }

    /** Indicates if the decoding can happen. */
    private volatile boolean decoding;

    /** List of encodings still applied to the decodeRepresentation */
    private final List<Encoding> wrappedEncodings;

    /**
     * Constructor.
     * 
     * @param wrappedRepresentation
     *            The wrapped representation.
     */
    public DecodeRepresentation(Representation wrappedRepresentation) {
        super(wrappedRepresentation);
        this.decoding = getSupportedEncodings().containsAll(
                wrappedRepresentation.getEncodings());
        this.wrappedEncodings = new CopyOnWriteArrayList<Encoding>(
                wrappedRepresentation.getEncodings());
    }
    
    @Override
    public long getAvailableSize() {
        return IoUtils.getAvailableSize(this);
    }

    /**
     * Returns a readable byte channel. If it is supported by a file a read-only
     * instance of FileChannel is returned.
     * 
     * @return A readable byte channel.
     */
    @Override
    public ReadableByteChannel getChannel() throws IOException {
        if (isDecoding()) {
            return IoUtils.getChannel(getStream());
        } else {
            return getWrappedRepresentation().getChannel();
        }
    }

    /**
     * Returns a decoded stream for a given encoding and coded stream.
     * 
     * @param encoding
     *            The encoding to use.
     * @param encodedStream
     *            The encoded stream.
     * @return The decoded stream.
     * @throws IOException
     */
    private InputStream getDecodedStream(Encoding encoding,
            InputStream encodedStream) throws IOException {
        InputStream result = null;

        if (encodedStream != null) {
            if (encoding.equals(Encoding.GZIP)) {
                result = new GZIPInputStream(encodedStream);
            } else if (encoding.equals(Encoding.DEFLATE)) {
                result = new InflaterInputStream(encodedStream);
            } else if (encoding.equals(Encoding.DEFLATE_NOWRAP)) {
                result = new InflaterInputStream(encodedStream, new Inflater(
                        true));
            } else if (encoding.equals(Encoding.ZIP)) {
                @SuppressWarnings("resource")
                final ZipInputStream stream = new ZipInputStream(encodedStream);
                if (stream.getNextEntry() != null) {
                    result = stream;
                }
            } else if (encoding.equals(Encoding.IDENTITY)) {
                throw new IOException(
                        "Decoder unecessary for identity decoding");
            }
        }

        return result;
    }

    /**
     * Returns the encodings applied to the entity.
     * 
     * @return The encodings applied to the entity.
     */
    @Override
    public List<Encoding> getEncodings() {
        if (isDecoding()) {
            return new ArrayList<Encoding>();
        } else {
            return this.wrappedEncodings;
        }
    }

    @Override
    public Reader getReader() throws IOException {
        if (isDecoding()) {
            return IoUtils.getReader(getStream(), getCharacterSet());
        } else {
            return getWrappedRepresentation().getReader();
        }
    }

    /**
     * Returns the size in bytes of the decoded representation if known,
     * UNKNOWN_SIZE (-1) otherwise.
     * 
     * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     */
    @Override
    public long getSize() {
        long result = UNKNOWN_SIZE;

        if (isDecoding()) {
            boolean identity = true;
            for (final Iterator<Encoding> iter = this.wrappedEncodings.iterator(); identity
                    && iter.hasNext();) {
                identity = (iter.next().equals(Encoding.IDENTITY));
            }
            if (identity) {
                result = getWrappedRepresentation().getSize();
            }
        } else {
            result = getWrappedRepresentation().getSize();
        }

        return result;
    }

    /**
     * Returns a stream with the representation's content.
     * 
     * @return A stream with the representation's content.
     */
    @Override
    public InputStream getStream() throws IOException {
        InputStream result = getWrappedRepresentation().getStream();

        if (isDecoding()) {
            for (int i = this.wrappedEncodings.size() - 1; i >= 0; i--) {
                if (!this.wrappedEncodings.get(i).equals(Encoding.IDENTITY)) {
                    result = getDecodedStream(this.wrappedEncodings.get(i),
                            result);
                }
            }
        }

        return result;
    }

    /**
     * Converts the representation to a string value. Be careful when using this
     * method as the conversion of large content to a string fully stored in
     * memory can result in OutOfMemoryErrors being thrown.
     * 
     * @return The representation as a string value.
     */
    @Override
    public String getText() throws IOException {
        if (isDecoding()) {
            return IoUtils.toString(getStream(), getCharacterSet());
        } else {
            return getWrappedRepresentation().getText();
        }
    }

    /**
     * Indicates if the decoding can happen.
     * 
     * @return True if the decoding can happen.
     */
    public boolean isDecoding() {
        return this.decoding;
    }

    /**
     * Writes the representation to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (isDecoding()) {
            IoUtils.copy(getStream(), outputStream);
        } else {
            getWrappedRepresentation().write(outputStream);
        }
    }

    /**
     * Writes the representation to a byte channel.
     * 
     * @param writableChannel
     *            A writable byte channel.
     */
    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        if (isDecoding()) {
            OutputStream os = IoUtils.getStream(writableChannel);
            write(os);
            os.flush();
        } else {
            getWrappedRepresentation().write(writableChannel);
        }
    }
}
