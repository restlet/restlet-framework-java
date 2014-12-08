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

package org.restlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.restlet.data.CharacterSet;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;

/**
 * Selection listener notifying new content as a {@link Reader}. It relies on
 * the representation's character set for proper character decoding.
 * 
 * @author Jerome Louvel
 */
public abstract class CharacterReadingListener extends ByteReadingListener {

    /** The character set of the associated representation. */
    private final CharacterSet characterSet;

    /**
     * Default constructor. Uses a byte buffer of {@link IoUtils#BUFFER_SIZE}
     * length.
     * 
     * @param source
     *            The source representation.
     * @throws IOException
     */
    public CharacterReadingListener(Representation source) throws IOException {
        this(source, IoUtils.BUFFER_SIZE);
    }

    /**
     * Constructor. Uses a byte buffer of a given size.
     * 
     * @param source
     *            The source representation.
     * @param bufferSize
     *            The byte buffer to use.
     * @throws IOException
     */
    public CharacterReadingListener(Representation source, int bufferSize)
            throws IOException {
        super(source, bufferSize);
        this.characterSet = source.getCharacterSet();
    }

    @Override
    protected final void onContent(InputStream inputStream) {
        InputStreamReader isr = new InputStreamReader(inputStream,
                this.characterSet.toCharset());
        onContent(isr);
    }

    /**
     * Callback invoked when new content is available.
     * 
     * @param reader
     *            The reader allowing to retrieve the new content.
     */
    protected abstract void onContent(Reader reader);

}
