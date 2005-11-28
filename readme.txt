
------------------------------------------------
---  Restlet API and Noelios Restlet Engine  ---
------------------------------------------------

Version : 0.11 beta
Date    : 11/25/2005
Author  : Jerome Louvel (mailto:jerome.louvel@noelios.com)
Home    : http://www.restlet.com

Mission
-------
Bring the simplicity and efficiency of the REST architectural style to Java developers.


Features
--------

Restlet API:
    * Supports all REST concepts (resource, representation, data, connector, components, etc.)
    * Suitable for both client and server REST applications
    * Complete replacement for the Servlet API
    * No dependency on any third-party library
    * Restlets and Maplets allow easy handling of hierarchical URIs
    * Connectors can be added for any protocol
    * Supports blocking and non-blocking inputs and outputs

Noelios Restlet Engine (NRE):
    * Reference implementation of the Restlet API
    * Server connector: HTTP
    * Client connectors: HTTP, JDBC, SMTP
    * Automatic metadata association based on file name extensions
    * Automatic server-side content negotiation
    * FreeMarker template representations provide an excellent alternative to JSP pages


Release notes
-------------

Dependencies
    * J2SE 5.0
    * Extensions (copy included in distribution):
          o Jetty 5.1.5 (for the server HTTP connector provided by NRE)
          o FreeMarker 2.3 (alternative to JSP pages provided by NRE)
          o JavaMail 1.3 (for the client SMTP connector provided by NRE)
          o Supporting third-party libraries

To do list:
    * Finish the implementation of the client HTTP connector (cookies, user agent preferences)
    * Implement the cache control options for Jetty connector and for the FileRepresentation.
    * Add support for SOAP XML message (via Apache Axis?)
    * Add better logging of errors
    * More testing of the JDBC and HTTP client connectors
    * Integration with Jetty 6 for efficient NIO usage
    * Add a SQLClient that provides a closer integration than the JdbcClient.
          o See the PHP REST SQL project at http://phprestsql.sourceforge.net/
    * Add integration with Hibernate
    * Add integration with EJB 3.0 and other useful JEE APIs
    * Integration with Grizzly server HTTP connector (from Sun’s Glassfish project)
    * Integration with Apache Tomcat Coyote server HTTP connector
    * Integration with Apache Commons HttpClient client HTTP connector
    * Dynamic management of restlet servers, containers, etc.
          o Web console
          o JMX handlers


Licensing
---------
    * "Restlet API" and "Noelios Restlet Engine" are distributed under the GPL license.
    * Internal use and modification is freely allowed for any purpose.
    * Redistribution with GPL-compatible software is freely allowed.
    * For redistribution with other OSI approved open source software, a free license can be requested.
    * For redistribution with commercial software, a license can be purchased.


Copyright © 2005 Jérôme Louvel. Restlet is a trademark and servicemark of Noelios Consulting.
