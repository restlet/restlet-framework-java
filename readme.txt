
------------------------------------------------
---  Restlet API and Noelios Restlet Engine  ---
------------------------------------------------

Mission : Bring the simplicity and efficiency of the REST architectural style to Java developers.
Author  : Jerome Louvel (mailto:contact@noelios.com)
Home    : http://www.restlet.org


Features
--------

Restlet API:
   * Supports all REST concepts (resource, representation, data, connector, components, etc.)</li>
   * Suitable for both client and server REST applications</li>
   * Maplets handle hierarchical URIs with advanced pattern matching features</li>
   * Chainlets filter calls to implement logging, authentication or compression features</li>
   * Complete alternative to Servlet API with no external dependency (JAR < 50kb)</li>
   * Supports blocking and non-blocking NIO modes</li>

Noelios Restlet Engine (NRE):
   * Reference implementation of the Restlet API provided by <a target="_top" href="http://www.noelios.com">Noelios Consulting</a> (core JAR < 60kb)</li>
   * Server connector provided: HTTP (via Jetty connectors)</li>
   * Client connectors provided: HTTP, JDBC, SMTP (via JavaMail)</li>
   * Support for logging (LoggerChainlet) and URI rewriting (RedirectRestlet)</li>
   * Static files serving (DirectoryRestlet) with metadata association based on file extensions</li>
   * FreeMarker template representations as an alternative to JSP pages</li>
   * Automatic server-side content negotiation based on media type and language</li>


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
