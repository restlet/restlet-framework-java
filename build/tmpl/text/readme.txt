
=========================================
Restlet, a RESTful Web framework for Java
=========================================
         http://www.restlet.org
-----------------------------------------


Native REST support
    * Core REST concepts have equivalent Java classes (UniformInterface, Resource, Representation, Connector for example).
    * Suitable for both client-side and server-side web applications. The innovation is that that it uses the same API, reducing the learning curve and the software footprint.
    * Restlet-GWT module available, letting you leverage the Restlet API from within any Web browser, without plugins.
    * Concept of "URIs as UI" supported based on the URI Templates standard. This results in a very flexible yet simple routing with automatic extraction of URI variables into request attributes.
    * Tunneling service lets browsers issue any HTTP method (PUT, DELETE, MOVE, etc.) through a simple HTTP POST. This service is transparent for Restlet applications.

Complete Web Server
    * Static file serving similar to Apache HTTP Server, with metadata association based on file extensions.
    * Transparent content negotiation based on client preferences.
    * Conditional requests automatically supported for resources.
    * Remote edition of files based on PUT and DELETE methods (aka mini-WebDAV mode).
    * Decoder service transparently decodes compressed or encoded input representations. This service is transparent for Restlet applications.
    * Log service writes all accesses to your applications in a standard Web log file. The log format follows the W3C Extended Log File Format and is fully customizable.
    * Powerful URI based redirection support similar to Apache Rewrite module.

Available Connectors
    * Multiple server HTTP connectors available, based on either Mortbay's Jetty or the Simple framework or Grizzly NIO framework.
    * AJP server connector available to let you plug behind an Apache HTTP server or Microsoft IIS. It is based on Jetty's connector.
    * Multiple client HTTP connectors available, based on either the JDK's HttpURLConnection class or on Apache HTTP Client.
    * Internal HTTP client and server connectors were also added in Restlet 1.1, with no external dependency.
    * Client SMTP, SMTPS, POP v3 and POPS v3 connectors are provided based on JavaMail and a custom email XML format.
    * Client JDBC connector based on the JDBC API, a custom request XML format and the JDBC WebRowSet interface for XML responses.
    * Client FILE connector supports GET, PUT and DELETE methods on files and directories. In addition, it is able to return directory listings.
    * Client CLAP connector to access to the Classloader resources.
    * Client RIAP connector to access to the Restlet internal resources, directly inside the JVM.

Available Representations
    * Built-in support for XML representations (JAX, JibX, DOM or SAX based) with a simple XPath API based on JDK's built-in XPath engine.
    * Integration with the FreeMarker template engine
    * Integration with the Velocity template engine
    * Integration with Apache FileUpload to support multi-part forms and easily handle large file uploads from browsers
    * Transformer filter to easily apply XSLT stylesheets on XML representations. It is based on JDK's built-in XSLT engine.
    * Extensible set of core representations based on NIO readable or writable channels, BIO input or output streams.
    * Support for Atom and JSON standards.

Flexible configuration
    * Complete configuration possible in Java via the Restlet API
    * Configuration possible via Restlet XML and WADL files
    * Servlet adapter provided to let you deploy any Restlet application in Servlet compliant containers like Tomcat, when the usage of standalone HTTP connectors is not possible.
    * Implementation of the JAX-RS standard API (based on draft JSR-311).
    * Deployment as native services is possible and illustrated using the powerful Java Service Wrapper.
    * Extensive integration with popular Spring IoC framework.
    * Deployment to Oracle 11g embedded JVM supported by special extension.

Security
    * Supports HTTP Basic and Digest authentication (client and server side)
    * Supports Amazon S3 authentication (client side)
    * Supports OAuth authentication (server side)
    * Supports HTTPS (HTTP over SSL)
    * Supports SMTPS (SMTP over SSL) and SMTP-STARTTLS
    * Supports POPS (POP over SSL)

Scalability
    * Fully multi-threaded design with per-request Resource instances to reduce thread-safety issues when developing applications.
    * Intentional removal of Servlet-like HTTP sessions. This concept, attractive as a first sight, is one of the major issue for Servlet scalability and is going against the stateless exchanges promoted by REST.
    * Supports non-blocking NIO modes to decouple the number of connections from the number of threads.
    * Supports asynchronous request processing, decoupled from IO operations. Unlike the Servlet API, the Restlet applications don't have a direct control on the outputstream, they only provide output representation to be written by the server connector.


Copyright 2005-2009 Noelios Technologies. Restlet is a registered trademark of Noelios Technologies.
