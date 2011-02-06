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

package org.restlet.engine.http.io;

import java.io.InputStream;

import org.restlet.engine.http.connector.Connection;
import org.restlet.engine.http.connector.ConnectionState;

/**
 * Input stream that synchronizes the state of a {@link Connection} instance and
 * an input stream.
 */
public abstract class InputEntityStream extends InputStream {

    /** The notifiable connection. */
    private volatile Notifiable notifiable;

    /** The inbound stream. */
    private InputStream inboundStream;

    /**
     * Constructor.
     * 
     * @param notifiable
     *            The notifiable connection.
     * @param inboundStream
     *            The inbound stream.
     */
    public InputEntityStream(Notifiable notifiable, InputStream inboundStream) {
        super();
        this.notifiable = notifiable;
        this.inboundStream = inboundStream;
    }

    /**
     * Returns the inbound stream.
     * 
     * @return The inbound stream.
     */
    protected InputStream getInboundStream() {
        return this.inboundStream;
    }

    /**
     * To be called when the end of the stream is reached. By default, it
     * updates the state of the connection (
     * {@link Connection#setInboundBusy(boolean)}) .
     */
    protected void onEndReached() {
        if (notifiable != null) {
            notifiable.onEndReached();
        }
    }

    /**
     * To be called when there is an error when handling the stream. By default
     * it calls {@link #onEndReached()} and set the state of the connection to
     * {@link ConnectionState#CLOSING} in order to release this stream.
     */
    protected void onError() {
        if (notifiable != null) {
            notifiable.onError();
        }
    }
}
