/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
import com.sun.net.httpserver.HttpServer;
import io.ikanos.Capability;
import io.ikanos.engine.exposes.mcp.McpServerAdapter;
import io.ikanos.spec.IkanosSpec;
import io.ikanos.spec.util.IkanosMappers;
import io.ikanos.spec.util.VersionHelper;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Source-launched in an isolated JVM with Ikanos' own dependencies, not Restlet's test classpath.
 * Uses the real capability, MCP adapter, resource, dispatcher and tool execution path.
 */
class IkanosReferenceEndpoint {
    public static void main(String[] args) throws Exception {
        HttpServer upstream = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        upstream.createContext("/echo/", exchange -> {
            try {
                String text = exchange.getRequestURI().getPath().substring("/echo/".length());
                byte[] body = ("echo:" + text).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders("fail".equals(text) ? 400 : 200, body.length);
                exchange.getResponseBody().write(body);
            } finally {
                exchange.close();
            }
        });
        upstream.start();
        Capability capability = null;
        try {
            String yaml = """
                    ikanos: "%s"
                    capability:
                      exposes:
                        - type: mcp
                          namespace: restlet-r0-reference
                          address: 127.0.0.1
                          port: 0
                          tools:
                            echo:
                              description: Returns the supplied text
                              inputParameters:
                                - name: text
                                  type: string
                                  required: true
                              call: upstream.echo
                      consumes:
                        - type: http
                          namespace: upstream
                          baseUri: "http://127.0.0.1:%d"
                          resources:
                            echo:
                              path: "/echo/{{text}}"
                              operations:
                                echo:
                                  method: GET
                    """.formatted(VersionHelper.getSchemaVersion(), upstream.getAddress().getPort());
            IkanosSpec spec = IkanosMappers.lenientYaml().readValue(yaml, IkanosSpec.class);
            capability = new Capability(spec);
            McpServerAdapter adapter = (McpServerAdapter) capability.getServerAdapters().getFirst();
            capability.start();
            Path ready = Path.of(args[0]);
            Path pending = ready.resolveSibling(ready.getFileName() + ".pending");
            Files.writeString(pending, "http://127.0.0.1:" + adapter.getServer().getActualPort() + "/mcp");
            Files.move(pending, ready);
            // The parent owns stdin; EOF is the normal shutdown signal.
            while (System.in.read() != -1) {
            }
        } finally {
            try {
                if (capability != null) {
                    capability.stop();
                }
            } finally {
                upstream.stop(0);
            }
        }
    }
}
