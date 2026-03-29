/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

/**
 * System utilities.
 *
 * @author Jerome Louvel
 */
public class SystemUtils {

    private static final String JAVA_VERSION_PROPERTY = "java.version";

    /**
     * Parses the "java.version" system property and returns the first digit of the version number
     * of the Java Runtime Environment (e.g., "1" for "1.3.0").
     *
     * @see <a href="http://java.sun.com/j2se/versioning_naming.html">Official Java versioning</a>
     * @return The major version number of the Java Runtime Environment.
     */
    public static int getJavaMajorVersion() {

        final String javaVersion = System.getProperty(JAVA_VERSION_PROPERTY);
        try {
            return Integer.parseInt(javaVersion.substring(0, javaVersion.indexOf('.')));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parses the "java.version" system property and returns the second digit of the version number
     * of the Java Runtime Environment (e.g., "3" for "1.3.0").
     *
     * @see <a href="http://java.sun.com/j2se/versioning_naming.html">Official Java versioning</a>
     * @return The minor version number of the Java Runtime Environment.
     */
    public static int getJavaMinorVersion() {
        final String javaVersion = System.getProperty(JAVA_VERSION_PROPERTY);
        try {
            return Integer.parseInt(javaVersion.split("\\.")[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Parses the "java.version" system property and returns the update release number of the Java
     * Runtime Environment (e.g., "10" for "1.3.0_10").
     *
     * @see <a href="http://java.sun.com/j2se/versioning_naming.html">Official Java versioning</a>
     * @return The release number of the Java Runtime Environment or 0 if it does not exist.
     */
    public static int getJavaUpdateVersion() {
        final String javaVersion = System.getProperty(JAVA_VERSION_PROPERTY);
        try {
            return Integer.parseInt(javaVersion.substring(javaVersion.indexOf('_') + 1));
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Computes the hash code of a set of objects. Follows the algorithm specified in
     * List.hasCode().
     *
     * @param objects the objects to compute the hashCode
     * @return The hash code of a set of objects.
     */
    public static int hashCode(Object... objects) {
        int result = 17;

        if (objects != null) {
            for (final Object obj : objects) {
                result = 31 * result + (obj == null ? 0 : obj.hashCode());
            }
        }

        return result;
    }

    /**
     * Indicates if the current operating system is in the Windows family.
     *
     * @return True if the current operating system is in the Windows family.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * The bug-id 6331920 is a ong-standing issue in HttpURLConnection that was addressed in the
     * mid-2000s, specifically around the transition to JDK 1.5.0_10 (Java 5 Update 10).
     *
     * <p>In versions of Java prior to the fix, HttpURLConnection had a hardcoded default behavior:
     * if a developer did not explicitly set a Content-Type header for a POST request, the JDK would
     * automatically default it to: application/x-www-form-urlencoded
     *
     * <p>While this was convenient for standard HTML form submissions, it caused significant issues
     * for emerging web services (like SOAP or early REST implementations) that required text/xml or
     * application/json. If a developer forgot to set the header, the server would receive the
     * "form-urlencoded" type and often fail to parse the payload correctly.
     *
     * @return True if the Content-type header should be defaulted to
     *     "application/x-www-form-urlencoded".
     */
    public static boolean shouldApplyBug6331920Patch() {
        boolean applyPatch = false;

        // This patch seems to apply to Sun JVM only.
        final String jvmVendor = System.getProperty("java.vm.vendor");
        if ((jvmVendor != null) && (jvmVendor.toLowerCase()).startsWith("sun")) {
            final int majorVersionNumber = SystemUtils.getJavaMajorVersion();
            final int minorVersionNumber = SystemUtils.getJavaMinorVersion();

            if (majorVersionNumber == 1) {
                if (minorVersionNumber < 5) {
                    applyPatch = true;
                } else if (minorVersionNumber == 5) {
                    // Sun fixed the bug in update 10
                    applyPatch = (SystemUtils.getJavaUpdateVersion() < 10);
                }
            }
        }
        return applyPatch;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class i.e., it isn't
     * instantiable and extensible.
     */
    private SystemUtils() {}
}
