/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.engine.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.Edition;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.representation.Representation;

// [excludes gwt]
/**
 * Utility methods for NIO processing.
 * 
 * @author Jerome Louvel
 */
public class NioUtils {

    /**
     * Writes the representation to a byte channel. Optimizes using the file
     * channel transferTo method.
     * 
     * @param fileChannel
     *            The readable file channel.
     * @param writableChannel
     *            A writable byte channel.
     */
    public static void copy(FileChannel fileChannel,
            WritableByteChannel writableChannel) throws IOException {
        long position = 0;
        long count = fileChannel.size();
        long written = 0;
        SelectableChannel selectableChannel = null;

        if (writableChannel instanceof SelectableChannel) {
            selectableChannel = (SelectableChannel) writableChannel;
        }

        while (count > 0) {
            NioUtils.waitForState(selectableChannel, SelectionKey.OP_WRITE);
            written = fileChannel.transferTo(position, count, writableChannel);
            position += written;
            count -= written;
        }
    }

    /**
     * Writes a NIO readable channel to a BIO output stream.
     * 
     * @param readableChannel
     *            The readable channel.
     * @param outputStream
     *            The output stream.
     * @throws IOException
     */
    public static void copy(ReadableByteChannel readableChannel,
            OutputStream outputStream) throws IOException {
        if ((readableChannel != null) && (outputStream != null)) {
            BioUtils.copy(new NbChannelInputStream(readableChannel),
                    outputStream);
        }
    }

    /**
     * Writes a readable channel to a writable channel.
     * 
     * @param readableChannel
     *            The readable channel.
     * @param writableChannel
     *            The writable channel.
     * @throws IOException
     */
    public static void copy(ReadableByteChannel readableChannel,
            WritableByteChannel writableChannel) throws IOException {
        if ((readableChannel != null) && (writableChannel != null)) {
            BioUtils.copy(new NbChannelInputStream(readableChannel),
                    new NbChannelOutputStream(writableChannel));
        }
    }

    /**
     * Read the current message line (start line or header line).
     * 
     * @param lineBuilder
     *            The line builder to fill.
     * @param builderState
     *            The builder state.
     * @param byteBuffer
     *            The byte buffer to read from.
     * @return The new builder state.
     * @throws IOException
     */
    public static BufferState fillLine(StringBuilder lineBuilder,
            BufferState builderState, ByteBuffer byteBuffer) throws IOException {
        int next;

        if (builderState == BufferState.IDLE) {
            builderState = BufferState.FILLING;
        }

        while ((builderState != BufferState.DRAINING)
                && byteBuffer.hasRemaining()) {
            next = (int) byteBuffer.get();

            switch (builderState) {
            case FILLING:
                if (HeaderUtils.isCarriageReturn(next)) {
                    builderState = BufferState.FILLED;
                } else {
                    lineBuilder.append((char) next);
                }

                break;

            case FILLED:
                if (HeaderUtils.isLineFeed(next)) {
                    builderState = BufferState.DRAINING;
                } else {
                    throw new IOException(
                            "Missing line feed character at the end of the line. Found character \""
                                    + (char) next + "\" (" + next + ") instead");
                }

                break;
            }
        }

        return builderState;
    }

    /**
     * Returns a readable byte channel based on a given input stream. If it is
     * supported by a file a read-only instance of FileChannel is returned.
     * 
     * @param inputStream
     *            The input stream to convert.
     * @return A readable byte channel.
     */
    public static ReadableByteChannel getChannel(InputStream inputStream) {
        ReadableByteChannel result = null;

        if (inputStream instanceof FileInputStream) {
            result = ((FileInputStream) inputStream).getChannel();
        } else if (inputStream != null) {
            result = new InputStreamChannel(inputStream);
        }

        return result;
    }

    /**
     * Returns a writable byte channel based on a given output stream.
     * 
     * @param outputStream
     *            The output stream.
     * @return A writable byte channel.
     */
    public static WritableByteChannel getChannel(OutputStream outputStream) {
        return (outputStream != null) ? Channels.newChannel(outputStream)
                : null;
    }

    /**
     * Returns a readable byte channel based on the given representation's
     * content and its write(WritableByteChannel) method. Internally, it uses a
     * writer thread and a pipe channel.
     * 
     * @param representation
     *            the representation to get the {@link OutputStream} from.
     * @return A readable byte channel.
     * @throws IOException
     */
    public static ReadableByteChannel getChannel(
            final Representation representation) throws IOException {
        ReadableByteChannel result = null;
        if (Edition.CURRENT != Edition.GAE) {
            // [ifndef gae]
            final java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
            final org.restlet.Application application = org.restlet.Application
                    .getCurrent();

            // Get a thread that will handle the task of continuously
            // writing the representation into the input side of the pipe
            application.getTaskService().execute(new Runnable() {
                public void run() {
                    try {
                        WritableByteChannel wbc = pipe.sink();
                        representation.write(wbc);
                        wbc.close();
                    } catch (IOException ioe) {
                        Context.getCurrentLogger().log(Level.FINE,
                                "Error while writing to the piped channel.",
                                ioe);
                    }
                }
            });

            result = pipe.source();
            // [enddef]
        } else {
            Context.getCurrentLogger()
                    .log(Level.WARNING,
                            "The GAE edition is unable to return a channel for a representation given its write(WritableByteChannel) method.");
        }
        return result;
    }

    /**
     * Returns an input stream based on a given readable byte channel.
     * 
     * @param readableChannel
     *            The readable byte channel.
     * @return An input stream based on a given readable byte channel.
     */
    public static InputStream getStream(ReadableByteChannel readableChannel) {
        InputStream result = null;

        if (readableChannel != null) {
            result = new NbChannelInputStream(readableChannel);
        }

        return result;
    }

    /**
     * Returns an output stream based on a given writable byte channel.
     * 
     * @param writableChannel
     *            The writable byte channel.
     * @return An output stream based on a given writable byte channel.
     */
    public static OutputStream getStream(WritableByteChannel writableChannel) {
        return isBlocking(writableChannel) ? Channels
                .newOutputStream(writableChannel) : new NbChannelOutputStream(
                writableChannel);
    }

    /**
     * Indicates if the channel is in blocking mode. It returns false when the
     * channel is selectable and configured to be non blocking.
     * 
     * @param channel
     *            The channel to test.
     * @return True if the channel is in blocking mode.
     */
    public static boolean isBlocking(Channel channel) {
        boolean result = true;

        if (channel instanceof SelectableChannel) {
            SelectableChannel selectableChannel = (SelectableChannel) channel;
            result = selectableChannel.isBlocking();
        }

        return result;
    }

    /**
     * Release the selection key, working around for bug #6403933.
     * 
     * @param selector
     *            The associated selector.
     * @param selectionKey
     *            The used selection key.
     * @throws IOException
     */
    public static void release(Selector selector, SelectionKey selectionKey)
            throws IOException {
        if (selectionKey != null) {
            // The key you registered on the temporary selector
            selectionKey.cancel();

            if (selector != null) {
                // Flush the canceled key
                selector.selectNow();
                SelectorFactory.returnSelector(selector);
            }
        }

    }

    /**
     * Waits for the given channel to be ready for a specific operation.
     * 
     * @param selectableChannel
     *            The channel to monitor.
     * @param operations
     *            The operations to be ready to do.
     * @throws IOException
     */
    public static void waitForState(SelectableChannel selectableChannel,
            int operations) throws IOException {
        if (selectableChannel != null) {
            Selector selector = null;
            SelectionKey selectionKey = null;
            int selected = 0;

            try {
                selector = SelectorFactory.getSelector();

                while (selected == 0) {
                    selectionKey = selectableChannel.register(selector,
                            operations);
                    selected = selector.select(IoUtils.IO_TIMEOUT);
                }
            } finally {
                NioUtils.release(selector, selectionKey);
            }
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private NioUtils() {
    }

}
