/*
 * Copyright 2005-2008 Noelios Consulting.
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

package com.noelios.restlet.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.restlet.Application;
import org.restlet.Filter;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
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
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TunnelFilter extends Filter {
    /**
     * Adds the given {@link Metadata} to the preferences {@link List}.<br>
     * You have to ensure that the metadata and the list of preferences fit
     * together.
     * 
     * @param metadata
     *                the preference's metadata.
     * @param quality
     *                the preference's quality.
     * @param preferences
     *                the list of preferences to update.
     * @param clearPrefs
     *                True if the list of preferences need to be cleared.
     */
    @SuppressWarnings("unchecked")
    private static void addPreference(Metadata metadata, float quality,
            List preferences, boolean clearPrefs) {
        if (clearPrefs)
            preferences.clear();
        Preference<Metadata> pref = new Preference<Metadata>(metadata, quality);
        preferences.add(pref);
    }

    /** The application. */
    private volatile Application application;

    /**
     * Constructor.
     * 
     * @param application
     *                The parent application.
     */
    public TunnelFilter(Application application) {
        super(application.getContext());
        this.application = application;
    }

    /**
     * Allows filtering before its handling by the target Restlet. Does nothing
     * by default.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     * @return The continuation status.
     */
    @Override
    public int beforeHandle(Request request, Response response) {
        Form query = request.getResourceRef().getQueryAsForm(null);
        boolean queryModified = false;

        TunnelService tunnelService = getApplication().getTunnelService();
        Method method = request.getMethod();
        if (tunnelService.isMethodTunnel() && method.equals(Method.POST)) {
            // Tunnels the extracted attributes into the proper call objects.
            String methodName = query.getFirstValue(tunnelService
                    .getMethodParameter());

            if (methodName != null) {
                request.setMethod(Method.valueOf(methodName));

                // The parameter is removed from the query
                query.removeFirst(tunnelService.getMethodParameter());
                queryModified = true;
            }
        }

        if (tunnelService.isPreferencesTunnel()) {
            // Tunnels the extracted query parameters into the proper client
            // preferences.
            queryModified = evaluateQueryParameters(request, query,
                    tunnelService);
        }

        if (tunnelService.isExtensionTunnel()) {
            // Tunnels the extracted extensions into the proper client
            // preferences.
            evaluateExtensions(request);
        }

        // Update the query if it has been modified
        if (queryModified) {
            request.getResourceRef().setQuery(query.getQueryString(null));
        }

        return CONTINUE;
    }

    /**
     * Update the client preferences according to the extensions (in the meaning
     * of file extensions) located in the resource URI. If the extension is
     * mapped to a {@link Metadata} in the {@link MetadataService}, then the
     * corresponding accept header is updated and the matched extension is
     * removed.<br>
     * See also section 3.6.1 of JAX-RS specification (<a
     * href="https://jsr311.dev.java.net">https://jsr311.dev.java.net</a>)
     * 
     * @param request
     *                the request to check.
     */
    private void evaluateExtensions(Request request) {
        Reference resourceRef = request.getResourceRef();
        Reference originalRef = resourceRef.clone();
        String path = resourceRef.getPath();

        MetadataService metadataService = getApplication().getMetadataService();

        float mediaTypeQuality = 1;
        float languageQuality = 1;
        float charsetQuality = 1;
        float encodingQuality = 1;

        // Stores the cut extensions.
        StringBuilder cutExts = new StringBuilder();

        int lpsStart = path.lastIndexOf('/') + 1;
        String[] lpss = path.substring(lpsStart).split("\\.");
        List<String> lps = new ArrayList<String>(Arrays.asList(lpss));
        Iterator<String> lpsIter = lps.iterator();
        if (lpsIter.hasNext()) {
            lpsIter.next(); // ignore not-extension-part
            ClientInfo clientInfo = request.getClientInfo();
            while (lpsIter.hasNext()) {
                String extension = lpsIter.next();
                Metadata metadata = metadataService.getMetadata(extension);
                if (metadata instanceof MediaType) {
                    addPreference(metadata, mediaTypeQuality, clientInfo
                            .getAcceptedMediaTypes(), mediaTypeQuality >= 0.99);
                    mediaTypeQuality *= 0.9;
                    lpsIter.remove();
                    cutExts.append('.');
                    cutExts.append(extension);
                } else if (metadata instanceof Language) {
                    addPreference(metadata, languageQuality, clientInfo
                            .getAcceptedLanguages(), languageQuality >= 0.99);
                    languageQuality *= 0.9;
                    lpsIter.remove();
                    cutExts.append('.');
                    cutExts.append(extension);
                } else if (metadata instanceof CharacterSet) {
                    addPreference(metadata, charsetQuality, clientInfo
                            .getAcceptedCharacterSets(), charsetQuality >= 0.99);
                    charsetQuality *= 0.9;
                    lpsIter.remove();
                    cutExts.append('.');
                    cutExts.append(extension);
                } else if (metadata instanceof Encoding) {
                    addPreference(metadata, encodingQuality, clientInfo
                            .getAcceptedEncodings(), encodingQuality >= 0.99);
                    encodingQuality *= 0.9;
                    lpsIter.remove();
                    cutExts.append('.');
                    cutExts.append(extension);
                }
            }
        }

        // Update the path of the resource's Reference.
        StringBuilder newPath = new StringBuilder();
        newPath.append(path, 0, lpsStart);
        lpsIter = lps.iterator();
        if (lpsIter.hasNext()) {
            String pathPart = lpsIter.next();
            newPath.append(pathPart);
            while (lpsIter.hasNext()) {
                String ext = lpsIter.next();
                newPath.append('.');
                newPath.append(ext);
            }
        }
        resourceRef.setPath(newPath.toString());

        Map<String, Object> attributes = request.getAttributes();
        attributes.put(TunnelService.REF_ORIGINAL_KEY, originalRef);
        attributes.put(TunnelService.REF_CUT_KEY, resourceRef);
        attributes.put(TunnelService.REF_EXTENSIONS_KEY, cutExts.toString());
    }

    /**
     * Update the client preferences according to some query parameters. The
     * matched query parameters are removed from the query.
     * 
     * @param request
     *                the request to update.
     * @param query
     *                the query from where the parameters are extracted.
     * @param tunnelService
     *                the TunnelService that defines the names of the query
     *                parameters.
     * @return True if the query has been updated, false otherwise.
     */
    private boolean evaluateQueryParameters(Request request, Form query,
            TunnelService tunnelService) {
        boolean queryModified = false;

        // Extract the header values
        String acceptCharset = query.getFirstValue(tunnelService
                .getCharacterSetParameter());
        String acceptEncoding = query.getFirstValue(tunnelService
                .getEncodingParameter());
        String acceptLanguage = query.getFirstValue(tunnelService
                .getLanguageParameter());
        String acceptMediaType = query.getFirstValue(tunnelService
                .getMediaTypeParameter());

        // Parse the headers and update the call preferences
        Metadata metadata = null;
        ClientInfo clientInfo = request.getClientInfo();
        MetadataService metadataService = getApplication().getMetadataService();
        if (acceptCharset != null) {
            metadata = metadataService.getMetadata(acceptCharset);

            if (metadata instanceof CharacterSet) {
                clientInfo.getAcceptedCharacterSets().clear();
                clientInfo.getAcceptedCharacterSets().add(
                        new Preference<CharacterSet>((CharacterSet) metadata));

                // The parameter is removed from the query
                query.removeFirst(tunnelService.getCharacterSetParameter());
                queryModified = true;
            }
        }

        if (acceptEncoding != null) {
            metadata = metadataService.getMetadata(acceptEncoding);

            if (metadata instanceof Encoding) {
                clientInfo.getAcceptedEncodings().clear();
                clientInfo.getAcceptedEncodings().add(
                        new Preference<Encoding>((Encoding) metadata));

                // The parameter is removed from the query
                query.removeFirst(tunnelService.getEncodingParameter());
                queryModified = true;
            }
        }

        if (acceptLanguage != null) {
            metadata = metadataService.getMetadata(acceptLanguage);

            if (metadata instanceof Language) {
                clientInfo.getAcceptedLanguages().clear();
                clientInfo.getAcceptedLanguages().add(
                        new Preference<Language>((Language) metadata));

                // The parameter is removed from the query
                query.removeFirst(tunnelService.getLanguageParameter());
                queryModified = true;
            }
        }

        if (acceptMediaType != null) {
            metadata = metadataService.getMetadata(acceptMediaType);

            if (metadata instanceof MediaType) {
                clientInfo.getAcceptedMediaTypes().clear();
                clientInfo.getAcceptedMediaTypes().add(
                        new Preference<MediaType>((MediaType) metadata));

                // The parameter is removed from the query
                query.removeFirst(tunnelService.getMediaTypeParameter());
                queryModified = true;
            }
        }

        return queryModified;
    }

    /**
     * Returns the application.
     * 
     * @return The application.
     */
    public Application getApplication() {
        return this.application;
    }
}
