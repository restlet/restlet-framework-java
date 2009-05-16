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

package org.restlet.engine.local;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Request;
import org.restlet.engine.ClientHelper;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

/**
 * Connector to the local resources accessible via file system, class loaders
 * and similar mechanisms. Here is the list of parameters that are supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>timeToLive</td>
 * <td>int</td>
 * <td>600</td>
 * <td>Time to live for a representation before it expires (in seconds). If you
 * set the value to '0', the representation will never expire.</td>
 * </tr>
 * <tr>
 * <td>defaultLanguage</td>
 * <td>String</td>
 * <td></td>
 * <td>When no metadata service is available (simple client connector with no
 * parent application), falls back on this default language. To indicate that no
 * default language should be set, "*" or "" can be used.</td>
 * </tr>
 * </table>
 * 
 * @see org.restlet.data.LocalReference
 * @author Jerome Louvel
 * @author Thierry Boileau
 */
public class LocalClientHelper extends ClientHelper {
    /**
     * Constructor. Note that the common list of metadata associations based on
     * extensions is added, see the addCommonExtensions() method.
     * 
     * @param client
     *            The client to help.
     */
    public LocalClientHelper(Client client) {
        super(client);
    }

    /**
     * Returns the default language. When no metadata service is available
     * (simple client connector with no parent application), falls back on this
     * default language.
     * 
     * @return The default language.
     */
    public String getDefaultLanguage() {
        return getHelpedParameters().getFirstValue("defaultLanguage", "");
    }

    /**
     * Returns the metadata service associated to a request.
     * 
     * @param request
     *            The request to lookup.
     * @return The metadata service associated to a request.
     */
    public MetadataService getMetadataService(Request request) {
        MetadataService result = null;
        final Application application = Application.getCurrent();

        if (application != null) {
            result = application.getMetadataService();
        } else {
            result = new MetadataService();
            result.setDefaultLanguage(Language.valueOf(getDefaultLanguage()));
        }

        return result;
    }

    /**
     * Returns the time to live for a file representation before it expires (in
     * seconds).
     * 
     * @return The time to live for a file representation before it expires (in
     *         seconds).
     */
    public int getTimeToLive() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "timeToLive", "600"));
    }

    /**
     * Updates some variant metadata based on a given entry name with
     * extensions.
     * 
     * @param metadataService
     *            The parent metadata service.
     * @param entryName
     *            The entry name with extensions.
     * @param variant
     *            The variant to update.
     */
    public void updateMetadata(MetadataService metadataService,
            String entryName, Variant variant) {
        if (variant != null) {
            final String[] tokens = entryName.split("\\.");
            Metadata current;

            // We found a potential variant
            for (int j = 1; j < tokens.length; j++) {
                current = metadataService.getMetadata(tokens[j]);
                if (current != null) {
                    // Metadata extension detected
                    if (current instanceof MediaType) {
                        variant.setMediaType((MediaType) current);
                    } else if (current instanceof CharacterSet) {
                        variant.setCharacterSet((CharacterSet) current);
                    } else if (current instanceof Encoding) {
                        variant.getEncodings().add((Encoding) current);
                    } else if (current instanceof Language) {
                        variant.getLanguages().add((Language) current);
                    }
                }

                final int dashIndex = tokens[j].indexOf('-');
                if (dashIndex != -1) {
                    // We found a language extension with a region area
                    // specified.
                    // Try to find a language matching the primary part of the
                    // extension.
                    final String primaryPart = tokens[j]
                            .substring(0, dashIndex);
                    current = metadataService.getMetadata(primaryPart);
                    if (current instanceof Language) {
                        variant.getLanguages().add((Language) current);
                    }
                }
            }

            // If no language is defined, take the default one
            if (variant.getLanguages().isEmpty()) {
                final Language defaultLanguage = metadataService
                        .getDefaultLanguage();

                if ((defaultLanguage != null)
                        && !defaultLanguage.equals(Language.ALL)) {
                    variant.getLanguages().add(defaultLanguage);
                }
            }

            // If no media type is defined, take the default one
            if (variant.getMediaType() == null) {
                final MediaType defaultMediaType = metadataService
                        .getDefaultMediaType();

                if ((defaultMediaType != null)
                        && !defaultMediaType.equals(MediaType.ALL)) {
                    variant.setMediaType(defaultMediaType);
                }
            }
        }
    }
}
