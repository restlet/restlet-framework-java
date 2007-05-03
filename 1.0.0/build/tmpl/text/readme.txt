
==============================================
Restlet, a lightweight REST framework for Java
==============================================
         http://www.restlet.org
----------------------------------------------


Features
    * REST concepts have equivalent Java classes (resource, representation, connector, etc.)
    * Suitable for both client and server Web applications
    * Automatic server-side content negotiation based on media type and language preferences
    * Static file serving and editing with metadata association based on file extensions
    * Support for representations like JSON, XML (DOM or SAX), FreeMarker templates (alternative to JSP)
    * Server connectors for HTTP, HTTPS and AJP (for Apache or IIS) protocols
    * Client connectors for HTTP, HTTPS, SMTP, JDBC and FILE protocols
    * Routers support the concept of URIs as UI with advanced pattern matching features
    * Filters support features such as authorization, browser tunnelling and extraction of call attributes
    * Support of HTTP Basic and Amazon Web Services authentication schemes
    * Deployment as native services using Java Service Wrapper
    * Supports blocking and non-blocking NIO modes
    * Clean Restlet API as a full alternative to the Servlet API
    * Noelios Restlet Engine (NRE) is the Reference Implementation (provided by Noelios Consulting)

Dependencies
    * For all applications: Java SE 5.0 or above. JDK 1.4 support is also available via Retroweaver.
    * For standalone HTTP servers: AsyncWeb 0.8, Jetty 6.1 and Simple 3.1
    * For embedded HTTP servers: Servlet 2.4 containers like Apache Tomcat
    * For dynamic documents: FreeMarker 2.3, Velocity 1.4, JSON 2.0
    * For uploading large files: Apache FileUpload 1.1
    * For sending emails: JavaMail 1.4

Licensing
    * "Restlet API" and "Noelios Restlet Engine" are distributed under the CDDL license (similar to Mozilla Public License).
    * Commercial licenses can be discussed for trademark usage and alternative usage models.


Copyright © 2005-2007 Jérôme Louvel. Restlet is a registered trademark of Noelios Consulting.