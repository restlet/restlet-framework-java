Changes log
===========

- 3.0 Milestone 2 (??-??-2026)
    - Bugs fixed
        - Slf4j bridge reports wrong line number and caller class in log4j. Issue #233.
        - When GET is an allowed method of a server resource, HEAD is now also listed (e.g. in the Allow header of a 405 response). Issue #266.
    - Misc
        - Pinned the Maven Deploy plugin to version 3.2.0 (avoids resolving incompatible Maven 4 beta versions).
 
- 3.0 Milestone 1 (10-11-2026)
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
        - Added `module-info.java` to the core and all extensions (JPMS modules named after the
          Maven artifacts, e.g. `org.restlet` and `org.restlet.ext.jackson`). The converter and
          authenticator helpers are declared with `provides`. Applications that use the module path
          must `requires` the Restlet modules they use. Third-party libraries without a module
          descriptor are required as automatic modules. Classpath usage is unchanged.
        - Removed `Reference.clone()`, deprecated since 2.7. Use `Reference.copy()` instead.
        - The `TaskService` methods `invokeAll` and `invokeAny` now have the generic signatures of
          `ExecutorService` instead of raw types.
        - The `ImmutableDate` setters, which always throw, are now marked as `@Deprecated`.
    - Bugs fixed
        - `Resource.isConfidential()` and `Resource.isLoggable()` threw a `NullPointerException`
          when the resource had no request. They now return `false`.
        - The `ClientResource(Context, Request)` constructor always threw a
          `NullPointerException`. It now creates a response linked to the request.
    - Misc
        - Upgraded Jackson extension to Jackson 3.2.3. The OpenAPI extension remains on Jackson 2
          (2.22.3), as required by swagger-core.
        - Replaced the deprecated Jetty `ConnectionLimit` with `NetworkConnectionLimit` and Spring's
          deprecated `@NonNull`/`@Nullable` with the JSpecify ones.
        - Upgraded JUnit library to 6.1.3.
        - Upgraded Hamcrest library to 3.0 (`hamcrest` replaces `hamcrest-all`).
        - Upgraded OSGi core library to 8.0.0 (`osgi.core` replaces `org.osgi.core`).
        - Upgraded Woodstox library to 7.2.1 and Stax2 API to 4.3.0.
        - Removed the unused Log4j 1.2.17 version property.
        - Upgraded Maven to 3.9.16 (also in the Maven wrapper).
        - Upgraded all Maven plugins (compiler, surefire, bundle, javadoc, enforcer, spotless, ...).
        - The build now enforces Maven 3.9+, Java 21+ and dependency convergence, compiles with
          `-Xlint:all -Werror` (no warnings left) and runs SpotBugs on `verify`.
        - Javadoc is now checked with doclint (html, syntax, reference and accessibility) and the
          errors found were fixed. Missing comments are not checked yet.
        - CI runs `verify` (Spotless, JaCoCo coverage and SpotBugs checks), fails when tests fail,
          publishes coverage reports. GitHub Actions are updated by Dependabot.
