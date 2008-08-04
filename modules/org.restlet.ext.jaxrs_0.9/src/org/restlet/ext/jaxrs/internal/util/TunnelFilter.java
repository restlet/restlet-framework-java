/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jaxrs.internal.util;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.service.MetadataService;
import org.restlet.service.TunnelService;

/**
 * Filter tunnelling browser calls into full REST calls. The request method can
 * be changed (via POST requests only) as well as the accepted media types,
 * languages, encodings and character sets.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @author Stephan Koops
 */
public class TunnelFilter extends Filter {

    /**
     * Constructor.
     * 
     * @param context
     *            The parent context.
     */
    public TunnelFilter(Context context) {
        super(context);
    }

    @Override
    public int beforeHandle(Request request, Response response) {
        processExtensions(request);
        return CONTINUE;
    }

    /**
     * Returns the metadata associated to the given extension using the
     * {@link MetadataService}.
     * 
     * @param extension
     *            The extension to lookup.
     * @return The matched metadata.
     */
    private Metadata getMetadata(String extension) {
        return getMetadataService().getMetadata(extension);
    }

    /**
     * Returns the metadata service of the parent application.
     * 
     * @return The metadata service of the parent application.
     */
    public MetadataService getMetadataService() {
        return getApplication().getMetadataService();
    }

    /**
     * Returns the tunnel service of the parent application.
     * 
     * @return The tunnel service of the parent application.
     */
    public TunnelService getTunnelService() {
        return getApplication().getTunnelService();
    }

    /**
     * Updates the client preferences based on file-like extensions. The matched
     * extensions are removed from the last segment.
     * 
     * See also section 3.6.1 of JAX-RS specification (<a
     * href="https://jsr311.dev.java.net">https://jsr311.dev.java.net</a>)
     * 
     * @param request
     *            The request to update.
     * @return True if the query has been updated, false otherwise.
     */
    private boolean processExtensions(Request request) {
        final TunnelService tunnelService = getTunnelService();
        boolean extensionsModified = false;

        // Tunnel the client preferences for all methods
        if (tunnelService.isPreferencesTunnel()) {
            // in JAX-RS this is not only for GET; it is also required for
            // responses to other methods.
            final Reference resourceRef = request.getResourceRef();

            if (resourceRef.hasExtensions()) {
                final ClientInfo clientInfo = request.getClientInfo();
                boolean encodingFound = false;
                boolean characterSetFound = false;
                boolean mediaTypeFound = false;
                boolean languageFound = false;
                String extensions = resourceRef.getExtensions();

                for (;;) {
                    final int lastIndexOfPoint = extensions.lastIndexOf('.');
                    final String extension = extensions
                            .substring(lastIndexOfPoint + 1);
                    final Metadata metadata = getMetadata(extension);

                    if (!mediaTypeFound && (metadata instanceof MediaType)) {
                        updateMetadata(clientInfo, metadata);
                        mediaTypeFound = true;
                    } else if (!languageFound && (metadata instanceof Language)) {
                        updateMetadata(clientInfo, metadata);
                        languageFound = true;
                    } else if (!characterSetFound
                            && (metadata instanceof CharacterSet)) {
                        updateMetadata(clientInfo, metadata);
                        characterSetFound = true;
                    } else if (!encodingFound && (metadata instanceof Encoding)) {
                        updateMetadata(clientInfo, metadata);
                        encodingFound = true;
                    } else {
                        // extension do not match -> break loop
                        break;
                    }
                    if (lastIndexOfPoint > 0) {
                        extensions = extensions.substring(0, lastIndexOfPoint);
                    } else {
                        extensions = "";
                        break;
                    }
                }

                // Update the extensions if necessary
                if (mediaTypeFound || languageFound || encodingFound
                        || characterSetFound) {
                    resourceRef.setExtensions(extensions);
                    extensionsModified = true;
                }
            }
        }

        return extensionsModified;
    }

    /**
     * Updates the client info with the given metadata. It clears exisiting
     * preferences for the same type of metadata if necessary.
     * 
     * @param clientInfo
     *            The client info to update.
     * @param metadata
     *            The metadata to use.
     */
    private void updateMetadata(ClientInfo clientInfo, Metadata metadata) {
        if (metadata != null) {
            if (metadata instanceof CharacterSet) {
                clientInfo.getAcceptedCharacterSets().clear();
                clientInfo.getAcceptedCharacterSets().add(
                        new Preference<CharacterSet>((CharacterSet) metadata));
            } else if (metadata instanceof Encoding) {
                clientInfo.getAcceptedEncodings().clear();
                clientInfo.getAcceptedEncodings().add(
                        new Preference<Encoding>((Encoding) metadata));
            } else if (metadata instanceof Language) {
                clientInfo.getAcceptedLanguages().clear();
                clientInfo.getAcceptedLanguages().add(
                        new Preference<Language>((Language) metadata));
            } else if (metadata instanceof MediaType) {
                clientInfo.getAcceptedMediaTypes().clear();
                clientInfo.getAcceptedMediaTypes().add(
                        new Preference<MediaType>((MediaType) metadata));
            }
        }
    }
}