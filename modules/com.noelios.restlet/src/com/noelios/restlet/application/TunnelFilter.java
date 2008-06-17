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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;

import org.restlet.Context;
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
import org.restlet.util.Engine;

import com.noelios.restlet.http.HttpConstants;
import com.noelios.restlet.http.PreferenceUtils;

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
 */
public class TunnelFilter extends Filter {

    /**
     * Constructor.
     * 
     * @param context
     *                The parent context.
     */
    public TunnelFilter(Context context) {
        super(context);
    }

    @Override
    public int beforeHandle(Request request, Response response) {
        if (getTunnelService().isUserAgentTunnel()) {
            processUserAgent(request);
        }

        if (getTunnelService().isExtensionsTunnel()) {
            processExtensions(request);
        }

        if (getTunnelService().isQueryTunnel()) {
            processQuery(request);
        }

        return CONTINUE;
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
                    queryModified = true;
                }

                metadata = getMetadata(acceptedEncoding);
                if (metadata instanceof Encoding) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(encodingParameter);
                    queryModified = true;
                }

                metadata = getMetadata(acceptedLanguage);
                if (metadata instanceof Language) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(languageParameter);
                    queryModified = true;
                }

                metadata = getMetadata(acceptedMediaType);
                if (metadata instanceof MediaType) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(mediaTypeParameter);
                    queryModified = true;
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
     * Updates the client preferences according to the user agent properties
     * (name, version, etc.) taken from the "agent.properties" file located in
     * the classpath. See {@link ClientInfo#getAgentAttributes()} for more
     * details.<br>
     * The list of new media type preferences is loaded from a property file
     * called "accept.properties" located in the classpath in the sub directory
     * "org/restlet/service". This property file is composed of blocks of
     * properties. One "block" of properties starts either with the beginning of
     * the properties file or with the end of the previous block. One block ends
     * with the "acceptNew" property which contains the value of the new accept
     * header. Here is a sample block.
     * 
     * <pre>
     * agentName: firefox
     * acceptOld: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,\*\/\*;q=0.5
     * acceptNew: application/xhtml+xml,text/html,text/xml;q=0.9,application/xml;q=0.9,text/plain;q=0.8,image/png,\*\/\*;q=0.5
     * </pre>
     * 
     * Each declared property is a condition that must be filled in order to
     * update the client preferences. For example "agentName: firefox" expresses
     * the fact this block concerns only "firefox" clients.
     * 
     * The "acceptOld" property allows to check the value of the current
     * "Accept" header. If the lattest equals to the value of the "acceptOld"
     * property then the preferences will be updated. This is useful for Ajax
     * clients which looks like their browser (same agentName, agentVersion,
     * etc.) but can provide their own "Accept" header.
     * 
     * @param request
     *                the request to update.
     */
    private void processUserAgent(Request request) {
        Map<String, String> agentAttributes = request.getClientInfo()
                .getAgentAttributes();
        if (agentAttributes != null) {
            URL userAgentPropertiesUrl = Engine.getClassLoader().getResource(
                    "org/restlet/service/accept.properties");
            if (userAgentPropertiesUrl != null) {
                // Get the old Accept header value
                Form headers = (Form) request.getAttributes().get(
                        HttpConstants.ATTRIBUTE_HEADERS);
                String acceptOld = headers
                        .getFirstValue(HttpConstants.HEADER_ACCEPT);

                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(
                            userAgentPropertiesUrl.openStream(),
                            CharacterSet.UTF_8.getName()));

                    boolean processAcceptHeader = true;

                    // Read the entire file, excluding comment lines starting
                    // with "#" character.
                    String line = reader.readLine();
                    for (; line != null; line = reader.readLine()) {
                        if (!line.startsWith("#")) {
                            String[] keyValue = line.split(":");
                            if (keyValue.length == 2) {
                                String key = keyValue[0].trim();
                                String value = keyValue[1].trim();
                                if ("acceptNew".equalsIgnoreCase(key)) {
                                    if (processAcceptHeader) {
                                        ClientInfo clientInfo = new ClientInfo();
                                        PreferenceUtils.parseMediaTypes(value,
                                                clientInfo);
                                        request
                                                .getClientInfo()
                                                .setAcceptedMediaTypes(
                                                        clientInfo
                                                                .getAcceptedMediaTypes());
                                        break;
                                    }
                                    processAcceptHeader = true;
                                } else {
                                    if (processAcceptHeader) {
                                        if ("acceptOld".equalsIgnoreCase(key)
                                                && !(value == null || value
                                                        .length() == 0)) {
                                            processAcceptHeader = value
                                                    .equalsIgnoreCase(acceptOld);
                                        } else {
                                            String attribute = agentAttributes
                                                    .get(key);
                                            processAcceptHeader = attribute != null
                                                    && attribute
                                                            .equalsIgnoreCase(value);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
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
