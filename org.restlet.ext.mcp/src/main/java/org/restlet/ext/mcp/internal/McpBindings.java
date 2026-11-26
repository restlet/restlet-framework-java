/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import io.modelcontextprotocol.modern.JsonRpc;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The SDK transport context entries binding a request's sealed {@code requestState}: the value of
 * an extension method's name param, and the digest of the request's arguments. Read through {@link
 * org.restlet.ext.mcp.McpRequestBinding}. Internal to the MCP extension.
 */
public final class McpBindings {

    /** The SDK transport context key of the value of an extension method's name param. */
    public static final String BOUND_NAME_KEY = "org.restlet.ext.mcp.boundName";

    /** The SDK transport context key of the request's arguments digest. */
    public static final String ARGUMENTS_DIGEST_KEY = "org.restlet.ext.mcp.argumentsDigest";

    /** The params a retry adds or changes, left out of the arguments digest. */
    private static final List<String> RETRY_PARAMS =
            List.of("_meta", "requestState", "inputResponses");

    private McpBindings() {}

    /**
     * The entries binding a request's sealed state.
     *
     * @param request The request.
     * @param nameParams The params field each extension method's {@code Mcp-Name} mirrors.
     * @return The entries: the arguments digest, and the name param value of an extension method
     *     declaring one.
     */
    public static Map<String, Object> of(
            JsonRpc.JSONRPCRequest request, Map<String, String> nameParams) {
        Map<String, Object> entries = new HashMap<>();
        if (request.params() instanceof Map<?, ?> params) {
            entries.put(ARGUMENTS_DIGEST_KEY, digest(params));
            String param = nameParams.get(request.method());
            if (param != null && params.get(param) instanceof String value) {
                entries.put(BOUND_NAME_KEY, value);
            }
        }
        return entries;
    }

    /**
     * Computes the arguments digest of request params.
     *
     * @param params The params.
     * @return The digest.
     */
    public static String digest(Map<?, ?> params) {
        Map<String, Object> arguments = new TreeMap<>();
        params.forEach(
                (key, value) -> {
                    if (!RETRY_PARAMS.contains(String.valueOf(key))) {
                        arguments.put(String.valueOf(key), value);
                    }
                });
        StringBuilder canonical = new StringBuilder();
        write(canonical, arguments);
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** Writes a JSON value with sorted object keys. */
    private static void write(StringBuilder out, Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sorted = new TreeMap<>();
            map.forEach((key, item) -> sorted.put(String.valueOf(key), item));
            out.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> entry : sorted.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                string(out, entry.getKey());
                out.append(':');
                write(out, entry.getValue());
            }
            out.append('}');
        } else if (value instanceof Iterable<?> items) {
            out.append('[');
            boolean first = true;
            for (Object item : items) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                write(out, item);
            }
            out.append(']');
        } else if (value instanceof String text) {
            string(out, text);
        } else if (value instanceof Number number) {
            // 1, 1.0 and 1e0 are one JSON number.
            out.append(canonical(number));
        } else {
            out.append(value);
        }
    }

    /**
     * A number's canonical form: 1, 1.0 and 1e0 are one JSON number. A number beyond a double, such
     * as 1e400 read as infinity, keeps its own form rather than failing the request.
     */
    private static String canonical(Number number) {
        if (number instanceof Double || number instanceof Float) {
            double value = number.doubleValue();
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return number.toString();
            }
        }
        try {
            return new java.math.BigDecimal(number.toString()).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException e) {
            return number.toString();
        }
    }

    private static void string(StringBuilder out, String text) {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (c < 0x20) {
                out.append(String.format("\\u%04x", (int) c));
            } else {
                out.append(c);
            }
        }
        out.append('"');
    }
}
