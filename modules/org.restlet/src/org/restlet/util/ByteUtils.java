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

package org.restlet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.restlet.data.CharacterSet;
import org.restlet.resource.Representation;

/**
 * Byte manipulation utilities.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public final class ByteUtils {
    /**
     * Returns a readable byte channel based on a given inputstream. If it is
     * supported by a file a read-only instance of FileChannel is returned.
     * 
     * @param inputStream
     *            The input stream to convert.
     * @return A readable byte channel.
     */
    public static ReadableByteChannel getChannel(InputStream inputStream)
            throws IOException {
        return (inputStream != null) ? Channels.newChannel(inputStream) : null;
    }

    /**
     * Returns a writable byte channel based on a given output stream.
     * 
     * @param outputStream
     *            The output stream.
     */
    public static WritableByteChannel getChannel(OutputStream outputStream)
            throws IOException {
        return (outputStream != null) ? Channels.newChannel(outputStream)
                : null;
    }

    /**
     * Returns a readable byte channel based on the given representation's
     * content and its write(WritableByteChannel) method. Internally, it uses a
     * writer thread and a pipe stream.
     * 
     * @return A readable byte channel.
     */
    public static ReadableByteChannel getChannel(
            final Representation representation) throws IOException {
        final Pipe pipe = Pipe.open();

        // Creates a thread that will handle the task of continuously
        // writing the representation into the input side of the pipe
        Thread writer = new Thread() {
            public void run() {
                try {
                    WritableByteChannel wbc = pipe.sink();
                    representation.write(wbc);
                    wbc.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        };

        // Starts the writer thread
        writer.start();
        return pipe.source();
    }

    /**
     * Returns an input stream based on a given readable byte channel.
     * 
     * @param readableChannel
     *            The readable byte channel.
     * @return An input stream based on a given readable byte channel.
     */
    public static InputStream getStream(ReadableByteChannel readableChannel)
            throws IOException {
        return (readableChannel != null) ? Channels
                .newInputStream(readableChannel) : null;
    }

    /**
     * Returns an input stream based on the given representation's content and
     * its write(OutputStream) method. Internally, it uses a writer thread and a
     * pipe stream.
     * 
     * @return A stream with the representation's content.
     */
    public static InputStream getStream(final Representation representation)
            throws IOException {
        if (representation != null) {
            final PipeStream pipe = new PipeStream();

            // Creates a thread that will handle the task of continuously
            // writing the representation into the input side of the pipe
            Thread writer = new Thread() {
                public void run() {
                    try {
                        OutputStream os = pipe.getOutputStream();
                        representation.write(os);
                        os.write(-1);
                        os.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            };

            // Starts the writer thread
            writer.start();
            return pipe.getInputStream();
        } else {
            return null;
        }
    }

    /**
     * Returns an output stream based on a given writable byte channel.
     * 
     * @param writableChannel
     *            The writable byte channel.
     * @return An output stream based on a given writable byte channel.
     */
    public static OutputStream getStream(WritableByteChannel writableChannel) {
        OutputStream result = null;

        if (writableChannel instanceof SelectableChannel) {
            SelectableChannel selectableChannel = (SelectableChannel) writableChannel;

            synchronized (selectableChannel.blockingLock()) {
                if (selectableChannel.isBlocking()) {
                    result = Channels.newOutputStream(writableChannel);
                } else {
                    result = new NbChannelOutputStream(writableChannel);
                }
            }
        } else {
            result = new NbChannelOutputStream(writableChannel);
        }

        return result;
    }

    /**
     * Converts an input stream to a string.<br/>As this method uses the
     * InputstreamReader class, the default character set is used for decoding
     * the input stream.
     * 
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html">InputStreamReader
     *      class</a>
     * @see #toString(InputStream, CharacterSet)
     * @param inputStream
     *            The input stream.
     * @return The converted string.
     */
    public static String toString(InputStream inputStream) {
        return toString(inputStream, null);
    }

    /**
     * Converts an input stream to a string using the specified character set
     * for decoding the input stream.
     * 
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html">InputStreamReader
     *      class</a>
     * @param inputStream
     *            The input stream.
     * @param characterSet
     *            The character set
     * @return The converted string.
     */
    public static String toString(InputStream inputStream,
            CharacterSet characterSet) {
        String result = null;

        if (inputStream != null) {
            try {
                StringBuilder sb = new StringBuilder();
                InputStreamReader isr = null;
                if (characterSet != null) {
                    isr = new InputStreamReader(inputStream, characterSet
                            .getName());
                } else {
                    isr = new InputStreamReader(inputStream);
                }
                BufferedReader br = new BufferedReader(isr);
                int nextByte = br.read();
                while (nextByte != -1) {
                    sb.append((char) nextByte);
                    nextByte = br.read();
                }
                br.close();
                result = sb.toString();
            } catch (Exception e) {
                // Returns an empty string
            }
        }

        return result;
    }

    /**
     * Writes an input stream to an output stream. When the reading is done, the
     * input stream is closed.
     * 
     * @param inputStream
     *            The input stream.
     * @param outputStream
     *            The output stream.
     * @throws IOException
     */
    public static void write(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        int bytesRead;
        byte[] buffer = new byte[2048];
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
    }

    /**
     * Writes a readable channel to a writable channel. It assumes that the
     * readable and writable channels are both in NIO blocking mode.
     * 
     * @param readableChannel
     *            The readable channel.
     * @param writableChannel
     *            The writable channel.
     * @throws IOException
     */
    public static void write(ReadableByteChannel readableChannel,
            WritableByteChannel writableChannel) throws IOException {
        if ((readableChannel != null) && (writableChannel != null)) {
            write(Channels.newInputStream(readableChannel), Channels
                    .newOutputStream(writableChannel));
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private ByteUtils() {

    }

    /**
     * Pipe stream that pipes output streams into input streams. Implementation
     * based on a shared synchronized queue.
     * 
     * @author Jerome Louvel (contact@noelios.com)
     */
    private final static class PipeStream {
        /** The supporting synchronized queue. */
        private final BlockingQueue<Integer> queue;

        /** Constructor. */
        public PipeStream() {
            this.queue = new ArrayBlockingQueue<Integer>(1024);
        }

        /**
         * Returns a new input stream that can read from the pipe.
         * 
         * @return A new input stream that can read from the pipe.
         */
        public InputStream getInputStream() {
            return new InputStream() {
                private boolean endReached = false;

                public int read() throws IOException {
                    try {
                        if (endReached)
                            return -1;
                        int value = queue.take();
                        endReached = (value == -1);
                        return value;
                    } catch (InterruptedException ie) {
                        throw new IOException(
                                "Interruption occurred while writing in the queue");
                    }
                }
            };
        }

        /**
         * Returns a new output stream that can write into the pipe.
         * 
         * @return A new output stream that can write into the pipe.
         */
        public OutputStream getOutputStream() {
            return new OutputStream() {
                public void write(int b) throws IOException {
                    try {
                        queue.put(b);
                    } catch (InterruptedException ie) {
                        throw new IOException(
                                "Interruption occurred while writing in the queue");
                    }
                }
            };
        }

    }

    /**
     * Output stream connected to a non-blocking writable channel.
     */
    private final static class NbChannelOutputStream extends OutputStream {
        /** The channel to write to. */
        private WritableByteChannel channel;

        private Selector selector;

        private SelectionKey selectionKey;

        private SelectableChannel selectableChannel;

        /**
         * Constructor.
         * 
         * @param channel
         *            The wrapped channel.
         */
        public NbChannelOutputStream(WritableByteChannel channel) {
            this.channel = channel;

            if (!(channel instanceof SelectableChannel)) {
                throw new IllegalArgumentException(
                        "Invalid channel provided. Please use only selectable channels.");
            } else {
                this.selectableChannel = (SelectableChannel) channel;
                this.selector = null;
                this.selectionKey = null;

                if (this.selectableChannel.isBlocking()) {
                    throw new IllegalArgumentException(
                            "Invalid blocking channel provided. Please use only non-blocking channels.");
                }
            }
        }

        @Override
        public void write(int b) throws IOException {
            ByteBuffer bb = ByteBuffer.wrap(new byte[] { (byte) b });

            if ((this.channel != null) && (bb != null)) {
                try {
                    int bytesWritten;

                    while (bb.hasRemaining()) {
                        bytesWritten = this.channel.write(bb);

                        if (bytesWritten < 0) {
                            throw new IOException(
                                    "Unexpected negative number of bytes written.");
                        } else if (bytesWritten == 0) {
                            registerSelectionKey();

                            if (getSelector().select(10000) == 0) {
                                throw new IOException(
                                        "Unable to select the channel to write to it. Selection timed out.");
                            }
                        }
                    }
                } catch (IOException ioe) {
                    throw new IOException(
                            "Unable to write to the non-blocking channel. "
                                    + ioe.getLocalizedMessage());
                }
            } else {
                throw new IOException(
                        "Unable to write. Null byte buffer or channel detected.");
            }
        }

        private Selector getSelector() throws IOException {
            if (this.selector == null)
                this.selector = Selector.open();

            return this.selector;
        }

        private void registerSelectionKey() throws ClosedChannelException,
                IOException {
            this.selectionKey = this.selectableChannel.register(getSelector(),
                    SelectionKey.OP_WRITE);
        }

        @Override
        public void close() throws IOException {
            if (this.selectionKey != null) {
                this.selectionKey.cancel();
            }

            if (this.selector != null)
                this.selector.close();

            super.close();
        }
    }

}
