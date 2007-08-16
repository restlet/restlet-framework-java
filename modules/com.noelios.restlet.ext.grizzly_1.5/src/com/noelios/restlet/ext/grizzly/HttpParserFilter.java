/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.grizzly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.util.WorkerThread;

/**
 * HTTP parser filter for Grizzly.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpParserFilter implements ProtocolFilter {

    /** The parent HTTP server helper. */
    private GrizzlyServerHelper helper;

    /**
     * Constructor.
     * 
     * @param helper
     *                The parent HTTP server helper.
     */
    public HttpParserFilter(GrizzlyServerHelper helper) {
        this.helper = helper;
    }

    /**
     * Execute a call.
     * 
     * @param context
     *                The call's context.
     */
    public boolean execute(Context context) throws IOException {
        // Create the HTTP call
        ByteBuffer byteBuffer = ((WorkerThread) Thread.currentThread())
                .getByteBuffer();
        byteBuffer.flip();
        SelectionKey key = context.getSelectionKey();
        GrizzlyServerCall serverCall = new GrizzlyServerCall(this.helper
                .getServer(), byteBuffer, key,
                (helper instanceof HttpsServerHelper));

        boolean keepAlive = false;

        // Handle the call
        this.helper.handle(serverCall);

        // Prepare for additional calls?
        if (keepAlive) {
            context
                    .setKeyRegistrationState(Context.KeyRegistrationState.REGISTER);
        } else {
            context
                    .setKeyRegistrationState(Context.KeyRegistrationState.CANCEL);
        }

        // Clean up
        byteBuffer.clear();

        // This is the last filter
        return true;
    }

    public boolean postExecute(Context context) throws IOException {
        return true;
    }

}
