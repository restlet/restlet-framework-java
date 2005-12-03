
------------------------------------------------
---  Restlet API and Noelios Restlet Engine  ---
------------------------------------------------

Version : 0.13 beta
Date    : 12/03/2005
Author  : Jerome Louvel (mailto:contact@noelios.com)
Home    : http://www.restlet.org

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
    * Complete the implementation of the client HTTP connector (cookies, user agent preferences)
    * Add support for SOAP XML message (via Apache Axis?)
    * Integration with Spring
    * Add better logging
    * Integration with Jetty 6 for efficient NIO usage
    * Integration with Grizzly server HTTP connector (from Sun’s Glassfish project)
    * Integration with Apache Commons HttpClient client HTTP connector
    * Integration with Apache Tomcat Coyote server HTTP connector
    * Add a SQLClient that provides a closer integration than the JdbcClient.
          o See the PHP REST SQL project at http://phprestsql.sourceforge.net/
    * Integration with Hibernate
    * Dynamic management of restlet servers, containers, etc.
          o Web console
          o JMX handlers


Legal
-----
    * "Restlet API" and "Noelios Restlet Engine" are distributed under the CDDL license (similar to Mozilla Public License).
    * "Noelios" and "Restlet" are trademarks and service marks of Noelios Consulting. Registration in progress.
    * Commercial licenses for source code or trademark can be purchased (mailto:licensing@noelios.com).
    

Copyright © 2005 Jérôme Louvel. Restlet is a trademark and service mark of Noelios Consulting.
