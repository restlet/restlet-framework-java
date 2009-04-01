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
 * String manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public class StringUtils {
    /**
     * Strips a delimiter character from both ends of the source string.
     * 
     * @param source
     *            The source string to strip.
     * @param delimiter
     *            The character to remove.
     * @return The stripped string.
     */
    public static String strip(String source, char delimiter) {
        return strip(source, delimiter, true, true);
    }

    /**
     * Strips a delimiter character from a source string.
     * 
     * @param source
     *            The source string to strip.
     * @param delimiter
     *            The character to remove.
     * @param start
     *            Indicates if start of source should be stripped.
     * @param end
     *            Indicates if end of source should be stripped.
     * @return The stripped source string.
     */
    public static String strip(String source, char delimiter, boolean start,
            boolean end) {
        int beginIndex = 0;
        int endIndex = source.length();
        boolean stripping = true;

        // Strip beginning
        while (stripping && (beginIndex < endIndex)) {
            if (source.charAt(beginIndex) == delimiter) {
                beginIndex++;
            } else {
                stripping = false;
            }
        }

        // Strip end
        stripping = true;
        while (stripping && (beginIndex < endIndex - 1)) {
            if (source.charAt(endIndex - 1) == delimiter) {
                endIndex--;
            } else {
                stripping = false;
            }
        }

        return source.substring(beginIndex, endIndex);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private StringUtils() {
    }

}
