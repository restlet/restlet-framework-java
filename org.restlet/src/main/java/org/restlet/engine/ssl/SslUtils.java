/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.ssl;

import java.lang.reflect.InvocationTargetException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.data.Parameter;
import org.restlet.engine.RestletHelper;
import org.restlet.util.Series;

/**
 * Various HTTPS utilities.
 *
 * @author Jerome Louvel
 */
public class SslUtils {

    /** Cache of SSL key sizes for various cipher suites. */
    private static final ConcurrentMap<String, Integer> keySizesCache = new ConcurrentHashMap<>();

    /**
     * Extract the SSL key size of a given cipher suite.
     *
     * @param sslCipherSuite The SSL cipher suite.
     * @return The SSL key size.
     */
    public static Integer extractKeySize(String sslCipherSuite) {
        Integer keySize = keySizesCache.get(sslCipherSuite);

        if (keySize == null) {
            final int encAlgorithmIndex = sslCipherSuite.indexOf("WITH_");
            if (encAlgorithmIndex >= 0) {
                keySize = getKeySize(sslCipherSuite, encAlgorithmIndex);

                if (keySize != null) {
                    keySizesCache.put(sslCipherSuite, keySize);
                }
            }
        }

        return keySize;
    }

    /*
     * (Encryption algorithms and key sizes, quoted from RFC 2246)
     *
     * Key Expanded Effective IV Block Cipher Type Material Key Material Key Bits
     * Size Size
     *
     * NULL Stream 0 0 0 0 N/A IDEA_CBC Block 16 16 128 8 8 RC2_CBC_40 Block 5 16 40
     * 8 8 RC4_40 Stream 5 16 40 0 N/A RC4_128 Stream 16 16 128 0 N/A DES40_CBC
     * Block 5 8 40 8 8 DES_CBC Block 8 8 56 8 8 3DES_EDE_CBC Block 24 24 168 8 8
     */
    private static Integer getKeySize(final String sslCipherSuite, final int encAlgorithmIndex) {
        Integer keySize = null;
        final String encAlgorithm = sslCipherSuite.substring(encAlgorithmIndex + 5);

        if (encAlgorithm.startsWith("NULL_")) {
            keySize = 0;
        } else if (encAlgorithm.startsWith("IDEA_CBC_")) {
            keySize = 128;
        } else if (encAlgorithm.startsWith("RC2_CBC_40_")) {
            keySize = 40;
        } else if (encAlgorithm.startsWith("RC4_40_")) {
            keySize = 40;
        } else if (encAlgorithm.startsWith("RC4_128_")) {
            keySize = 128;
        } else if (encAlgorithm.startsWith("DES40_CBC_")) {
            keySize = 40;
        } else if (encAlgorithm.startsWith("DES_CBC_")) {
            keySize = 56;
        } else if (encAlgorithm.startsWith("3DES_EDE_CBC_")) {
            keySize = 168;
        } else {
            final StringTokenizer st = new StringTokenizer(encAlgorithm, "_");

            while (st.hasMoreTokens()) {
                try {
                    keySize = Integer.valueOf(st.nextToken());
                    break;
                } catch (NumberFormatException ignored) {
                    // Tokens that are not integers are ignored.
                }
            }
        }
        return keySize;
    }

    /**
     * Returns the SSL context factory. It first looks for a "sslContextFactory" attribute
     * (instance), then for a "sslContextFactory" parameter (class name to instantiate).
     *
     * @param helper The helper to use.
     * @return The SSL context factory.
     */
    public static SslContextFactory getSslContextFactory(RestletHelper<?> helper) {

        SslContextFactory result = getSslContextFactoryFromContext(helper.getContext());

        if (result == null) {
            final Series<Parameter> helpedParameters = helper.getHelpedParameters();
            result = getSslContextFactoryFromHelpedParameters(helpedParameters);
        }

        if (result == null) {
            result = getDefaultSslContextFactory(helper);
        }

        return result;
    }

    private static SslContextFactory getDefaultSslContextFactory(final RestletHelper<?> helper) {
        SslContextFactory result;
        result = new DefaultSslContextFactory();
        result.init(helper.getHelpedParameters());
        return result;
    }

    private static SslContextFactory getSslContextFactoryFromHelpedParameters(
            final Series<Parameter> helpedParameters) {

        String[] sslContextFactoryNames = helpedParameters.getValuesArray("sslContextFactory");
        if (sslContextFactoryNames == null) {
            return null;
        }

        SslContextFactory result = null;
        for (String sslContextFactoryName : sslContextFactoryNames) {
            if (result == null && sslContextFactoryName != null) {
                result = getSslContextFactoryByClassName(helpedParameters, sslContextFactoryName);
            }
        }
        return result;
    }

    private static SslContextFactory getSslContextFactoryByClassName(
            final Series<Parameter> helpedParameters, final String sslContextFactoryName) {
        try {
            Class<? extends SslContextFactory> sslContextFactoryClass =
                    Class.forName(sslContextFactoryName).asSubclass(SslContextFactory.class);
            SslContextFactory result =
                    sslContextFactoryClass.getDeclaredConstructor().newInstance();
            result.init(helpedParameters);
            return result;
        } catch (ClassNotFoundException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            e,
                            () ->
                                    "Unable to find SslContextFactory class: "
                                            + sslContextFactoryName);
        } catch (ClassCastException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            e,
                            () ->
                                    "Class "
                                            + sslContextFactoryName
                                            + " does not implement SslContextFactory");
        } catch (InstantiationException | NoSuchMethodException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            e,
                            () ->
                                    "Could not instantiate class "
                                            + sslContextFactoryName
                                            + " with default constructor");
        } catch (IllegalAccessException e) {
            Context.getCurrentLogger()
                    .log(
                            Level.WARNING,
                            e,
                            () ->
                                    "Illegal access when instantiating class "
                                            + sslContextFactoryName);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(
                    "Cannot instantiate SslContextFactory class: " + sslContextFactoryName, e);
        }

        return null;
    }

    private static SslContextFactory getSslContextFactoryFromContext(final Context context) {
        return context == null
                ? null
                : (SslContextFactory) context.getAttributes().get("sslContextFactory");
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class i.e., it isn't
     * instantiable and extensible.
     */
    private SslUtils() {}
}
