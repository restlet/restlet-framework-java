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

package org.restlet.representation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.io.IoUtils;
import org.restlet.util.WrapperRepresentation;

/**
 * Representation capable of buffering the wrapped representation. This is
 * useful when you want to prevent chunk encoding from being used for dynamic
 * representations or when you want to reuse a transient representation several
 * times.<br>
 * <br>
 * Be careful as this class could create potentially very large byte buffers in
 * memory that could impact your application performance.
 * 
 * @author Thierry Boileau
 */
public class BufferingRepresentation extends WrapperRepresentation {

    /** The cached content as an array of bytes. */
    private volatile byte[] buffer;

    /** Indicates if the wrapped entity has been already cached. */
    private volatile boolean buffered;

    /**
     * Constructor.
     * 
     * @param bufferedRepresentation
     *            The representation to buffer.
     */
    public BufferingRepresentation(Representation bufferedRepresentation) {
        super(bufferedRepresentation);
        setTransient(false);
    }

    /**
     * Buffers the content of the wrapped entity.
     * 
     * @throws IOException
     */
    private void buffer() throws IOException {
        if (!isBuffered()) {
            if (getWrappedRepresentation().isAvailable()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                getWrappedRepresentation().write(baos);
                baos.flush();
                setBuffer(baos.toByteArray());
                setBuffered(true);
            }
        }
    }

    @Override
    public long getAvailableSize() {
        return getSize();
    }

    /**
     * Returns the buffered content as an array of bytes.
     * 
     * @return The buffered content as an array of bytes.
     */
    protected byte[] getBuffer() {
        return buffer;
    }

    @Override
    public java.nio.channels.ReadableByteChannel getChannel()
            throws IOException {
        return IoUtils.getChannel(getStream());
    }

    @Override
    public Reader getReader() throws IOException {
        return IoUtils.getReader(getStream(), getCharacterSet());
    }

    @Override
    public long getSize() {
        // Read the content, store it and compute the size.
        try {
            buffer();
        } catch (IOException e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to buffer the wrapped representation", e);
        }

        return (getBuffer() != null) ? getBuffer().length : -1l;
    }

    @Override
    public InputStream getStream() throws IOException {
        buffer();
        return (getBuffer() != null) ? new ByteArrayInputStream(getBuffer())
                : null;
    };

    @Override
    public String getText() throws IOException {
        buffer();

        if (getBuffer() != null) {
            return (getCharacterSet() != null) ? 
                    new String(getBuffer(), getCharacterSet().toCharset().name()) : 
                    new String(getBuffer());
        }

        return null;
    }

    @Override
    public boolean isAvailable() {
        try {
            buffer();
        } catch (IOException e) {
            Context.getCurrentLogger().log(Level.FINER,
                    "Unable to buffer the wrapped representation", e);
        }

        return isBuffered();
    }

    /**
     * Indicates if the wrapped entity has been already buffered.
     * 
     * @return True if the wrapped entity has been already buffered.
     */
    protected boolean isBuffered() {
        return buffered;
    }

    /**
     * Sets the buffered content as an array of bytes.
     * 
     * @param buffer
     *            The buffered content as an array of bytes.
     */
    protected void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    /**
     * Indicates if the wrapped entity has been already buffered.
     * 
     * @param buffered
     *            True if the wrapped entity has been already buffered.
     */
    protected void setBuffered(boolean buffered) {
        this.buffered = buffered;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        buffer();

        if (getBuffer() != null) {
            outputStream.write(getBuffer());
        }
    }

    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        buffer();

        if (getBuffer() != null) {
            writableChannel.write(ByteBuffer.wrap(getBuffer()));
        }
    }

    @Override
    public void write(Writer writer) throws IOException {
        String text = getText();

        if (text != null) {
            writer.write(text);
        }
    }

}
