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

package org.restlet.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;

/**
 * Selection listener notifying new content is read into a {@link ByteBuffer}.
 * 
 * @author Jerome Louvel
 */
public abstract class ReadingListener implements SelectionListener {

    /** The internal byte buffer. */
    private final ByteBuffer byteBuffer;

    /** The byte channel to read from when selected. */
    private final ReadableByteChannel byteChannel;

    /**
     * Constructor. Uses a byte buffer of a given size.
     * 
     * @param byteChannel
     *            The source byte channel.
     * @param byteBuffer
     *            The byte buffer to use.
     * @throws IOException
     */
    public ReadingListener(ReadableByteChannel byteChannel,
            ByteBuffer byteBuffer) throws IOException {
        this.byteBuffer = byteBuffer;
        this.byteChannel = byteChannel;
    }

    /**
     * Constructor. Uses a byte buffer of a given size.
     * 
     * @param byteChannel
     *            The source byte channel.
     * @param bufferSize
     *            The size of the byte buffer to use.
     * @throws IOException
     */
    public ReadingListener(ReadableByteChannel byteChannel, int bufferSize)
            throws IOException {
        this(byteChannel, ByteBuffer.allocate(bufferSize));
    }

    /**
     * Default constructor. Uses a byte buffer of {@link IoUtils#BUFFER_SIZE}
     * length.
     * 
     * @param source
     *            The source representation.
     * @throws IOException
     */
    public ReadingListener(Representation source) throws IOException {
        this(source, IoUtils.BUFFER_SIZE);
    }

    /**
     * Constructor. Uses a byte buffer of a given size.
     * 
     * @param source
     *            The source byte channel.
     * @param bufferSize
     *            The size of the byte buffer to use.
     * @throws IOException
     */
    public ReadingListener(Representation source, int bufferSize)
            throws IOException {
        this(source.getChannel(), bufferSize);
    }

    /**
     * Callback invoked when new content is available.
     * 
     * @param byteBuffer
     *            The byte buffer filled with the new content (correctly flip).
     */
    protected abstract void onContent(ByteBuffer byteBuffer);

    /**
     * Callback invoked when the end of the representation has been reached. By
     * default, it does nothing.
     */
    protected void onEnd() {
    }

    /**
     * Callback invoked when an IO exception occurs. By default, it logs the
     * exception at the {@link Level#WARNING} level.
     * 
     * @param ioe
     *            The exception caught.
     */
    protected void onError(IOException ioe) {
        Context.getCurrentLogger().log(Level.WARNING, "", ioe);
    }

    /**
     * Callback invoked when new content is available. It reads the available
     * bytes from the source channel into an internal buffer then calls
     * {@link #onContent(ByteBuffer)} method or the {@link #onEnd()} method or
     * the {@link #onError(IOException)} method.
     */
    public final void onSelected(SelectionRegistration selectionRegistration)
            throws IOException {
        try {
            synchronized (this.byteBuffer) {
                this.byteBuffer.clear();
                int result = this.byteChannel.read(this.byteBuffer);

                if (result > 0) {
                    this.byteBuffer.flip();
                    onContent(this.byteBuffer);
                } else if (result == -1) {
                    onEnd();
                } else {
                    Context.getCurrentLogger().fine(
                            "NIO selection detected with no content available");
                }
            }
        } catch (IOException ioe) {
            onError(ioe);
        }
    }
}
