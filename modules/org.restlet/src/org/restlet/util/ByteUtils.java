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
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.EmptyStackException;
import java.util.Stack;
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
     * Input stream connected to a non-blocking readable channel.
     */
    private final static class NbChannelInputStream extends InputStream {
        /** The channel to read from. */
        private ReadableByteChannel channel;

        /** The selectable channel to read from. */
        private SelectableChannel selectableChannel;

        private ByteBuffer bb;

        /**
         * Constructor.
         * 
         * @param channel
         * @param channelBuffer
         */
        public NbChannelInputStream(ReadableByteChannel channel) {
            this.channel = channel;
            this.selectableChannel = (SelectableChannel) channel;
            this.bb = ByteBuffer.allocate(8192);
        }

        @Override
        public int read() throws IOException {
            int result = 0;
            Selector selector = null;
            SelectionKey selectionKey = null;

            try {
                // Are there available byte in the buffer?
                if (bb.hasRemaining()) {
                    // Yes, let's return the next one
                    result = bb.get();
                } else {
                    // No, let's try to read more
                    int bytesRead = readChannel();

                    // If no bytes were read, try to register a select key to
                    // get more
                    if (bytesRead == 0) {
                        selector = SelectorFactory.getSelector();

                        if (selector != null) {
                            selectionKey = selectableChannel.register(selector,
                                    SelectionKey.OP_READ);
                            selector.select(10000);
                        }

                        bytesRead = readChannel();
                    }

                    if (bytesRead == 0) {
                        result = -1;
                    } else {
                        result = bb.get();
                    }
                }
            } finally {
                // Workaround for bug #6403933
                if (selectionKey != null) {
                    // The key you registered on the temporary selector
                    selectionKey.cancel();

                    // Flush the cancelled key
                    selector.selectNow();
                    SelectorFactory.returnSelector(selector);
                }
            }

            if (result == -1) {
                System.out.println();
            } else {
                System.out.print((char) result);
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
            bb.clear();
            result = channel.read(bb);
            bb.flip();
            return result;
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

        private ByteBuffer bb = ByteBuffer.allocate(8192);

        /**
         * Constructor.
         * 
         * @param channel
         *                The wrapped channel.
         */
        public NbChannelOutputStream(WritableByteChannel channel) {
            this.channel = channel;
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

        @Override
        public void write(byte b[], int off, int len) throws IOException {
            bb.clear();
            bb.put(b, off, len);
            bb.flip();

            if ((this.channel != null) && (bb != null)) {
                try {
                    int bytesWritten;

                    while (bb.hasRemaining()) {
                        bytesWritten = this.channel.write(bb);

                        if (bytesWritten < 0) {
                            throw new IOException(
                                    "Unexpected negative number of bytes written.");
                        } else if (bytesWritten == 0) {
                            if (SelectorFactory.getSelector().select(10000) == 0) {
                                throw new IOException(
                                        "Unable to select the channel to write to it. Selection timed out.");
                            }
                        }
                    }
                } catch (IOException ioe) {
                    throw new IOException(
                            "Unable to write to the non-blocking channel. "
                                    + ioe.getLocalizedMessage());
                } finally {
                    bb.clear();
                }
            } else {
                throw new IOException(
                        "Unable to write. Null byte buffer or channel detected.");
            }
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[] { (byte) b }, 0, 1);
        }
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
     * Factory used to dispatch/share <code>Selector</code>.
     * 
     * @author Jean-Francois Arcand
     */
    private final static class SelectorFactory {
        /**
         * The timeout before we exit.
         */
        public static long timeout = 5000;

        /**
         * The number of <code>Selector</code> to create.
         */
        public static int maxSelectors = 20;

        /**
         * Cache of <code>Selector</code>
         */
        private final static Stack<Selector> selectors = new Stack<Selector>();

        /**
         * Creates the <code>Selector</code>
         */
        static {
            try {
                for (int i = 0; i < maxSelectors; i++)
                    selectors.add(Selector.open());
            } catch (IOException ex) {
                // do nothing.
            }
        }

        /**
         * Get a exclusive <code>Selector</code>
         * 
         * @return <code>Selector</code>
         */
        public final static Selector getSelector() {
            synchronized (selectors) {
                Selector selector = null;
                try {
                    if (selectors.size() != 0)
                        selector = selectors.pop();
                } catch (EmptyStackException ex) {
                }

                int attempts = 0;
                try {
                    while (selector == null && attempts < 2) {
                        selectors.wait(timeout);
                        try {
                            if (selectors.size() != 0)
                                selector = selectors.pop();
                        } catch (EmptyStackException ex) {
                            break;
                        }
                        attempts++;
                    }
                } catch (InterruptedException ex) {
                }

                return selector;
            }
        }

        /**
         * Return the <code>Selector</code> to the cache
         * 
         * @param s
         *                <code>Selector</code>
         */
        public final static void returnSelector(Selector s) {
            synchronized (selectors) {
                selectors.push(s);
                if (selectors.size() == 1)
                    selectors.notify();
            }
        }
    }

    /**
     * Returns a readable byte channel based on a given inputstream. If it is
     * supported by a file a read-only instance of FileChannel is returned.
     * 
     * @param inputStream
     *                The input stream to convert.
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
     *                The output stream.
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
     * Returns a reader from an input stream and a character set.
     * 
     * @param stream
     *                The input stream.
     * @param characterSet
     *                The character set.
     * @return The equivalent reader.
     * @throws IOException
     */
    public static Reader getReader(InputStream stream, CharacterSet characterSet)
            throws IOException {
        if (characterSet != null) {
            return new InputStreamReader(stream, characterSet.getName());
        } else {
            return new InputStreamReader(stream);
        }
    }

    /**
     * Returns an input stream based on a given readable byte channel.
     * 
     * @param readableChannel
     *                The readable byte channel.
     * @return An input stream based on a given readable byte channel.
     */
    public static InputStream getStream(ReadableByteChannel readableChannel)
            throws IOException {
        InputStream result = null;

        if (readableChannel != null) {
            result = new NbChannelInputStream(readableChannel);
        }

        return result;
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
     *                The writable byte channel.
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
     * Returns an output stream based on a given writer.
     * 
     * @param writer
     *                The writer.
     */
    public static OutputStream getStream(Writer writer) throws IOException {
        return null;
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
     *                The input stream.
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
     *                The input stream.
     * @param characterSet
     *                The character set
     * @return The converted string.
     */
    public static String toString(InputStream inputStream,
            CharacterSet characterSet) {
        String result = null;

        if (inputStream != null) {
            try {
                if (characterSet != null) {
                    result = toString(new InputStreamReader(inputStream,
                            characterSet.getName()));
                } else {
                    result = toString(new InputStreamReader(inputStream));
                }
            } catch (Exception e) {
                // Returns an empty string
            }
        }

        return result;
    }

    /**
     * Converts a reader to a string.
     * 
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html">InputStreamReader
     *      class</a>
     * @param reader
     *                The characters reader.
     * @return The converted string.
     */
    public static String toString(Reader reader) {
        String result = null;

        if (reader != null) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(reader);
                int nextChar = br.read();
                while (nextChar != -1) {
                    sb.append((char) nextChar);
                    nextChar = br.read();
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
     *                The input stream.
     * @param outputStream
     *                The output stream.
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
     *                The readable channel.
     * @param writableChannel
     *                The writable channel.
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
     * Writes characters from a reader to a writer. When the reading is done,
     * the reader is closed.
     * 
     * @param reader
     *                The reader.
     * @param writer
     *                The writer.
     * @throws IOException
     */
    public static void write(Reader reader, Writer writer) throws IOException {
        int charsRead;
        char[] buffer = new char[2048];
        while ((charsRead = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, charsRead);
        }
        reader.close();
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private ByteUtils() {

    }

}
