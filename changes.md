Changes log
===========

- 2.6.0 (??-05-2025)
    - Misc
        - Upgraded to Jakarta EE 10
          - JaxB 4.0.0 
        - Upgraded Gson library to 2.13.1.
        - Upgraded Jackson library to 2.19.1.
        - Upgraded Jetty library to version 2.0.17.
        - Upgraded Joda-time library to 2.14.0.
        - Upgraded Json library to 20250517.
        - Upgraded OSGi library to 4.3.0.
        - Upgraded Spring library to 6.2.8.

- 2.6 Release Candidate 1 (28-04-2025)
    - Enhancements
        - Added MultiPartRepresentation to the Jetty extension to support generation and parsing.
        - Added support for the "charset" parameter in HTTP BASIC challenges. Reported by Marc Lafon.
            - Added MediaType constructors to help with cloning and customization needs.
            - Added support for HTTP2 and HTTP3 protocols by the Jetty HTTP server connector.
            - Added support for HTTP2 and HTTP3 protocols by the Jetty HTTP client connector.
        - Misc
            - Upgraded Thymeleaf library to 3.1.3.RELEASE.
            - Upgraded Slf4j library to 5.12.0.
            - Upgraded GWT libraries to version 2.12.2.
            - Upgraded Jetty library to version 2.0.17.

- 2.6 Milestone 2 (02-03-2025)
    - Enhancements
        - Added support for graceful shutdown to the Jetty server.
        - Updated default configuration of Jetty connector and fixed unit tests.
    - Misc
        - Deprecated internal HTTP connector that will be removed in favor of Jetty in 2.7/3.0 version.
        - Deprecated the GWT edition that will be removed in 2.7/3.0 version.
        - Refactored unit tests to follow standard Maven location and removed "org.restlet.test" module.
        - Moved "org.restlet.examples" module
          into [separate repository]([url](https://github.com/restlet/restlet-examples/tree/2.6))
        - Updated FreeMarker library to version 2.3.34.
        - Updated GSON library to version 2.12.1.
        - Updated Jackson library to version 2.17.3.
        - Updated JODA Time library to version 2.13.1.
        - Updated JSON library to version 20250107.
        - Updated JUnit library to version 5.11.4.
        - Updated Spring Framework dependency to version 6.2.3.

- 2.6 Milestone 1 (26-01-2025)
    - Enhancements
        - Java 17 is the minimum requirement.
    - Misc
        - Upgraded GWT (org.gwtproject:gwt-dev/gwt-user) to 2.12.1.
        - Upgraded Guice to 7.0.0.
        - Upgraded Jetty to 12.0.16.
        - Upgraded Spring to 6.2.0.
        - Removed deprecated extensions FileUpload, GAE, OSGi, RDF.
        - Removed deprecated code related to WebDAV, NIO, POP, POPS, SDC, SIP, SIPS, SMTP, SMTPS.
        - Deprecated the Atom, JAXB, Guice, GWT, OData and Servlet extensions.
