/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.ssl;

import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.RestletHelper;

/**
 * Various HTTPS utilities.
 * 
 * @author Jerome Louvel
 */
public class SslUtils {

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
                final String encAlgorithm = sslCipherSuite.substring(encAlgorithmIndex + 5);

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

    // [ifndef gae] method
    /**
     * Returns the SSL context factory. It first look for a "sslContextFactory"
     * attribute (instance), then for a "sslContextFactory" parameter (class
     * name to instantiate).
     * 
     * @param helper
     *            The helper to use.
     * 
     * @return The SSL context factory.
     */
    public static SslContextFactory getSslContextFactory(RestletHelper<?> helper) {
        SslContextFactory result = (SslContextFactory) ((helper.getContext() == null) ? null
                : helper.getContext().getAttributes().get("sslContextFactory"));

        if (result == null) {
            String[] sslContextFactoryNames = helper.getHelpedParameters()
                    .getValuesArray("sslContextFactory");

            if (sslContextFactoryNames != null) {
                for (String sslContextFactoryName : sslContextFactoryNames) {
                    if ((result == null) && (sslContextFactoryName != null)) {
                        try {
                            Class<? extends SslContextFactory> sslContextFactoryClass = Class
                                    .forName(sslContextFactoryName).asSubclass(
                                            SslContextFactory.class);
                            result = sslContextFactoryClass.newInstance();
                            result.init(helper.getHelpedParameters());
                        } catch (ClassNotFoundException e) {
                            Context.getCurrentLogger().log(
                                    Level.WARNING,
                                    "Unable to find SslContextFactory class: "
                                            + sslContextFactoryName, e);
                        } catch (ClassCastException e) {
                            Context.getCurrentLogger()
                                    .log(Level.WARNING,
                                            "Class "
                                                    + sslContextFactoryName
                                                    + " does not implement SslContextFactory.",
                                            e);
                        } catch (InstantiationException e) {
                            Context.getCurrentLogger().log(
                                    Level.WARNING,
                                    "Could not instantiate class "
                                            + sslContextFactoryName
                                            + " with default constructor.", e);
                        } catch (IllegalAccessException e) {
                            Context.getCurrentLogger().log(
                                    Level.WARNING,
                                    "Illegal access when instantiating class "
                                            + sslContextFactoryName + ".", e);
                        }
                    }
                }
            }
        }

        if (result == null) {
            result = new DefaultSslContextFactory();
            result.init(helper.getHelpedParameters());
        }

        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private SslUtils() {
    }
}
