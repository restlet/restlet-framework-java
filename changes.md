Changes log
===========

- 2.7 Release Candidate 1 (??-06-2026)
  - Misc
    - Upgraded Gson library to version 2.14.0.
    - Upgraded Jackson library to version 2.19.4.
    - Upgraded Jetty library to version 2.0.36.
    - Upgraded JSON library to version 20260522.
    - Upgraded Slf4j library to version 2.0.18.
    - Upgraded Springframework library to version 6.2.19.
    - Upgraded Swagger library to version 2.2.50.
    - Upgraded Swagger parser library to version 2.1.43.
    - Upgraded Thymeleaf library to version 3.1.5.RELEASE.

- 2.7 Milestone 3 (10-06-2026)
  - Enhancements
    - New OpenApi extension (OAS 3.1 only). Contributed by Antoine Nicolas.
  - Bugs fixed
      - Reuse an instance of Random class in RandomUtils. Issue #1487.
      - Complete test classes. Issue #1490.
      - Avoid non-short-circuit logic in FileClientHelper. Issue #1495.
      - Drop the implementations of the clone method. Issue #1498.

- 2.7 Milestone 2 (29-06-2025)
    - Misc
      - Removed deprecated Servlet extension and related classes in Spring extension
      - Removed deprecated methods in Restlet API
      - Removed internal HTTP and FTP connectors
      - Merged the Jetty client and server HTTP connector into Restlet Engine (new default connectors)

- 2.7 Milestone 1 (21-06-2025)
    - Misc
        - Removed deprecated extension Atom, Guice, GWT, JAXB, OData
        - Removed edition GWT
