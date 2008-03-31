/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipInputStream;

import org.restlet.data.Encoding;
import org.restlet.resource.Representation;
import org.restlet.util.ByteUtils;
import org.restlet.util.WrapperRepresentation;

/**
 * Representation that decodes a wrapped representation if its encoding is
 * supported.<br>
 * If at least one encoding of the wrapped representation is not supported, then
 * the wrapped representation is not decoded.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class DecodeRepresentation extends WrapperRepresentation {
    /**
     * Returns the list of supported encodings.
     * 
     * @return The list of supported encodings.
     */
    public static List<Encoding> getSupportedEncodings() {
        return Arrays.<Encoding> asList(Encoding.GZIP, Encoding.DEFLATE,
                Encoding.ZIP, Encoding.IDENTITY);
    }

    /** Indicates if the decoding can happen. */
    private volatile boolean canDecode;

    /** List of encodings still applied to the decodeRepresentation */
    private volatile List<Encoding> wrappedEncodings;

    /**
     * Constructor.
     * 
     * @param wrappedRepresentation
     *                The wrapped representation.
     */
    public DecodeRepresentation(Representation wrappedRepresentation) {
        super(wrappedRepresentation);
        this.canDecode = getSupportedEncodings().containsAll(
                wrappedRepresentation.getEncodings());
        wrappedEncodings = new ArrayList<Encoding>();
        wrappedEncodings.addAll(wrappedRepresentation.getEncodings());
    }

    /**
     * Indicates if the decoding can happen.
     * 
     * @return True if the decoding can happen.
     */
    public boolean canDecode() {
        return this.canDecode;
    }

    /**
     * Returns a readable byte channel. If it is supported by a file a read-only
     * instance of FileChannel is returned.
     * 
     * @return A readable byte channel.
     */
    @Override
    public ReadableByteChannel getChannel() throws IOException {
        if (canDecode()) {
            return ByteUtils.getChannel(getStream());
        } else {
            return getWrappedRepresentation().getChannel();
        }
    }

    /**
     * Returns a decoded stream for a given encoding and coded stream.
     * 
     * @param encoding
     *                The encoding to use.
     * @param encodedStream
     *                The encoded stream.
     * @return The decoded stream.
     * @throws IOException
     */
    private InputStream getDecodedStream(Encoding encoding,
            InputStream encodedStream) throws IOException {
        InputStream result = null;

        if (encoding.equals(Encoding.GZIP)) {
            result = new GZIPInputStream(encodedStream);
        } else if (encoding.equals(Encoding.DEFLATE)) {
            result = new InflaterInputStream(encodedStream);
        } else if (encoding.equals(Encoding.ZIP)) {
            result = new ZipInputStream(encodedStream);
        } else if (encoding.equals(Encoding.IDENTITY)) {
            throw new IOException("Decoder unecessary for identity decoding");
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
        if (canDecode()) {
            return new ArrayList<Encoding>();
        } else {
            return wrappedEncodings;
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

        if (canDecode()) {
            boolean identity = true;
            for (Iterator<Encoding> iter = getEncodings().iterator(); identity
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
        InputStream result = null;

        if (canDecode()) {
            result = getWrappedRepresentation().getStream();
            for (int i = wrappedEncodings.size() - 1; i >= 0; i--) {
                if (!wrappedEncodings.get(i).equals(Encoding.IDENTITY)) {
                    result = getDecodedStream(wrappedEncodings.get(i), result);
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
        String result = null;

        if (canDecode()) {
            result = ByteUtils.toString(getStream(), getCharacterSet());
        } else {
            result = getWrappedRepresentation().getText();
        }

        return result;
    }

    /**
     * Writes the representation to a byte stream.
     * 
     * @param outputStream
     *                The output stream.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (canDecode()) {
            ByteUtils.write(getStream(), outputStream);
        } else {
            getWrappedRepresentation().write(outputStream);
        }
    }

    /**
     * Writes the representation to a byte channel.
     * 
     * @param writableChannel
     *                A writable byte channel.
     */
    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        if (canDecode()) {
            write(ByteUtils.getStream(writableChannel));
        } else {
            getWrappedRepresentation().write(writableChannel);
        }
    }
}
