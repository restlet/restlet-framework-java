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

import static org.restlet.data.Range.isBytesRange;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.channels.Channel;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Range;
import org.restlet.engine.Edition;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;

/**
 * IO manipulation utilities.
 * 
 * @author Thierry Boileau
 */
public class IoUtils {

    /**
     * The size to use when instantiating buffered items such as instances of
     * the {@link BufferedReader} class. It looks for the System property
     * "org.restlet.engine.io.bufferSize" and if not defined, uses the "8192"
     * default value.
     */
    public static final int BUFFER_SIZE = getProperty(
            "org.restlet.engine.io.bufferSize", 8192);

    // [ifndef gwt] member
    /** Support for byte to hexa conversions. */
    private static final char[] HEXDIGITS = "0123456789ABCDEF".toCharArray();

    /**
     * The number of milliseconds after which IO operation will time out. It
     * looks for the System property "org.restlet.engine.io.timeoutMs" and if
     * not defined, uses the "60000" default value.
     */
    public final static int TIMEOUT_MS = getProperty(
            "org.restlet.engine.io.timeoutMs", 60000);

    // [ifndef gwt] method
    /**
     * Copies an input stream to an output stream. When the reading is done, the
     * input stream is closed.
     * 
     * @param inputStream
     *            The input stream.
     * @param outputStream
     *            The output stream.
     * @throws IOException
     */
    public static void copy(InputStream inputStream,
            java.io.OutputStream outputStream) throws IOException {
        if (inputStream != null) {
            if (outputStream != null) {
                int bytesRead;
                byte[] buffer = new byte[2048];

                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
                inputStream.close();
            } else {
                Context.getCurrentLogger()
                        .log(Level.FINE,
                                "Unable to copy input to output stream. Output stream is null.");
            }
        } else {
            Context.getCurrentLogger()
                    .log(Level.FINE,
                            "Unable to copy input to output stream. Input stream is null.");
        }
    }

    // [ifndef gwt] method
    /**
     * Copies an input stream to a random access file. When the reading is done,
     * the input stream is closed.
     * 
     * @param inputStream
     *            The input stream.
     * @param randomAccessFile
     *            The random access file.
     * @throws IOException
     */
    public static void copy(InputStream inputStream,
            java.io.RandomAccessFile randomAccessFile) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[2048];

        while ((bytesRead = inputStream.read(buffer)) > 0) {
            randomAccessFile.write(buffer, 0, bytesRead);
        }

        inputStream.close();
    }

    // [ifndef gwt] method
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
            copy(getStream(readableChannel), getStream(writableChannel));
        }
    }

    // [ifndef gwt] method
    /**
     * Copies characters from a reader to a writer. When the reading is done,
     * the reader is closed.
     * 
     * @param reader
     *            The reader.
     * @param writer
     *            The writer.
     * @throws IOException
     */
    public static void copy(Reader reader, java.io.Writer writer)
            throws IOException {
        int charsRead;
        char[] buffer = new char[2048];

        while ((charsRead = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, charsRead);
        }

        writer.flush();
        reader.close();
    }

    // [ifndef gwt] method
    /**
     * Deletes an individual file or an empty directory.
     * 
     * @param file
     *            The individual file or directory to delete.
     * @return True if the deletion was successful.
     */
    public static boolean delete(java.io.File file) {
        return IoUtils.delete(file, false);
    }

    // [ifndef gwt] method
    /**
     * Deletes an individual file or a directory. A recursive deletion can be
     * forced as well. Under Windows operating systems, the garbage collector
     * will be invoked once before attempting to delete in order to prevent
     * locking issues.
     * 
     * @param file
     *            The individual file or directory to delete.
     * @param recursive
     *            Indicates if directory with content should be deleted
     *            recursively as well.
     * @return True if the deletion was successful or if the file or directory
     *         didn't exist.
     */
    public static boolean delete(java.io.File file, boolean recursive) {
        String osName = System.getProperty("os.name").toLowerCase();
        return IoUtils.delete(file, recursive, osName.startsWith("windows"));
    }

    // [ifndef gwt] method
    /**
     * Deletes an individual file or a directory. A recursive deletion can be
     * forced as well. The garbage collector can be run once before attempting
     * to delete, to workaround lock issues under Windows operating systems.
     * 
     * @param file
     *            The individual file or directory to delete.
     * @param recursive
     *            Indicates if directory with content should be deleted
     *            recursively as well.
     * @param garbageCollect
     *            Indicates if the garbage collector should be run.
     * @return True if the deletion was successful or if the file or directory
     *         didn't exist.
     */
    public static boolean delete(java.io.File file, boolean recursive,
            boolean garbageCollect) {
        boolean result = true;
        boolean runGC = garbageCollect;

        if (file.exists()) {
            if (file.isDirectory()) {
                java.io.File[] entries = file.listFiles();

                // Check if the directory is empty
                if (entries.length > 0) {
                    if (recursive) {
                        for (int i = 0; result && (i < entries.length); i++) {
                            if (runGC) {
                                System.gc();
                                runGC = false;
                            }

                            result = delete(entries[i], true, false);
                        }
                    } else {
                        result = false;
                    }
                }
            }

            if (runGC) {
                System.gc();
                runGC = false;
            }

            result = result && file.delete();
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Exhaust the content of the representation by reading it and silently
     * discarding anything read.
     * 
     * @param input
     *            The input stream to exhaust.
     * @return The number of bytes consumed or -1 if unknown.
     */
    public static long exhaust(InputStream input) throws IOException {
        long result = -1L;

        if (input != null) {
            byte[] buf = new byte[2048];
            int read = input.read(buf);
            result = (read == -1) ? -1 : 0;

            while (read != -1) {
                result += read;
                read = input.read(buf);
            }
        }

        return result;
    }

    /**
     * Returns the size effectively available. This returns the same value as {@link Representation#getSize()} if no
     * range is defined, otherwise it
     * returns the size of the range using {@link Range#getSize()}.
     * 
     * @param representation
     *            The representation to evaluate.
     * @return The available size.
     */
    public static long getAvailableSize(Representation representation) {
        // [ifndef gwt]
        Range range = representation.getRange();
        if (range == null || !isBytesRange(range)) {
            return representation.getSize();
        } else if (range.getSize() != Range.SIZE_MAX) {
            if (representation.hasKnownSize()) {
                return Math.min(range.getIndex() + range.getSize(),
                        representation.getSize()) - range.getIndex();
            } else {
                return Representation.UNKNOWN_SIZE;
            }
        } else if (representation.hasKnownSize()) {
            if (range.getIndex() != Range.INDEX_LAST) {
                return representation.getSize() - range.getIndex();
            }

            return representation.getSize();
        }

        return Representation.UNKNOWN_SIZE;
        // [enddef]
        // [ifdef gwt] line uncomment
        // return representation.getSize();
    }

    // [ifndef gwt] method
    /**
     * Returns a readable byte channel based on a given input stream. If it is
     * supported by a file a read-only instance of FileChannel is returned.
     * 
     * @param inputStream
     *            The input stream to convert.
     * @return A readable byte channel.
     */
    public static ReadableByteChannel getChannel(InputStream inputStream)
            throws IOException {
        ReadableByteChannel result = null;

        if (inputStream instanceof FileInputStream) {
            result = ((FileInputStream) inputStream).getChannel();
        } else if (inputStream != null) {
            result = new InputStreamChannel(inputStream);
        }

        return result;
    }

    // [ifndef gwt] method
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

    // [ifndef gwt] method
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

            // Get a thread that will handle the task of continuously
            // writing the representation into the input side of the pipe
            Runnable task = new Runnable() {
                public void run() {
                    WritableByteChannel wbc = null;

                    try {
                        wbc = pipe.sink();
                        representation.write(wbc);
                    } catch (IOException ioe) {
                        Context.getCurrentLogger().log(Level.WARNING,
                                "Error while writing to the piped channel.",
                                ioe);
                    } finally {
                        if (wbc != null)
                            try {
                                wbc.close();
                            } catch (IOException e) {
                                Context.getCurrentLogger()
                                        .log(Level.WARNING,
                                                "Error while closing to the piped channel.",
                                                e);
                            }
                    }
                }
            };

            org.restlet.Context context = org.restlet.Context.getCurrent();

            if (context != null && context.getExecutorService() != null) {
                context.getExecutorService().execute(task);
            } else {
                Engine.createThreadWithLocalVariables(task, "Restlet-IoUtils")
                        .start();
            }

            result = pipe.source();
            // [enddef]
        } else {
            Context.getCurrentLogger()
                    .log(Level.WARNING,
                            "The GAE edition is unable to return a channel for a representation given its write(WritableByteChannel) method.");
        }
        return result;
    }

    private static int getProperty(String name, int defaultValue) {
        int result = defaultValue;

        // [ifndef gwt]
        try {
            result = Integer.parseInt(System.getProperty(name));
        } catch (NumberFormatException nfe) {
            result = defaultValue;
        }
        // [enddef]

        return result;
    }

    /**
     * Returns a reader from an input stream and a character set.
     * 
     * @param stream
     *            The input stream.
     * @param characterSet
     *            The character set. May be null.
     * @return The equivalent reader.
     * @throws UnsupportedEncodingException
     *             if a character set is given, but not supported
     */
    public static Reader getReader(InputStream stream, CharacterSet characterSet)
            throws UnsupportedEncodingException {
        if (characterSet != null) {
            return new InputStreamReader(stream, characterSet.getName());
        }

        return new InputStreamReader(stream);
    }

    // [ifndef gwt] method
    /**
     * Returns a reader from a writer representation.Internally, it uses a
     * writer thread and a pipe stream.
     * 
     * @param representation
     *            The representation to read from.
     * @return The character reader.
     * @throws IOException
     */
    public static Reader getReader(
            final org.restlet.representation.WriterRepresentation representation)
            throws IOException {
        Reader result = null;
        final java.io.PipedWriter pipedWriter = new java.io.PipedWriter();

        java.io.PipedReader pipedReader = new java.io.PipedReader(pipedWriter);

        // Gets a thread that will handle the task of continuously
        // writing the representation into the input side of the pipe
        Runnable task = new org.restlet.engine.util.ContextualRunnable() {
            public void run() {
                try {
                    representation.write(pipedWriter);
                    pipedWriter.flush();
                } catch (IOException ioe) {
                    Context.getCurrentLogger().log(Level.WARNING,
                            "Error while writing to the piped reader.", ioe);
                } finally {
                    try {
                        pipedWriter.close();
                    } catch (IOException ioe2) {
                        Context.getCurrentLogger().log(Level.WARNING,
                                "Error while closing the pipe.", ioe2);
                    }
                }
            }
        };

        org.restlet.Context context = org.restlet.Context.getCurrent();

        if (context != null && context.getExecutorService() != null) {
            context.getExecutorService().execute(task);
        } else {
            Engine.createThreadWithLocalVariables(task, "Restlet-IoUtils")
                    .start();
        }

        result = pipedReader;

        return result;

    }

    // [ifndef gwt] method
    /**
     * Returns an output stream based on a given writer.
     * 
     * @param writer
     *            The writer.
     * @param characterSet
     *            The character set used to write on the output stream.
     * @return the output stream of the writer
     */
    public static java.io.OutputStream getStream(java.io.Writer writer,
            CharacterSet characterSet) {
        return new WriterOutputStream(writer, characterSet);
    }

    // [ifndef gwt] method
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
            result = isBlocking(readableChannel) ? Channels
                    .newInputStream(readableChannel)
                    : new NbChannelInputStream(readableChannel);
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Returns an input stream based on a given character reader.
     * 
     * @param reader
     *            The character reader.
     * @param characterSet
     *            The stream character set.
     * @return An input stream based on a given character reader.
     */
    public static InputStream getStream(Reader reader, CharacterSet characterSet) {
        InputStream result = null;

        try {
            result = new ReaderInputStream(reader, characterSet);
        } catch (IOException e) {
            Context.getCurrentLogger().log(Level.WARNING,
                    "Unable to create the reader input stream", e);
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Returns an input stream based on the given representation's content and
     * its write(OutputStream) method. Internally, it uses a writer thread and a
     * pipe stream.
     * 
     * @param representation
     *            the representation to get the {@link java.io.OutputStream} from.
     * @return A stream with the representation's content.
     */
    public static InputStream getStream(final Representation representation) {
        InputStream result = null;

        if (representation == null) {
            return null;
        }

        final PipeStream pipe = new PipeStream();
        final java.io.OutputStream os = pipe.getOutputStream();

        // Creates a thread that will handle the task of continuously
        // writing the representation into the input side of the pipe
        Runnable task = new org.restlet.engine.util.ContextualRunnable() {
            public void run() {
                try {
                    representation.write(os);
                    os.flush();
                } catch (IOException ioe) {
                    Context.getCurrentLogger().log(Level.WARNING,
                            "Error while writing to the piped input stream.",
                            ioe);
                } finally {
                    try {
                        os.close();
                    } catch (IOException ioe2) {
                        Context.getCurrentLogger().log(Level.WARNING,
                                "Error while closing the pipe.", ioe2);
                    }
                }
            }
        };

        org.restlet.Context context = org.restlet.Context.getCurrent();

        if (context != null && context.getExecutorService() != null) {
            context.getExecutorService().execute(task);
        } else {
            Engine.createThreadWithLocalVariables(task, "Restlet-IoUtils")
                    .start();
        }

        result = pipe.getInputStream();

        return result;
    }

    // [ifndef gwt] method
    /**
     * Returns an output stream based on a given writable byte channel.
     * 
     * @param writableChannel
     *            The writable byte channel.
     * @return An output stream based on a given writable byte channel.
     */
    public static OutputStream getStream(WritableByteChannel writableChannel) {
        OutputStream result = null;

        if (writableChannel != null) {
            result = isBlocking(writableChannel) ? Channels
                    .newOutputStream(writableChannel)
                    : new NbChannelOutputStream(writableChannel);
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Converts the representation to a string value. Be careful when using this
     * method as the conversion of large content to a string fully stored in
     * memory can result in OutOfMemoryErrors being thrown.
     * 
     * @param representation
     *            The representation to convert.
     * @return The representation as a string value.
     */
    public static String getText(Representation representation)
            throws IOException {
        String result = null;

        if (representation.isAvailable()) {
            if (representation.getSize() == 0) {
                result = "";
            } else {
                java.io.StringWriter sw = new java.io.StringWriter();
                representation.write(sw);
                sw.flush();
                result = sw.toString();
            }
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Returns a writer to the given output stream, using the given character
     * set for encoding to bytes.
     * 
     * @param outputStream
     *            The target output stream.
     * @param characterSet
     *            The character set for encoding.
     * @return The wrapping writer.
     */
    public static Writer getWriter(OutputStream outputStream,
            CharacterSet characterSet) {
        Writer result = null;

        if (characterSet != null) {
            result = new OutputStreamWriter(outputStream,
                    characterSet.toCharset());
        } else {
            // Use the default HTTP character set
            result = new OutputStreamWriter(outputStream,
                    CharacterSet.ISO_8859_1.toCharset());
        }

        return result;
    }

    // [ifndef gwt] method
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

    // [ifndef gwt] method
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

    // [ifndef gwt] method
    /**
     * Converts a char array into a byte array using the default character set.
     * 
     * @param chars
     *            The source characters.
     * @return The result bytes.
     */
    public static byte[] toByteArray(char[] chars) {
        return IoUtils.toByteArray(chars, java.nio.charset.Charset
                .defaultCharset().name());
    }

    // [ifndef gwt] method
    /**
     * Converts a char array into a byte array using the provided character set.
     * 
     * @param chars
     *            The source characters.
     * @param charsetName
     *            The character set to use.
     * @return The result bytes.
     */
    public static byte[] toByteArray(char[] chars, String charsetName) {
        java.nio.CharBuffer cb = java.nio.CharBuffer.wrap(chars);
        java.nio.ByteBuffer bb = java.nio.charset.Charset.forName(charsetName)
                .encode(cb);
        byte[] r = new byte[bb.remaining()];
        bb.get(r);
        return r;
    }

    // [ifndef gwt] method
    /**
     * Converts a byte array into a character array using the default character
     * set.
     * 
     * @param bytes
     *            The source bytes.
     * @return The result characters.
     */
    public static char[] toCharArray(byte[] bytes) {
        return IoUtils.toCharArray(bytes, java.nio.charset.Charset
                .defaultCharset().name());
    }

    // [ifndef gwt] method
    /**
     * Converts a byte array into a character array using the default character
     * set.
     * 
     * @param bytes
     *            The source bytes.
     * @param charsetName
     *            The character set to use.
     * @return The result characters.
     */
    public static char[] toCharArray(byte[] bytes, String charsetName) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes);
        java.nio.CharBuffer cb = java.nio.charset.Charset.forName(charsetName)
                .decode(bb);
        char[] r = new char[cb.remaining()];
        cb.get(r);
        return r;
    }

    // [ifndef gwt] method
    /**
     * Converts a byte array into an hexadecimal string.
     * 
     * @param byteArray
     *            The byte array to convert.
     * @return The hexadecimal string.
     */
    public static String toHexString(byte[] byteArray) {
        final char[] hexChars = new char[2 * byteArray.length];
        int i = 0;

        for (final byte b : byteArray) {
            hexChars[i++] = HEXDIGITS[(b >> 4) & 0xF];
            hexChars[i++] = HEXDIGITS[b & 0xF];
        }

        return new String(hexChars);
    }

    /**
     * Converts an input stream to a string.<br>
     * As this method uses the InputstreamReader class, the default character
     * set is used for decoding the input stream.
     * 
     * @see InputStreamReader
     * @see IoUtils#toString(InputStream, CharacterSet)
     * @param inputStream
     *            The input stream.
     * @return The converted string.
     */
    public static String toString(InputStream inputStream) {
        return IoUtils.toString(inputStream, null);
    }

    /**
     * Converts an input stream to a string using the specified character set
     * for decoding the input stream. Once read, the input stream is closed.
     * 
     * @see InputStreamReader
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
            // [ifndef gwt]
            try {
                if (characterSet != null) {
                    result = IoUtils.toString(new InputStreamReader(
                            inputStream, characterSet.getName()));
                } else {
                    result = IoUtils
                            .toString(new InputStreamReader(inputStream));
                }

                inputStream.close();
            } catch (Exception e) {
                // Returns an empty string
            }
            // [enddef]
            // [ifdef gwt] uncomment
            // if (inputStream instanceof StringInputStream) {
            // return ((StringInputStream) inputStream).getText();
            // } else {
            // try {
            // if (characterSet != null) {
            // result = toString(new InputStreamReader(inputStream,
            // characterSet.getName()));
            // } else {
            // result = toString(new InputStreamReader(inputStream));
            // }
            // } catch (Exception e) {
            // // Returns an empty string
            // }
            // }
            // [enddef]
        }

        return result;
    }

    /**
     * Converts a reader to a string.
     * 
     * @see InputStreamReader
     * 
     * @param reader
     *            The characters reader.
     * @return The converted string.
     */
    public static String toString(Reader reader) {
        String result = null;

        if (reader != null) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader
                        : new BufferedReader(reader, BUFFER_SIZE);
                char[] buffer = new char[2048];
                int charsRead = br.read(buffer);

                while (charsRead != -1) {
                    sb.append(buffer, 0, charsRead);
                    charsRead = br.read(buffer);
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
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private IoUtils() {
    }
}
