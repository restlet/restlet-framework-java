# Restlet Framework

[![Build Status](https://github.com/restlet/restlet-framework-java/actions/workflows/build.yaml/badge.svg)](https://github.com/restlet/restlet-framework-java/actions/workflows/ci.yaml) [![Apache License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Overview 

Launched in 2005, Restlet Framework is the first open source project that embraced REST in the Java world.

Since its inception, its Java API has closely matched the concepts and terminology of the REST architecture style as well as the HTTP protocol. It includes powerful and dynamic routing and security features, as well as fully unified way to develop both web clients, web servers or both at the same time. Its power and maturity gives you an alternative to developing with the Servlet API or with the JAX-RS API.

Restlet applications run directly in JVMs with minimal dependencies, resulting in a small footprint suitable for microservices running in a service mesh. It offers several extensions for common use cases and can be easily extended if needed.

![](https://avatars.githubusercontent.com/u/374452?s=400&u=191f2eada671b2a07f61782cf638bdda0ccb6d3f&v=4)

## R1 transport foundations

R1 supplies protocol-neutral mechanics, not production MCP, gRPC or Smithy adapters.
The extensions remain independent and retain their official SDK contracts. No RPC superclass,
cross-protocol message model, dispatcher or status mapping is introduced.

### Supported byte transport and ownership

`org.restlet.util.StreamTransport` is the supported core seam. Its implementation in
`org.restlet.engine.stdio` is not exported. It needs no protocol SDK or Netty types:

```java
try (var transport = new StreamTransport(input, output, cancelIo)) {
    transport.setMaxOperationBytes(65536);
    transport.start();
    transport.write(new byte[] {0, 1, 2});
    int count = transport.read(buffer, 0, buffer.length);
}
```

The streams and cancellation action are supplied explicitly. Borrowed streams, including
standard streams, are **never closed**; output is flushed on writes and stop. The cancellation
action must promptly release blocked borrowed I/O without closing global standard streams.
Interruption is also attempted, but an arbitrary `InputStream` may ignore it. If the owner's
cancellation does not release I/O, stop reports a timeout; cleanup remains pending until the
owner releases it. This is not a guarantee of safely interrupting arbitrary `System.in` reads.
No global stream is redirected.

`new StreamTransport(List.of(executable, argument))` instead owns the subprocess and its pipes.
No shell or URI-derived command is used. Stop closes owned pipes and terminates only that
process, not a process tree. Stderr is drained independently into a bounded tail; protocol
output is never mixed with it. Startup, I/O, unexpected nonzero exit, stderr-drain and shutdown
failures are surfaced. The stderr tail remains available after stop.

| Configuration | Default | Contract |
|---|---|---|
| `maxOperationBytes` | 65536 | Positive; reads are capped, oversized writes rejected before I/O |
| `maxStderrBytes` | 8192 | Positive bounded tail for owned subprocess stderr |
| `shutdownTimeout` | 5 seconds | Positive caller wait bound, greater than exit grace |
| `processExitGrace` | 250 milliseconds | Nonnegative and below shutdown timeout; wait before destroy and again before forced termination |

Configuration is fixed after start/stop. Reads are synchronous, demand-driven and serialized:
there is no protocol reader, prefetch or input queue. Writes are serialized and flushed as
opaque sequences, with no internal task queue. Extensions must bound their own callers,
message assembly, SDK demand and pending calls; a read chunk is not a protocol message.
Stopped transports reject new work and cannot restart.

`Protocol.STDIO` identifies bytes/pipes, **not MCP**. Core installs no default STDIO helper.
Connector construction requires one uniquely registered supporting helper class or an
explicit supporting helper class; missing/ambiguous choices fail rather than taking the
first protocol implementation. MCP framing, SDK correlation, routing and helpers belong to R2.

### HTTP lifecycle and connection decisions

`Request.getDisconnectSignal()` returns a read-only stage completed once with the first
abnormal transport cause, including explicit client cancellation. Normal completion does not
complete it. Netty reports it independently of application entity I/O, including an idle
server whose entire request body has been consumed. Completion runs off the connector event
loop; expensive continuations should still use an application executor. Wrappers delegate
the signal; request copies have independent lifecycle state.

An abort before connector registration is remembered, although `abort()` still returns false
until an active action accepts it. Connector abort actions must be idempotent. Use a new
request after cancellation. Connection/send failures also complete the signal.
Client completion state is published before waking response-body consumers at EOF, so an
empty completed response is not exposed while its exchange still appears active.

HTTP/1.1 performs a read after the body to observe peer closure. Pipelined requests from that
read wait until the preceding response is flushed, preserving response order. This decoded
lookahead is limited to 1024 messages and 1 MiB of content/header storage accounting; overflow
reports a transport failure and closes the connection. Reads pause when lookahead is queued,
so notification remains subject to transport back-pressure. Upgrade/CONNECT requests retain
their existing read handoff to the replacement protocol. HTTP/2 and experimental HTTP/3 retain
their stream lifecycle and flow control; this is not a new SDK flow-control implementation.

R1 deliberately adds **no physical-connection control API**. The destination URI remains the
source of HTTP/TLS authority, DNS resolution and pool identity in existing connectors.
Extensions must not pretend that replacing `Host` implements separate SNI/resolved-address
selection. gRPC resolver-address/authority divergence, physical connection pinning, per-connection
admission, GOAWAY, PING and SDK keepalive must be explicitly rejected if requested by a future
adapter until implemented and tested. Start with URI-origin routing and adapter-wide limits;
SDK streaming/readiness conformance remains R3/R4/R6 work.

### Independent SDK integration contracts

| Extension delivery | SDK-owned contract | Restlet integration / ownership / finite limits |
|---|---|---|
| MCP (R2) | SDK 2.1.0 records, codecs, dispatch, correlation and supported revisions | Borrow injected HTTP connectors or `StreamTransport`; close adapter sessions/tasks, not borrowed connectors. Bound request/frame bytes, pending calls, sessions, replay history and SDK queues before admission. Profile compatibility remains explicit; R0 did not authorize Ikanos migration. |
| gRPC (R3/R4/R6) | grpc-java 1.85.1 `ManagedChannel`, `ClientCall`, service definitions, SDK framing/status/metadata | Borrow configured HTTP/2 connectors; own logical SDK runtimes/call workers. Pin internal SPI use. Bound inbound/outbound message bytes, active calls and queued bytes; map receive demand/readiness before claiming streaming support. URI-origin routing only initially. |
| Smithy (R5/R6) | Candidate smithy-java 1.7.0 native client transport, modeled operations/errors, signing | API remains unvalidated: prototype before adding adapter classes. Borrow Restlet HTTP connectors, preserve SDK authority/signing, bound bodies/concurrency and release owned work. Experimental server remains deferred. |

These are delivery contracts, **not available adapter constructors or validated Smithy APIs**.
Each delivery must choose and test finite protocol limits rather than inheriting an unlimited
queue. Core transport chunks do not substitute for SDK message limits. Borrowed connector
instances remain running after adapter shutdown; only explicitly owned resources may be stopped.

Initial extension configuration is defined independently (positive limits fixed before start):
MCP uses 1 MiB request/frame bytes, 128 pending calls, 64 sessions, and 1 MiB queued bytes per
session, with replay disabled unless a finite history is explicitly configured. gRPC uses
4 MiB inbound and outbound messages, 128 adapter-wide active calls, and at most 4 MiB queued
bytes per call, including any frame awaiting SDK demand. Smithy's provisional prototype uses
1 MiB buffered bodies and 128 active calls, subject to R5 verification rather than a supported
SDK claim. All over-limit admissions fail through the selected protocol's own error contract;
they are never truncated, silently dropped or mapped through a common RPC error. Deadlines
remain native SDK settings. These are R1 design defaults to enforce and test when each extension
lands, not limits already enforced by the R0 fixtures or by core `StreamTransport`.

The regression suite compiles a separate named-module consumer of the public byte seam and
checks that its implementation remains unexported and core has no MCP/gRPC SDK requirements.
The inherited R0 fixtures compile and exercise the independent native MCP/gRPC contracts;
they remain test-only. Run the foundations plus relevant connector/SDK regressions:

```powershell
.\mvnw.cmd -pl org.restlet '-Dtest=StreamTransportTestCase,RequestLifecycleTestCase,WrapperRequestTestCase,StdioHelperSelectionTestCase,RpcSdkBoundaryTestCase,HttpDisconnectTestCase,NettyConnectionUpgradeTestCase,SecurityTestCase,GrpcSdkTransportSpikeTestCase,GrpcSdkServerHostSpikeTestCase,McpSdkTransportSpikeTestCase,McpStreamableSdkTransportSpikeTestCase,*Http*TestCase,*EventStream*TestCase' test
```

Use the opt-in Ikanos properties below to include the live differential rather than skip it.

R1 closing validation on Java 25: the live-enabled selector above passed **145 tests, zero
failures/errors/skips**, including HTTP/3, TLS, SSE, protocol upgrades, exact lookahead limits,
STDIO ownership/shutdown and independent named-module compilation. The full core run executed
2257 tests with one shutdown wall-clock tolerance failure (1.715 s versus 1 s +/- 200 ms).
The same test failed on the unchanged R0 baseline (1.635 s); the five-case shutdown class then
passed in isolation on R1. No timing assertion was relaxed. This is a recorded baseline timing
limitation, not a clean full-suite claim.
The expanded selector adds a deterministic terminal-publication regression (confirmed failing
before its fix), repeated immediate client closes on empty 401/403 responses over HTTP/1.1 and
HTTP/2, and the eight existing authorization tests after a CI investigation. It was rerun together
with strict `javadoc:javadoc` generation; both passed. No quality gate or workflow was weakened.

## R0 SDK interoperability probes

The MCP and gRPC interoperability spikes are **test-only**, not production RPC extensions.
R0 is a feasibility/decision phase, not delivery of all features in R1-R6. Its outcome is
to proceed with narrowly scoped SDK integrations and defer unsupported profiles/features,
not to promote these fixtures into production adapters.
Run the focused suite from the repository root:

```powershell
.\mvnw.cmd -pl org.restlet '-Dtest=GrpcSdkTransportSpikeTestCase,GrpcSdkServerHostSpikeTestCase,RpcSdkBoundaryTestCase,McpSdkTransportSpikeTestCase,McpStreamableSdkTransportSpikeTestCase' test
```

The MCP suite compares a 20-request corpus with
[recorded Ikanos responses](org.restlet/src/test/resources/mcp-ikanos/observations.json).
These observations were verified against Ikanos commit
`3f4532efebd237498f6e253f0277e213d5995623`, then revalidated at
`932f2322d7dcf4ec973f650f3daf5e9ddf1b4c71`, with its own dependencies.
Full status/body snapshots and JSON-pointer difference paths are asserted, not just error codes.
JSON object ordering is ignored; response fields, error messages and absent fields are not removed.
All cases assert that neither endpoint creates an MCP session.

After the compatibility follow-up, **19 cases match exactly**. The test-only bridge adds
fixture-specific server/cache metadata, discovery, legacy-initialize rejection, unknown-tool
error adaptation, dispatch-error HTTP mapping, parse-error envelopes, and GET/DELETE response
text. Normal tools still dispatch through the SDK; only the profile-specific discovery and
legacy-initialize behavior bypass it. Unknown-tool adaptation matches only the pinned SDK's
specific error data, not every invalid-params failure.

**One explicit difference remains:** malformed-JSON diagnostic text from Jackson 2 versus
Ikanos' Jackson 3. Both responses now use HTTP 400, `-32700` and `id: null`; only
`/body/error/message` differs. The snapshots assert both exact messages and do not normalize
them or synthesize Jackson 3 diagnostics.

A regression test demonstrates that SDK 2.1.0's `JSONRPCResponse` rejects null IDs. The parse-error
path therefore uses an explicit null-ID envelope with the SDK's error type and JSON mapper.
This is a measured compatibility exception, not a general replacement codec or dispatcher.
A passing differential test means these bounded observations are unchanged, **not** full profile
equivalence. Metadata/discovery are specific to the tools fixture; other methods, notifications,
identity/capability checks and production lifecycle are not validated. Ikanos migration remains
deferred.

The Streamable HTTP probe also opens a session-bound GET listening stream, sends two SDK-generated
notifications, and verifies the returned SSE event IDs and JSON. It replays events after a known
`Last-Event-ID` from a test-only per-session history capped at eight events; unknown or expired IDs
are rejected. Each live stream is deliberately bounded to two events. This history is a probe-owned
compatibility seam, not SDK replay: `McpStreamableServerSession.replay` is empty in MCP SDK 2.1.0.
The probe does not establish production SSE lifecycle, reconnection, or full Streamable HTTP
conformance.

The gRPC probes now also invoke SDK `BIDI_STREAMING` methods in both directions. Each sends one
request, waits for its response while the request stream is still open, then sends a second request
and half-closes. The Restlet-backed client streams request frames from a bounded pipe while reading
the HTTP/2 response, and the Restlet-hosted server reads request frames concurrently with writing
queued responses. This validates finite full-duplex message exchange through Restlet's current
HTTP/2 client/server connectors and the SDK APIs. The transport adapters and gRPC framing remain
test-only. The client bridge honors SDK receive credits and the probe confirms the second response
is withheld until another message is requested; both bridge directions hold at most one already
framed message beyond the available credit. This is a bounded SDK-demand probe, not complete HTTP/2
or gRPC flow-control conformance.

Active bidi cancellation is now exercised in both directions after the first response, while the
request stream remains open. The client observes `CANCELLED` once, the remote SDK cancellation
handler runs once, both bridge I/O workers finish, and a subsequent unary call succeeds on the same
channel. The client bridge records cancellation before aborting HTTP I/O so an abort failure cannot
replace the cancellation status. A separate regression verifies that closing demand wakes a blocked
receiver and does not reopen on later credits. Additional probes cancel before response headers
and shut down a channel with two active calls, checking remote cancellation, terminal callbacks,
I/O completion and borrowed-connector ownership. Server-side completion is observed through
`Response.onSent`, including cancellation before the response writer starts. Cancellation before
HTTP request registration, server-initiated cancellation and long-lived stream lifecycle are
not exhaustively covered.

### SDK-owned server runtime and measured core fixes

`GrpcSdkServerHostSpikeTestCase` separately hosts the real `ServerImplBuilder` runtime over
Restlet, using `AbstractServerStream` for **SDK-owned framing and deframing**, dispatch,
interceptors, status encoding and half-close. The official Netty client verifies unary success,
client-streaming aggregation, a trailers-only SDK error with a percent/non-ASCII description,
and SDK `UNIMPLEMENTED` dispatch for an unknown method. It verifies SDK/executor termination
without stopping the borrowed Restlet server. No SDK listener is bound. This initial host
buffers at most 1 MiB per direction and models each HTTP call as a logical SDK transport;
it is not a connection-aware, streaming production host.

The older bidirectional fixture still implements frames and directly invokes SDK handlers.
It is useful transport/demand/cancellation evidence, **not** proof that the complete SDK
server runtime supports full-duplex over the proposed production seam. In particular, neither
fixture is a production fallback for SDK framing/flow control.

The closing probes reproduced and fixed two protocol-neutral core gaps:

- HTTP/2 clients previously peeked at the body before delivering headers. Multiplexed responses
  now expose headers without waiting for data; HTTP/1.1 keeps its existing empty-body handling.
  A regression withholds the first body byte until after the caller receives the headers.
- Empty multiplexed server responses with no entity/trailers now end with their initial
  HEADERS, rather than a later empty DATA frame. The native SDK client previously reported
  `INTERNAL` instead of the trailers-only SDK error. The same test now preserves its exact
  status and description. Responses with late trailers retain the existing streaming path.

Both paths also serve HTTP/3, but native HTTP/3 validation remains platform-dependent.
Late first trailers were already supported and have an existing core regression.

### R0 supported-feature matrix and decisions

| Area | Measured result | Decision / boundary |
|---|---|---|
| gRPC client internal SPI | Pinned `ManagedChannelImplBuilder` transport compiles and interoperates over Restlet HTTP/2 | Proceed with unary extension work; keep the version pinned |
| gRPC SDK server host | Real SDK runtime, codecs, interceptors, unary, client half-close and trailers-only errors | Socket-less hosting is feasible; bounded buffering is not the production design |
| gRPC metadata/status | Repeated ASCII, binary metadata and `grpc-status-details-bin`, empty success, errors after a delivered message, exactly one observed terminal callback, in both directions | Preserve native metadata/status; do not drop messages preceding errors |
| gRPC demand/cancellation | Finite bidi exchange, receive-credit withholding, established/pre-header cancellation, active channel shutdown and worker completion | Bounded diagnostic evidence only; full SDK readiness, byte-budget and long-lived conformance remain R6 |
| gRPC addressing | Exact origin authority reaches the SDK service; mismatched authorities and ordinary resolved socket addresses are rejected | No claim of TLS/SNI override, DNS/LB/subchannel or connection-aware integration |
| gRPC connection controls | PING reports `UNIMPLEMENTED`; deadlines, compression and authority overrides throw explicitly in the prototype | Unsupported, not silently emulated; connection draining/GOAWAY/keepalive need a supported core seam |
| MCP SDK-supported profiles | Stateless HTTP/2, Streamable initialize/POST SSE, bounded GET notifications/replay and official STDIO subprocess round-trip | Proceed with SDK-backed extension work; production STDIO mechanics and lifecycle belong to R1/R2 |
| MCP Ikanos profile | 19/20 exact corpus responses; revision/session mismatch, null-ID SDK restriction and one parser diagnostic difference are explicit | **Do not migrate Ikanos** or advertise full profile equivalence; fixture compatibility code is not a second dispatcher |
| MCP ownership | Client/server SDK closure leaves borrowed connectors started; Streamable DELETE removes session, replay history and listening state | Bounded ownership proof, not indefinite SSE/process lifecycle certification |
| SDK module path | Actual named layer resolves Restlet, gRPC and MCP without `grpc-netty-shaded`; Jackson mapper service loads there; no default SDK gRPC channel provider remains | No production RPC dependency/module added; extension module/API delivery remains R1 |
| Smithy | Not part of spikes A/B; `1.7.0` is still an unvalidated candidate | Pin and prototype client/server APIs in R5/R6 before accepting the proposed API shape |

The pinned automatic module names are `io.grpc`, `io.grpc.internal`, `io.grpc.protobuf`,
`io.grpc.stub`, `io.modelcontextprotocol.sdk.mcp.core` and
`io.modelcontextprotocol.sdk.mcp.json.jackson2`. `RpcSdkBoundaryTestCase` checks the actual
jar descriptors, loads SDK classes from the named layer and instantiates the MCP mapper supplier.
This is not an extension module-path test: those modules do not exist yet.

R0 authorizes the next **implementation phase**, not release or Ikanos migration. Keep independent
protocol APIs, explicit connector ownership and finite limits; do not add a shared RPC layer or
an SDK-owned HTTP engine. Generated gRPC stubs, SDK-owned streaming transports, complete HTTP
validation, idle disconnect notification, connection policies, native-image behavior and production
lifecycle remain delivery/conformance gates. Ikanos still targets Java 21 while this Restlet build
requires Java 25, so runtime/build/container migration is a separate adoption prerequisite.
No Ikanos source was copied into these new probes; existing attribution is retained.

Delivery tracking: [MCP #1558](https://github.com/restlet/restlet-framework-java/issues/1558),
[gRPC #1561](https://github.com/restlet/restlet-framework-java/issues/1561),
[Smithy #1559](https://github.com/restlet/restlet-framework-java/issues/1559) and
[core seams #1560](https://github.com/restlet/restlet-framework-java/issues/1560).
Separate Ikanos adoption gates are
[MCP #838](https://github.com/naftiko/ikanos/issues/838),
[gRPC #837](https://github.com/naftiko/ikanos/issues/837) and
[Smithy #839](https://github.com/naftiko/ikanos/issues/839).

Closing validation on Java 25: the live-enabled SDK suite combined with
`*Http*TestCase,*EventStream*TestCase` passed **107 tests, zero failures/errors/skips**.
This includes native HTTP/3 tests on this host, TLS/HTTP transport selection, the STDIO
subprocess and the actual Ikanos comparison. It is not a full-repository test run.

To recheck the same corpus against the actual Ikanos endpoint, prepare an Ikanos checkout at the
reference revision, then run the following PowerShell commands (adjust the checkout path):

```powershell
$ikanos = (Resolve-Path '..\ikanos').Path
$classpath = Join-Path (Get-Location) 'org.restlet\target\ikanos-classpath.txt'
mvn -f "$ikanos\pom.xml" -pl modules\ikanos-engine -am compile
mvn -f "$ikanos\pom.xml" -pl modules\ikanos-engine dependency:build-classpath "-Dmdep.outputFile=$classpath" -DincludeScope=test
.\mvnw.cmd -pl org.restlet '-Dtest=GrpcSdkTransportSpikeTestCase,GrpcSdkServerHostSpikeTestCase,RpcSdkBoundaryTestCase,McpSdkTransportSpikeTestCase,McpStreamableSdkTransportSpikeTestCase' "-Drestlet.mcp.ikanos.home=$ikanos" "-Drestlet.mcp.ikanos.classpathFile=$classpath" test
```

The opt-in test source-launches a separate JVM with Ikanos' classpath. It boots a real capability
and MCP HTTP endpoint plus a deterministic loopback upstream; it does not replace Ikanos'
resource, dispatcher or tool handler with mocks. The reference is called over HTTP/1.1 and the
prototype over Restlet HTTP/2; this compares MCP semantics, not transport implementation parity.
The reference process is stopped after the test. Startup failures fail the test rather than
falling back to recorded responses.

The SDK-focused selector contains twenty tests: eight transport gRPC cases, one native SDK-host
case, one module-boundary case, seven MCP stateless/STDIO/profile cases and three MCP Streamable
cases. Without opt-in properties the live comparison is skipped. Add
`Http2FullDuplexTestCase,Http2CancellationTestCase,HttpTrailersTestCase,Http2TrailerHeadersTestCase`
to include seven connector regression cases; add `*Http*TestCase,*EventStream*TestCase` instead for
broader HTTP/TLS/SSE coverage. The official STDIO subprocess has a five-second initialization
budget and has intermittently timed out in combined runs; an isolated pass does not turn a failed
combined run into a clean one.

Recorded and live reports are written separately under `org.restlet/target` as
`mcp-ikanos-recorded.json` and `mcp-ikanos-differential.json`; reference-process logs are kept in
per-run `mcp-ikanos-*` directories there. Review changed observations explicitly before updating
the checked-in baseline. No Ikanos or additional production dependency is added to Restlet.

## Learn more

To learn more about Restlet Framework, please have a look at the following resources:

* [Download](https://restlet.talend.com/downloads/current/)
* [Tutorials](https://restlet.talend.com/documentation/tutorials/2.7/)
* [User Guide](https://restlet.talend.com/documentation/user-guide/2.7/introduction/overview/)
* [JavaDocs](https://javadoc.io/doc/org.restlet/org.restlet/latest/index.html)
* [Change Log](https://restlet.talend.com/documentation/2.7/changelog)
* [Issues](https://github.com/restlet/restlet-framework-java/issues)
* [Discussions](https://github.com/restlet/restlet-framework-java/discussions)
* [Stack Overflow](http://stackoverflow.com/questions/tagged/restlet)

Copyright 2025 Qlik
