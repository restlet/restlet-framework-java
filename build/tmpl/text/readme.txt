
=================================================================
Restlet Framework, the leading RESTful web API framework for Java
=================================================================
                     http://restlet.org
-----------------------------------------------------------------


Native REST support
 - Core REST concepts have equivalent Java artifact (Resource, Representation, Connector or Component for example).
 - Suitable for both client-side and server-side web applications. The innovation is that that it uses the same API, reducing the learning curve and the software footprint.
 - Concept of "URIs as UI" supported based on the URI Templates standard. This results in a very flexible yet simple routing with automatic extraction of URI variables into request attributes.
 - Tunneling service lets browsers issue any HTTP method (PUT, DELETE, MOVE, etc.) through a simple HTTP POST. This service is transparent for Restlet applications.
 - Ready for the Semantic Web (Web 3.0), with full RDF reading and writing support.
 - Client-side support for OData (Open Data Protocol) including kit generation.

Available editions
 - Restlet edition for Java SE/EE, letting you run your Restlet applications on regular JVMs or in regular Servlet containers.
 - Restlet edition for GWT, letting you use the Restlet API from within any Web browser, without plugins.
 - Restlet edition for GAE, letting you deploy Restlet applications on Google AppEngine cloud computing platform.
 - Restlet edition for Android, letting you deploy Restlet applications on Google Android mobile devices.
 - Restlet edition for OSGi environments, letting you develop highly dynamic Restlet Applications, running in Equinox, Felix or other OSGi containers.

Complete Web Server
 - Static file serving similar to Apache HTTP Server, with metadata association based on file extensions.
 - Automated content negotiation based on client preferences.
 - Conditional requests automatically supported for resources.
 - Partial requests automatically supported for resources to retrieve or update a range of a representation.
 - Remote edition of files based on PUT and DELETE methods (aka mini-WebDAV mode).
 - Encoder and decoder service transparently compress or uncompress representations exchanged.
 - Log service writes all accesses to your applications in a standard Web log file. The log format follows the W3C Extended Log File Format and is fully customizable.
 - Powerful URI based redirection support similar to Apache Rewrite module.
 - Extensive and flexible security with support for authentication, authorization, role management, SSL certificates, JAAS integration, OAuth 2.0 and more.

Available Connectors
 - Multiple server HTTP connectors available, based on either Mortbay's Jetty or the Simple framework
 - AJP server connector available to let you plug behind an Apache HTTP server or Microsoft IIS. It is based on Jetty's connector.
 - Multiple client HTTP connectors available, based on either the JDK's HttpURLConnection class or on Apache HTTP Client.
 - Built-in internal HTTP, SIP client and server connectors for development mode and light deployments. No external dependency needed.
 - Client SMTP, SMTPS, POP v3 and POPS v3 connectors are provided based on JavaMail and a custom email XML format.
 - Client JDBC connector based on the JDBC API, a custom request XML format and the JDBC WebRowSet interface for XML responses.
 - Client FILE connector supports GET, PUT and DELETE methods on files and directories. In addition, it is able to return directory listings.
 - Client CLAP connector to access to the Classloader resources.
 - Client RIAP connector to access to the Restlet internal resources, directly inside the JVM.
 - Client SOLR connector to call embedded Apache Lucene Solr search and indexing engine.

Available Representations
 - Automated marshalling and unmarshalling between POJOs and representations based on an extensible converter service. Leverages XStream for XML and Jackson for JSON. Also works with a GWT and Java object serialization.
 - Built-in support for XML representations (JAXB, JibX, DOM or SAX based) with a simple XPath API based on JDK's built-in XPath engine.
 - Integration with the FreeMarker and Velocity template engines
 - Integration with Apache FileUpload to support multi-part forms and easily handle large file uploads from browsers
 - Transformer filter to easily apply XSLT stylesheets on XML representations. It is based on JDK's built-in XSLT engine.
 - Extensible set of core representations based on NIO readable or writable byte channels, BIO input or output byte streams, BIO reader and writer character streams.
 - Support for Atom, RSS and JSON standards via several extension including ROME integration.
 - Integration with Apache Lucene Tika to support metadata extraction from any representation.

Flexible configuration
 - Complete configuration possible in Java via the Restlet API
 - Configuration possible via Restlet XML and WADL files
 - Servlet adapter provided to let you deploy any Restlet application in Servlet compliant containers like Tomcat, when the usage of standalone HTTP connectors is not possible.
 - Implementation of the JAX-RS 1.0 standard API (based on JSR-311).
 - Deployment as native services is possible and illustrated using the powerful Java Service Wrapper.
 - Extensive integration with popular Spring IoC framework.
 - Deployment to Oracle 11g embedded JVM supported by special extension.
 - Logging based on JULI (java.util.logging) with an extensibility system, including an adapter for SLF4J.

Security
 - Supports Google SDC (Secure Data Connector) to connect to intranets from public cloud applications
 - Supports HTTP Basic and Digest authentication
 - Supports Amazon S3 authentication
 - Supports Microsoft Shared Key and Shared Key Lite authentication (client side only)
 - Supports OAuth 2.0 authentication
 - Supports HTTPS (HTTP over SSL)
 - Supports SMTPS (SMTP over SSL) and SMTP-STARTTLS
 - Supports POPS (POP over SSL)

Scalability
 - Fully multi-threaded design with per-request resource instances to reduce thread-safety issues when developing applications.
 - Intentional removal of Servlet-like HTTP sessions. This concept, attractive as a first sight, is one of the major issue for Servlet scalability and is going against the stateless exchanges promoted by REST.
 - Supports non-blocking NIO modes to decouple the number of connections from the number of threads.
 - Supports asynchronous request processing, decoupled from IO operations. Unlike the Servlet API, the Restlet applications don't have a direct control on the outputstream, they only provide output representation to be written by the server connector.

Copyright 2005-2020 Talend
Restlet is a registered trademark of Talend S.A.
