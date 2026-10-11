Changes log
===========

- 3.0 Milestone 1 (??-??-2026)
    - Breaking changes
        - The Jackson extension now relies on Jackson 3 (`tools.jackson` packages, version 3.2.3).
          The Jackson types exposed by `JacksonRepresentation` (`ObjectMapper`, `ObjectReader`,
          `ObjectWriter`, `CsvSchema`) are now the Jackson 3 ones. Jackson 3 exceptions are
          unchecked (`JsonParseException` becomes `StreamReadException`).
        - Jackson 3 defaults change the generated JSON, XML, YAML and CSV: properties are sorted
          alphabetically and dates are written as ISO-8601 strings instead of timestamps.
        - `ObjectRepresentation.VARIANT_OBJECT_XML_SUPPORTED` and
          `ObjectRepresentation.VARIANT_OBJECT_BINARY_SUPPORTED` are now `final`. They can only be
          enabled with the system properties of the same names, and no longer at runtime.
    - Bugs fixed
        - `Resource.isConfidential()` and `Resource.isLoggable()` threw a `NullPointerException`
          when the resource had no request. They now return `false`.
        - The `ClientResource(Context, Request)` constructor always threw a
          `NullPointerException`. It now creates a response linked to the request.
    - Misc
        - Upgraded Jackson extension to Jackson 3.2.3. The OpenAPI extension remains on Jackson 2
          (2.22.3), as required by swagger-core.
        - Upgraded Hamcrest library to 3.0 (`hamcrest` replaces `hamcrest-all`).
        - Upgraded OSGi core library to 8.0.0 (`osgi.core` replaces `org.osgi.core`).
        - Upgraded Woodstox library to 7.2.1 and Stax2 API to 4.3.0.
        - Removed the unused Log4j 1.2.17 version property.
        - Upgraded Maven to 3.9.16 (also in the Maven wrapper).
        - Upgraded all Maven plugins (compiler, surefire, bundle, javadoc, enforcer, spotless, ...).
        - The build now enforces Maven 3.9+, Java 21+ and dependency convergence, compiles with
          `-Xlint:all` and runs SpotBugs on `verify`.
        - CI runs `verify` (Spotless, JaCoCo coverage and SpotBugs checks), fails when tests fail,
          publishes coverage reports and runs CodeQL. GitHub Actions are updated by Dependabot.
