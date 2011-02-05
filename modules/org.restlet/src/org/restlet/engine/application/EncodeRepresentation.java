/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.restlet.data.Disposition;
import org.restlet.data.Encoding;
import org.restlet.engine.io.BioUtils;
import org.restlet.engine.io.NioUtils;
import org.restlet.representation.Representation;
import org.restlet.util.WrapperList;
import org.restlet.util.WrapperRepresentation;

// [excludes gwt]
/**
 * Content that encodes a wrapped content. Allows to apply only one encoding.
 * 
 * @author Jerome Louvel
 */
public class EncodeRepresentation extends WrapperRepresentation {
    /**
     * Returns the list of supported encodings.
     * 
     * @return The list of supported encodings.
     */
    public static List<Encoding> getSupportedEncodings() {
        return Arrays.<Encoding> asList(Encoding.GZIP, Encoding.DEFLATE,
                Encoding.ZIP, Encoding.IDENTITY);
    }

    /** Indicates if the encoding can happen. */
    private volatile boolean canEncode;

    /** The encoding to apply. */
    private volatile Encoding encoding;

    /** The applied encodings. */
    private volatile List<Encoding> encodings;

    /**
     * Constructor.
     * 
     * @param encoding
     *            Encoder algorithm.
     * @param wrappedRepresentation
     *            The wrapped representation.
     */
    public EncodeRepresentation(Encoding encoding,
            Representation wrappedRepresentation) {
        super(wrappedRepresentation);
        this.canEncode = getSupportedEncodings().contains(encoding);
        this.encodings = null;
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
     * Returns the available size in bytes of the encoded representation if
     * known, UNKNOWN_SIZE (-1) otherwise.
     * 
     * @return The available size in bytes if known, UNKNOWN_SIZE (-1)
     *         otherwise.
     */
    @Override
    public long getAvailableSize() {
        long result = UNKNOWN_SIZE;

        if (canEncode()) {
            if (this.encoding.equals(Encoding.IDENTITY)) {
                result = getWrappedRepresentation().getAvailableSize();
            }
        } else {
            result = getWrappedRepresentation().getAvailableSize();
        }

        return result;
    }

    /**
     * Returns a readable byte channel. If it is supported by a file a read-only
     * instance of FileChannel is returned.
     * 
     * @return A readable byte channel.
     */
    @Override
    public ReadableByteChannel getChannel() throws IOException {
        if (canEncode()) {
            return NioUtils.getChannel(getStream());
        }

        return getWrappedRepresentation().getChannel();
    }

    /**
     * Returns the applied encodings.
     * 
     * @return The applied encodings.
     */
    @Override
    public List<Encoding> getEncodings() {
        if (this.encodings == null) {
            this.encodings = new WrapperList<Encoding>() {

                @Override
                public boolean add(Encoding element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    }

                    return super.add(element);
                }

                @Override
                public void add(int index, Encoding element) {
                    if (element == null) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    }

                    super.add(index, element);
                }

                @Override
                public boolean addAll(Collection<? extends Encoding> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (final Iterator<? extends Encoding> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    }

                    return super.addAll(elements);
                }

                @Override
                public boolean addAll(int index,
                        Collection<? extends Encoding> elements) {
                    boolean addNull = (elements == null);
                    if (!addNull) {
                        for (final Iterator<? extends Encoding> iterator = elements
                                .iterator(); !addNull && iterator.hasNext();) {
                            addNull = (iterator.next() == null);
                        }
                    }
                    if (addNull) {
                        throw new IllegalArgumentException(
                                "Cannot add a null encoding.");
                    }

                    return super.addAll(index, elements);
                }
            };
            this.encodings.addAll(getWrappedRepresentation().getEncodings());
            if (canEncode()) {
                this.encodings.add(this.encoding);
            }
        }
        return this.encodings;
    }

    /**
     * Returns the size in bytes of the encoded representation if known,
     * UNKNOWN_SIZE (-1) otherwise.
     * 
     * @return The size in bytes if known, UNKNOWN_SIZE (-1) otherwise.
     */
    @Override
    public long getSize() {
        long result = UNKNOWN_SIZE;

        if (canEncode()) {
            if (this.encoding.equals(Encoding.IDENTITY)) {
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
        if (canEncode()) {
            return BioUtils.getStream(this);
        }

        return getWrappedRepresentation().getStream();
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

        if (canEncode()) {
            result = BioUtils.toString(getStream(), getCharacterSet());
        } else {
            result = getWrappedRepresentation().getText();
        }

        return result;
    }

    /**
     * Writes the representation to a byte stream.
     * 
     * @param outputStream
     *            The output stream.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (canEncode()) {
            DeflaterOutputStream encoderOutputStream = null;

            if (this.encoding.equals(Encoding.GZIP)) {
                encoderOutputStream = new GZIPOutputStream(outputStream);
            } else if (this.encoding.equals(Encoding.DEFLATE)) {
                encoderOutputStream = new DeflaterOutputStream(outputStream);
            } else if (this.encoding.equals(Encoding.ZIP)) {
                final ZipOutputStream stream = new ZipOutputStream(outputStream);
                String name = "entry";
                if (getWrappedRepresentation().getDisposition() != null) {
                    name = getWrappedRepresentation().getDisposition()
                            .getParameters().getFirstValue(
                                    Disposition.NAME_FILENAME, true, name);
                }
                stream.putNextEntry(new ZipEntry(name));

                encoderOutputStream = stream;
            } else if (this.encoding.equals(Encoding.IDENTITY)) {
                // Encoder unnecessary for identity encoding
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
     * Writes the representation to a byte channel.
     * 
     * @param writableChannel
     *            A writable byte channel.
     */
    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        if (canEncode()) {
            write(NioUtils.getStream(writableChannel));
        } else {
            getWrappedRepresentation().write(writableChannel);
        }
    }
}
