# Restlet Framework

[![Build Status](https://github.com/restlet/restlet-framework-java/actions/workflows/build.yaml/badge.svg)](https://github.com/restlet/restlet-framework-java/actions/workflows/ci.yaml) [![Apache License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Overview 

Launched in 2005, Restlet Framework is the first open source project that embraced REST in the Java world.

Since its inception, its Java API has closely matched the concepts and terminology of the REST architecture style as well as the HTTP protocol. It includes powerful and dynamic routing and security features, as well as fully unified way to develop both web clients, web servers or both at the same time. Its power and maturity gives you an alternative to developing with the Servlet API or with the JAX-RS API.

Restlet applications run directly in JVMs with minimal dependencies, resulting in a small footprint suitable for microservices running in a service mesh. It offers several extensions for common use cases and can be easily extended if needed.

![](https://avatars.githubusercontent.com/u/374452?s=400&u=191f2eada671b2a07f61782cf638bdda0ccb6d3f&v=4)

## What's new in 3.0

Restlet Framework 3.0 modernizes the platform while keeping its REST-first API:

* **Modern Java**: Java 25 minimum, JPMS modules for the core and every extension, and virtual threads by default for server calls.
* **New HTTP engine**: Netty 4.2 based HTTP/1.1 and HTTP/2 connectors replace Jetty, with experimental HTTP/3 over QUIC.
* **Streaming**: Server-Sent Events, HTTP trailers, request cancellation, and an experimental WebSocket extension.
* **Updated serialization**: the Jackson extension now relies on Jackson 3.

## Beyond REST

Restlet 3.0 lets the same application and the same connectors serve APIs in several styles, side by side with REST resources. Each extension builds on the official SDK of its protocol rather than reimplementing it:

| Style | Extension | Status |
|---|---|---|
| [GraphQL](https://graphql.org/) | `org.restlet.ext.graphql` | Experimental: server and client on graphql-java, with subscriptions over SSE and WebSocket |
| [gRPC](https://grpc.io/) | `org.restlet.ext.grpc` | Experimental: services and clients of every streaming type, over HTTP/2 |
| [MCP](https://modelcontextprotocol.io/) | `org.restlet.ext.mcp` | Experimental: tools, resources and prompts for AI agents on the official MCP Java SDK, over stateless Streamable HTTP and STDIO |
| [Smithy](https://smithy.io/) | `org.restlet.ext.smithy` | smithy-java clients and experimental servers on Restlet HTTP/1.1 and HTTP/2 connectors, discovered or set explicitly |

Upcoming enhancements include MCP sessions and resumable streams. Feedback is welcome in [Discussions](https://github.com/restlet/restlet-framework-java/discussions).

## Learn more

To learn more about Restlet Framework, please have a look at the following resources:

* [Download](https://restlet.talend.com/downloads/current/)
* [Tutorials](https://restlet.talend.com/documentation/tutorials/2.7/)
* [User Guide](https://restlet.talend.com/documentation/user-guide/2.7/introduction/overview/)
* [JavaDocs](https://javadoc.io/doc/org.restlet/org.restlet/latest/index.html)
* [Change Log](https://github.com/restlet/restlet-framework-java/blob/3.0/changes.md)
* [Issues](https://github.com/restlet/restlet-framework-java/issues)
* [Discussions](https://github.com/restlet/restlet-framework-java/discussions)
* [Stack Overflow](http://stackoverflow.com/questions/tagged/restlet)

Copyright 2026 Qlik
