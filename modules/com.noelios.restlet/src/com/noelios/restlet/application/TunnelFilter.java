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

import java.util.List;

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
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.service.MetadataService;
import org.restlet.service.TunnelService;
import org.restlet.util.Series;

import com.noelios.restlet.http.HttpConstants;

/**
 * Filter tunnelling browser calls into full REST calls. The request method can
 * be changed (via POST requests only) as well as the accepted media types,
 * languages, encodings and character sets.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TunnelFilter extends Filter {

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

    @Override
    public int beforeHandle(Request request, Response response) {
        Reference originalRef = new Reference(request.getResourceRef()
                .toString());
        boolean queryModified = false;
        boolean extensionsModified = false;

        if (getTunnelService().isQueryTunnel()) {
            queryModified = processQuery(request);
        }

        if (getTunnelService().isExtensionsTunnel()) {
            extensionsModified = processExtensions(request);
        }

        if (getTunnelService().isUserAgentTunnel()) {
            processUserAgent(request);
        }

        if (queryModified || extensionsModified) {
            request.getAttributes().put(TunnelService.ATTRIBUTE_ORIGINAL_REF,
                    originalRef);
        }

        return CONTINUE;
    }

    /**
     * Returns the application.
     * 
     * @return The application.
     */
    public Application getApplication() {
        return this.application;
    }

    /**
     * Returns the metadata associated to the given extension using the
     * {@link MetadataService}.
     * 
     * @param extension
     *                The extension to lookup.
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
     *                The request to update.
     * @return True if the query has been updated, false otherwise.
     */
    private boolean processExtensions(Request request) {
        TunnelService tunnelService = getTunnelService();
        boolean extensionsModified = false;

        // Tunnel the client preferences only for GET or HEAD requests
        Method method = request.getMethod();
        if (tunnelService.isPreferencesTunnel()
                && (method.equals(Method.GET) || method.equals(Method.HEAD))) {
            Reference resourceRef = request.getResourceRef();

            if (resourceRef.hasExtensions()) {
                ClientInfo clientInfo = request.getClientInfo();
                boolean encodingFound = false;
                boolean characterSetFound = false;
                boolean mediaTypeFound = false;
                boolean languageFound = false;
                String extensions = resourceRef.getExtensions();

                // Discover extensions from right to left and stop at the first
                // unknown extension. Only one extension per type of metadata is
                // also allowed: i.e. one language, one mediatype, one encoding,
                // one character set.
                while (true) {
                    int lastIndexOfPoint = extensions.lastIndexOf('.');
                    String extension = extensions
                            .substring(lastIndexOfPoint + 1);
                    Metadata metadata = getMetadata(extension);

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
                        // no more extensions -> break loop
                        extensions = "";
                        break;
                    }
                }

                // Update the extensions if necessary
                if (encodingFound || characterSetFound || mediaTypeFound
                        || languageFound) {
                    resourceRef.setExtensions(extensions);
                    extensionsModified = true;
                }
            }
        }

        return extensionsModified;
    }

    /**
     * Updates the request method and client preferences based on query
     * parameters. The matched parameters are removed from the query.
     * 
     * @param request
     *                The request to update.
     * @return True if the query has been updated, false otherwise.
     */
    private boolean processQuery(Request request) {
        TunnelService tunnelService = getTunnelService();
        boolean queryModified = false;
        Reference resourceRef = request.getResourceRef();

        if (resourceRef.hasQuery()) {
            Form query = resourceRef.getQueryAsForm(null);

            // Tunnel the request method
            Method method = request.getMethod();
            if (tunnelService.isMethodTunnel() && method.equals(Method.POST)) {
                String methodName = query.getFirstValue(tunnelService
                        .getMethodParameter());

                if (methodName != null) {
                    request.setMethod(Method.valueOf(methodName));
                    query.removeFirst(tunnelService.getMethodParameter());
                    queryModified = true;
                }
            }

            // Tunnel the client preferences
            if (tunnelService.isPreferencesTunnel()) {
                // Get the parameter names to look for
                String charSetParameter = tunnelService
                        .getCharacterSetParameter();
                String encodingParameter = tunnelService.getEncodingParameter();
                String languageParameter = tunnelService.getLanguageParameter();
                String mediaTypeParameter = tunnelService
                        .getMediaTypeParameter();

                // Get the preferences from the query
                String acceptedCharSet = query.getFirstValue(charSetParameter);
                String acceptedEncoding = query
                        .getFirstValue(encodingParameter);
                String acceptedLanguage = query
                        .getFirstValue(languageParameter);
                String acceptedMediaType = query
                        .getFirstValue(mediaTypeParameter);

                // Updates the client preferences
                ClientInfo clientInfo = request.getClientInfo();
                Metadata metadata = getMetadata(acceptedCharSet);
                if (metadata instanceof CharacterSet) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(charSetParameter);
                }

                metadata = getMetadata(acceptedEncoding);
                if (metadata instanceof Encoding) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(encodingParameter);
                }

                metadata = getMetadata(acceptedLanguage);
                if (metadata instanceof Language) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(languageParameter);
                }

                metadata = getMetadata(acceptedMediaType);
                if (metadata instanceof MediaType) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(mediaTypeParameter);
                }
            }

            // Update the query if it has been modified
            if (queryModified) {
                request.getResourceRef().setQuery(query.getQueryString(null));
            }
        }

        return queryModified;
    }

    /**
     * Updates the client preferences based on user agent type.
     * 
     * @param request
     *                The request to update.
     */
    private void processUserAgent(Request request) {
        if (shouldTweakHtmlPreferences(request)) {
            request.getClientInfo().setAcceptedMediaTypes(
                    tweakHtmlPreference(request.getClientInfo()
                            .getAcceptedMediaTypes()));
        }
    }

    /**
     * Returns true if the user agent is suspected to declare a higher
     * preference for media types other than HTML.
     * 
     * @return True if the user agent is suspected to declare a higher
     *         preference for media types other than HTML.
     */
    protected boolean shouldTweakHtmlPreferences(Request request) {
        boolean result = false;

        // Have XML mediatypes higher preference than HTML ones?
        // Does the user agent have a preference for "*/*"?
        boolean shouldTweak = false;
        boolean allMediaTypesPreference = false;
        boolean htmlFound = false;
        boolean xmlFound = false;
        List<Preference<MediaType>> preferences = request.getClientInfo()
                .getAcceptedMediaTypes();
        for (Preference<MediaType> preference : preferences) {
            MediaType mediaType = preference.getMetadata();

            if (MediaType.ALL.equals(mediaType)) {
                allMediaTypesPreference = true;
                break;
            }

            if (!htmlFound && mediaType.getName().toLowerCase().contains("htm")) {
                // We have found html media type.
                htmlFound = true;
            } else if (htmlFound && !xmlFound) {
                // Have we found xml media type?
                xmlFound = mediaType.getName().toLowerCase().contains("xml");
            }
        }
        shouldTweak = xmlFound && htmlFound;

        if (shouldTweak || allMediaTypesPreference) {
            // Ajax Clients does not need to tweak preferences.
            Series<Parameter> requestHeaders = (Series<Parameter>) request
                    .getAttributes().get(HttpConstants.ATTRIBUTE_HEADERS);
            Parameter parameter = requestHeaders.getFirst("X-Request-With",
                    true);
            boolean ajaxClient = parameter != null
                    && ("HTTP.Request".equalsIgnoreCase(parameter.getName()) || "XMLHttpRequest"
                            .equalsIgnoreCase(parameter.getName()));

            if (!ajaxClient) {
                result = allMediaTypesPreference;
            }
        }

        return result;
    }

    /**
     * Alter the client's preferences in order to give HTML media types a higher
     * preference.
     * 
     * @param preferences
     *                The list of client's preferences.
     * @return the updated list of preferences.
     */
    protected List<Preference<MediaType>> tweakHtmlPreference(
            List<Preference<MediaType>> preferences) {
        preferences.add(0, new Preference<MediaType>(MediaType.TEXT_HTML));
        preferences.add(0, new Preference<MediaType>(
                MediaType.APPLICATION_XHTML_XML));

        return preferences;
    }

    /**
     * Updates the client info with the given metadata. It clears exisiting
     * preferences for the same type of metadata if necessary.
     * 
     * @param clientInfo
     *                The client info to update.
     * @param metadata
     *                The metadata to use.
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
