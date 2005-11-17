
------------------------------------------------
---  Restlet API and Noelios Restlet Engine  ---
------------------------------------------------

Version : 0.10 beta
Date    : 11/17/2005
Author  : Jérôme Louvel (jerome.louvel@noelios.com)
License : GPL (commercial license also available, please contact me)


Mission
-------
Bring the power and simplicity of the REST architectural style into the hands of Java developers.


Features
--------

Restlet API:
    * Supports all REST concepts (resource, representation, data, connector, components, etc.)
    * Restlet concept allow easy handling of REST calls
    * Connectors can be added for any protocol
    * Blocking and non-blocking input/output are supported
    * Suitable for development of REST clients and servers
    * No external dependency

Noelios Restlet Engine:
    * Reference implementation of the Restlet API
    * Server connector: HTTP
    * Client connectors: SMTP, JDBC
    * Automatic content negotiation
    * Automatic metadata association based on file extensions
    * Commercial license available if you can't redistribute in open source


Requirements
------------
    * J2SE 5.0 (for enumerations and generics)
    * Jetty 5.1.5 (server HTTP connector used in Noelios Restlet Server)
    * FreeMarker 2.3 (alternative to JSP used in Noelios Restlet Server)
    * JavaMail 1.3 to use the client JavaMail connector


Related resources
-----------------
    * Original specification of REST by Roy T. Fielding
    * Servlets must DIE! Slowly!! by Greg Wilkins
    * NIO and the Servlet API by Greg Wilkins
    * The Makings of a Good HTTP API by Benjamin Carlyle

