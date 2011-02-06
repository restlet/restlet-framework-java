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

package org.restlet.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;

/**
 * Selection listener notifying new content as an {@link InputStream}.
 * 
 * @author Jerome Louvel
 */
public abstract class InputListener implements SelectionListener {

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
    public InputListener(Representation source) throws IOException {
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
    public InputListener(Representation source, int bufferSize)
            throws IOException {
        this.byteBuffer = ByteBuffer.allocate(bufferSize);
        this.byteChannel = source.getChannel();
    }

    /**
     * Callback invoked when new content is available.
     * 
     * @param inputStream
     *            The input stream allowing to retrieve the new content.
     */
    protected abstract void onContent(InputStream inputStream);

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
     * {@link #onContent(InputStream)}.
     */
    public final void onSelected() {
        try {
            synchronized (byteBuffer) {
                byteBuffer.clear();
                int result = byteChannel.read(byteBuffer);

                if (result > 0) {
                    byteBuffer.flip();
                    ByteArrayInputStream bais = new ByteArrayInputStream(
                            byteBuffer.array(), byteBuffer.arrayOffset(),
                            byteBuffer.remaining());
                    onContent(bais);
                } else if (result == -1) {
                    onEnd();
                } else {
                    Context.getCurrentLogger()
                            .fine("Input stream selection found with no content available");
                }
            }
        } catch (IOException ioe) {
            onError(ioe);
        }
    }
}
