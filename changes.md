Changes log
===========

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
