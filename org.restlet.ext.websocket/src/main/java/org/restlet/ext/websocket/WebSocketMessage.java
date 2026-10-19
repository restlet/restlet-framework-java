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

    /** Either a {@link String} or a {@code byte[]}, never null. */
    private final Object payload;

    private WebSocketMessage(Object payload) {
        this.payload = payload;
    }

    /**
     * Creates a text message.
     *
     * @param text The text.
     * @return The message.
     */
    public static WebSocketMessage text(String text) {
        return new WebSocketMessage(Objects.requireNonNull(text, "text"));
    }

    /**
     * Creates a binary message.
     *
     * @param data The bytes, copied.
     * @return The message.
     */
    public static WebSocketMessage binary(byte[] data) {
        return new WebSocketMessage(Objects.requireNonNull(data, "data").clone());
    }

    /**
     * Indicates whether this is a text message.
     *
     * @return True for a text message.
     */
    public boolean isText() {
        return payload instanceof String;
    }

    /**
     * Indicates whether this is a binary message.
     *
     * @return True for a binary message.
     */
    public boolean isBinary() {
        return payload instanceof byte[];
    }

    /**
     * Returns the text of a text message, or the UTF-8 decoding of a binary message.
     *
     * @return The text.
     */
    public String getText() {
        return (payload instanceof String text)
                ? text
                : new String((byte[]) payload, StandardCharsets.UTF_8);
    }

    /**
     * Returns the bytes of a binary message, or the UTF-8 encoding of a text message.
     *
     * @return A copy of the bytes.
     */
    public byte[] getData() {
        return (payload instanceof byte[] data)
                ? data.clone()
                : ((String) payload).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WebSocketMessage that)) {
            return false;
        }
        if (payload instanceof byte[] data) {
            return that.payload instanceof byte[] other && Arrays.equals(data, other);
        }
        return payload.equals(that.payload);
    }

    @Override
    public int hashCode() {
        return (payload instanceof byte[] data) ? Arrays.hashCode(data) : payload.hashCode();
    }

    @Override
    public String toString() {
        return (payload instanceof String text)
                ? "text[" + text.length() + " chars]"
                : "binary[" + ((byte[]) payload).length + " bytes]";
    }
}
