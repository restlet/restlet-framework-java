/**
 * Copyright 2005-2011 Noelios Technologies.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

// [excludes gwt]
/**
 * Pipe stream that pipes output streams into input streams. Implementation
 * based on a shared synchronized queue.
 * 
 * @author Jerome Louvel
 */
public class PipeStream {
    /** The queue timeout. */
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

            @Override
            public int read() throws IOException {
                try {
                    if (this.endReached) {
                        return -1;
                    }

                    final Integer value = queue.poll(QUEUE_TIMEOUT,
                            TimeUnit.SECONDS);
                    if (value == null) {
                        throw new IOException(
                                "Timeout while reading from the queue-based input stream");
                    }

                    this.endReached = (value.intValue() == -1);
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
            @Override
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