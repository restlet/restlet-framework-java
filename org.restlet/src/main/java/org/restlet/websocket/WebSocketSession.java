/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.websocket;

import java.io.IOException;

/**
 * An open WebSocket connection, used with blocking calls from a (virtual) thread. Messages are
 * meant to be received by a single thread; they may be sent from several threads.
 */
public interface WebSocketSession extends AutoCloseable {

    /** Normal closure status code. */
    int NORMAL_CLOSURE = 1000;

    /** Status code sent when an endpoint is going away. */
    int GOING_AWAY = 1001;

    /** Status code sent on a protocol error. */
    int PROTOCOL_ERROR = 1002;

    /** Status code reported when the peer closed without a status code. */
    int NO_STATUS = 1005;

    /** Status code reported when the connection closed without a close frame. */
    int ABNORMAL_CLOSURE = 1006;

    /** Status code sent when a message is too big to process. */
    int MESSAGE_TOO_BIG = 1009;

    /** Status code sent on an unexpected condition. */
    int INTERNAL_ERROR = 1011;

    /**
     * Returns the negotiated subprotocol.
     *
     * @return The subprotocol, or null if none was negotiated.
     */
    String getSubprotocol();

    /**
     * Blocks until the next complete message arrives. Ping and pong frames are handled by the
     * connector.
     *
     * @return The next message, or null once the session is closed.
     * @throws IOException If the wait is interrupted.
     */
    WebSocketMessage receive() throws IOException;

    /**
     * Sends a text message, blocking while the peer is slow.
     *
     * @param text The text.
     * @throws IOException If the session is closed or the write fails.
     */
    void sendText(String text) throws IOException;

    /**
     * Sends a binary message, blocking while the peer is slow.
     *
     * @param data The bytes.
     * @throws IOException If the session is closed or the write fails.
     */
    void sendBinary(byte[] data) throws IOException;

    /**
     * Sends a message, blocking while the peer is slow.
     *
     * @param message The message.
     * @throws IOException If the session is closed or the write fails.
     */
    default void send(WebSocketMessage message) throws IOException {
        if (message.isText()) {
            sendText(message.getText());
        } else {
            sendBinary(message.getData());
        }
    }

    /**
     * Starts the close handshake and waits briefly for the peer to complete it. Does nothing if the
     * session is already closing.
     *
     * @param code The close status code (1000, or 3000-4999 for applications).
     * @param reason The close reason, may be null.
     * @throws IOException If the wait is interrupted.
     */
    void close(int code, String reason) throws IOException;

    /**
     * Closes the session normally.
     *
     * @throws IOException If the wait is interrupted.
     */
    @Override
    default void close() throws IOException {
        close(NORMAL_CLOSURE, null);
    }

    /**
     * Indicates whether the session is still open: no close frame was sent or received and the
     * connection is active.
     *
     * @return True if the session is open.
     */
    boolean isOpen();

    /**
     * Returns the status code of the close frame received from the peer, {@link #NO_STATUS} if it
     * had none, or {@link #ABNORMAL_CLOSURE} if the connection closed without one.
     *
     * @return The close status code, or -1 while no close frame was received.
     */
    int getCloseCode();

    /**
     * Returns the reason of the close frame received from the peer.
     *
     * @return The close reason, or null.
     */
    String getCloseReason();
}
