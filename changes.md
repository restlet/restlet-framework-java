Changes log
===========

- 2.7.1 (??-??-2026)
  - Misc
    - Upgraded Jetty library to version 12.1.14. Issue #1515.
    - Upgraded Jackson library to version 2.22.4.
    - Upgraded org.json library to version 20260814.
    - Deprecated the legacy RFC 2109/2965 cookie API (cookie version, comment and related constructors), to be removed in Restlet 3.0 in favor of RFC 6265.
    - Added an optional origin matcher to CORS (`CorsService`, `CorsFilter`, `setAllowedOriginMatcher(Predicate<String>)`) to accept origins that are not listed, for example any sub-domain. The response always contains the exact request origin; it has no effect while the allowed origins contain `*`. Issue #1611.
    - CorsService and CorsFilter now log a warning when `*` is allowed as origin together with credentials (any web site could then send credentialed requests), and their Javadoc references the Fetch standard instead of the W3C CORS draft. Issue #1611.
    - Added an opt-in UTF-8 handling of the HTTP Basic credentials (RFC 7617), enabled with the `org.restlet.engine.security.basic.utf8` system property (`HttpBasicHelper.UTF8_PROPERTY`): a Basic challenge then advertises `charset="UTF-8"`, a server decodes the credentials as UTF-8 (falling back to ISO-8859-1 if they are not valid UTF-8), and a client answering such a challenge encodes its credentials in UTF-8. Disabled by default (ISO-8859-1). Issue #1455.
    - Added `Tag.strongMatch(Tag)` and `Tag.weakMatch(Tag)`, the strong and weak comparison functions of RFC 9110 section 8.8.3.2. `Tag.equals(Object, boolean)` is deprecated in favor of them.
  - Bugs fixed
    - Slf4j bridge reports wrong line number and caller class in log4j. Issue #233.
    - When GET is an allowed method of a server resource, HEAD is now also listed (e.g. in the Allow header of a 405 response). Issue #266.
    - The Max-Age and Path attributes of a version 1 cookie are no longer quoted in the Set-Cookie header, as browsers ignore them otherwise. Issue #1334.
    - CookieAuthenticator no longer lets the next restlet overwrite the redirection sent after a login. Issue #554.
    - CorsService and CorsFilter now add `Vary: Origin` to every response when the CORS answer depends on the request origin (origin allow-list or credentials allowed), including requests without an `Origin` header, so shared caches no longer serve the response of one origin to another. A static `*` without credentials still sends no `Vary`, as recommended by the Fetch standard. Issue #1611.
    - Fixed the parsing of a `WWW-Authenticate` challenge made of a token68 ending with `=` (such as a `Negotiate` token): the whole header was dropped. Issue #191.
    - The `realm` and `charset` parameters of an HTTP Basic challenge are now recognized whatever their case (`Realm=`, `Charset=`), as required by RFC 9110. Issue #1455.
    - `ChallengeScheme.valueOf()` returned `HTTP_OAUTH` for the names of the Bearer and MAC schemes (`HTTP_Bearer`, `HTTP_MAC`) instead of `HTTP_OAUTH_BEARER` and `HTTP_OAUTH_MAC`. Issue #1618.
    - `Tag.equals(Object)` compared the names only since 2.7.0, while `hashCode()` includes the weakness, and the flag of `Tag.equals(Object, boolean)` was inverted compared to its Javadoc and to Restlet 2.6. Both compare the weakness again as documented, so `W/"a"` no longer equals `"a"`. Code comparing a tag parsed from a header with `new Tag("x")` must use `new Tag("x", false)`, as `new Tag(String)` creates a weak tag. Regression of 2.7.0.
    - `If-Range` accepted weak entity tags since 2.7.0, which RFC 9110 forbids (the partial content of a weak representation can be spliced with another one): it now uses the strong comparison, so a resource returning a weak tag (the default of `new Tag(String)`) answers a `Range` request with an `If-Range` with a complete 200 response. Use `new Tag(value, false)` for a strong tag. Regression of 2.7.0.
    - `If-None-Match` compares the entity tags with the weak comparison whatever the method: since 2.7.0, a weak tag and a strong tag with the same name did not match on a PUT, POST or DELETE. Regression of 2.7.0.
    - `If-None-Match: *` was ignored for a resource having an entity tag: a "create only" PUT overwrote the existing resource instead of getting a 412 (a GET did not get a 304). It is now false for any existing resource, as required by RFC 9110 section 13.1.2. `*` is also recognized when it is not the first tag of an `If-Match` or `If-None-Match` list.
    - `If-Range` with a date never matched when the modification date of the entity had milliseconds, as `FileRepresentation` has, so clients could not resume a download. The dates are now compared with a precision of one second.

- 2.7.0 (30-09-2026)
  - Misc
    - Upgraded Freemarker library to version 2.3.35.
    - Upgraded Jackson library to version 2.22.3.
    - Upgraded Jetty library to version 12.0.39.
    - Upgraded Swagger library to version 2.2.55.
    - Upgraded Swagger parser library to version 2.1.48.

- 2.7 Release Candidate 1 (23-07-2026)
  - Misc
    - Upgraded Gson library to version 2.14.0.
    - Upgraded Jackson library to version 2.21 (LTS).
    - Upgraded Jetty library to version 2.0.36.
    - Upgraded JSON library to version 20260522.
    - Upgraded Slf4j library to version 2.0.18.
    - Upgraded Springframework library to version 7.0.8.
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
