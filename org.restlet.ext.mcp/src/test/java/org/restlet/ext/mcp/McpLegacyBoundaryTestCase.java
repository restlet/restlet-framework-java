/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Keeps legacy MCP a thin, deletable edge: only {@code internal.legacy} and its seam touch the
 * legacy package, only the legacy package touches the SDK's legacy API, and the legacy package
 * touches nothing MCP 2026-07-28 deprecates or removes.
 */
class McpLegacyBoundaryTestCase {
    private static final Path SOURCES = Path.of("src/main/java/org/restlet/ext/mcp");
    private static final Path LEGACY = SOURCES.resolve("internal/legacy");
    private static final Path SEAM = SOURCES.resolve("internal/McpLegacyEdge.java");

    /** The SDK's legacy client, server and schema packages, wildcard imports included. */
    private static final Pattern LEGACY_SDK =
            Pattern.compile("io\\.modelcontextprotocol\\.(spec|server|client)\\.[\\w.]*[\\w*]");

    /**
     * Legacy SDK types the modern core can't avoid, by file: the modern SDK's own API takes them.
     */
    private static final Map<String, List<String>> ALLOWED =
            Map.of(
                    "McpModernExchange.java",
                    List.of("io.modelcontextprotocol.server.transport.HeaderAccessor"));

    /** What 2026-07-28 deprecates or removes: logging, sampling and roots. */
    private static final Pattern DEPRECATED =
            Pattern.compile(
                    "\\b(Logging\\w*|SetLevelRequest|setLoggingLevel|loggingConsumer"
                            + "|CreateMessage\\w*|sampling|ListRoots\\w*|Root|roots"
                            + "|METHOD_LOGGING\\w*|METHOD_SAMPLING\\w*|METHOD_ROOTS\\w*)\\b");

    private static List<Path> sources() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            return files.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    /** The code of a file, without its comments. */
    private static String code(Path file) throws IOException {
        return Files.readString(file).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "");
    }

    @Test
    void onlyTheSeamReachesTheLegacyPackage() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : sources()) {
            if (!file.startsWith(LEGACY)
                    && !file.equals(SEAM)
                    && code(file).contains("org.restlet.ext.mcp.internal.legacy")) {
                offenders.add(file.toString());
            }
        }
        assertEquals(List.of(), offenders);
    }

    @Test
    void onlyTheLegacyPackageUsesTheLegacySdk() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : sources()) {
            if (file.startsWith(LEGACY)) {
                continue;
            }
            List<String> allowed = ALLOWED.getOrDefault(file.getFileName().toString(), List.of());
            var matcher = LEGACY_SDK.matcher(code(file));
            while (matcher.find()) {
                if (!allowed.contains(matcher.group())) {
                    offenders.add(file + ": " + matcher.group());
                }
            }
        }
        assertEquals(List.of(), offenders);
    }

    /** A wildcard import is caught too, while the allowed type still matches exactly. */
    @Test
    void theLegacySdkPatternCatchesWildcardImports() {
        for (String line :
                List.of(
                        "import io.modelcontextprotocol.spec.*;",
                        "import io.modelcontextprotocol.server.*;",
                        "import io.modelcontextprotocol.client.*;",
                        "import static io.modelcontextprotocol.spec.McpSchema.*;")) {
            var matcher = LEGACY_SDK.matcher(line);
            assertTrue(matcher.find(), line);
            assertTrue(matcher.group().endsWith("*"), matcher.group());
        }
        var matcher =
                LEGACY_SDK.matcher(
                        "import io.modelcontextprotocol.server.transport.HeaderAccessor;");
        assertTrue(matcher.find());
        assertEquals("io.modelcontextprotocol.server.transport.HeaderAccessor", matcher.group());
    }

    @Test
    void theLegacyEdgeAvoidsWhatTheModernRevisionDropped() throws IOException {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.list(LEGACY)) {
            for (Path file : files.toList()) {
                var matcher = DEPRECATED.matcher(code(file));
                while (matcher.find()) {
                    offenders.add(file.getFileName() + ": " + matcher.group());
                }
            }
        }
        assertEquals(List.of(), offenders);
        // The server edge never issues a session; the client sends one only on opt-in.
        String server = code(LEGACY.resolve("McpLegacyServer.java"));
        assertFalse(server.contains("SESSION_HEADER") || server.contains("Mcp-Session-Id"));
    }
}
