Changes log
===========

- 3.0 Milestone 2 (??-??-2026)
    - Breaking changes
        - Java 25 is now the minimum version (compiled with `--release 25`).
        - The HTTP/HTTPS server and client connectors of the core now rely on Netty 4.2 instead of
          Jetty 12. The connector class names are unchanged
          (`org.restlet.engine.connector.HttpServerHelper`, `HttpsServerHelper`,
          `HttpClientHelper`), and the Jetty dependencies are removed. No Netty type is exposed by
          the public API: the module requires the Netty modules without `requires transitive`.
        - Server calls run by default on virtual threads, one per call ("threadPool.virtual"
          parameter, `true` by default). Set it to `false` to use a pool of platform threads
          configured by the existing "threadPool.*" parameters.
        - Server connector parameters: added "threadPool.virtual", "server.maxConcurrentCalls",
          "eventLoop.bossThreads", "eventLoop.workerThreads". Kept "threadPool.minThreads",
          "threadPool.maxThreads", "threadPool.threadsPriority", "threadPool.idleTimeout",
          "connector.acceptQueueSize", "connector.idleTimeout", "http.requestHeaderSize",
          "http.outputBufferSize", "server.maxConnections", "shutdown.gracefully",
          "shutdown.timeout", "http.transport.protocol(s)". The other Jetty-specific parameters
          ("connector.acceptors", "connector.selectors", "lowResource.*", "http.*CacheSize", ...)
          are removed and ignored.
        - HTTP/3 is now **experimental**, on Netty's QUIC and HTTP/3 codecs. It is off by default,
          and enabled with HTTP3 in "http.transport.protocols" (server, combined with HTTP1_1 or
          HTTP2) or "httpClientTransportMode" (client). It requires the optional
          `io.netty:netty-codec-http3` dependency and the native QUIC library of the platform
          (`io.netty:netty-codec-native-quic` with the platform classifier: Linux, macOS, Windows
          x86_64). The server serves HTTP/3 on the UDP port having the same number as the TCP port
          and advertises it with the "Alt-Svc" response header; the client doesn't discover it
          with "Alt-Svc" and only uses it when asked to. TLS comes from the key and trust stores
          of the `DefaultSslContextFactory` (custom `SslContextFactory` implementations and the
          cipher suite and protocol settings are not supported). The Jetty "http3PemWorkDir" and
          "http3.pem.workdir" parameters are removed. See `org.restlet.engine.netty.Http3Support`
          for the documented limits.
        - Client connector: removed `getHttpClient()`, `createHttpClient()`, the Jetty
          authentication and cookie stores (`get/setAuthenticationStore`, `get/setCookieStore`),
          `get/setExecutor`, `getScheduler` and the "cookieSupported", "addressResolutionTimeout",
          "connectBlocking", "destinationIdleTimeout", "httpComplianceMode",
          "maxRequestsQueuedPerDestination", "responseBufferSize", "strictEventOrdering" and
          "http3PemWorkDir" parameters. Added "hostnameVerification" (`true` by default) and
          "eventLoop.workerThreads". "maxResponseHeadersSize" is renamed "maxResponseHeaderSize"
          (8192 by default). Redirects ("followRedirects", "maxRedirects") are now followed by
          the connector for synchronous calls, without leaking credentials to other origins.
        - Removed `JettyServerHelper`, `JettyServerCall`, `JettyClientCall`, `JettyHandler`,
          `RestletSslContextFactoryClient` and `RestletSslContextFactoryServer`.
        - Removed the `org.restlet.engine.adapter` package (`Adapter`, `Call`, `ClientAdapter`,
          `ClientCall`, `HttpClientHelper`, `HttpRequest`, `HttpResponse`, `HttpServerHelper`,
          `ServerAdapter`, `ServerCall`) and the "adapter" connector parameter. The Netty
          connectors now map Netty messages directly to Restlet requests and responses, and
          `NettyServerHelper` and `NettyClientHelper` extend `ServerHelper` and `ClientHelper`.
          Custom HTTP connectors should extend those classes. Request headers remain available
          with `Message.getHeaders()`.
        - `MultiPartRepresentation` no longer exposes Jetty types: parts are instances of the new
          `MultiPartRepresentation.Part` class (name, file name, headers, content
          representation), parsing takes an optional storage directory instead of a Jetty
          `MultiPartConfig`, and generation streams the parts.
        - Removed the OSGi support: the `Activator`, the `osgi.core` dependency and the bundle
          packaging (Maven artifacts are now plain JARs, without `Bundle-*` manifest headers).
          `XmlFactoryProvider` of the Jackson extension now relies on the plain `ServiceLoader`.
          OSGi users should stay on 2.x.
        - Removed the `Protocol.WAR` constant ("war" scheme, Web Archive Access Protocol), a
          leftover of the Servlet mode, which is no longer supported. `Protocol.valueOf("war")`
          now returns a new, unknown protocol. Use CLAP or FILE references instead.
        - Cookies now follow RFC 6265 only, the obsolete RFC 2109 and RFC 2965 formats are dropped.
          Removed `Cookie.get/setVersion()`, `CookieSetting.get/setComment()` and the constructors
          of `Cookie` and `CookieSetting` taking a version or a comment, as well as the
          `Set-Cookie2` support (`HeaderConstants.HEADER_SET_COOKIE2`): a `Set-Cookie2` response
          header is no longer parsed as a cookie setting. `Set-Cookie` no longer writes `Version`,
          `Comment` or `Discard`, nor quotes values and attributes, and writes `Max-Age` together
          with `Expires`. The request `Cookie` header no longer writes `$Version`, `$Path` and
          `$Domain`. When reading, these legacy pairs and attributes are silently ignored, and a
          comma is no longer a separator between cookies (several `Cookie` headers are joined with
          semicolons). `Max-Age` now takes precedence over `Expires` when both are present.
        - CORS: allowing credentials together with the `*` origin (`CorsService`, `CorsFilter`) is now
          rejected with an `IllegalStateException` (when the service creates its filter, or when the
          filter starts), because it required echoing any request origin. List the allowed origins
          explicitly. Without a valid configuration, `CorsFilter` adds no CORS headers. Issue #1611.
        - CORS: `CorsFilter` now applies configuration changes made after the first request (the
          internal `CorsResponseHelper` is rebuilt by the setters). Issue #1611.
    - Enhancements
        - Added the `EventStreamRepresentation`, `ServerSentEvent` and `EventStreamReader` APIs for
          streaming and reading Server-Sent Events over the HTTP connectors, including optional
          comment heartbeats and reconnects honoring `retry:` and `Last-Event-ID`. The
          `server.maxEventStreams` parameter limits concurrent event streams.
        - Added the experimental `org.restlet.ext.websocket` extension, on HTTP/1.1 upgrades.
          `WebSocketUpgrader` (or `WebSocketUpgrade.accept`) accepts upgrades that went through
          the normal routing, filters and authenticators, and runs a `WebSocketHandler` on its own
          virtual thread with a blocking `WebSocketSession` (`receive`, `sendText`, `sendBinary`,
          `close`). `WebSocketClient` opens client sessions on "ws" and "wss" URIs. The server
          answers pings, pings idle sessions, and supports the "websocket.maxSessions",
          "websocket.maxMessageSize", "websocket.maxFrameSize" and "websocket.compression"
          parameters. Server and client support "permessage-deflate" compression (RFC 7692),
          enabled by default.
        - Added the experimental `org.restlet.ext.graphql` extension, on graphql-java.
          `GraphqlServerAdapter` serves a `graphql.GraphQL` instance as a Restlet, following the
          GraphQL over HTTP draft (`application/graphql-response+json` and `application/json`);
          operations run on the call's virtual thread. `GraphqlClientAdapter` sends operations
          through a borrowed `Client` and returns native `ExecutionResult` values, throwing
          `GraphqlTransportException` when no GraphQL response is received. The server accepts
          queries over `GET` when enabled (off by default), applies the draft's status codes per
          response media type, limits the request size (1 MiB by default) and the parser per
          adapter (`ParserOptions`, never the process-wide defaults), and gives data fetchers the
          Restlet request and client info through `RestletGraphql` context keys. The client can
          validate documents locally against a `GraphQLSchema` without a network call, bounds
          responses (10 MiB by default, never truncated) and accepts a request customizer for API
          key headers. Subscriptions are served and consumed over Server-Sent Events
          (`graphql-sse`, distinct connections mode) when `GraphqlTransport.SSE` is enabled: one
          result requested at a time, client disconnects cancel the subscription, and finite
          limits apply (64 active subscriptions and 1 MiB of queued bytes per subscription by
          default). `GraphqlClientAdapter.subscribe` returns a `Publisher<ExecutionResult>`
          honoring demand; cancelling it aborts the request. With `GraphqlTransport.WEBSOCKET`,
          the server adapter also accepts `graphql-transport-ws` WebSocket upgrades on its own path
          (HTTP/1.1 only), with several operations per connection, a `connection_init` timeout
          and the protocol's close codes; `org.restlet.ext.websocket` is an optional dependency,
          loaded only then. Issue #1581.
        - Added the `NettyConnectionUpgrade` engine hook: a Restlet returning a "101 Switching
          Protocols" response with this response attribute takes over the HTTP/1.1 connection
          (`NettyUpgradeContext`), which the WebSocket extension relies on.
        - Added `Response.getProtocol()` and `setProtocol()`: the protocol, with its version,
          used by the server to reply (for example HTTP/1.1, HTTP/2.0 or HTTP/3.0), set by the HTTP
          client connector. When unknown, the protocol of the request is returned.
          `Response.toString()` now shows it.
        - Added request and response trailers to the HTTP connectors, supporting HTTP/1.1 chunked
          transfers and HTTP/2 trailing headers.
        - HTTP/2 and HTTP/3 responses can add trailers after streaming the response head and body;
          HTTP/1.1 still requires trailer names to be known before the response head is sent.
        - HTTP/2 clients receive a response as soon as its headers arrive, while the request entity
          continues to be written on a virtual thread.
        - Active client requests can be aborted; HTTP stream failures surface as I/O errors and a
          communication-error response callback.
        - HTTP/2 and HTTP/3 requests advertise `te: trailers`; stream header names are normalized
          to lowercase at the HTTP boundary.
        - Added the `Protocol.STDIO` pseudo-protocol constant for standard input/output connectors.
        - Added an optional origin matcher to CORS (`CorsService`, `CorsFilter`, `setAllowedOriginMatcher(Predicate<String>)`) to accept origins that are not listed, for example any sub-domain. The response always contains the exact request origin. Issue #1611.
        - Added the `SameSite` attribute to cookie settings (`CookieSetting.get/setSameSite()`,
          `SameSite` enum), read and written in `Set-Cookie`.
    - Bugs fixed
        - Fixed the OpenAPI extension build path for Commons Lang types inherited from Swagger.
        - Slf4j bridge reports wrong line number and caller class in log4j. Issue #233.
        - When GET is an allowed method of a server resource, HEAD is now also listed (e.g. in the Allow header of a 405 response). Issue #266.
        - The `Max-Age` and `Path` attributes of a cookie setting are no longer quoted. Issue #1334.
        - `Restlet.handle()` no longer fails with a `NullPointerException` when the Restlet has no context and cannot be started.
        - `CookieAuthenticator` no longer lets the next restlet overwrite the redirection sent after
          a login. Issue #554.
        - CorsService and CorsFilter now add `Vary: Origin` to every response when the CORS answer depends on the request origin (origin allow-list or credentials allowed), including requests without an `Origin` header, so shared caches no longer serve the response of one origin to another. A static `*` without credentials still sends no `Vary`, as recommended by the Fetch standard. Issue #1611.
    - Misc
        - Fixed JDK HTTP client accessibility in core and WebSocket tests in Eclipse and VS Code,
          without adding a production module dependency.
        - Pinned the Maven Deploy plugin to version 3.2.0 (avoids resolving incompatible Maven 4 beta versions).
        - Added Netty 4.2.19 (imported with the `netty-bom`). CI builds with Java 25.
        - Added graphql-java 26.1 (GraphQL extension).
        - The `dangling-doc-comments` lint, new in Java 23, is disabled because license headers
          use Javadoc-style comments.
 
- 3.0 Milestone 1 (10-11-2026)
    - Breaking changes
        - The Jackson extension now relies on Jackson 3 (`tools.jackson` packages, version 3.2.3).
          The Jackson types exposed by `JacksonRepresentation` (`ObjectMapper`, `ObjectReader`,
          `ObjectWriter`, `CsvSchema`) are now the Jackson 3 ones. Jackson 3 exceptions are
          unchecked (`JsonParseException` becomes `StreamReadException`).
        - Jackson 3 defaults change the generated JSON, XML, YAML and CSV: properties are sorted
          alphabetically and dates are written as ISO-8601 strings instead of timestamps.
        - `ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED` and
          `ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED` are now `final`. They can only be
          enabled with the system properties of the same names, and no longer at runtime.
        - Added `module-info.java` to the core and all extensions (JPMS modules named after the
          Maven artifacts, e.g. `org.restlet` and `org.restlet.ext.jackson`). The converter and
          authenticator helpers are declared with `provides`. Applications that use the module path
          must `requires` the Restlet modules they use. Third-party libraries without a module
          descriptor are required as automatic modules. Classpath usage is unchanged.
        - Removed `Reference.clone()`, deprecated since 2.7. Use `Reference.copy()` instead.
        - The `TaskService` methods `invokeAll` and `invokeAny` now have the generic signatures of
          `ExecutorService` instead of raw types.
        - The `ImmutableDate` setters, which always throw, are now marked as `@Deprecated`.
    - Bugs fixed
        - `Resource.isConfidential()` and `Resource.isLoggable()` threw a `NullPointerException`
          when the resource had no request. They now return `false`.
        - The `ClientResource(Context, Request)` constructor always threw a
          `NullPointerException`. It now creates a response linked to the request.
    - Misc
        - Upgraded Jackson extension to Jackson 3.2.3. The OpenAPI extension remains on Jackson 2
          (2.22.3), as required by swagger-core.
        - Replaced the deprecated Jetty `ConnectionLimit` with `NetworkConnectionLimit` and Spring's
          deprecated `@NonNull`/`@Nullable` with the JSpecify ones.
        - Upgraded JUnit library to 6.1.3.
        - Upgraded Hamcrest library to 3.0 (`hamcrest` replaces `hamcrest-all`).
        - Upgraded OSGi core library to 8.0.0 (`osgi.core` replaces `org.osgi.core`).
        - Upgraded Woodstox library to 7.2.1 and Stax2 API to 4.3.0.
        - Removed the unused Log4j 1.2.17 version property.
        - Upgraded Maven to 3.9.16 (also in the Maven wrapper).
        - Upgraded all Maven plugins (compiler, surefire, bundle, javadoc, enforcer, spotless, ...).
        - The build now enforces Maven 3.9+, Java 21+ and dependency convergence, compiles with
          `-Xlint:all -Werror` (no warnings left) and runs SpotBugs on `verify`.
        - Javadoc is now checked with doclint (html, syntax, reference and accessibility) and the
          errors found were fixed. Missing comments are not checked yet.
        - CI runs `verify` (Spotless, JaCoCo coverage and SpotBugs checks), fails when tests fail,
          publishes coverage reports. GitHub Actions are updated by Dependabot.
