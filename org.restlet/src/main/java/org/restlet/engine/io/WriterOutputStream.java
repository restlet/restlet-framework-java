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
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.restlet.data.CharacterSet;

// [excludes gwt]
/**
 * Output stream wrapping a character writer.
 * 
 * @author Kevin Conaway
 */
public class WriterOutputStream extends OutputStream {

    /** The character set to use when parsing byte arrays. */
    private final Charset charSet;

    /** The wrapped writer. */
    private final Writer writer;

    /**
     * Constructor.
     * 
     * @param writer
     *            The wrapped writer.
     * @param characterSet
     *            The character set. Use {@link CharacterSet#ISO_8859_1} by
     *            default if a null value is given.
     */
    public WriterOutputStream(Writer writer, CharacterSet characterSet) {
        this.writer = writer;
        this.charSet = (characterSet == null) ? Charset.forName("ISO-8859-1")
                : characterSet.toCharset();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.writer.close();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.writer.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        CharBuffer charBuffer = this.charSet.decode(ByteBuffer
                .wrap(b, off, len));
        this.writer.write(charBuffer.toString());
    }

    @Override
    public void write(int b) throws IOException {
        this.writer.write(b);
    }
}
