/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.grizzly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.util.WorkerThread;

/**
 * HTTP parser filter for Grizzly.
 * 
 * @author Jerome Louvel
 */
public class HttpParserFilter implements ProtocolFilter {

    /** The parent HTTP server helper. */
    private volatile GrizzlyServerHelper helper;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent HTTP server helper.
     */
    public HttpParserFilter(GrizzlyServerHelper helper) {
        this.helper = helper;
    }

    /**
     * Execute a call.
     * 
     * @param context
     *            The call's context.
     */
    public boolean execute(Context context) throws IOException {
        // Create the HTTP call
        final ByteBuffer byteBuffer = ((WorkerThread) Thread.currentThread())
                .getByteBuffer();
        byteBuffer.flip();
        if (byteBuffer.hasRemaining()) {
            final SelectionKey key = context.getSelectionKey();
            final GrizzlyServerCall serverCall = new GrizzlyServerCall(
                    this.helper.getHelped(), byteBuffer, key,
                    (this.helper instanceof HttpsServerHelper));

            final boolean keepAlive = false;

            // Handle the call
            this.helper.handle(serverCall);

            // TODO Should we use httpCall#isKeepAlive?
            // TODO The "keepAlive" boolean is always set to false at this time.
            // Prepare for additional calls?
            if (keepAlive) {
                context
                        .setKeyRegistrationState(Context.KeyRegistrationState.REGISTER);
            } else {
                context
                        .setKeyRegistrationState(Context.KeyRegistrationState.CANCEL);
            }
        }

        // Clean up
        byteBuffer.clear();

        // This is the last filter
        return true;
    }

    /**
     * Post execute method.
     * 
     * @param context
     *            The call's context.
     */
    public boolean postExecute(Context context) throws IOException {
        return true;
    }

}
