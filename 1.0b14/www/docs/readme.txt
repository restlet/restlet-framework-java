
======================================
Restlet API and Noelios Restlet Engine
======================================

Mission : Bring the simplicity and efficiency of the REST architectural style to Java developers.
Author  : Jerome Louvel (mailto:contact@noelios.com)
Home    : http://www.restlet.org


Features
--------

Restlet API
    * All REST concepts are supported (resource, representation, data, connector, components, etc.)
    * Suitable for both client and server Web applications
    * Maplets implement the concept of URIs as UI with advanced pattern matching features
    * Chainlets filter calls to implement features such as authentication and logging
    * Complete alternative to the Servlet API with no external dependency (JAR about 60kb)
    * Supports blocking and non-blocking NIO modes

Noelios Restlet Engine (NRE)
    * Reference implementation of the Restlet API provided by Noelios Consulting (core JAR about 120kb)
    * Server connectors for HTTP, HTTPS, AJP (for Apache or IIS)
    * Client connectors for HTTP, HTTPS, SMTP, JDBC
    * Supports logging (LogChainlet), authentication (GuardChainlet) and cool URIs rewriting (RedirectRestlet)
    * Static files serving (DirectoryRestlet) with metadata association based on file extensions
    * Automatic server-side content negotiation based on media type and language preferences
    * FreeMarker template representations as an alternative to JSP pages


Release notes
-------------

Dependencies:    
    * Java SE 5.0
    * Jetty 5.1 or 6.0: standalone mode
    * Servlet 2.4 container: embedded mode
    * FreeMarker 2.3: dynamic pages
    * JavaMail 1.3: email sending

Bugs, enhancements and new features: 
    * Issues database: http://restlet.tigris.org/issues/


Legal
-----
    * "Restlet API" and "Noelios Restlet Engine" are distributed under the CDDL license (similar to Mozilla Public License).
    * "Noelios" and "Restlet" are trademarks and service marks of Noelios Consulting. Registration in progress.
    * Commercial licenses for source code or trademark can be purchased
    

Copyright 2005-2006 Jérôme Louvel. Restlet is a trademark and service mark of Noelios Consulting.
