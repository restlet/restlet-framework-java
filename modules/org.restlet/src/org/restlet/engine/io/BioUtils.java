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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.engine.Edition;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

/**
 * Basic IO manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public final class BioUtils {

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
    public static void copy(InputStream inputStream, OutputStream outputStream)
            throws IOException {
        int bytesRead;
        byte[] buffer = new byte[2048];

        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
        inputStream.close();
    }

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
            RandomAccessFile randomAccessFile) throws IOException {
        int bytesRead;
        byte[] buffer = new byte[2048];

        while ((bytesRead = inputStream.read(buffer)) > 0) {
            randomAccessFile.write(buffer, 0, bytesRead);
        }

        inputStream.close();
    }

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
    public static void copy(Reader reader, Writer writer) throws IOException {
        int charsRead;
        char[] buffer = new char[2048];

        while ((charsRead = reader.read(buffer)) > 0) {
            writer.write(buffer, 0, charsRead);
        }

        writer.flush();
        reader.close();
    }

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

    /**
     * Returns a reader from a writer representation.Internally, it uses a
     * writer thread and a pipe stream.
     * 
     * @param representation
     *            The representation to read from.
     * @return The character reader.
     * @throws IOException
     */
    public static Reader getReader(final WriterRepresentation representation)
            throws IOException {
        Reader result = null;
        if (Edition.CURRENT != Edition.GAE) {
            // [ifndef gae]
            final java.io.PipedWriter pipedWriter = new java.io.PipedWriter();
            final java.io.PipedReader pipedReader = new java.io.PipedReader(
                    pipedWriter);
            final org.restlet.Application application = org.restlet.Application
                    .getCurrent();

            // Gets a thread that will handle the task of continuously
            // writing the representation into the input side of the pipe
            application.getTaskService().execute(new Runnable() {
                public void run() {
                    try {
                        representation.write(pipedWriter);
                        pipedWriter.close();
                    } catch (IOException ioe) {
                        Context
                                .getCurrentLogger()
                                .log(
                                        Level.FINE,
                                        "Error while writing to the piped reader.",
                                        ioe);
                    }
                }
            });

            result = pipedReader;
            // [enddef]
        } else {
            Context
                    .getCurrentLogger()
                    .log(Level.WARNING,
                            "The GAE edition is unable to return a reader for a writer representation.");
        }
        return result;

    }

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

    /**
     * Returns an input stream based on the given representation's content and
     * its write(OutputStream) method. Internally, it uses a writer thread and a
     * pipe stream.
     * 
     * @param representation
     *            the representation to get the {@link OutputStream} from.
     * @return A stream with the representation's content.
     */
    public static InputStream getStream(final Representation representation) {
        InputStream result = null;
        if (Edition.CURRENT != Edition.GAE) {
            // [ifndef gae]
            if (representation == null) {
                return null;
            }

            final PipeStream pipe = new PipeStream();
            final org.restlet.Application application = org.restlet.Application
                    .getCurrent();

            // Creates a thread that will handle the task of continuously
            // writing the representation into the input side of the pipe
            org.restlet.service.TaskService taskService = (application == null) ? new org.restlet.service.TaskService()
                    : application.getTaskService();
            taskService.execute(new Runnable() {
                public void run() {
                    try {
                        OutputStream os = pipe.getOutputStream();
                        representation.write(os);
                        os.write(-1);
                        os.close();
                    } catch (IOException ioe) {
                        Context
                                .getCurrentLogger()
                                .log(
                                        Level.FINE,
                                        "Error while writing to the piped input stream.",
                                        ioe);
                    }
                }
            });

            result = pipe.getInputStream();
            // [enddef]
        } else {
            Context
                    .getCurrentLogger()
                    .log(
                            Level.WARNING,
                            "The GAE edition is unable to get an InputStream out of an OutputRepresentation.");
        }

        return result;
    }

    /**
     * Returns an output stream based on a given writer.
     * 
     * @param writer
     *            The writer.
     * @return the output stream of the writer
     */
    public static OutputStream getStream(Writer writer) {
        return new WriterOutputStream(writer);
    }

    /**
     * Converts a char array into a byte array using the default character set.
     * 
     * @param chars
     *            The source characters.
     * @return The result bytes.
     */
    public static byte[] toByteArray(char[] chars) {
        return toByteArray(chars, Charset.defaultCharset().name());
    }

    /**
     * Converts a char array into a byte array using the default character set.
     * 
     * @param chars
     *            The source characters.
     * @param charsetName
     *            The character set to use.
     * @return The result bytes.
     */
    public static byte[] toByteArray(char[] chars, String charsetName) {
        CharBuffer cb = CharBuffer.wrap(chars);
        ByteBuffer bb = Charset.forName(charsetName).encode(cb);
        byte[] r = new byte[bb.remaining()];
        bb.get(r);
        return r;
    }

    /**
     * Converts a byte array into a character array using the default character
     * set.
     * 
     * @param bytes
     *            The source bytes.
     * @return The result characters.
     */
    public static char[] toCharArray(byte[] bytes) {
        return toCharArray(bytes, Charset.defaultCharset().name());
    }

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
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        CharBuffer cb = Charset.forName(charsetName).decode(bb);
        char[] r = new char[cb.remaining()];
        cb.get(r);
        return r;
    }

    /**
     * Converts an input stream to a string.<br>
     * As this method uses the InputstreamReader class, the default character
     * set is used for decoding the input stream.
     * 
     * @see <a href=
     *      "http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html"
     *      >InputStreamReader class</a>
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
     * @see <a href=
     *      "http://java.sun.com/j2se/1.5.0/docs/api/java/io/InputStreamReader.html"
     *      >InputStreamReader class</a>
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
     *            The characters reader.
     * @return The converted string.
     */
    public static String toString(Reader reader) {
        String result = null;

        if (reader != null) {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = (reader instanceof BufferedReader) ? (BufferedReader) reader
                        : new BufferedReader(reader);
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
    private BioUtils() {
    }

}
