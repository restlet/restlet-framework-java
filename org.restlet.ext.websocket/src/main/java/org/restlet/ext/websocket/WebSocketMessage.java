/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/** An immutable, complete WebSocket data message: either text or binary. */
public final class WebSocketMessage {

    private final String text;

    private final byte[] data;

    private WebSocketMessage(String text, byte[] data) {
        this.text = text;
        this.data = data;
    }

    /**
     * Creates a text message.
     *
     * @param text The text.
     * @return The message.
     */
    public static WebSocketMessage text(String text) {
        return new WebSocketMessage(Objects.requireNonNull(text, "text"), null);
    }

    /**
     * Creates a binary message.
     *
     * @param data The bytes, copied.
     * @return The message.
     */
    public static WebSocketMessage binary(byte[] data) {
        return new WebSocketMessage(null, Objects.requireNonNull(data, "data").clone());
    }

    /**
     * Indicates whether this is a text message.
     *
     * @return True for a text message.
     */
    public boolean isText() {
        return text != null;
    }

    /**
     * Indicates whether this is a binary message.
     *
     * @return True for a binary message.
     */
    public boolean isBinary() {
        return data != null;
    }

    /**
     * Returns the text of a text message, or the UTF-8 decoding of a binary message.
     *
     * @return The text.
     */
    public String getText() {
        return isText() ? text : new String(data, StandardCharsets.UTF_8);
    }

    /**
     * Returns the bytes of a binary message, or the UTF-8 encoding of a text message.
     *
     * @return A copy of the bytes.
     */
    public byte[] getData() {
        return isBinary() ? data.clone() : text.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof WebSocketMessage that
                && Objects.equals(text, that.text)
                && Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return 31 * Objects.hashCode(text) + Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return isText() ? "text[" + text.length() + " chars]" : "binary[" + data.length + " bytes]";
    }
}
