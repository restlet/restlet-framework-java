
------------------------------------------------
---  Restlet API and Noelios Restlet Engine  ---
------------------------------------------------

Mission : Bring the simplicity and efficiency of the REST architectural style to Java developers.
Author  : Jerome Louvel (mailto:contact@noelios.com)
Home    : http://www.restlet.org


Features
--------

Restlet API
    * Supports all REST concepts (resource, representation, data, connector, components, etc.)
    * Suitable for both client and server REST applications
    * Maplets support the concept of URIs as UI with advanced pattern matching features
    * Chainlets filter calls to implement features like logging, authentication or compression
    * Complete alternative to Servlet API with no external dependency (JAR < 50kb)
    * Supports blocking and non-blocking NIO modes

Noelios Restlet Engine (NRE)
    * Reference implementation of the Restlet API provided by Noelios Consulting (core JAR < 60kb)
    * Server connector provided: HTTP (via Jetty connectors)
    * Client connectors provided: HTTP, JDBC, SMTP (via JavaMail)
    * Support for logging (LoggerChainlet) and cool URIs rewriting (RedirectRestlet)
    * Static files serving (DirectoryRestlet) with metadata association based on file extensions
    * FreeMarker template representations as an alternative to JSP pages
    * Automatic server-side content negotiation based on media type and language


Release notes
-------------

Requirements:
    * J2SE 5.0
    
Dependencies:    
    * Only when using NRE extensions:
          o Jetty 5.1.5 or 6.0 beta (HTTP server connector)
          o FreeMarker 2.3 (template representation, alternative to JSP pages)
          o JavaMail 1.3 RI (SMTP client connector)

Bugs, enhancements and new features: 
    * Issues database: http://restlet.tigris.org/issues/


Legal
-----
    * "Restlet API" and "Noelios Restlet Engine" are distributed under the CDDL license (similar to Mozilla Public License).
    * "Noelios" and "Restlet" are trademarks and service marks of Noelios Consulting. Registration in progress.
    * Commercial licenses for source code or trademark can be purchased (mailto:licensing@noelios.com).
    

Copyright © 2005 Jérôme Louvel. Restlet is a trademark and service mark of Noelios Consulting.
