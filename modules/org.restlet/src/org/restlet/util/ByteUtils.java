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
import java.util.concurrent.TimeUnit;

import org.restlet.data.CharacterSet;
import org.restlet.resource.Representation;
import org.restlet.resource.WriterRepresentation;

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
        private ByteBuffer bb;

        /** The channel to read from. */
        private ReadableByteChannel channel;

        /** The selectable channel to read from. */
        private SelectableChannel selectableChannel;

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
            this.bb.flip();
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

                    if (bytesRead <= 0) {
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
        private ByteBuffer bb = ByteBuffer.allocate(8192);

        /** The channel to write to. */
        private WritableByteChannel channel;

        private SelectionKey selectionKey;

        private Selector selector;

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
        private static final long QUEUE_TIMEOUT = 5;

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

                        Integer value = queue.poll(QUEUE_TIMEOUT,
                                TimeUnit.SECONDS);
                        if (value == null) {
                            throw new IOException(
                                    "Timeout while reading from the queue-based input stream");
                        }

                        endReached = (value.intValue() == -1);
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
                        if (!queue.offer(b, QUEUE_TIMEOUT, TimeUnit.SECONDS)) {
                            throw new IOException(
                                    "Timeout while writing to the queue-based output stream");
                        }
                    } catch (InterruptedException ie) {
                        throw new IOException(
                                "Interruption occurred while writing in the queue");
                    }
                }
            };
        }

    }

    /**
     * Input stream based on a reader.
     */
    private static class ReaderInputStream extends InputStream {
        private byte[] buffer;

        private CharacterSet characterSet;

        private int index;

        private BufferedReader localReader;

        public ReaderInputStream(Reader reader, CharacterSet characterSet) {
            this.localReader = (reader instanceof BufferedReader) ? (BufferedReader) reader
                    : new BufferedReader(reader);
            this.buffer = null;
            this.index = -1;
            this.characterSet = characterSet;
        }

        @Override
        public int read() throws IOException {
            int result = -1;

            // If the buffer is empty, read a new line
            if (this.buffer == null) {
                String line = this.localReader.readLine();

                if (line != null) {
                    this.buffer = line.getBytes(this.characterSet.getName());
                    this.index = 0;
                }
            }

            if (this.buffer != null) {
                // Read the next byte and increment the index
                result = this.buffer[index++];

                // Check if the buffer has been fully read
                if (this.index == this.buffer.length) {
                    this.buffer = null;
                }
            }

            return result;
        }
    }

    /**
     * Factory used to dispatch/share <code>Selector</code>.
     * 
     * @author Jean-Francois Arcand
     */
    private final static class SelectorFactory {
        /**
         * The number of <code>Selector</code> to create.
         */
        public static int maxSelectors = 20;

        /**
         * Cache of <code>Selector</code>
         */
        private final static Stack<Selector> selectors = new Stack<Selector>();

        /**
         * The timeout before we exit.
         */
        public static long timeout = 5000;

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
     * Outputstream wrapping a character writer.
     * 
     * @author Kevin Conaway
     */
    private final static class WriterOutputStream extends OutputStream {
        private final Writer writer;

        public WriterOutputStream(Writer writer) {
            this.writer = writer;
        }

        @Override
        public void close() throws IOException {
            super.close();
            writer.close();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            writer.flush();
        }

        @Override
        public void write(int b) throws IOException {
            writer.write(b);
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
     * writer thread and a pipe channel.
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
        writer.setDaemon(false);

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
     * Returns a reader from a writer representation.Internally, it uses a
     * writer thread and a pipe stream.
     * 
     * 
     * @param representation
     *                The representation to read from.
     * @return The character reader.
     */
    public static Reader getReader(WriterRepresentation representation) {
        // TODO
        return null;
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
     * Returns an input stream based on a given character reader.
     * 
     * @param reader
     *                The character reader.
     * @param characterSet
     *                The stream character set.
     * @return An input stream based on a given character reader.
     */
    public static InputStream getStream(Reader reader, CharacterSet characterSet) {
        return new ReaderInputStream(reader, characterSet);
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
            writer.setDaemon(false);

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
        return new WriterOutputStream(writer);
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
            write(new NbChannelInputStream(readableChannel),
                    new NbChannelOutputStream(writableChannel));
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
