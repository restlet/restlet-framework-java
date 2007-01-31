/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

/**
 * Preference manipulation utilities.<br/>
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class PreferenceUtils {
    /**
     * Formats a list of preferences with a comma separator.
     * 
     * @param prefs
     *            The list of preferences.
     * @return The formatted list of preferences.
     * @throws IOException
     */
    public static String format(List<? extends Preference> prefs)
            throws IOException {
        StringBuilder sb = new StringBuilder();

        Preference pref;
        for (int i = 0; i < prefs.size(); i++) {
            if (i > 0)
                sb.append(", ");
            pref = prefs.get(i);
            format(pref, sb);
        }

        return sb.toString();
    }

    /**
     * Formats a preference.
     * 
     * @param pref
     *            The preference to format.
     * @param destination
     *            The appendable destination.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static void format(Preference pref, Appendable destination)
            throws IOException {
        destination.append(pref.getMetadata().getName());

        if (pref.getMetadata() instanceof MediaType) {
            MediaType mediaType = (MediaType) pref.getMetadata();

            if (mediaType.getParameters() != null) {
                Parameter param;
                for (Iterator<Parameter> iter = mediaType.getParameters()
                        .iterator(); iter.hasNext();) {
                    param = iter.next();

                    if (param.getName() != null) {
                        destination.append(';').append(param.getName());

                        if ((param.getValue() != null)
                                && (param.getValue().length() > 0)) {
                            destination.append('=').append(param.getValue());
                        }
                    }
                }
            }
        }

        if (pref.getQuality() < 1F) {
            destination.append(";q=");
            formatQuality(pref.getQuality(), destination);
        }

        if (pref.getParameters() != null) {
            Parameter param;
            for (Iterator<Parameter> iter = pref.getParameters().iterator(); iter
                    .hasNext();) {
                param = iter.next();

                if (param.getName() != null) {
                    destination.append(';').append(param.getName());

                    if ((param.getValue() != null)
                            && (param.getValue().length() > 0)) {
                        destination.append('=').append(param.getValue());
                    }
                }
            }
        }
    }

    /**
     * Formats a quality value.<br/> If the quality is invalid, an
     * IllegalArgumentException is thrown.
     * 
     * @param quality
     *            The quality value as a float.
     * @param destination
     *            The appendable destination;
     * @throws IOException
     */
    public static void formatQuality(float quality, Appendable destination)
            throws IOException {
        if (!isQuality(quality)) {
            throw new IllegalArgumentException(
                    "Invalid quality value detected. Value must be between 0 and 1.");
        } else {
            NumberFormat formatter = DecimalFormat.getNumberInstance(Locale.US);
            formatter.setMaximumFractionDigits(2);
            destination.append(formatter.format(quality));
        }
    }

    /**
     * Parses a quality value.<br/> If the quality is invalid, an
     * IllegalArgumentException is thrown.
     * 
     * @param quality
     *            The quality value as a string.
     * @return The parsed quality value as a float.
     */
    public static float parseQuality(String quality) {
        try {
            float result = Float.valueOf(quality);

            if (isQuality(result)) {
                return result;
            } else {
                throw new IllegalArgumentException(
                        "Invalid quality value detected. Value must be between 0 and 1.");
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                    "Invalid quality value detected. Value must be between 0 and 1.");
        }
    }

    /**
     * Indicates if the quality value is valid.
     * 
     * @param quality
     *            The quality value.
     * @return True if the quality value is valid.
     */
    public static boolean isQuality(float quality) {
        return (quality >= 0F) && (quality <= 1F);
    }

    /**
     * Parses character set preferences from a header.
     * 
     * @param acceptCharsetHeader
     *            The header to parse.
     * @param client
     *            The client preferences to update.
     */
    @SuppressWarnings("unchecked")
    public static void parseCharacterSets(String acceptCharsetHeader,
            ClientInfo client) {
        if (acceptCharsetHeader != null) {
            // Implementation according to
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.2
            if (acceptCharsetHeader.length() == 0) {
                client.getAcceptedCharacterSets().add(
                        new Preference<CharacterSet>(CharacterSet.ISO_8859_1));
            } else {
                try {
                    PreferenceReader pr = new PreferenceReader(
                            PreferenceReader.TYPE_CHARACTER_SET,
                            acceptCharsetHeader);
                    Preference currentPref = pr.readPreference();
                    while (currentPref != null) {
                        client.getAcceptedCharacterSets().add(currentPref);
                        currentPref = pr.readPreference();
                    }
                } catch (IOException ioe) {
                    throw new IllegalArgumentException(
                            "An exception occurred during character set preferences parsing. Header: "
                                    + acceptCharsetHeader
                                    + ". Ignoring header.");
                }
            }
        } else {
            client.getAcceptedCharacterSets().add(
                    new Preference(CharacterSet.ALL));
        }
    }

    /**
     * Parses encoding preferences from a header.
     * 
     * @param acceptEncodingHeader
     *            The header to parse.
     * @param preference
     *            The client preferences to update.
     */
    @SuppressWarnings("unchecked")
    public static void parseEncodings(String acceptEncodingHeader,
            ClientInfo preference) {
        if (acceptEncodingHeader != null) {
            try {
                PreferenceReader pr = new PreferenceReader(
                        PreferenceReader.TYPE_ENCODING, acceptEncodingHeader);
                Preference currentPref = pr.readPreference();
                while (currentPref != null) {
                    preference.getAcceptedEncodings().add(currentPref);
                    currentPref = pr.readPreference();
                }
            } catch (IOException ioe) {
                throw new IllegalArgumentException(
                        "An exception occurred during encoding preferences parsing. Header: "
                                + acceptEncodingHeader + ". Ignoring header.");
            }
        } else {
            preference.getAcceptedEncodings().add(
                    new Preference(Encoding.IDENTITY));
        }
    }

    /**
     * Parses language preferences from a header.
     * 
     * @param acceptLanguageHeader
     *            The header to parse.
     * @param preference
     *            The client preferences to update.
     */
    @SuppressWarnings("unchecked")
    public static void parseLanguages(String acceptLanguageHeader,
            ClientInfo preference) {
        if (acceptLanguageHeader != null) {
            try {
                PreferenceReader pr = new PreferenceReader(
                        PreferenceReader.TYPE_LANGUAGE, acceptLanguageHeader);
                Preference currentPref = pr.readPreference();
                while (currentPref != null) {
                    preference.getAcceptedLanguages().add(currentPref);
                    currentPref = pr.readPreference();
                }
            } catch (IOException ioe) {
                throw new IllegalArgumentException(
                        "An exception occurred during language preferences parsing. Header: "
                                + acceptLanguageHeader + ". Ignoring header.");
            }
        } else {
            preference.getAcceptedLanguages().add(new Preference(Language.ALL));
        }
    }

    /**
     * Parses media type preferences from a header.
     * 
     * @param acceptMediaTypeHeader
     *            The header to parse.
     * @param preference
     *            The client preferences to update.
     */
    @SuppressWarnings("unchecked")
    public static void parseMediaTypes(String acceptMediaTypeHeader,
            ClientInfo preference) {
        if (acceptMediaTypeHeader != null) {
            try {
                PreferenceReader pr = new PreferenceReader(
                        PreferenceReader.TYPE_MEDIA_TYPE, acceptMediaTypeHeader);
                Preference currentPref = pr.readPreference();
                while (currentPref != null) {
                    preference.getAcceptedMediaTypes().add(currentPref);
                    currentPref = pr.readPreference();
                }
            } catch (IOException ioe) {
                throw new IllegalArgumentException(
                        "An exception occurred during media type preferences parsing. Header: "
                                + acceptMediaTypeHeader + ". Ignoring header.");
            }
        } else {
            preference.getAcceptedMediaTypes().add(
                    new Preference(MediaType.ALL));
        }
    }

}
