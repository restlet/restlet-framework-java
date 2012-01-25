/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
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
 * Selection listener notifying new content as an {@link ByteBuffer}.
 * 
 * @author Jerome Louvel
 */
public abstract class ReadableListener implements SelectionListener {

    /** The internal byte buffer. */
    private final ByteBuffer byteBuffer;

    /** The byte channel to read from when selected. */
    private final ReadableByteChannel byteChannel;

    /**
     * Default constructor. Uses a byte buffer of {@link IoUtils#BUFFER_SIZE}
     * length.
     * 
     * @param source
     *            The source representation.
     * @throws IOException
     */
    public ReadableListener(Representation source) throws IOException {
        this(source, IoUtils.BUFFER_SIZE);
    }

    /**
     * Constructor. Uses a byte buffer of a given size.
     * 
     * @param source
     *            The source byte channel.
     * @param bufferSize
     *            The byte buffer to use.
     * @throws IOException
     */
    public ReadableListener(Representation source, int bufferSize)
            throws IOException {
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
        this.byteChannel = source.getChannel();
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
     * {@link #onContent(ByteBuffer)}.
     */
    public final void onSelected() {
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
