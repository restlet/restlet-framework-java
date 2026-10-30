/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * JSON encoding of GraphQL requests and results. GraphQL values are plain maps, lists, strings,
 * numbers, booleans and nulls, so no binding to Java types is needed.
 */
public final class GraphqlJson {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private static final TypeReference<Object> ANY = new TypeReference<>() {};

    private GraphqlJson() {}

    /**
     * Reads a JSON value from at most {@code maxBytes} bytes.
     *
     * @param in The input stream, not closed.
     * @param maxBytes The maximum number of bytes to read.
     * @return The value: a map, a list, a string, a number, a boolean or null.
     * @throws TooLargeException If the stream has more than {@code maxBytes} bytes.
     * @throws InvalidJsonException If the content is not valid JSON.
     * @throws IOException If reading fails.
     */
    public static Object read(InputStream in, long maxBytes) throws IOException {
        return parse(readBytes(in, maxBytes));
    }

    /**
     * Parses a JSON value.
     *
     * @param json The JSON text.
     * @return The value: a map, a list, a string, a number, a boolean or null.
     * @throws InvalidJsonException If the text is not valid JSON.
     */
    public static Object parse(String json) {
        try {
            return MAPPER.readValue(json, ANY);
        } catch (JacksonException e) {
            throw new InvalidJsonException(e.getOriginalMessage(), e);
        }
    }

    /**
     * Parses a JSON value.
     *
     * @param json The JSON bytes, in UTF-8.
     * @return The value: a map, a list, a string, a number, a boolean or null.
     * @throws InvalidJsonException If the bytes are not valid JSON.
     */
    public static Object parse(byte[] json) {
        try {
            return MAPPER.readValue(json, ANY);
        } catch (JacksonException e) {
            throw new InvalidJsonException(e.getOriginalMessage(), e);
        }
    }

    /**
     * Reads at most {@code maxBytes} bytes.
     *
     * @param in The input stream, not closed.
     * @param maxBytes The maximum number of bytes to read.
     * @return The bytes.
     * @throws TooLargeException If the stream has more than {@code maxBytes} bytes.
     * @throws IOException If reading fails.
     */
    public static byte[] readBytes(InputStream in, long maxBytes) throws IOException {
        int limit = (int) Math.min(maxBytes, Integer.MAX_VALUE - 8L);
        byte[] bytes = in.readNBytes(limit);
        if (bytes.length == limit && in.read() != -1) {
            throw new TooLargeException(maxBytes);
        }
        return bytes;
    }

    /**
     * Writes a value as JSON.
     *
     * @param value The value, typically from {@code ExecutionResult.toSpecification()}.
     * @return The UTF-8 bytes.
     */
    public static byte[] write(Object value) {
        return MAPPER.writeValueAsBytes(value);
    }

    /**
     * Writes a value as JSON.
     *
     * @param value The value, typically from {@code ExecutionResult.toSpecification()}.
     * @return The JSON text.
     */
    public static String writeString(Object value) {
        return MAPPER.writeValueAsString(value);
    }

    /**
     * Returns a JSON object as a map.
     *
     * @param value A value returned by {@link #parse(String)}.
     * @return The map, or null if the value is not a JSON object.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    /** Thrown when content is not valid JSON. */
    public static final class InvalidJsonException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        InvalidJsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** Thrown when content exceeds its size limit. */
    public static final class TooLargeException extends IOException {

        private static final long serialVersionUID = 1L;

        TooLargeException(long maxBytes) {
            super("Content exceeds " + maxBytes + " bytes");
        }
    }
}
