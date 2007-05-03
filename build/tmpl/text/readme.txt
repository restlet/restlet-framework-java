
==============================================
Restlet, a lightweight REST framework for Java
==============================================
         http://www.restlet.org
----------------------------------------------


REST support
    * Core REST concepts have equivalent Java classes (resource, representation, connector, etc.).
    * Suitable for both client and server Web Applications, using the same API which reduces the learning curve.
    * Routers support the concept of URIs as UI with built-in support for URI Templates.
    * Tunnelling service let browsers issue any method (PUT, DELETE, MOVE, etc.) through a simple HTTP POST. This service is transparent for Restlet applications.

Complete Web Server
    * Static file serving similar to Apache HTTP Server, with metadata association based on file extensions.
    * Transparent content negotiation based on client preferences.
    * Remote edition of files based on PUT and DELETE methods (aka mini-WebDAV mode).
    * Decoder service transparently decodes compressed or encoded input representations. This service is transparent for Restlet applications.
    * Log service writes all accesses to your applications in a standard Web log file. The log format follows the W3C Extended Log File Format and is fully customizable.
    * Powerful URI based redirection support similar to Apache Rewrite module.

Available Connectors
    * Multiple server HTTP connectors available, based on either AsyncWeb (now part of Apache MINA), Mortbay's Jetty or the Simple Framework.
    * AJP server connector available to let you plug behind an Apache HTTP server or Microsoft IIS. It is based on Jetty's connector.
    * Multiple client HTTP connectors available, based on either the JDK's HttpURLConnection class or on Apache HTTP Client.
    * Client SMTP connector based on JavaMail and a custom email XML format.
    * Client JDBC connector based on the JDBC API, a custom request XML format and the JDBC WebRowSet interface for XML responses.
    * Client FILE connector support GET, PUT and DELETE methods on files and directories. In addition, it is able to return directory listings.

Available Representations
    * Built-in support for XML representations (DOM or SAX based) with a simple XPath API based on JDK's built-in XPath engine.
    * Integration with the FreeMarker template engine
    * Integration with the Velocity template engine
    * Integration with Apache FileUpload to support multi-part forms and easily handle large file uploads from browsers
    * Transformer filter to easily apply XSLT stylesheets on XML representations. It is based on JDK's built-in XSLT engine.
    * Extensible set of core representations, based on NIO readable or writable channels, BIO input or output streams.

Security
    * Supports HTTP Basic authentication (client and server side)
    * Supports Amazon Web Services authentication (client side)
    * Supports HTTPS (HTTP over SSL)
    * Supports SMTPS (SMTP over SSL)

Scalability
    * Fully multi-threaded design with few synchronization points.
    * Intentional removal of Servlet-like HTTP sessions. This concept, attractive as a first sight, is one of the major issue for Servlet scalability and is going against the stateless exchanges promoted by REST.
    * Supports non-blocking NIO modes to decouple the number of connections from the number of threads.
    * Supports asynchronous request processing, decoupled from IO operations. Unlike the Servlet API, the Restlet applications don't have a direct control on the outputstream, they only provide output representation to be written by the server connector.

Portability
    * The Restlet API is cleanly seperated from the Reference Implementation called the Noelios Restlet Engine. Alternative implementation are possible and encouraged.
    * An adapter Servlet is provided to let you deploy any Restlet application in any Servlet compliant container, when the usage of standalone HTTP connectors is not possible.

Deployment
    * Deployment as native services is possible and illustrated using the powerful Java Service Wrapper.
    * Integration with Spring lets you apply the Inversion of Control design pattern to configure your applications using a central XML file.


Copyright © 2005-2007 Jérôme Louvel. Restlet is a registered trademark of Noelios Consulting.
