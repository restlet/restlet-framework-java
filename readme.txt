
------------------------------------------------
---  Restlet API and Noelios Restlet Engine  ---
------------------------------------------------

Version : 0.11 beta
Date    : 11/??/2005
Author  : Jerome Louvel (mailto:jerome.louvel@noelios.com)
License : GPL (commercial license also available, please contact me)
Home    : http://www.restlet.org

Mission
-------
Bring the simplicity and efficiency of the REST architectural style to Java developers.


Features
--------

Restlet API:
   * Supports all REST concepts (resource, representation, data, connector, components, etc.)
   * Complete alternative to the Servlet API
   * No dependency to any third-party library
   * Restlets and Maplets allow easy handling of hierarchical URIs
   * Connectors can be added for any protocol
   * Supports blocking and non-blocking inputs and outputs
   * Suitable for both client and server REST applications

Noelios Restlet Engine (NRE):
   * Reference implementation of the Restlet API
   * Server connector: HTTP
   * Client connectors: HTTP, JDBC, SMTP
   * Automatic metadata association based on file name extensions
   * Automatic server-side content negotiation
   * FreeMarker template representations provide an excellent alternative to JSP pages


Release notes
-------------

System requirements:
    * Java Standard Edition 5.0
    * Extensions (copy included in distribution):
          o Mortbay Jetty 5.1.5 for the HTTP server connector.
          o FreeMarker 2.3 for the TemplateRepresentation class.
          o JavaMail 1.3 reference implementation for the JavaMailClient class.
          o Supporting third-party libraries
 
Major changes:
    * Added a DirectoryRestlet that behaves like a regular Web server. In addition, automatic media type, language and character set detection and setting is available in a way that hides technical names and extensions, allowing a clean content negotiation.
    * Refactored the Jetty HTTP server connector to bypass their HTTP handler layer. Now we directly work out of the Jetty SocketListener and HttpConnection class. This will improve performances and reduce the memory consumed. Note that version 5.1.5 of Jetty is required as it contains a necessary fix.
    * Upgraded the JDBC Client to support multiple databases, drivers and connection pools.
    * Major refactoring.

To do list:
    * Integration with java.net.HttpUrlConnection as a client HTTP connector
    * Implement the cache control options for Jetty connector and for the FileRepresentation.
    * Add support for SOAP (via Apache Axis?)
    * Add better logging of errors
    * Testing of the JDBC Client
    * Integration with Jetty 6 for efficient NIO usage
    * Integration with Grizzly server HTTP connector (from Sun’s Glassfish project)
    * Integration with Apache Tomcat Coyote server HTTP connector
    * Integration with Apache Commons HttpClient client HTTP connector
    * Allow configuration of a restlet server using an XML document (containers, connectors, maplets, restlets, etc.).
    * Dynamic management of restlet servers, containers, etc.
          o Web console
          o JMX handlers
    * Add a SQLClient that provides a closer integration than the JdbcClient.
          o See the PHP REST SQL project at http://phprestsql.sourceforge.net/
    * Add integration with EJB 3.0 and other useful JEE APIs
    * Add integration with Hibernate
