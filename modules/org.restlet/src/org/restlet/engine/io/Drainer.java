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

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Callback interface invoked when draining can be effectively done for a
 * {@link ReadableBufferedChannel}.
 * 
 * @author Jerome Louvel
 */
public interface Drainer {

    /**
     * Indicates if a new read can be attempted.
     * 
     * @param lastRead
     *            The number of bytes just read.
     * @param targetBuffer
     *            The target buffer.
     * @return True if a new read can be attempted.
     */
    public boolean canRetry(int lastRead, ByteBuffer targetBuffer);

    /**
     * Drains the byte buffer.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @return The number of bytes added to the target buffer.
     */
    public int drain(ByteBuffer targetBuffer) throws IOException;
}
