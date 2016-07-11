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

package org.restlet.engine.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;

// [excludes gwt]
/**
 * Output stream connected to a non-blocking writable channel.
 * 
 * @author Jerome Louvel
 */
public class NbChannelOutputStream extends OutputStream {

    /** The internal byte buffer. */
    private final ByteBuffer bb = ByteBuffer.allocate(IoUtils.BUFFER_SIZE);

    /** The channel to write to. */
    private final WritableByteChannel channel;

    /** The selectable channel to write to. */
    private final SelectableChannel selectableChannel;

    /** The selection key. */
    private volatile SelectionKey selectionKey;

    /** The selector. */
    private volatile Selector selector;

    /**
     * Constructor.
     * 
     * @param channel
     *            The wrapped channel.
     */
    public NbChannelOutputStream(WritableByteChannel channel) {
        if (channel instanceof SelectableChannel) {
            this.selectableChannel = (SelectableChannel) channel;
        } else {
            this.selectableChannel = null;
        }

        this.channel = channel;
        this.selector = null;
    }

    /**
     * Effectively write the current byte buffer.
     * 
     * @throws IOException
     */
    private void doWrite() throws IOException {
        if ((this.channel != null) && (this.bb != null)) {
            try {
                int bytesWritten;
                int attempts = 0;

                while (this.bb.hasRemaining()) {
                    bytesWritten = this.channel.write(this.bb);
                    attempts++;

                    if (bytesWritten < 0) {
                        throw new EOFException(
                                "Unexpected negative number of bytes written. End of file detected.");
                    } else if (bytesWritten == 0) {
                        if (this.selectableChannel != null) {
                            if (this.selector == null) {
                                this.selector = SelectorFactory.getSelector();
                            }

                            if (this.selector == null) {
                                if (attempts > 2) {
                                    throw new IOException(
                                            "Unable to obtain a selector. Selector factory returned null.");
                                }
                            } else {
                                // Register a selector to write more
                                this.selectionKey = this.selectableChannel
                                        .register(this.selector,
                                                SelectionKey.OP_WRITE);

                                if (this.selector.select(IoUtils.TIMEOUT_MS) == 0) {
                                    if (attempts > 2) {
                                        throw new IOException(
                                                "Unable to select the channel to write to it. Selection timed out.");
                                    }
                                } else {
                                    attempts--;
                                }
                            }
                        }
                    } else {
                        attempts = 0;
                    }
                }
            } catch (IOException ioe) {
                throw new IOException(
                        "Unable to write to the non-blocking channel. "
                                + ioe.getLocalizedMessage());
            } finally {
                this.bb.clear();
                IoUtils.release(this.selector, this.selectionKey);
            }
        } else {
            throw new IOException(
                    "Unable to write. Null byte buffer or channel detected.");
        }
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        for (int index = 0; index < len; index = index + IoUtils.BUFFER_SIZE) {
            int size = len - index > IoUtils.BUFFER_SIZE ? IoUtils.BUFFER_SIZE
                    : len - index;
            this.bb.clear();
            this.bb.put(b, index, size);
            this.bb.flip();
            doWrite();
        }
    }

    @Override
    public void write(int b) throws IOException {
        this.bb.clear();
        this.bb.put((byte) b);
        this.bb.flip();
        doWrite();
    }
}
