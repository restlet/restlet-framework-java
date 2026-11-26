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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.modern.JsonRpc;
import io.modelcontextprotocol.modern.McpSchema.MetaKeys;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** The JSON mapper of the MCP messages, the SDK's Jackson 3 one or the application's. */
class McpJsonMapperTestCase {

    /** The SDK's Jackson 3 mapper, counting its conversions. */
    private record Counting(AtomicInteger conversions) implements McpJsonMapper {
        private static final McpJsonMapper DELEGATE =
                new JacksonMcpJsonMapper(JsonMapper.builder().build());

        @Override
        public <T> T readValue(String content, Class<T> type) throws java.io.IOException {
            return DELEGATE.readValue(content, type);
        }

        @Override
        public <T> T readValue(byte[] content, Class<T> type) throws java.io.IOException {
            return DELEGATE.readValue(content, type);
        }

        @Override
        public <T> T readValue(String content, io.modelcontextprotocol.json.TypeRef<T> type)
                throws java.io.IOException {
            return DELEGATE.readValue(content, type);
        }

        @Override
        public <T> T readValue(byte[] content, io.modelcontextprotocol.json.TypeRef<T> type)
                throws java.io.IOException {
            return DELEGATE.readValue(content, type);
        }

        @Override
        public <T> T convertValue(Object fromValue, Class<T> type) {
            conversions.incrementAndGet();
            return DELEGATE.convertValue(fromValue, type);
        }

        @Override
        public <T> T convertValue(Object fromValue, io.modelcontextprotocol.json.TypeRef<T> type) {
            conversions.incrementAndGet();
            return DELEGATE.convertValue(fromValue, type);
        }

        @Override
        public String writeValueAsString(Object value) throws java.io.IOException {
            return DELEGATE.writeValueAsString(value);
        }

        @Override
        public byte[] writeValueAsBytes(Object value) throws java.io.IOException {
            return DELEGATE.writeValueAsBytes(value);
        }
    }

    /** The default mapper is the SDK's Jackson 3 one. */
    @Test
    void theDefaultMapperIsJackson3() {
        assertTrue(
                new McpServerAdapter().getJsonMapper() instanceof JacksonMcpJsonMapper,
                "Jackson 3 mapper expected");
        assertTrue(
                new McpClientAdapter(
                                        new org.restlet.Client(org.restlet.data.Protocol.HTTP),
                                        "http://localhost/mcp")
                                .getJsonMapper()
                        instanceof JacksonMcpJsonMapper,
                "Jackson 3 mapper expected");
    }

    /** The application's mapper serves the adapter's messages, and is fixed once it starts. */
    @Test
    void theApplicationsMapperServesTheMessages() throws Exception {
        AtomicInteger conversions = new AtomicInteger();
        McpJsonMapper counting = new Counting(conversions);
        McpServerAdapter adapter = new McpServerAdapter();
        adapter.setJsonMapper(counting);
        assertSame(counting, adapter.getJsonMapper());
        adapter.start();
        try {
            assertThrows(IllegalStateException.class, () -> adapter.setJsonMapper(counting));
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put(MetaKeys.PROTOCOL_VERSION, McpProtocol.MODERN_VERSION);
            meta.put(MetaKeys.CLIENT_CAPABILITIES, Map.of());
            JsonRpc.JSONRPCResponse discovered =
                    adapter.dispatch(
                                    new JsonRpc.JSONRPCRequest(
                                            "2.0", "server/discover", 1, Map.of("_meta", meta)),
                                    new McpRequestContext(null))
                            .orElseThrow();
            assertEquals(null, discovered.error());
            assertTrue(conversions.get() > 0, "the application's mapper must be used");
        } finally {
            adapter.stop();
        }

        McpClientAdapter client =
                new McpClientAdapter(
                        new org.restlet.Client(org.restlet.data.Protocol.HTTP),
                        "http://localhost/mcp");
        client.setJsonMapper(counting);
        assertSame(counting, client.getJsonMapper());
        assertThrows(NullPointerException.class, () -> client.setJsonMapper(null));
    }
}
