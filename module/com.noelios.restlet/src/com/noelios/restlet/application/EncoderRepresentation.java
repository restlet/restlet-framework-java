/*
 * Copyright 2005-2007 Noelios Consulting.
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
import java.util.Arrays;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import org.restlet.data.Encoding;
import org.restlet.resource.Representation;
import org.restlet.util.ByteUtils;
import org.restlet.util.WrapperRepresentation;

/**
 * Content that encodes a wrapped content.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class EncoderRepresentation extends WrapperRepresentation {
    /** Indicates if the encoding can happen. */
    private boolean canEncode;

    /** The encoding to apply. */
    private Encoding encoding;

    /**
     * Constructor.
     * 
     * @param encoding
     *            Encoder algorithm.
     * @param wrappedRepresentation
     *            The wrapped representation.
     */
    public EncoderRepresentation(Encoding encoding,
            Representation wrappedRepresentation) {
        super(wrappedRepresentation);
        this.canEncode = getSupportedEncodings().contains(encoding);
        this.encoding = encoding;
    }

    /**
     * Indicates if the encoding can happen.
     * 
     * @return True if the encoding can happen.
     */
    public boolean canEncode() {
        return this.canEncode;
    }

    /**
     * Returns the size in bytes of the encoded representation if known,
     * UNKNOWN_SIZE (-1) otherwise.
     * 
     * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     */
    public long getSize() {
        long result = -1;

        if (canEncode()) {
            if (getEncoding().equals(Encoding.IDENTITY)) {
                result = getWrappedRepresentation().getSize();
            }
        } else {
            result = getWrappedRepresentation().getSize();
        }

        return result;
    }

    /**
     * Returns the encoding or null if identity encoding applies.
     * 
     * @return The encoding or null if identity encoding applies.
     */
    public Encoding getEncoding() {
        if (canEncode()) {
            return this.encoding;
        } else {
            return getWrappedRepresentation().getEncoding();
        }
    }

    /**
     * Sets the encoding or null if identity encoding applies.
     * 
     * @param encoding
     *            The encoding or null if identity encoding applies.
     */
    public void setEncoding(Encoding encoding) {
        this.canEncode = getSupportedEncodings().contains(encoding);
        this.encoding = encoding;
    }

    /**
     * Returns a readable byte channel. If it is supported by a file a read-only
     * instance of FileChannel is returned.
     * 
     * @return A readable byte channel.
     */
    public ReadableByteChannel getChannel() throws IOException {
        if (canEncode()) {
            return ByteUtils.getChannel(getStream());
        } else {
            return getWrappedRepresentation().getChannel();
        }
    }

    /**
     * Returns a stream with the representation's content.
     * 
     * @return A stream with the representation's content.
     */
    public InputStream getStream() throws IOException {
        if (canEncode()) {
            return ByteUtils.getStream(this);
        } else {
            return getWrappedRepresentation().getStream();
        }
    }

    /**
     * Writes the representation to a byte channel.
     * 
     * @param writableChannel
     *            A writable byte channel.
     */
    public void write(WritableByteChannel writableChannel) throws IOException {
        if (canEncode()) {
            write(ByteUtils.getStream(writableChannel));
        } else {
            getWrappedRepresentation().write(writableChannel);
        }
    }

    /**
     * Writes the representation to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     */
    public void write(OutputStream outputStream) throws IOException {
        if (canEncode()) {
            DeflaterOutputStream encoderOutputStream = null;

            if (getEncoding().equals(Encoding.GZIP)) {
                encoderOutputStream = new GZIPOutputStream(outputStream);
            } else if (getEncoding().equals(Encoding.DEFLATE)) {
                encoderOutputStream = new DeflaterOutputStream(outputStream);
            } else if (getEncoding().equals(Encoding.ZIP)) {
                encoderOutputStream = new ZipOutputStream(outputStream);
            } else if (getEncoding().equals(Encoding.IDENTITY)) {
                // Encoder unecessary for identity encoding
            }

            if (encoderOutputStream != null) {
                getWrappedRepresentation().write(encoderOutputStream);
                encoderOutputStream.finish();
            } else {
                getWrappedRepresentation().write(outputStream);
            }
        } else {
            getWrappedRepresentation().write(outputStream);
        }
    }

    /**
     * Converts the representation to a string value. Be careful when using this
     * method as the conversion of large content to a string fully stored in
     * memory can result in OutOfMemoryErrors being thrown.
     * 
     * @return The representation as a string value.
     */
    public String getText() throws IOException {
        String result = null;

        if (canEncode()) {
            result = ByteUtils.toString(getStream());
        } else {
            result = getWrappedRepresentation().getText();
        }

        return result;
    }

    /**
     * Returns the list of supported encodings.
     * 
     * @return The list of supported encodings.
     */
    public static List<Encoding> getSupportedEncodings() {
        return Arrays.<Encoding> asList(Encoding.GZIP, Encoding.DEFLATE,
                Encoding.ZIP, Encoding.IDENTITY);
    }
}
