/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.util;

/**
 * System utilities.
 * 
 * @author Jerome Louvel
 */
public class SystemUtils {

    /**
     * Parses the "java.version" system property and returns the first digit of
     * the version number of the Java Runtime Environment (e.g. "1" for
     * "1.3.0").
     * 
     * @see <a href="http://java.sun.com/j2se/versioning_naming.html">Official
     *      Java versioning</a>
     * @return The major version number of the Java Runtime Environment.
     */
    public static int getJavaMajorVersion() {
        int result;
        final String javaVersion = System.getProperty("java.version");
        try {
            result = Integer.parseInt(javaVersion.substring(0, javaVersion
                    .indexOf(".")));
        } catch (Exception e) {
            result = 0;
        }

        return result;
    }

    /**
     * Parses the "java.version" system property and returns the second digit of
     * the version number of the Java Runtime Environment (e.g. "3" for
     * "1.3.0").
     * 
     * @see <a href="http://java.sun.com/j2se/versioning_naming.html">Official
     *      Java versioning</a>
     * @return The minor version number of the Java Runtime Environment.
     */
    public static int getJavaMinorVersion() {
        int result;
        final String javaVersion = System.getProperty("java.version");
        try {
            result = Integer.parseInt(javaVersion.split("\\.")[1]);
        } catch (Exception e) {
            result = 0;
        }

        return result;
    }

    /**
     * Parses the "java.version" system property and returns the update release
     * number of the Java Runtime Environment (e.g. "10" for "1.3.0_10").
     * 
     * @see <a href="http://java.sun.com/j2se/versioning_naming.html">Official
     *      Java versioning</a>
     * @return The release number of the Java Runtime Environment or 0 if it
     *         does not exist.
     */
    public static int getJavaUpdateVersion() {
        int result;
        final String javaVersion = System.getProperty("java.version");
        try {
            result = Integer.parseInt(javaVersion.substring(javaVersion
                    .indexOf('_') + 1));
        } catch (Exception e) {
            result = 0;
        }

        return result;
    }

}
