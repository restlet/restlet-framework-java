/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The era each MCP HTTP endpoint was found to speak, modern or legacy, as the 2026-07-28 revision
 * recommends a dual-era client remember it. It is kept by endpoint URI rather than by origin, since
 * one origin may host servers of both eras on different paths, and only for this JVM. A client
 * starting on a cached era that the server contradicts forgets it and probes again.
 */
public final class McpEras {
    /** An endpoint answering {@code server/discover}. */
    public static final String MODERN = "modern";

    /** An endpoint expecting an {@code initialize} handshake. */
    public static final String LEGACY = "legacy";

    /** The most endpoints remembered: past it, the cache starts over. */
    static final int MAX_ENTRIES = 1024;

    private static final Map<String, String> ERAS = new ConcurrentHashMap<>();

    private McpEras() {}

    /**
     * The era last found for an endpoint.
     *
     * @param target The endpoint URI.
     * @return {@link #MODERN}, {@link #LEGACY}, or null if unknown.
     */
    public static String get(String target) {
        return ERAS.get(target);
    }

    /**
     * Remembers the era of an endpoint.
     *
     * @param target The endpoint URI.
     * @param era {@link #MODERN} or {@link #LEGACY}.
     */
    public static void put(String target, String era) {
        if (ERAS.size() >= MAX_ENTRIES && !ERAS.containsKey(target)) {
            ERAS.clear();
        }
        ERAS.put(target, era);
    }

    /**
     * Forgets the era of an endpoint.
     *
     * @param target The endpoint URI.
     */
    public static void remove(String target) {
        ERAS.remove(target);
    }

    /** Forgets every endpoint. */
    public static void clear() {
        ERAS.clear();
    }
}
