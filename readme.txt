
------------------------------------------------
---  Restlet API and Noelios Restlet Engine  ---
------------------------------------------------

Version : 0.15 beta
Date    : 12/**/2005
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

Requirements:
    * J2SE 5.0
    
Dependencies:    
    * Only when using NRE extensions:
          o Jetty 5.1.5 (HTTP server connector)
          o Jetty 6.0 beta 5 (HTTP server connector)
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
