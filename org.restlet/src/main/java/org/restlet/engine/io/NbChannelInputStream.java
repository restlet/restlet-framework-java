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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.util.SelectionListener;
import org.restlet.util.SelectionRegistration;

// [excludes gwt]
/**
 * Input stream connected to a non-blocking readable channel.
 * 
 * @author Jerome Louvel
 */
public class NbChannelInputStream extends InputStream {

    /** The internal byte buffer. */
    private final ByteBuffer byteBuffer;

    /** The channel to read from. */
    private final ReadableByteChannel channel;

    /** Indicates if further reads can be attempted. */
    private volatile boolean endReached;

    /** The registered selection registration. */
    private volatile SelectionRegistration selectionRegistration;

    /** The optional selectable channel to read from. */
    private final SelectableChannel selectableChannel;

    /** The optional selection channel to read from. */
    private final SelectionChannel selectionChannel;

    /**
     * Constructor.
     * 
     * @param channel
     *            The channel to read from.
     */
    public NbChannelInputStream(ReadableByteChannel channel) {
        this.channel = channel;

        if (channel instanceof ReadableSelectionChannel) {
            this.selectionChannel = (ReadableSelectionChannel) channel;
            this.selectableChannel = null;
        } else if (channel instanceof SelectableChannel) {
            this.selectionChannel = null;
            this.selectableChannel = (SelectableChannel) channel;
        } else if (channel instanceof SelectionChannel) {
            this.selectionChannel = (SelectionChannel) channel;
            this.selectableChannel = null;
        } else {
            this.selectionChannel = null;
            this.selectableChannel = null;
        }

        this.byteBuffer = ByteBuffer.allocate(IoUtils.BUFFER_SIZE);
        this.byteBuffer.flip();
        this.endReached = false;
        this.selectionRegistration = null;
    }

    @Override
    public int read() throws IOException {
        int result = -1;

        if (!this.endReached) {
            if (!this.byteBuffer.hasRemaining()) {
                // Let's refill
                refill();
            }

            if (!this.endReached) {
                // Let's return the next one
                result = this.byteBuffer.get() & 0xff;
            }
        }

        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = -1;

        if (!this.endReached) {
            if (!this.byteBuffer.hasRemaining()) {
                // Let's try to refill
                refill();
            }

            if (!this.endReached) {
                // Let's return the next ones
                result = Math.min(len, this.byteBuffer.remaining());
                this.byteBuffer.get(b, off, result);
            }
        }

        return result;
    }

    /**
     * Reads the available bytes from the channel into the byte buffer.
     * 
     * @return The number of bytes read or -1 if the end of channel has been
     *         reached.
     * @throws IOException
     */
    private int readChannel() throws IOException {
        int result = 0;
        this.byteBuffer.clear();
        result = this.channel.read(this.byteBuffer);

        if (result > 0) {
            if (Context.getCurrentLogger().isLoggable(Level.FINE)) {
                Context.getCurrentLogger().log(
                        Level.FINE,
                        "NbChannelInputStream#readChannel : " + result
                                + " bytes read");
            }

            this.byteBuffer.flip();
        }

        return result;
    }

    /**
     * Refill the byte buffer by attempting to read the channel.
     * 
     * @throws IOException
     */
    private void refill() throws IOException {
        int bytesRead = 0;

        while (bytesRead == 0) {
            bytesRead = readChannel();

            if (bytesRead == 0) {
                // No bytes were read, try to register
                // a select key to get more
                if (selectionChannel != null) {
                    try {
                        if (this.selectionRegistration == null) {
                            this.selectionRegistration = this.selectionChannel
                                    .getRegistration();
                            this.selectionRegistration
                                    .setInterestOperations(SelectionKey.OP_READ);
                            this.selectionRegistration
                                    .setSelectionListener(new SelectionListener() {
                                        public void onSelected(
                                                SelectionRegistration registration)
                                                throws IOException {
                                            if (Context.getCurrentLogger()
                                                    .isLoggable(Level.FINER)) {
                                                Context.getCurrentLogger()
                                                        .log(Level.FINER,
                                                                "NbChannelInputStream selected");
                                            }

                                            // Stop listening at this point
                                            selectionRegistration.suspend();

                                            // Unblock the user thread
                                            selectionRegistration.unblock();
                                        }
                                    });
                        } else {
                            this.selectionRegistration.resume();
                        }

                        // Block until new content arrives or a timeout occurs
                        this.selectionRegistration.block();

                        // Attempt to read more content
                        bytesRead = readChannel();
                    } catch (Exception e) {
                        Context.getCurrentLogger()
                                .log(Level.FINE,
                                        "Exception while registering or waiting for new content",
                                        e);
                    }
                } else if (selectableChannel != null) {
                    Selector selector = null;
                    SelectionKey selectionKey = null;

                    try {
                        selector = SelectorFactory.getSelector();

                        if (selector != null) {
                            selectionKey = this.selectableChannel.register(
                                    selector, SelectionKey.OP_READ);
                            selector.select(IoUtils.TIMEOUT_MS);
                        }
                    } finally {
                        IoUtils.release(selector, selectionKey);
                    }

                    bytesRead = readChannel();
                }
            }
        }

        if (bytesRead == -1) {
            this.endReached = true;

            if (this.selectionRegistration != null) {
                this.selectionRegistration.setCanceling(true);
                this.selectionRegistration.setSelectionListener(null);
            }
        }
    }
}