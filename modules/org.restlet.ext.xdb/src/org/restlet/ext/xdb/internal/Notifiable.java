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

package org.restlet.ext.xdb.internal;

/**
 * Marker class for objects such as
 * {@link org.restlet.engine.http.connector.Connection} that can be notified of
 * stream events.
 * 
 * @author Jerome Louvel
 * @deprecated Not actively developed anymore.
 */
@Deprecated
public interface Notifiable {

    /**
     * To be called when the end of the stream is reached. By default, it
     * updates the state of the connection (
     * {@link org.restlet.engine.http.connector.Connection#setInboundBusy(boolean)}
     * ) .
     */
    void onEndReached();

    /**
     * To be called when there is an error when handling the stream. By default
     * it calls {@link #onEndReached()} and set the state of the connection to
     * {@link org.restlet.engine.http.connector.ConnectionState#CLOSING} in
     * order to release this stream.
     */
    void onError();

}
