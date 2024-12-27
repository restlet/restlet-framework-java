Changes log
===========

- 2.5.0 (??-11-2024)
    - Security
       - Spring Framework before 6.0.0 suffers from a potential remote code execution (RCE) issue if used for Java deserialization
         of untrusted data. Depending on how the library is implemented within a product, this issue may or not occur, and
         authentication may be required. Restlet Framework isn't able to upgrade to Spring Framewortk version 6.0 due to its
         requirement to use Java 8. If you are running Java 17+, please override the Spring dependency in your POM to version 6.0+
    - Bugs fixed
       - Fixed serialization issues in the GWT edition between client and servver sides.
    - Misc
      - Deprecated JDBC, POP, POPS, SMTP, SMTPS protocol constants and SmtpPlainHelper for upcoming removal as the JavaMail extension
        is no more and added other missing code deprecations.        
      - Removed deprecation of GWT extension as the new intent is to keep the GWT edition and extension maintained in the new
        major 2.6 release.
      - Deprecated extensions (Apache) HTTP Client, HTML and (Apache) FileUpload as we intend to remove them in version 2.6 to
        rely instead on the Jetty extension for a more robust alternative. Issue #1437.

- 2.5 Release Candidate 1 (12-11-2024)
    - Enhancements
      - Changed the minimum requirements back to Java 8 like for Restlet Framework 2.4 to facilitate upgrade.
      - Transitioned project to native Maven including code base, build and delivery.
      - Artifacts are now published in Maven Central and GitHub Packages repository for snapshots and only there.
      - Updated all libraries used as dependency for various extensions.
      - RestletFileUpload can now dynamically adapt to Java SE or Java EE environments.
    - Bugs fixed
      - Ensured that all 2.4.4 fixes have been ported forward to the 2.5 branch.
      - Non-serializable internal attributes (status, request, and message) marked as transient,
        this is to allow ResourceException to be serialized, passing over the message and stack-trace
        (which is typically all that is needed to show the error or print it in the log).
    - Misc
      - Replaced separate Android, GAE, Java EE and OSGi editions by a single 'Java' edition
        (tested to work in those various environments).
      - Kept a distinct 'GWT' edition using a forked code base due to many GWT specificities.
      - Removed incubator to rely instead on pull requests.
      - Renamed extension Inject into Guice as it isn't truly generic.
      - Updated the links in all Javadocs file for accuracy.
      - Deprecated GAE, OSGi and RDF extensions for removal in next major release.
      - Deprecated SDC, SIP protocol and challenge scheme
      - Deprecated NIO flavor of Restlet API for removal in next major release, with plan to use
        virtual threads instead to achieve additional scalability and reactive behavior.
      - Removed code deprecated in version 2.4, including native Restlet XML configuration mechasism.
        Please use Spring for an alternative approach if needed.
      - Upgrade Apache File Upload library to version 1.5.
      - Upgrade Apache HTTP Client library to version 4.5.14.
      - Upgrade Apache FreeMarker library to version 2.3.32.
      - Upgrade Apache Velocity library to version 2.4.1.
      - Upgrade Eclipse Jetty library to version 9.4.56.
      - Upgrade GAE library to version 1.9.98.
      - Upgrade GSON library to version 2.10.1.
      - Upgrade GWT library to version 2.11.0.
      - Upgrade Jackson library to version 2.17.0.
      - Upgrade JAXB library to version 2.4.0.
      - Upgrade JSON library to version 20240303.
      - Upgrade Servlet library to version 3.1.0.
      - Upgrade SLF4J library to version 2.0.16.
      - Upgrade Spring Framework library to version 5.3.34.
      - Upgrade Thymeleaf library to version 3.0.11.

- 2.5 Milestone 1 (03-09-2020)
    - Bugs fixed
      - Allow parsing of double values like "2.0" or "4.0" being received in
        the Retry-After header. Issue #1355.
        Reported by Brett Cooper.
      - MemoryRealm.unmap method leads to ArrayOutOfBound exception. Issue #1358.
        Reported by j-perrin.
    - Misc
      - Removed deprecated extensions EMF, JavaMail, JAX-RS, JibX, Lucene, NIO, 
        OAuth, OpenID, Platform, RAML, Simple, WADL. Issue #1351.
      - Removed generation of Eclipse p2 artifacts from the build since the 
        Eclipse p2 site is no more maintained.
      - Refresh copyright headers. Issue #1351.
      - Upgraded to JDK 11. Issue #1351.
      - Upgraded Apache FileUpload library to version 1.4. Issue #1353.
      - Upgraded Apache Velocity library to version 2.1. Issue #1353.
      - Upgraded Eclipse Jetty library to version 9.4.24.v20191120. Issue #1353.
      - Upgraded GSON library to version 2.8.6. Issue #1353.
      - Upgraded Guava library to version 28.1-jre. Issue #1353.
      - Upgraded Guice library to version 4.2.2. Issue #1353.
      - Upgraded GWT library to version 2.8.2. Issue #1363.
      - Upgraded Jackson library to version 2.10.1. Issue #1353.
      - Upgraded JAXB library to version 2.4.0-b180830.0438. Issue #1353.
      - Upgraded Snakeyaml library to version 1.24. Issue #1353.
      - Upgraded Spring Framework library to version 5.2.2-RELEASE. Issue #1353.
      - Upgraded Thymeleaf library to version 3.0.11. Issue #1353.
