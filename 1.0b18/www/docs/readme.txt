
==============================
Welcome to the Restlet project
==============================


Mission : Bring the simplicity and efficiency of the REST architectural style to Java developers.
Author  : Jerome Louvel (mailto:contact@noelios.com)
Home    : http://www.restlet.org


Restlet API
    * All REST concepts are supported (resource, representation, data, connector, components, etc.)
    * Suitable for both client and server Web applications
    * Routers support the concept of URIs as UI with advanced pattern matching features
    * Filters support features such as authentication and logging
    * Complete alternative to the Servlet API with no external dependency
    * Supports blocking and non-blocking NIO modes

Noelios Restlet Engine (NRE)
    * Reference implementation of the Restlet API provided by Noelios Consulting
    * Server connectors for HTTP, HTTPS and AJP (for Apache or IIS) protocols
    * Client connectors for HTTP, HTTPS, SMTP, JDBC and FILE protocols.
    * Supports logging (LogFilter), authentication (GuardFilter) and cool URIs rewriting (RedirectRestlet)
    * Static files serving (DirectoryHandler) with metadata association based on file extensions
    * Automatic server-side content negotiation based on media type and language preferences
    * FreeMarker template representations as an alternative to JSP pages
    * Supports Restlet applications as native services using Java Service Wrapper

Dependencies
    * For all applications: Java SE 5.0 or above. Older versions also supported using Retroweaver.
    * For standalone HTTP servers: Jetty 5.1 (stable), Jetty 6.0 (beta), Simple 3.1 and AsyncWeb 0.8 (lightest)
    * For embedded HTTP servers: Servlet 2.4 containers like Apache Tomcat
    * For dynamic documents: FreeMarker 2.3
    * For email sending: JavaMail 1.3

Licensing
    * "Restlet API" and "Noelios Restlet Engine" are distributed under the CDDL license (similar to Mozilla Public License).
    * Commercial licenses can be discussed for trademark usage and alternative usage models.


Copyright © 2005-2006 Jérôme Louvel. Restlet is a registered trademark of Noelios Consulting.