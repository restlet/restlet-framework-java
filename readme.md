# Restlet Framework

[![Build Status](https://github.com/restlet/restlet-framework-java/actions/workflows/build.yaml/badge.svg)](https://github.com/restlet/restlet-framework-java/actions/workflows/ci.yaml) [![Apache License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Overview 

Launched in 2005, Restlet Framework is the first open source project that embraced REST in the Java world.

Since its inception, its Java API has closely matched the concepts and terminology of the REST architecture style as well as the HTTP protocol. It includes powerful and dynamic routing and security features, as well as fully unified way to develop both web clients, web servers or both at the same time. Its power and maturity gives you an alternative to developing with the Servlet API or with the JAX-RS API.

Restlet applications run directly in JVMs with minimal dependencies, resulting in a small footprint suitable for microservices running in a service mesh. It offers several extensions for common use cases and can be easily extended if needed.

![](https://avatars.githubusercontent.com/u/374452?s=400&u=191f2eada671b2a07f61782cf638bdda0ccb6d3f&v=4)

## R0 SDK interoperability probes

The MCP and gRPC interoperability spikes are **test-only**, not production RPC extensions.
Run the focused suite from the repository root:

```powershell
.\mvnw.cmd -pl org.restlet '-Dtest=GrpcSdkTransportSpikeTestCase,McpSdkTransportSpikeTestCase,McpStreamableSdkTransportSpikeTestCase' test
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

To recheck the same corpus against the actual Ikanos endpoint, prepare an Ikanos checkout at the
reference revision, then run the following PowerShell commands (adjust the checkout path):

```powershell
$ikanos = (Resolve-Path '..\ikanos').Path
$classpath = Join-Path (Get-Location) 'org.restlet\target\ikanos-classpath.txt'
mvn -f "$ikanos\pom.xml" -pl modules\ikanos-engine -am compile
mvn -f "$ikanos\pom.xml" -pl modules\ikanos-engine dependency:build-classpath "-Dmdep.outputFile=$classpath" -DincludeScope=test
.\mvnw.cmd -pl org.restlet '-Dtest=GrpcSdkTransportSpikeTestCase,McpSdkTransportSpikeTestCase,McpStreamableSdkTransportSpikeTestCase' "-Drestlet.mcp.ikanos.home=$ikanos" "-Drestlet.mcp.ikanos.classpathFile=$classpath" test
```

The opt-in test source-launches a separate JVM with Ikanos' classpath. It boots a real capability
and MCP HTTP endpoint plus a deterministic loopback upstream; it does not replace Ikanos'
resource, dispatcher or tool handler with mocks. The reference is called over HTTP/1.1 and the
prototype over Restlet HTTP/2; this compares MCP semantics, not transport implementation parity.
The reference process is stopped after the test. Startup failures fail the test rather than
falling back to recorded responses.

Without the opt-in properties, ten tests pass and the live comparison is skipped; with them,
all eleven run. Recorded and live reports are written separately under `org.restlet/target` as
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
