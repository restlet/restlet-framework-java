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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;

import org.restlet.data.CharacterSet;

// [excludes gwt]
/**
 * Input stream based on a reader. The implementation relies on the NIO
 * {@link CharsetEncoder} class.
 * 
 * @author Jerome Louvel
 */
public class ReaderInputStream extends InputStream {

    /** The NIO byte buffer. */
    private final ByteBuffer byteBuffer;

    /** The NIO character buffer. */
    private final CharBuffer charBuffer;

    /** The character set encoder. */
    private final CharsetEncoder charsetEncoder;

    /** Indicates if the end of the wrapped reader has been reached. */
    private volatile boolean endReached;

    /** The wrapped reader. */
    private final BufferedReader reader;

    /**
     * Constructor. Uses the {@link CharacterSet#ISO_8859_1} character set by
     * default.
     * 
     * @param reader
     *            The reader to wrap as an input stream.
     * @throws IOException
     */
    public ReaderInputStream(Reader reader) throws IOException {
        this(reader, CharacterSet.ISO_8859_1);
    }

    /**
     * Constructor.
     * 
     * @param reader
     *            The reader to wrap as an input stream.
     * @param characterSet
     *            The character set to use for encoding.
     * @throws IOException
     */
    public ReaderInputStream(Reader reader, CharacterSet characterSet)
            throws IOException {
        this.byteBuffer = ByteBuffer.allocate(1024);
        this.byteBuffer.flip();
        this.charBuffer = CharBuffer.allocate(1024);
        this.charBuffer.flip();
        this.charsetEncoder = (characterSet == null) ? CharacterSet.ISO_8859_1
                .toCharset().newEncoder() : characterSet.toCharset()
                .newEncoder();
        this.endReached = false;
        this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader
                : new BufferedReader(reader, IoUtils.BUFFER_SIZE);
    }

    @Override
    public int available() throws IOException {
        return this.byteBuffer.hasRemaining() ? this.byteBuffer.remaining() : 0;
    }

    /**
     * Closes the wrapped reader.
     */
    @Override
    public void close() throws IOException {
        this.reader.close();
    }

    @Override
    public int read() throws IOException {
        byte[] temp = new byte[1];
        return (read(temp) == -1) ? -1 : temp[0] & 0xFF;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = 0;
        boolean iterate = true;

        while (iterate) {
            // Do we need to refill the byte buffer?
            if (!this.byteBuffer.hasRemaining() && !this.endReached) {
                // Do we need to refill the char buffer?
                if (!this.charBuffer.hasRemaining()) {
                    this.charBuffer.clear();
                    int read = this.reader.read(this.charBuffer);
                    this.charBuffer.flip();

                    if (read == -1) {
                        this.endReached = true;
                    }
                }

                if ((len > 0) && this.charBuffer.hasRemaining()) {
                    // Refill the byte buffer
                    this.byteBuffer.clear();
                    this.charsetEncoder.encode(this.charBuffer,
                            this.byteBuffer, this.endReached);
                    this.byteBuffer.flip();
                }
            }

            // Copies as much bytes as possible in the target array
            int readLength = Math.min(len, this.byteBuffer.remaining());

            if (readLength > 0) {
                this.byteBuffer.get(b, off, readLength);
                off += readLength;
                len -= readLength;
                result += readLength;
            }

            // Can we iterate again?
            iterate = (len > 0)
                    && (!this.endReached || this.byteBuffer.hasRemaining() || this.charBuffer
                            .hasRemaining());
        }

        if (this.endReached && (result == 0)) {
            result = -1;
        }

        return result;
    }
}
