/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.engine.http.header;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;

/**
 * Preference manipulation utilities.<br>
 * 
 * @author Jerome Louvel
 */
public class PreferenceWriter extends HeaderWriter {
    /**
     * Indicates if the quality value is valid.
     * 
     * @param quality
     *            The quality value.
     * @return True if the quality value is valid.
     */
    public static boolean isValidQuality(float quality) {
        return (quality >= 0F) && (quality <= 1F);
    }

    /**
     * Writes a list of preferences with a comma separator.
     * 
     * @param prefs
     *            The list of preferences.
     * @return The formatted list of preferences.
     * @throws IOException
     */
    public static String write(List<? extends Preference<?>> prefs) {
        return new PreferenceWriter().append(prefs).toString();
    }

    /**
     * Formats a quality value.<br>
     * If the quality is invalid, an IllegalArgumentException is thrown.
     * 
     * @param quality
     *            The quality value as a float.
     */
    public void appendQuality(float quality) {
        if (!isValidQuality(quality)) {
            throw new IllegalArgumentException(
                    "Invalid quality value detected. Value must be between 0 and 1.");
        }

        // [ifndef gwt]
        java.text.NumberFormat formatter = java.text.NumberFormat
                .getNumberInstance(java.util.Locale.US);
        formatter.setMaximumFractionDigits(2);
        append(formatter.format(quality));
        // [enddef]
    }

    /**
     * Appends a list of preferences with a comma separator.
     * 
     * @param prefs
     *            The list of preferences.
     * @return This writer.
     */
    public PreferenceWriter append(List<? extends Preference<?>> prefs) {
        Preference<?> pref;

        for (int i = 0; i < prefs.size(); i++) {
            if (i > 0) {
                append(", ");
            }

            pref = prefs.get(i);
            append(pref);
        }

        return this;
    }

    /**
     * Formats a preference.
     * 
     * @param pref
     *            The preference to format.
     * @return This writer.
     */
    @SuppressWarnings("unchecked")
    public PreferenceWriter append(Preference pref) {
        append(pref.getMetadata().getName());

        if (pref.getMetadata() instanceof MediaType) {
            MediaType mediaType = (MediaType) pref.getMetadata();

            if (mediaType.getParameters() != null) {
                Parameter param;

                for (Iterator<Parameter> iter = mediaType.getParameters()
                        .iterator(); iter.hasNext();) {
                    param = iter.next();

                    if (param.getName() != null) {
                        append(';').append(param.getName());

                        if ((param.getValue() != null)
                                && (param.getValue().length() > 0)) {
                            append('=').append(param.getValue());
                        }
                    }
                }
            }
        }

        if (pref.getQuality() < 1F) {
            append(";q=");
            appendQuality(pref.getQuality());
        }

        if (pref.getParameters() != null) {
            Parameter param;

            for (Iterator<Parameter> iter = pref.getParameters().iterator(); iter
                    .hasNext();) {
                param = iter.next();

                if (param.getName() != null) {
                    append(';').append(param.getName());

                    if ((param.getValue() != null)
                            && (param.getValue().length() > 0)) {
                        append('=').append(param.getValue());
                    }
                }
            }
        }

        return this;
    }

}
