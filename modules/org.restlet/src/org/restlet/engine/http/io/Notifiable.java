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

/**
 * Marker class for objects such as
 * {@link org.restlet.engine.http.connector.Connection} that can be notified of
 * stream events.
 * 
 * @author Jerome Louvel
 */
public interface Notifiable {

    /**
     * To be called when the end of the stream is reached. By default, it
     * updates the state of the connection (
     * {@link org.restlet.engine.http.connector.Connection#setInboundBusy(boolean)}
     * ) .
     */
    public void onEndReached();

    /**
     * To be called when there is an error when handling the stream. By default
     * it calls {@link #onEndReached()} and set the state of the connection to
     * {@link org.restlet.engine.http.connector.ConnectionState#CLOSING} in
     * order to release this stream.
     */
    public void onError();

}
