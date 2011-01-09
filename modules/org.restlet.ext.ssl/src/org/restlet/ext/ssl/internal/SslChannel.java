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

package org.restlet.ext.ssl.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.restlet.engine.io.BufferState;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.SelectionChannel;
import org.restlet.engine.io.WrapperSelectionChannel;

/**
 * Filter byte channel that enables secure communication using SSL/TLS
 * protocols. It is important to inherit from {@link SelectableChannel} as some
 * framework classes rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public abstract class SslChannel<T extends SelectionChannel> extends
        WrapperSelectionChannel<T> {

    /** The parent SSL connection. */
    private final SslConnection<?> connection;

    /** The SSL engine to use of wrapping and unwrapping. */
    private volatile SslManager manager;

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     * @param manager
     *            The SSL manager.
     * @param connection
     *            The parent SSL connection.
     */
    public SslChannel(T wrappedChannel, SslManager manager,
            SslConnection<?> connection) {
        super(wrappedChannel);
        this.manager = manager;
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
     * Returns the connection logger.
     * 
     * @return The connection logger.
     */
    public Logger getLogger() {
        return getConnection().getLogger();
    }

    /**
     * Returns the SSL manager wrapping the SSL context and engine.
     * 
     * @return The SSL manager wrapping the SSL context and engine.
     */
    public SslManager getManager() {
        return this.manager;
    }

    /**
     * Returns the SSL/TLS packet byte buffer.
     * 
     * @return The SSL/TLS packet byte buffer.
     */
    protected abstract ByteBuffer getPacketBuffer();

    /**
     * Returns the byte buffer state.
     * 
     * @return The byte buffer state.
     */
    protected abstract BufferState getPacketBufferState();

    /**
     * Handles the handshake states.
     * 
     * @param sslResult
     *            The SSL result to handle.
     * @param applicationBuffer
     *            The related application data buffer.
     */
    protected void handleHandshake(SSLEngineResult sslResult,
            ByteBuffer applicationBuffer) {

        // Handle the status
        switch (sslResult.getHandshakeStatus()) {
        case FINISHED:
            onHandshakeFinished(sslResult);
            break;

        case NEED_TASK:
            runTask(sslResult, applicationBuffer);
            break;

        case NEED_UNWRAP:
            onUnwrap(sslResult);
            break;

        case NEED_WRAP:
            onWrap(sslResult);
            break;

        case NOT_HANDSHAKING:
            // Don't do anything
            break;
        }
    }

    /**
     * Handles the result of a previous SSL engine processing.
     * 
     * @param sslResult
     *            The SSL result to handle.
     * @param applicationBuffer
     *            The related application data buffer.
     */
    protected void handleResult(SSLEngineResult sslResult,
            ByteBuffer applicationBuffer) {
        if (sslResult != null) {
            log(sslResult);

            // Store the engine status
            getManager().setEngineStatus(sslResult.getStatus());

            // Store the handshake status
            getManager().setHandshakeStatus(sslResult.getHandshakeStatus());

            switch (sslResult.getStatus()) {
            case BUFFER_OVERFLOW:
                onBufferOverflow(sslResult);
                break;

            case BUFFER_UNDERFLOW:
                onBufferUnderflow(sslResult);
                break;

            case CLOSED:
                onClosed(sslResult, applicationBuffer);
                break;

            case OK:
                onOk(sslResult, applicationBuffer);
                break;
            }
        }
    }

    /**
     * Logs the SSL engine result.
     * 
     * @param sslResult
     *            The SSL engine result to log.
     */
    protected void log(SSLEngineResult sslResult) {
        if (getLogger().isLoggable(Level.INFO)) {
            getLogger().log(Level.INFO, "SSL I/O result: " + sslResult);
            getLogger().log(Level.INFO, "SSL Manager: " + getManager());
        }
    }

    /**
     * Warns that an SSL buffer overflow exception occurred.
     * 
     * @param sslResult
     *            The SSL engine result.
     */
    protected void onBufferOverflow(SSLEngineResult sslResult) {
        getLogger().log(Level.WARNING, "SSL buffer overflow");
    }

    /**
     * Warns that an SSL buffer underflow exception occurred.
     * 
     * @param sslResult
     *            The SSL engine result.
     */
    protected void onBufferUnderflow(SSLEngineResult sslResult) {
        getLogger().log(Level.WARNING, "SSL buffer underflow");
    }

    /**
     * Notifies that the SSL engine has been properly closed and can no longer
     * be used.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application data buffer.
     */
    protected void onClosed(SSLEngineResult sslResult,
            ByteBuffer applicationBuffer) {
        getLogger().log(Level.INFO, "SSL engine closed");
        getManager().setState(SslState.CLOSED);
        getConnection().close(true);
        handleHandshake(sslResult, applicationBuffer);
    }

    /**
     * Notifies that the SSL handshake is finished. Application data can now be
     * exchanged.
     * 
     * @param sslResult
     *            The SSL engine result.
     */
    protected void onHandshakeFinished(SSLEngineResult sslResult) {
        getLogger().log(Level.INFO, "SSL handshake finished");

        if (getManager().getState() == SslState.HANDSHAKING) {
            if (getManager().isClientSide()) {
                getManager().setState(SslState.WRITING_APPLICATION_DATA);
            } else {
                getManager().setState(SslState.READING_APPLICATION_DATA);
            }

            getManager().setHandshakeStatus(HandshakeStatus.NOT_HANDSHAKING);
        }

        if (getConnection().isClientSide()) {
            getConnection().getInboundWay().setIoState(IoState.IDLE);
            getConnection().getOutboundWay().setIoState(IoState.INTEREST);
        } else {
            getConnection().getInboundWay().setIoState(IoState.INTEREST);
            getConnection().getOutboundWay().setIoState(IoState.IDLE);
        }
    }

    /**
     * Notifies that the SSL handling was successful.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application data buffer.
     */
    protected void onOk(SSLEngineResult sslResult, ByteBuffer applicationBuffer) {
        getLogger().log(Level.INFO, "SSL OK. Handle ");
        handleHandshake(sslResult, applicationBuffer);
    }

    /**
     * Called back when the delegated tasks have been run.
     */
    protected abstract void onDelegatedTasksCompleted();

    /**
     * Unwraps packet data into handshake or application data. Need to read
     * next.
     * 
     * @param sslResult
     *            The SSL engine result.
     */
    protected void onUnwrap(SSLEngineResult sslResult) {
        getLogger().log(Level.INFO, "SSL unwrap: " + sslResult);

        if (getConnection().getInboundWay().getIoState() != IoState.PROCESSING) {
            getConnection().getInboundWay().setIoState(IoState.INTEREST);
            getConnection().getOutboundWay().setIoState(IoState.IDLE);
        }
    }

    /**
     * Wraps the handshake or application data into packet data. Need to write
     * next.
     * 
     * @param sslResult
     *            The SSL engine result.
     */
    protected void onWrap(SSLEngineResult sslResult) {
        getLogger().log(Level.INFO, "SSL wrap: " + sslResult);

        if (getConnection().getOutboundWay().getIoState() != IoState.PROCESSING) {
            getConnection().getInboundWay().setIoState(IoState.IDLE);
            getConnection().getOutboundWay().setIoState(IoState.INTEREST);
        }
    }

    /**
     * Runs the SSL engine to do either wrapping or unwrapping.
     * 
     * @param applicationBuffer
     *            The related application data buffer.
     * @return The SSL engine result.
     */
    protected abstract SSLEngineResult runEngine(ByteBuffer applicationBuffer)
            throws IOException;

    /**
     * Runs the pending lengthy task.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application data buffer.
     */
    protected void runTask(SSLEngineResult sslResult,
            final ByteBuffer applicationBuffer) {
        // Delegate lengthy tasks to the connector's worker
        // service before checking again
        final Runnable task = getManager().getEngine().getDelegatedTask();

        if (task != null) {
            // Suspend IO processing until the task completes
            getConnection().getInboundWay().setIoState(IoState.IDLE);
            getConnection().getOutboundWay().setIoState(IoState.IDLE);

            // Runs the pending lengthy task.
            getConnection().getHelper().getWorkerService()
                    .execute(new Runnable() {
                        public void run() {
                            getLogger().log(Level.INFO,
                                    "Running delegated tasks...");
                            task.run();

                            // Check if a next task is pending
                            Runnable nextTask = getManager().getEngine()
                                    .getDelegatedTask();

                            // Run any pending task sequentially
                            while (nextTask != null) {
                                nextTask.run();
                                nextTask = getManager().getEngine()
                                        .getDelegatedTask();
                            }

                            onDelegatedTasksCompleted();
                            getLogger().log(Level.INFO,
                                    "Done running delegated tasks");
                        }
                    });
        }
    }

    /**
     * Sets the buffer state.
     * 
     * @param bufferState
     *            The buffer state.
     */
    protected abstract void setPacketBufferState(BufferState bufferState);

}
