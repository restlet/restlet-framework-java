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

package org.restlet.ext.nio.internal.channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import javax.net.ssl.SSLEngineResult;

import org.restlet.Context;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.SelectionChannel;
import org.restlet.engine.io.WakeupListener;
import org.restlet.ext.nio.internal.buffer.Buffer;
import org.restlet.ext.nio.internal.connection.SslConnection;
import org.restlet.ext.nio.internal.state.IoState;
import org.restlet.ext.nio.internal.state.MessageState;
import org.restlet.ext.nio.internal.util.TasksListener;

/**
 * SSL byte channel that unwraps all read data using the SSL/TLS protocols. It
 * is important to implement {@link SelectionChannel} as some framework classes
 * rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class ReadableSslChannel extends ReadableBufferedChannel implements
        TasksListener {

    /** The parent SSL connection. */
    private final SslConnection<?> connection;

    /**
     * Constructor.
     * 
     * @param source
     *            The source channel.
     * @param connection
     *            The parent SSL connection.
     * @param wakeupListener
     *            The wakeup listener that will be notified.
     */
    public ReadableSslChannel(ReadableSelectionChannel source,
            SslConnection<?> connection, WakeupListener wakeupListener) {
        super(null, new Buffer(connection.getPacketBufferSize(), connection
                .getHelper().isDirectBuffers()), source, wakeupListener);

        if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
            Context.getCurrentLogger().log(
                    Level.FINER,
                    "ReadableSslChannel created from: " + source
                            + ". Registration: " + getRegistration());
        }

        this.connection = connection;
    }

    /**
     * Returns the parent SSL connection.
     * 
     * @return The parent SSL connection.
     */
    protected SslConnection<?> getConnection() {
        return connection;
    }

    /**
     * Callback method invoked upon delegated tasks completion.
     */
    public void onCompleted() {
        if ((getConnection().getInboundWay().getMessageState() == MessageState.START)
                && (getConnection().getInboundWay().getIoState() == IoState.IDLE)) {
            getConnection().getInboundWay().setIoState(IoState.READY);
        }
    }

    /**
     * Drains the byte buffer. By default, it decrypts the SSL data and copies
     * as many byte as possible to the target buffer, with no modification.
     */
    @Override
    public int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        ByteBuffer applicationBuffer = (ByteBuffer) args[0];
        int initialSize = buffer.remaining();
        SSLEngineResult sslResult = getConnection().getSslEngine().unwrap(
                buffer.getBytes(), applicationBuffer);
        getConnection().setSslResult(sslResult);
        return initialSize - buffer.remaining();
    }

    @Override
    public void postProcess(int drained) throws IOException {
        getConnection().handleSslResult();
    }

}
