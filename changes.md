Changes log
===========

- 2.6.2 (??-??-2026)
    - Misc
        - Added an optional origin matcher to CORS (`CorsService`, `CorsFilter`, `setAllowedOriginMatcher(Predicate<String>)`) to accept origins that are not listed, for example any sub-domain. The response always contains the exact request origin; it has no effect while the allowed origins contain `*`. Issue #1611.
        - CorsService and CorsFilter now log a warning when `*` is allowed as origin together with credentials (any web site could then send credentialed requests), and their Javadoc references the Fetch standard instead of the W3C CORS draft. Issue #1611.
        - Added an opt-in UTF-8 handling of the HTTP Basic credentials (RFC 7617), enabled with the `org.restlet.engine.security.basic.utf8` system property (`HttpBasicHelper.UTF8_PROPERTY`): a Basic challenge then advertises `charset="UTF-8"`, a server decodes the credentials as UTF-8 (falling back to ISO-8859-1 if they are not valid UTF-8), and a client answering such a challenge encodes its credentials in UTF-8. Disabled by default (ISO-8859-1). Issue #1455.
    - Bugs fixed
        - Missing throw in JsonConverter.java. Issue #729.
        - Type mismatch on container access in MethodAnnotationInfo. Issue #1278.
        - Boolean canAppend in ZipClientHelper can only be false. Issue #1279.
        - CorsService and CorsFilter now add `Vary: Origin` to every response when the CORS answer depends on the request origin (origin allow-list or credentials allowed), including requests without an `Origin` header, so shared caches no longer serve the response of one origin to another. A static `*` without credentials still sends no `Vary`, as recommended by the Fetch standard. Issue #1611.
        - Fixed the parsing of a `WWW-Authenticate` challenge made of a token68 ending with `=` (such as a `Negotiate` token): the whole header was dropped. Issue #191.
        - The `realm` and `charset` parameters of an HTTP Basic challenge are now recognized whatever their case (`Realm=`, `Charset=`), as required by RFC 9110. Issue #1455.

- 2.6.1 (30-09-2026)
    - Bugs fixed
        - Reuse an instance of Random class in RandomUtils. Issue #1487.
        - Complete test classes. Issue #1490.
        - Deprecate the implementations of the clone method. Issue #1498.
        - Avoid non-short-circuit logic in FileClientHelper. Issue #1495. 
    - Misc
        - Upgraded Freemarker library to version 2.3.35.
        - Upgraded Gson library to 2.14.0.
        - Upgraded GWT library to 2.13.0.
        - Upgraded Jackson library to 2.19.4.
        - Upgraded JaxB runtime library to 4.0.9.
        - Upgraded JaxB API library to 4.0.5.
        - Upgraded Jetty library to version 12.0.39.
        - Upgraded Joda-Time library to 2.14.4.
        - Upgraded Slf4j library to 2.0.28.
        - Upgraded Spring library to 6.2.19.
        - Upgraded Thymeleaf library to 3.1.5.RELEASE.

- 2.6.0 (29-06-2025)

- 2.6 Release Candidate 2 (21-06-2025)
    - Misc
        - Upgraded to Jakarta EE 10
          - JAXB to 4.0.0
          - Servlet API to 6.0.0
        - Upgraded Gson library to 2.13.1.
        - Upgraded Jackson library to 2.19.1.
        - Upgraded Jetty library to version 12.0.22.
        - Upgraded Joda-Time library to 2.14.0.
        - Upgraded Json library to 20250517.
        - Upgraded OSGi library to 4.3.1.
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
            - Upgraded Slf4j library to 2.0.17.
            - Upgraded GWT libraries to version 2.12.2.
            - Upgraded Jetty library to version 12.0.17.

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
        - Updated Joda-Time library to version 2.13.1.
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
