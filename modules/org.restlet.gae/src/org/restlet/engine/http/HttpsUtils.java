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

package org.restlet.engine.http;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.engine.Helper;

/**
 * Various HTTPS utilities.
 * 
 * @author Jerome Louvel
 */
public class HttpsUtils {

    /** Cache of SSL key sizes for various cipher suites. */
    private final static ConcurrentMap<String, Integer> keySizesCache = new ConcurrentHashMap<String, Integer>();

    /**
     * Extract the SSL key size of a given cipher suite.
     * 
     * @param sslCipherSuite
     *            The SSL cipher suite.
     * @return The SSL key size.
     */
    public static Integer extractKeySize(String sslCipherSuite) {
        Integer keySize = keySizesCache.get(sslCipherSuite);

        if (keySize == null) {
            final int encAlgorithmIndex = sslCipherSuite.indexOf("WITH_");
            if (encAlgorithmIndex >= 0) {
                final String encAlgorithm = sslCipherSuite
                        .substring(encAlgorithmIndex + 5);

                /*
                 * (Encryption algorithms and key sizes, quoted from RFC 2246)
                 * 
                 * Key Expanded Effective IV Block Cipher Type Material Key
                 * Material Key Bits Size Size
                 * 
                 * NULL Stream 0 0 0 0 N/A IDEA_CBC Block 16 16 128 8 8
                 * RC2_CBC_40 Block 5 16 40 8 8 RC4_40 Stream 5 16 40 0 N/A
                 * RC4_128 Stream 16 16 128 0 N/A DES40_CBC Block 5 8 40 8 8
                 * DES_CBC Block 8 8 56 8 8 3DES_EDE_CBC Block 24 24 168 8 8
                 */
                if (encAlgorithm != null) {
                    if (encAlgorithm.startsWith("NULL_")) {
                        keySize = Integer.valueOf(0);
                    } else if (encAlgorithm.startsWith("IDEA_CBC_")) {
                        keySize = Integer.valueOf(128);
                    } else if (encAlgorithm.startsWith("RC2_CBC_40_")) {
                        keySize = Integer.valueOf(40);
                    } else if (encAlgorithm.startsWith("RC4_40_")) {
                        keySize = Integer.valueOf(40);
                    } else if (encAlgorithm.startsWith("RC4_128_")) {
                        keySize = Integer.valueOf(128);
                    } else if (encAlgorithm.startsWith("DES40_CBC_")) {
                        keySize = Integer.valueOf(40);
                    } else if (encAlgorithm.startsWith("DES_CBC_")) {
                        keySize = Integer.valueOf(56);
                    } else if (encAlgorithm.startsWith("3DES_EDE_CBC_")) {
                        keySize = Integer.valueOf(168);
                    } else {
                        final StringTokenizer st = new StringTokenizer(
                                encAlgorithm, "_");

                        while (st.hasMoreTokens()) {
                            try {
                                keySize = Integer.valueOf(st.nextToken());
                                break;
                            } catch (NumberFormatException e) {
                                // Tokens that are not integers are ignored.
                            }
                        }
                    }

                    if (keySize != null) {
                        keySizesCache.put(sslCipherSuite, keySize);
                    }
                }
            }
        }

        return keySize;
    }

    /**
     * Returns the list of disabled cipher suites.
     * 
     * @param helper
     *            The helper to use.
     * @return The list of disabled cipher suites.
     */
    public static String[] getDisabledCipherSuites(Helper<?> helper) {
        List<String> disabledCipherSuites = new ArrayList<String>();
        String[] disabledCipherSuitesParams = helper.getHelpedParameters()
                .getValuesArray("disabledCipherSuites");
        for (String disabledCipherSuitesParam : disabledCipherSuitesParams) {
            StringTokenizer st = new StringTokenizer(disabledCipherSuitesParam);
            while (st.hasMoreElements()) {
                disabledCipherSuites.add(st.nextToken());
            }
        }
        return disabledCipherSuites.size() > 0 ? disabledCipherSuites
                .toArray(new String[0]) : null;
    }

    /**
     * Returns the list of enabled cipher suites.
     * 
     * @param helper
     *            The helper to use.
     * @return The list of enabled cipher suites.
     */
    public static String[] getEnabledCipherSuites(Helper<?> helper) {
        List<String> enabledCipherSuites = new ArrayList<String>();
        String[] enabledCipherSuitesParams = helper.getHelpedParameters()
                .getValuesArray("enabledCipherSuites");
        for (String enabledCipherSuitesParam : enabledCipherSuitesParams) {
            StringTokenizer st = new StringTokenizer(enabledCipherSuitesParam);
            while (st.hasMoreElements()) {
                enabledCipherSuites.add(st.nextToken());
            }
        }
        return enabledCipherSuites.size() > 0 ? enabledCipherSuites
                .toArray(new String[0]) : null;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private HttpsUtils() {
    }
}
