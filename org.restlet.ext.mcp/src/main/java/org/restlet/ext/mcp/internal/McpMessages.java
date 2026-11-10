/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Bounded transport encoding; all MCP message decoding remains in the official SDK. */
public final class McpMessages {
    private McpMessages() {}

    public static byte[] read(InputStream input, int limit) throws IOException {
        byte[] bytes = input.readNBytes(limit + 1);
        if (bytes.length > limit) {
            throw new MessageLimitException("MCP message exceeds " + limit + " bytes");
        }
        return bytes;
    }

    public static String utf8(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
    }

    public static byte[] encode(McpJsonMapper mapper, Object message, int limit)
            throws IOException {
        byte[] bytes = mapper.writeValueAsString(message).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > limit) {
            throw new IOException("MCP encoded message exceeds " + limit + " bytes");
        }
        return bytes;
    }

    public static McpSchema.JSONRPCMessage decode(McpJsonMapper mapper, String json)
            throws IOException {
        Object decoded = mapper.readValue(json, new TypeRef<Object>() {});
        if (!(decoded instanceof Map<?, ?> envelope)
                || !"2.0".equals(envelope.get("jsonrpc"))
                || !(envelope.get("method") instanceof String method)
                || method.isBlank()
                || envelope.containsKey("params")
                        && envelope.get("params") != null
                        && !(envelope.get("params") instanceof Map<?, ?>)
                || envelope.containsKey("id")
                        && !(envelope.get("id") instanceof String
                                || envelope.get("id") instanceof Number)) {
            throw new InvalidEnvelopeException("Invalid MCP request or notification envelope");
        }

        return McpSchema.deserializeJsonRpcMessage(mapper, json);
    }

    public static final class MessageLimitException extends IOException {
        private static final long serialVersionUID = 1L;

        public MessageLimitException(String message) {
            super(message);
        }
    }

    public static final class InvalidEnvelopeException extends IOException {
        private static final long serialVersionUID = 1L;

        public InvalidEnvelopeException(String message) {
            super(message);
        }
    }

    public static Map<String, Object> error(Object id, int code, String message) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("jsonrpc", "2.0");
        envelope.put("id", id);
        envelope.put("error", Map.of("code", code, "message", message));
        return envelope;
    }

    public static void writeFrame(org.restlet.util.StreamTransport transport, byte[] bytes)
            throws IOException {
        byte[] frame = java.util.Arrays.copyOf(bytes, bytes.length + 1);
        frame[bytes.length] = '\n';
        if (frame.length > transport.getMaxOperationBytes()) {
            throw new IOException("MCP frame exceeds configured stream operation limit");
        }
        transport.write(frame);
    }

    public static byte[] readFrame(org.restlet.util.StreamTransport transport, int limit)
            throws IOException {
        var frame = new ByteArrayOutputStream(Math.min(limit, 8192));
        byte[] single = new byte[1];
        while (transport.read(single, 0, 1) != -1) {
            if (single[0] == '\n') {
                return frame.toByteArray();
            }
            if (frame.size() == limit) {
                throw new IOException("MCP STDIO frame exceeds " + limit + " bytes");
            }
            frame.write(single[0]);
        }
        if (frame.size() != 0) {
            throw new IOException("Truncated MCP STDIO frame");
        }
        return null;
    }

    /**
     * A request id comparable across its JSON number forms, such as 1 and 1.0: a long when the
     * number is an integer in its range, exactly, else the number itself, normalised.
     *
     * @param id The id.
     * @return Its key.
     */
    public static Object idKey(Object id) {
        if (!(id instanceof Number number)) {
            return id;
        }
        BigDecimal exact = exact(number);
        if (exact == null) {
            return id; // NaN or infinite: compared as given.
        }
        try {
            return exact.longValueExact();
        } catch (ArithmeticException _) {
            return exact.stripTrailingZeros();
        }
    }

    /**
     * The exact value of a number: a floating-point one by its binary value, not its shortest
     * decimal form, which may round an integer such as 1000000000000000128.0.
     *
     * @return The value, or null for NaN or an infinity.
     */
    private static BigDecimal exact(Number number) {
        return switch (number) {
            case BigDecimal decimal -> decimal;
            case BigInteger integer -> new BigDecimal(integer);
            case Double _, Float _ ->
                    Double.isFinite(number.doubleValue())
                            ? new BigDecimal(number.doubleValue())
                            : null;
            default -> BigDecimal.valueOf(number.longValue());
        };
    }
}
