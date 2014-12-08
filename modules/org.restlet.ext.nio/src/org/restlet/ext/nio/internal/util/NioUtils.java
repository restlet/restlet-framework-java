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

package org.restlet.ext.nio.internal.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;

import org.restlet.engine.io.IoUtils;
import org.restlet.engine.io.SelectorFactory;
import org.restlet.ext.nio.internal.channel.ChannelInputStream;

/**
 * Utility methods for NIO processing.
 * 
 * @author Jerome Louvel
 */
public class NioUtils {

    /**
     * Writes the source buffer to the target buffer, up to a maximum number of
     * bytes.
     * 
     * @param sourceBuffer
     *            The source buffer.
     * @param targetBuffer
     *            The target buffer.
     * @param maxCopied
     *            The maximum number of bytes copied by this call or 0 for
     *            unlimited length.
     * @return The number of bytes added to the target buffer.
     */
    public static int copy(ByteBuffer sourceBuffer, ByteBuffer targetBuffer,
            long maxCopied) {
        int maxBuffer = Math.min(sourceBuffer.remaining(),
                targetBuffer.remaining());
        int result = (maxCopied == 0) ? maxBuffer : Math.min((int) maxCopied,
                maxBuffer);

        // Copy the byte to the target buffer
        for (int i = 0; i < result; i++) {
            targetBuffer.put(sourceBuffer.get());
        }

        return result;
    }

    /**
     * Writes the representation to a byte channel. Optimizes using the file
     * channel transferTo method.
     * 
     * @param fileChannel
     *            The readable file channel.
     * @param writableChannel
     *            A writable byte channel.
     */
    public static void copy(FileChannel fileChannel,
            WritableByteChannel writableChannel) throws IOException {
        long position = 0;
        long count = fileChannel.size();
        long written = 0;
        SelectableChannel selectableChannel = null;

        if (writableChannel instanceof SelectableChannel) {
            selectableChannel = (SelectableChannel) writableChannel;
        }

        while (count > 0) {
            NioUtils.waitForState(selectableChannel, SelectionKey.OP_WRITE);
            written = fileChannel.transferTo(position, count, writableChannel);
            position += written;
            count -= written;
        }
    }

    /**
     * Writes a NIO readable channel to a BIO output stream.
     * 
     * @param readableChannel
     *            The readable channel.
     * @param outputStream
     *            The output stream.
     * @throws IOException
     */
    public static void copy(ReadableByteChannel readableChannel,
            OutputStream outputStream) throws IOException {
        if ((readableChannel != null) && (outputStream != null)) {
            IoUtils.copy(new ChannelInputStream(readableChannel), outputStream);
        }
    }

    /**
     * Waits for the given channel to be ready for a specific operation.
     * 
     * @param selectableChannel
     *            The channel to monitor.
     * @param operations
     *            The operations to be ready to do.
     * @throws IOException
     */
    public static void waitForState(SelectableChannel selectableChannel,
            int operations) throws IOException {
        if (selectableChannel != null) {
            Selector selector = null;
            SelectionKey selectionKey = null;
            int selected = 0;

            try {
                selector = SelectorFactory.getSelector();

                while (selected == 0) {
                    selectionKey = selectableChannel.register(selector,
                            operations);
                    selected = selector.select(IoUtils.TIMEOUT_MS);
                }
            } finally {
                IoUtils.release(selector, selectionKey);
            }
        }
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private NioUtils() {
    }

}
