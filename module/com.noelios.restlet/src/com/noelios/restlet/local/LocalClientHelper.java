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

package com.noelios.restlet.local;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Request;
import org.restlet.resource.Representation;
import org.restlet.service.MetadataService;

import com.noelios.restlet.ClientHelper;

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
 * <td>Time to live for a file representation before it expires (in seconds).</td>
 * </tr>
 * </table>
 * 
 * @see org.restlet.data.LocalReference
 * @author Jerome Louvel (contact@noelios.com)
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
     * Returns the metadata service associated to a request.
     * 
     * @param request
     *            The request to lookup.
     * @return The metadata service associated to a request.
     */
    public MetadataService getMetadataService(Request request) {
        MetadataService result = null;
        Application application = (Application) request.getAttributes().get(
                Application.class.getCanonicalName());

        if (application != null) {
            result = application.getMetadataService();
        } else {
            result = new MetadataService();
        }

        return result;
    }

    /**
     * Updates some representation metadata based on a given entry name with
     * extensions.
     * 
     * @param metadataService
     *            The parent metadata service.
     * @param entryName
     *            The entry name with extensions.
     * @param representation
     *            The representation to update.
     */
    public void updateMetadata(MetadataService metadataService,
            String entryName, Representation representation) {
        if (representation != null) {
            String[] tokens = entryName.split("\\.");
            Metadata current;

            // We found a potential variant
            for (int j = 1; j < tokens.length; j++) {
                current = metadataService.getMetadata(tokens[j]);
                if (current != null) {
                    // Metadata extension detected
                    if (current instanceof MediaType)
                        representation.setMediaType((MediaType) current);
                    if (current instanceof CharacterSet)
                        representation.setCharacterSet((CharacterSet) current);
                    if (current instanceof Encoding)
                        representation.setEncoding((Encoding) current);
                    if (current instanceof Language)
                        representation.setLanguage((Language) current);
                }

                int dashIndex = tokens[j].indexOf('-');
                if (dashIndex != -1) {
                    // We found a language extension with a region area
                    // specified
                    // Try to find a language matching the primary part of the
                    // extension
                    String primaryPart = tokens[j].substring(0, dashIndex);
                    current = metadataService.getMetadata(primaryPart);
                    if (current instanceof Language)
                        representation.setLanguage((Language) current);
                }
            }

            // If no language is defines, take the default language
            if (representation.getLanguage() == null) {
                representation
                        .setLanguage(metadataService.getDefaultLanguage());
            }
        }
    }

    /**
     * Returns the time to live for a file representation before it expires (in
     * seconds).
     * 
     * @return The time to live for a file representation before it expires (in
     *         seconds).
     */
    public int getTimeToLive() {
        return Integer.parseInt(getParameters().getFirstValue("timeToLive",
                "600"));
    }

}
