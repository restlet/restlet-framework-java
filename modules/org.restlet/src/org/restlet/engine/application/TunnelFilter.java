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

package org.restlet.engine.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.PreferenceReader;
import org.restlet.engine.io.IoUtils;
import org.restlet.routing.Filter;
import org.restlet.service.MetadataService;
import org.restlet.service.TunnelService;
import org.restlet.util.Series;

// [excludes gwt]
/**
 * Filter tunneling browser calls into full REST calls. The request method can
 * be changed (via POST requests only) as well as the accepted media types,
 * languages, encodings and character sets.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class TunnelFilter extends Filter {

    /**
     * Used to describe the replacement value for an old client preference and
     * for a a series of specific agent (i.e. web client) attributes.
     * 
     * @author Thierry Boileau
     */
    private static class HeaderReplacer {

        static class Builder {
            Map<String, String> agentAttributes = new HashMap<String, String>();

            String newValue;

            String oldValue;

            HeaderReplacer build() {
                return new HeaderReplacer(oldValue, newValue, agentAttributes);
            }

            void putAgentAttribute(String key, String value) {
                agentAttributes.put(key, value);
            }

            void setNewValue(String newValue) {
                this.newValue = newValue;
            }

            void setOldValue(String oldValue) {
                this.oldValue = oldValue;
            }
        }

        /** Agent attributes that must be checked. */
        private final Map<String, String> agentAttributes;

        /** New header value. */
        private final String headerNew;

        /** Old header value. */
        private final String headerOld;

        HeaderReplacer(String headerOld, String headerNew,
                Map<String, String> agentAttributes) {
            this.headerOld = headerOld;
            this.headerNew = headerNew;
            this.agentAttributes = Collections.unmodifiableMap(agentAttributes);
        }

        public Map<String, String> getAgentAttributes() {
            return agentAttributes;
        }

        public String getHeaderNew() {
            return headerNew;
        }

        public String getHeaderOld() {
            return headerOld;
        }

        /**
         * Indicates if the current header replacer matches the request
         * attributes.
         * 
         * @param agentAttributes
         *            The user agent attributes to match.
         * @param headerOld
         *            The facultative value of the current's request header to
         *            match.
         * @return true if the given request's attibutes match the current
         *         header replacer.
         */
        public boolean matchesConditions(Map<String, String> agentAttributes,
                String headerOld) {
            // Check the conditions
            boolean checked = true;
            // Check that the agent properties match the properties
            // set by the rule.
            for (Iterator<Entry<String, String>> iterator = getAgentAttributes()
                    .entrySet().iterator(); checked && iterator.hasNext();) {
                Entry<String, String> entry = iterator.next();
                String attribute = agentAttributes.get(entry.getKey());
                checked = (attribute != null && attribute
                        .equalsIgnoreCase(entry.getValue()));
            }
            if (checked && getHeaderOld() != null) {
                // If the rule defines an old header value, check that it is the
                // same than the user agent's header value.
                checked = getHeaderOld().equals(headerOld);
            }
            return checked;
        }

    }

    /** Used to replace accept-encoding header values. */
    private final List<HeaderReplacer> acceptEncodingReplacers = getAcceptEncodingReplacers();

    /** Used to replace accept header values. */
    private final List<HeaderReplacer> acceptReplacers = getAcceptReplacers();

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
        if (getTunnelService().isUserAgentTunnel()) {
            processUserAgent(request);
        }

        if (getTunnelService().isExtensionsTunnel()) {
            processExtensions(request);
        }

        if (getTunnelService().isQueryTunnel()) {
            processQuery(request);
        }

        if (getTunnelService().isHeadersTunnel()) {
            processHeaders(request);
        }

        return CONTINUE;
    }

    /**
     * Returns the list of new accept-encoding header values. Each of them
     * describe also a set of conditions required to set the new value. This
     * method is used only to initialize the headerReplacers field.
     * 
     * @return The list of new accept-encoding header values.
     */
    private List<HeaderReplacer> getAcceptEncodingReplacers() {
        // Load the accept.properties file.
        return getheaderReplacers(
                Engine.getResource("org/restlet/service/accept-encoding.properties"),
                "acceptEncodingOld", "acceptEncodingNew");
    }

    /**
     * Returns the list of new accept header values. Each of them describe also
     * a set of conditions required to set the new value. This method is used
     * only to initialize the headerReplacers field.
     * 
     * @return The list of new accept header values.
     */
    private List<HeaderReplacer> getAcceptReplacers() {
        // Load the accept.properties file.
        return getheaderReplacers(
                Engine.getResource("org/restlet/service/accept.properties"),
                "acceptOld", "acceptNew");
    }

    /**
     * Returns the list of new header values. Each of them describe also a set
     * of conditions required to set the new value. This method is used only to
     * initialize the headerReplacers field.
     * 
     * @param userAgentPropertiesUrl
     *            The URL of the properties file that describe replacement
     *            values based on the user agent string.
     * @param oldHeaderName
     *            The name of the property that gives the value of the header to
     *            be replaced (could be null - in that case, the new value is
     *            unconditionnaly set.
     * @param newHeaderName
     *            The name of the property that gives the replacement value.
     * @return The list of new header values.
     */
    private List<HeaderReplacer> getheaderReplacers(
            final URL userAgentPropertiesUrl, String oldHeaderName,
            String newHeaderName) {
        List<HeaderReplacer> headerReplacers = new ArrayList<HeaderReplacer>();

        if (userAgentPropertiesUrl != null) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(
                        userAgentPropertiesUrl.openStream(),
                        CharacterSet.UTF_8.getName()), IoUtils.BUFFER_SIZE);

                HeaderReplacer.Builder headerReplacerBuilder = new HeaderReplacer.Builder();

                try {
                    // Read the entire file, excluding comment lines starting
                    // with "#" character.
                    String line = reader.readLine();
                    for (; line != null; line = reader.readLine()) {
                        if (!line.startsWith("#")) {
                            final String[] keyValue = line.split(":");
                            if (keyValue.length == 2) {
                                final String key = keyValue[0].trim();
                                final String value = keyValue[1].trim();
                                if (oldHeaderName.equalsIgnoreCase(key)) {
                                    headerReplacerBuilder.setOldValue((""
                                            .equals(value)) ? null : value);
                                } else if (newHeaderName.equalsIgnoreCase(key)) {
                                    headerReplacerBuilder.setNewValue(value);
                                    headerReplacers.add(headerReplacerBuilder
                                            .build());

                                    headerReplacerBuilder = new HeaderReplacer.Builder();
                                } else {
                                    headerReplacerBuilder.putAgentAttribute(
                                            key, value);
                                }
                            }
                        }
                    }
                } finally {
                    reader.close();
                }
            } catch (IOException e) {
                getContext().getLogger().warning(
                        "Cannot read '" + userAgentPropertiesUrl.toString()
                                + "' due to: " + e.getMessage());
            }
        }

        return headerReplacers;
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

        // Tunnel the client preferences only for GET or HEAD requests
        final Method method = request.getMethod();
        if (tunnelService.isPreferencesTunnel()
                && (method.equals(Method.GET) || method.equals(Method.HEAD))) {
            final Reference resourceRef = request.getResourceRef();

            if (resourceRef.hasExtensions()) {
                final ClientInfo clientInfo = request.getClientInfo();
                boolean encodingFound = false;
                boolean characterSetFound = false;
                boolean mediaTypeFound = false;
                boolean languageFound = false;
                String extensions = resourceRef.getExtensions();

                // Discover extensions from right to left and stop at the first
                // unknown extension. Only one extension per type of metadata is
                // also allowed: i.e. one language, one media type, one
                // encoding, one character set.
                while (true) {
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
     * Updates the request method based on specific header.
     * 
     * @param request
     *            The request to update.
     */
    @SuppressWarnings("unchecked")
    private void processHeaders(Request request) {
        final TunnelService tunnelService = getTunnelService();

        if (tunnelService.isMethodTunnel()) {
            // get the headers
            Series<Header> extraHeaders = (Series<Header>) request
                    .getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);

            if (extraHeaders != null) {
                // look for the new value of the method
                final String newMethodValue = extraHeaders.getFirstValue(
                        getTunnelService().getMethodHeader(), true);

                if (newMethodValue != null
                        && newMethodValue.trim().length() > 0) {
                    // set the current method to the new method
                    request.setMethod(Method.valueOf(newMethodValue));
                }
            }
        }
    }

    /**
     * Updates the request method and client preferences based on query
     * parameters. The matched parameters are removed from the query.
     * 
     * @param request
     *            The request to update.
     * @return True if the query has been updated, false otherwise.
     */
    private boolean processQuery(Request request) {
        TunnelService tunnelService = getTunnelService();
        boolean queryModified = false;
        Reference resourceRef = request.getResourceRef();

        if (resourceRef.hasQuery()) {
            Form query = resourceRef.getQueryAsForm(CharacterSet.UTF_8);

            // Tunnel the request method
            Method method = request.getMethod();
            if (tunnelService.isMethodTunnel()) {
                String methodName = query.getFirstValue(tunnelService
                        .getMethodParameter());

                Method tunnelledMethod = Method.valueOf(methodName);
                // The OPTIONS method can be tunneled via GET requests.
                if (tunnelledMethod != null
                        && (Method.POST.equals(method) || Method.OPTIONS
                                .equals(tunnelledMethod))) {
                    request.setMethod(tunnelledMethod);
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

                if ((metadata == null) && (acceptedCharSet != null)) {
                    metadata = CharacterSet.valueOf(acceptedCharSet);
                }

                if (metadata instanceof CharacterSet) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(charSetParameter);
                    queryModified = true;
                }

                metadata = getMetadata(acceptedEncoding);

                if ((metadata == null) && (acceptedEncoding != null)) {
                    metadata = Encoding.valueOf(acceptedEncoding);
                }

                if (metadata instanceof Encoding) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(encodingParameter);
                    queryModified = true;
                }

                metadata = getMetadata(acceptedLanguage);

                if ((metadata == null) && (acceptedLanguage != null)) {
                    metadata = Language.valueOf(acceptedLanguage);
                }

                if (metadata instanceof Language) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(languageParameter);
                    queryModified = true;
                }

                metadata = getMetadata(acceptedMediaType);

                if ((metadata == null) && (acceptedMediaType != null)) {
                    metadata = MediaType.valueOf(acceptedMediaType);
                }

                if (metadata instanceof MediaType) {
                    updateMetadata(clientInfo, metadata);
                    query.removeFirst(mediaTypeParameter);
                    queryModified = true;
                }
            }

            // Update the query if it has been modified
            if (queryModified) {
                request.getResourceRef().setQuery(query.getQueryString(CharacterSet.UTF_8));
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
     * "Accept" header. If the latest equals to the value of the "acceptOld"
     * property then the preferences will be updated. This is useful for Ajax
     * clients which looks like their browser (same agentName, agentVersion,
     * etc.) but can provide their own "Accept" header.
     * 
     * @param request
     *            the request to update.
     */
    private void processUserAgent(Request request) {
        final Map<String, String> agentAttributes = request.getClientInfo()
                .getAgentAttributes();
        if (agentAttributes != null) {
            if (!this.acceptReplacers.isEmpty()
                    || !this.acceptEncodingReplacers.isEmpty()) {
                // Get the old Accept header value
                @SuppressWarnings("unchecked")
                Series<Header> headers = (Series<Header>) request
                        .getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);

                String acceptOld = (headers != null) ? headers.getFirstValue(
                        HeaderConstants.HEADER_ACCEPT, true) : null;
                // Check each replacer
                for (HeaderReplacer headerReplacer : this.acceptReplacers) {
                    if (headerReplacer.matchesConditions(agentAttributes,
                            acceptOld)) {
                        ClientInfo clientInfo = new ClientInfo();
                        PreferenceReader.addMediaTypes(
                                headerReplacer.getHeaderNew(), clientInfo);
                        request.getClientInfo().setAcceptedMediaTypes(
                                clientInfo.getAcceptedMediaTypes());
                        break;
                    }
                }
                String acceptEncodingOld = (headers != null) ? headers
                        .getFirstValue(HeaderConstants.HEADER_ACCEPT_ENCODING,
                                true) : null;
                // Check each replacer
                for (HeaderReplacer headerReplacer : this.acceptEncodingReplacers) {
                    if (headerReplacer.matchesConditions(agentAttributes,
                            acceptEncodingOld)) {
                        ClientInfo clientInfo = new ClientInfo();
                        PreferenceReader.addEncodings(
                                headerReplacer.getHeaderNew(), clientInfo);
                        request.getClientInfo().setAcceptedEncodings(
                                clientInfo.getAcceptedEncodings());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Updates the client info with the given metadata. It clears existing
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
