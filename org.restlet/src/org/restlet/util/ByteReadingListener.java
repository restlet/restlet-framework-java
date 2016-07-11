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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;

/**
 * Selection listener notifying new content as an {@link InputStream}.
 * 
 * @author Jerome Louvel
 */
public abstract class ByteReadingListener extends ReadingListener {

    /**
     * Default constructor. Uses a byte buffer of {@link IoUtils#BUFFER_SIZE}
     * length.
     * 
     * @param source
     *            The source representation.
     * @throws IOException
     */
    public ByteReadingListener(Representation source) throws IOException {
        super(source);
    }

    /**
     * Constructor. Uses a byte buffer of a given size.
     * 
     * @param source
     *            The source byte channel.
     * @param bufferSize
     *            The byte buffer to use.
     * @throws IOException
     */
    public ByteReadingListener(Representation source, int bufferSize)
            throws IOException {
        super(source, bufferSize);
    }

    /**
     * Callback invoked when new content is available.
     * 
     * @param byteBuffer
     *            The byte buffer filled with the new content (correctly flip).
     */
    protected final void onContent(ByteBuffer byteBuffer) {
        onContent(new ByteArrayInputStream(byteBuffer.array(),
                byteBuffer.arrayOffset(), byteBuffer.remaining()));
    }

    /**
     * Callback invoked when new content is available.
     * 
     * @param inputStream
     *            The input stream allowing to retrieve the new content.
     */
    protected abstract void onContent(InputStream inputStream);

}
