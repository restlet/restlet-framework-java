/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;

/** Recorded and opt-in live comparisons; no Ikanos dependency enters the Restlet module. */
final class McpIkanosDifferentialProbe {
    private static final McpJsonMapper JSON = McpJsonDefaults.getMapper();
    private static final int MAX_BYTES = 64 * 1024;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final String REVISION = "2026-07-28";

    private McpIkanosDifferentialProbe() {}

    private record ProbeCase(
            String name, String method, String body, Map<String, String> headers) {}

    @FunctionalInterface
    private interface ReferenceEndpoint {
        Map<String, Object> observe(ProbeCase test) throws IOException, InterruptedException;
    }

    static void runRecorded(Client prototype, String endpoint) throws Exception {
        Map<String, Map<String, Object>> baseline = baseline();
        compare(
                prototype,
                endpoint,
                test ->
                        JSON.convertValue(
                                baseline.get(test.name()).get("reference"),
                                new TypeRef<Map<String, Object>>() {}),
                baseline,
                "mcp-ikanos-recorded.json");
    }

    private static Map<String, Map<String, Object>> baseline() throws IOException {
        try (InputStream input =
                McpIkanosDifferentialProbe.class.getResourceAsStream(
                        "/mcp-ikanos/observations.json")) {
            assertNotNull(input, "Missing recorded Ikanos observations");
            return JSON.readValue(
                    new String(input.readAllBytes(), StandardCharsets.UTF_8),
                    new TypeRef<Map<String, Map<String, Object>>>() {});
        }
    }

    private static void compare(
            Client prototype,
            String endpoint,
            ReferenceEndpoint reference,
            Map<String, Map<String, Object>> baseline,
            String reportName)
            throws Exception {
        Map<String, Object> observations = new LinkedHashMap<>();
        Set<String> failures = new TreeSet<>();
        for (ProbeCase test : corpus()) {
            Map<String, Object> actual = observePrototype(prototype, endpoint, test);
            Map<String, Object> expected = reference.observe(test);
            Set<String> differences = new TreeSet<>();
            differences("", expected, actual, differences);
            Map<String, Object> observation =
                    Map.of(
                            "reference",
                            expected,
                            "prototype",
                            actual,
                            "differences",
                            List.copyOf(differences));
            observations.put(test.name(), observation);
            if (!observation.equals(baseline.get(test.name()))) {
                failures.add(test.name());
            }
        }
        Path report = Path.of("target", reportName).toAbsolutePath();
        Files.writeString(report, JSON.writeValueAsString(observations));
        assertEquals(baseline.keySet(), observations.keySet(), "Corpus and baseline differ");
        assertTrue(failures.isEmpty(), "Changed observations: " + failures + "\nReport: " + report);
    }

    static void run(Client prototype, String endpoint) throws Exception {
        Path home = Path.of(System.getProperty("restlet.mcp.ikanos.home")).toAbsolutePath();
        String configuredClasspath = System.getProperty("restlet.mcp.ikanos.classpathFile");
        assertNotNull(
                configuredClasspath,
                "Set restlet.mcp.ikanos.classpathFile to Ikanos' dependency:build-classpath output");
        Path classpathFile = Path.of(configuredClasspath);
        for (String module : List.of("ikanos-engine", "ikanos-spec")) {
            assertTrue(
                    Files.isDirectory(
                            home.resolve("modules")
                                    .resolve(module)
                                    .resolve("target")
                                    .resolve("classes")),
                    "Compile the Ikanos reactor first: " + module);
        }
        String classpath =
                home.resolve("modules")
                                .resolve("ikanos-engine")
                                .resolve("target")
                                .resolve("classes")
                        + File.pathSeparator
                        + home.resolve("modules")
                                .resolve("ikanos-spec")
                                .resolve("target")
                                .resolve("classes")
                        + File.pathSeparator
                        + Files.readString(classpathFile).strip();
        Path target = Path.of("target").toAbsolutePath();
        Path work = Files.createTempDirectory(target, "mcp-ikanos-");
        Path ready = work.resolve("endpoint");
        Path log = work.resolve("reference.log");
        String java =
                Path.of(
                                System.getProperty("java.home"),
                                "bin",
                                System.getProperty("os.name").startsWith("Windows")
                                        ? "java.exe"
                                        : "java")
                        .toString();
        Process reference =
                new ProcessBuilder(
                                java,
                                "--class-path",
                                classpath,
                                Path.of(
                                                "src",
                                                "test",
                                                "resources",
                                                "mcp-ikanos",
                                                "IkanosReferenceEndpoint.java")
                                        .toAbsolutePath()
                                        .toString(),
                                ready.toString())
                        .redirectErrorStream(true)
                        .redirectOutput(log.toFile())
                        .start();
        try (HttpClient http =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(TIMEOUT)
                        .build()) {
            long deadline = System.nanoTime() + Duration.ofSeconds(45).toNanos();
            while (!Files.exists(ready) && reference.isAlive() && System.nanoTime() < deadline) {
                Thread.sleep(50);
            }
            assertTrue(Files.exists(ready), "Ikanos reference did not become ready; see " + log);
            String referenceEndpoint = Files.readString(ready);
            compare(
                    prototype,
                    endpoint,
                    test -> observeReference(http, referenceEndpoint, test),
                    baseline(),
                    "mcp-ikanos-differential.json");
        } finally {
            try {
                reference.getOutputStream().close();
            } finally {
                if (!reference.waitFor(10, TimeUnit.SECONDS)) {
                    reference.destroy();
                    if (!reference.waitFor(5, TimeUnit.SECONDS)) {
                        reference.destroyForcibly();
                        assertTrue(
                                reference.waitFor(5, TimeUnit.SECONDS),
                                "Ikanos reference process did not terminate");
                    }
                    throw new AssertionError(
                            "Ikanos reference required forced shutdown; see " + log);
                }
            }
        }
    }

    private static List<ProbeCase> corpus() throws IOException {
        Map<String, Object> call = Map.of("name", "echo", "arguments", Map.of("text", "profile"));
        List<ProbeCase> cases = new ArrayList<>();
        cases.add(rpc("list", "tools/list", Map.of(), Map.of()));
        cases.add(rpc("call", "tools/call", call, Map.of()));
        cases.add(
                rpc(
                        "tool-error",
                        "tools/call",
                        Map.of("name", "echo", "arguments", Map.of("text", "fail")),
                        Map.of()));
        cases.add(
                rpc("encoded-name", "tools/call", call, Map.of("Mcp-Name", "=?base64?ZWNobw==?=")));
        cases.add(rpc("missing-method", "tools/call", call, Map.of("Mcp-Method", "")));
        cases.add(rpc("mismatched-method", "tools/call", call, Map.of("Mcp-Method", "tools/list")));
        cases.add(rpc("missing-version", "tools/call", call, Map.of("MCP-Protocol-Version", "")));
        cases.add(
                rpc(
                        "mismatched-version",
                        "tools/call",
                        call,
                        Map.of("MCP-Protocol-Version", "2025-11-25")));
        cases.add(rpc("missing-name", "tools/call", call, Map.of("Mcp-Name", "")));
        cases.add(rpc("mismatched-name", "tools/call", call, Map.of("Mcp-Name", "other")));
        cases.add(rpc("invalid-base64", "tools/call", call, Map.of("Mcp-Name", "=?base64?%%%?=")));
        cases.add(
                rpc(
                        "unsupported-version",
                        "tools/call",
                        Map.of(
                                "name",
                                "echo",
                                "_meta",
                                Map.of("io.modelcontextprotocol/protocolVersion", "2025-11-25")),
                        Map.of("MCP-Protocol-Version", "2025-11-25")));
        cases.add(
                rpc(
                        "unknown-tool",
                        "tools/call",
                        Map.of("name", "missing", "arguments", Map.of()),
                        Map.of("Mcp-Name", "missing")));
        cases.add(rpc("unknown-method", "unknown", Map.of(), Map.of()));
        cases.add(rpc("discovery", "server/discover", Map.of(), Map.of()));
        cases.add(
                new ProbeCase(
                        "legacy-initialize",
                        "POST",
                        JSON.writeValueAsString(
                                Map.of(
                                        "jsonrpc",
                                        "2.0",
                                        "id",
                                        17,
                                        "method",
                                        "initialize",
                                        "params",
                                        Map.of(
                                                "protocolVersion",
                                                "2025-11-25",
                                                "capabilities",
                                                Map.of(),
                                                "clientInfo",
                                                Map.of("name", "r0", "version", "1")))),
                        Map.of()));
        cases.add(new ProbeCase("malformed-json", "POST", "{", Map.of()));
        cases.add(new ProbeCase("empty-body", "POST", "", Map.of()));
        for (String method : List.of("GET", "DELETE")) {
            cases.add(new ProbeCase(method, method, "", Map.of()));
        }
        return cases;
    }

    private static ProbeCase rpc(
            String name, String method, Map<String, Object> params, Map<String, String> overrides)
            throws IOException {
        Map<String, Object> bodyParams = new LinkedHashMap<>(params);
        bodyParams.putIfAbsent(
                "_meta", Map.of("io.modelcontextprotocol/protocolVersion", REVISION));
        Map<String, String> headers =
                new LinkedHashMap<>(
                        Map.of(
                                "Mcp-Method",
                                method,
                                "MCP-Protocol-Version",
                                REVISION,
                                "Mcp-Name",
                                "echo"));
        headers.putAll(overrides);
        headers.values().removeIf(String::isEmpty);
        return new ProbeCase(
                name,
                "POST",
                JSON.writeValueAsString(
                        Map.of("jsonrpc", "2.0", "id", 17, "method", method, "params", bodyParams)),
                headers);
    }

    private static Map<String, Object> observePrototype(
            Client client, String endpoint, ProbeCase test) throws IOException {
        Request request = new Request(Method.valueOf(test.method()), endpoint);
        if ("POST".equals(test.method())) {
            request.setEntity(new StringRepresentation(test.body(), MediaType.APPLICATION_JSON));
        }
        test.headers().forEach((name, value) -> request.getHeaders().add(name, value));
        Response response = client.handle(request);
        try {
            assertEquals("2.0", response.getProtocol().getVersion());
            byte[] body;
            if (response.getEntity() == null) {
                body = new byte[0];
            } else {
                try (InputStream input = response.getEntity().getStream()) {
                    body = input == null ? new byte[0] : input.readNBytes(MAX_BYTES + 1);
                }
            }
            return observation(
                    response.getStatus().getCode(),
                    response.getHeaders().getFirstValue("Mcp-Session-Id", true),
                    body,
                    response.getEntity() != null
                            && MediaType.APPLICATION_JSON.equals(
                                    response.getEntity().getMediaType(), true));
        } finally {
            if (response.getEntity() != null) {
                response.getEntity().release();
            }
        }
    }

    private static Map<String, Object> observeReference(
            HttpClient client, String endpoint, ProbeCase test)
            throws IOException, InterruptedException {
        HttpRequest.Builder request =
                HttpRequest.newBuilder(URI.create(endpoint))
                        .timeout(TIMEOUT)
                        .header("Content-Type", "application/json")
                        .method(
                                test.method(),
                                "POST".equals(test.method())
                                        ? HttpRequest.BodyPublishers.ofString(test.body())
                                        : HttpRequest.BodyPublishers.noBody());
        test.headers().forEach(request::header);
        HttpResponse<InputStream> response =
                client.send(request.build(), HttpResponse.BodyHandlers.ofInputStream());
        try (InputStream input = response.body()) {
            return observation(
                    response.statusCode(),
                    response.headers().firstValue("Mcp-Session-Id").orElse(null),
                    input.readNBytes(MAX_BYTES + 1),
                    response.headers()
                            .firstValue("Content-Type")
                            .orElse("")
                            .startsWith("application/json"));
        }
    }

    private static Map<String, Object> observation(
            int status, String session, byte[] bytes, boolean json) throws IOException {
        assertTrue(bytes.length <= MAX_BYTES, "Differential response exceeds limit");
        assertNull(session, "Unexpected MCP session");
        String text = new String(bytes, StandardCharsets.UTF_8);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", status);
        result.put(
                "body",
                json && !text.isEmpty() ? JSON.readValue(text, new TypeRef<Object>() {}) : text);
        return result;
    }

    private static void differences(
            String path, Object expected, Object actual, Set<String> result) {
        if (expected instanceof Map<?, ?> left && actual instanceof Map<?, ?> right) {
            Set<Object> keys = new java.util.HashSet<>(left.keySet());
            keys.addAll(right.keySet());
            for (Object key : keys) {
                String child = path + "/" + key.toString().replace("~", "~0").replace("/", "~1");
                if (!left.containsKey(key) || !right.containsKey(key)) {
                    result.add(child);
                } else {
                    differences(child, left.get(key), right.get(key), result);
                }
            }
        } else if (expected instanceof List<?> left
                && actual instanceof List<?> right
                && left.size() == right.size()) {
            for (int index = 0; index < left.size(); index++) {
                differences(path + "/" + index, left.get(index), right.get(index), result);
            }
        } else if (!Objects.equals(expected, actual)) {
            result.add(path);
        }
    }
}
