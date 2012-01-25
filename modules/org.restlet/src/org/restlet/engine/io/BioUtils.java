/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Range;
import org.restlet.engine.Edition;
import org.restlet.representation.Representation;

/**
 * Basic IO manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public final class BioUtils {

    // [ifndef gwt] member
    /** Support for byte to hexa conversions. */
    private static final char[] HEXDIGITS = "0123456789ABCDEF".toCharArray();

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
        return delete(file, false);
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
        return delete(file, recursive, osName.startsWith("windows"));
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
     * Returns the size effectively available. This returns the same value as
     * {@link Representation#getSize()} if no range is defined, otherwise it
     * returns the size of the range using {@link Range#getSize()}.
     * 
     * @param representation
     *            The representation to evaluate.
     * @return The available size.
     */
    public static long getAvailableSize(Representation representation) {
        // [ifndef gwt]
        if (representation.getRange() == null) {
            return representation.getSize();
        } else if (representation.getRange().getSize() != Range.SIZE_MAX) {
            return representation.getRange().getSize();
        } else if (representation.hasKnownSize()) {
            if (representation.getRange().getIndex() != Range.INDEX_LAST) {
                return representation.getSize()
                        - representation.getRange().getIndex();
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
     * Returns an input stream based on a given character reader.
     * 
     * @param reader
     *            The character reader.
     * @param characterSet
     *            The stream character set.
     * @return An input stream based on a given character reader.
     */
    public static InputStream getInputStream(Reader reader,
            CharacterSet characterSet) {
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
     *            the representation to get the {@link java.io.OutputStream}
     *            from.
     * @return A stream with the representation's content.
     */
    public static InputStream getInputStream(final Representation representation) {
        InputStream result = null;

        if (Edition.CURRENT != Edition.GAE) {
            // [ifndef gae]
            if (representation == null) {
                return null;
            }

            final PipeStream pipe = new PipeStream();
            // Creates a thread that will handle the task of continuously
            // writing the representation into the input side of the pipe
            Runnable task = new Runnable() {
                public void run() {
                    try {
                        java.io.OutputStream os = pipe.getOutputStream();
                        representation.write(os);
                        os.write(-1);
                        os.flush();
                        os.close();
                    } catch (IOException ioe) {
                        Context.getCurrentLogger()
                                .log(Level.FINE,
                                        "Error while writing to the piped input stream.",
                                        ioe);
                    }
                }
            };

            org.restlet.Application application = org.restlet.Application
                    .getCurrent();

            if (application != null && application.getTaskService() != null) {
                application.getTaskService().execute(task);
            } else {
                new Thread(task, "Restlet-PipedOutputStream").start();
            }

            result = pipe.getInputStream();
            // [enddef]
        } else {
            Context.getCurrentLogger()
                    .log(Level.WARNING,
                            "The GAE edition is unable to get an InputStream out of an OutputRepresentation.");
        }

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
    public static java.io.OutputStream getOutputStream(java.io.Writer writer,
            CharacterSet characterSet) {
        return new WriterOutputStream(writer, characterSet);
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
        if (Edition.CURRENT != Edition.GAE) {
            // [ifndef gae]
            final java.io.PipedWriter pipedWriter = new java.io.PipedWriter();
            java.io.PipedReader pipedReader = new java.io.PipedReader(
                    pipedWriter);
            org.restlet.Application application = org.restlet.Application
                    .getCurrent();

            // Gets a thread that will handle the task of continuously
            // writing the representation into the input side of the pipe
            Runnable task = new Runnable() {
                public void run() {
                    try {
                        representation.write(pipedWriter);
                        pipedWriter.flush();
                        pipedWriter.close();
                    } catch (IOException ioe) {
                        Context.getCurrentLogger()
                                .log(Level.FINE,
                                        "Error while writing to the piped reader.",
                                        ioe);
                    }
                }
            };

            if (application != null && application.getTaskService() != null) {
                application.getTaskService().execute(task);
            } else {
                new Thread(task, "Restlet-PipeWriter").start();
            }

            result = pipedReader;
            // [enddef]
        } else {
            Context.getCurrentLogger()
                    .log(Level.WARNING,
                            "The GAE edition is unable to return a reader for a writer representation.");
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
     * Converts a char array into a byte array using the default character set.
     * 
     * @param chars
     *            The source characters.
     * @return The result bytes.
     */
    public static byte[] toByteArray(char[] chars) {
        return toByteArray(chars, java.nio.charset.Charset.defaultCharset()
                .name());
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
        return toCharArray(bytes, java.nio.charset.Charset.defaultCharset()
                .name());
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
                    result = toString(new InputStreamReader(inputStream,
                            characterSet.getName()));
                } else {
                    result = toString(new InputStreamReader(inputStream));
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
                        : new BufferedReader(reader, IoUtils.BUFFER_SIZE);
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
