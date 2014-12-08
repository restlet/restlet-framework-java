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

package org.restlet.ext.nio.internal.channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.WakeupListener;
import org.restlet.ext.nio.internal.buffer.Buffer;
import org.restlet.ext.nio.internal.buffer.BufferState;
import org.restlet.ext.nio.internal.state.ChunkState;
import org.restlet.ext.nio.internal.util.CompletionListener;

// [excludes gwt]
/**
 * Readable byte channel capable of decoding chunked entities.
 */
public class ReadableChunkedChannel extends ReadableBufferedChannel {

    /** The chunk state. */
    private volatile ChunkState chunkState;

    /** The line builder to parse chunk size or trailer. */
    private final StringBuilder lineBuilder;

    /** The line builder state. */
    private volatile BufferState lineBuilderState;

    /** The remaining chunk size that should be read from the source channel. */
    private volatile int remainingChunkSize;

    /**
     * Constructor.
     * 
     * @param completionListener
     *            The listener to callback upon reading completion.
     * @param buffer
     *            The source byte buffer, typically remaining from previous read
     *            processing.
     * @param source
     *            The source channel.
     * @param wakeupListener
     *            The wakeup listener that will be notified.
     */
    public ReadableChunkedChannel(CompletionListener completionListener,
            Buffer buffer, ReadableSelectionChannel source,
            WakeupListener wakeupListener) {
        super(completionListener, buffer, source, wakeupListener);

        if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
            Context.getCurrentLogger().log(
                    Level.FINER,
                    "ReadableChunkedChannel created from: " + source
                            + ". Registration: " + getRegistration());
        }

        this.remainingChunkSize = 0;
        this.chunkState = ChunkState.SIZE;
        this.lineBuilder = new StringBuilder();
        this.lineBuilderState = BufferState.IDLE;
    }

    /**
     * Clears the line builder and adjust its state.
     */
    protected void clearLineBuilder() {
        getLineBuilder().delete(0, getLineBuilder().length());
        setLineBuilderState(BufferState.IDLE);
    }

    /**
     * Returns the chunk state.
     * 
     * @return The chunk state.
     */
    protected ChunkState getChunkState() {
        return chunkState;
    }

    /**
     * Returns the line builder to parse chunk size or trailer.
     * 
     * @return The line builder to parse chunk size or trailer.
     */
    protected StringBuilder getLineBuilder() {
        return lineBuilder;
    }

    /**
     * Returns the line builder state.
     * 
     * @return The line builder state.
     */
    protected BufferState getLineBuilderState() {
        return lineBuilderState;
    }

    /**
     * Returns the remaining chunk size that should be read from the source
     * channel.
     * 
     * @return The remaining chunk size that should be read from the source
     *         channel.
     */
    protected int getRemainingChunkSize() {
        return remainingChunkSize;
    }

    /**
     * Drains the byte buffer.
     * 
     * @param buffer
     *            The IO buffer to drain.
     * @param maxDrained
     *            The maximum number of bytes drained by this call.
     * @param args
     *            The optional arguments to pass back to the callbacks.
     * @return The number of "functional" bytes drained.
     * @throws IOException
     */
    public int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        int result = 0;
        ByteBuffer targetBuffer = (ByteBuffer) args[0];
        boolean doLoop = true;

        while (doLoop) {
            if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
                Context.getCurrentLogger().log(Level.FINER,
                        "Readable chunk state: " + getChunkState());
            }

            switch (getChunkState()) {
            case SIZE:
                // Some bytes are available, fill the line builder
                setLineBuilderState(buffer.drain(getLineBuilder(),
                        getLineBuilderState()));

                if (getLineBuilderState() == BufferState.DRAINING) {
                    // The chunk size line was fully read into the line builder
                    int length = getLineBuilder().length();

                    if (length == 0) {
                        throw new IOException(
                                "An empty chunk size line was detected");
                    }

                    int index = getLineBuilder().indexOf(";");
                    index = (index == -1) ? getLineBuilder().length() : index;

                    try {
                        setRemainingChunkSize(Integer.parseInt(getLineBuilder()
                                .substring(0, index).trim(), 16));

                        if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
                            Context.getCurrentLogger().log(
                                    Level.FINER,
                                    "New readable chunk detected. Size: "
                                            + this.remainingChunkSize);
                        }
                    } catch (NumberFormatException ex) {
                        throw new IOException("\"" + getLineBuilder()
                                + "\" has an invalid chunk size");
                    } finally {
                        clearLineBuilder();
                    }

                    if (getRemainingChunkSize() == 0) {
                        setChunkState(ChunkState.TRAILER);
                    } else {
                        setChunkState(ChunkState.DATA);
                    }
                    break;
                } else {
                    // Need to fill more content into the buffer.
                    doLoop = false;
                }

                break;

            case DATA:
                int read = 0;
                if (getRemainingChunkSize() > 0) {
                    read = super.onDrain(buffer, this.remainingChunkSize,
                            targetBuffer);
                    result += read;

                    if (read > 0) {
                        setRemainingChunkSize(getRemainingChunkSize() - read);
                    } else {
                        if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
                            Context.getCurrentLogger().finer(
                                    "No readable chunk data found");
                        }
                    }
                }

                if (getRemainingChunkSize() == 0) {
                    // Try to read the new line marking the end of the chunk.
                    setLineBuilderState(buffer.drain(getLineBuilder(),
                            getLineBuilderState()));

                    if (getLineBuilderState() == BufferState.DRAINING) {
                        // Done, can read the next chunk
                        setChunkState(ChunkState.SIZE);
                        clearLineBuilder();
                    } else {
                        // Need to fill more content into the buffer.
                        doLoop = false;
                    }
                } else {
                    // Need to fill more content into the buffer.
                    doLoop = false;
                }
                break;

            case TRAILER:
                // TODO
                setChunkState(ChunkState.END);
                break;

            case END:
                // Try to read the new line marking the end of the chunk.
                setLineBuilderState(buffer.drain(getLineBuilder(),
                        getLineBuilderState()));

                if (getLineBuilderState() == BufferState.DRAINING) {
                    if (getLineBuilder().length() != 0) {
                        Context.getCurrentLogger()
                                .log(Level.FINE,
                                        "The last readable chunk line had a non empty line");
                    }

                    setEndReached(true);

                    if (result <= 0) {
                        result = -1;
                    }

                    doLoop = false;
                } else {
                    // Need to fill more content into the buffer.
                    doLoop = false;
                }

                break;
            }
        }

        return result;
    }

    /**
     * Sets the chunk state.
     * 
     * @param chunkState
     *            The chunk state.
     */
    protected void setChunkState(ChunkState chunkState) {
        this.chunkState = chunkState;
    }

    /**
     * Sets the line builder state.
     * 
     * @param lineBuilderState
     *            The line builder state.
     */
    protected void setLineBuilderState(BufferState lineBuilderState) {
        this.lineBuilderState = lineBuilderState;
    }

    /**
     * Sets the remaining chunk size that should be read from the source
     * channel.
     * 
     * @param remainingChunkSize
     *            The remaining chunk size that should be read from the source
     *            channel.
     */
    protected void setRemainingChunkSize(int remainingChunkSize) {
        this.remainingChunkSize = remainingChunkSize;
    }
}
