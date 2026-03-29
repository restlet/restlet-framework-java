/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
import org.restlet.engine.util.StringUtils;
import org.restlet.routing.Filter;
import org.restlet.service.MetadataService;
import org.restlet.service.TunnelService;
import org.restlet.util.Series;

/**
 * Filter tunneling browser calls into full REST calls. The request method can be changed (via POST
 * requests only) as well as the accepted media types, languages, encodings, and character sets.
 *
 * <p>Concurrency note: instances of this class or its subclasses can be invoked by several threads
 * at the same time and therefore must be thread-safe. You should be especially careful when storing
 * state in member variables.
 *
 * @author Jerome Louvel
 */
public class TunnelFilter extends Filter {

    /** Used to replace accept-encoding header values. */
    private final List<HeaderReplacer> acceptEncodingReplacers = getAcceptEncodingReplacers();

    /** Used to replace accept header values. */
    private final List<HeaderReplacer> acceptReplacers = getAcceptReplacers();

    /**
     * Constructor.
     *
     * @param context The parent context.
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
     * Returns the list of new accept-encoding header values. Each of them describes also a set of
     * conditions required to set the new value. This method is used only to initialize the
     * headerReplacers field.
     *
     * @return The list of new accept-encoding header values.
     */
    private List<HeaderReplacer> getAcceptEncodingReplacers() {
        // Load the accept.properties file.
        return getHeaderReplacers(
                Engine.getResource("org/restlet/service/accept-encoding.properties"),
                "acceptEncodingOld",
                "acceptEncodingNew");
    }

    /**
     * Returns the list of new accept header values. Each of them describes also a set of conditions
     * required to set the new value. This method is used only to initialize the headerReplacers
     * field.
     *
     * @return The list of new accept header values.
     */
    private List<HeaderReplacer> getAcceptReplacers() {
        // Load the accept.properties file.
        return getHeaderReplacers(
                Engine.getResource("org/restlet/service/accept.properties"),
                "acceptOld",
                "acceptNew");
    }

    /**
     * Returns the list of new header values. Each of them describes also a set of conditions
     * required to set the new value. This method is used only to initialize the headerReplacers
     * field. The properties file is composed of blocks of properties. One "block" of properties
     * contains three lines like this one:
     *
     * <pre>
     * agentName: msie
     * acceptEncodingOld: deflate
     * acceptEncodingNew: deflate-no-wrap
     * </pre>
     *
     * The first line indicates that the "agentName" property is the condition that must be filled
     * to update the client preferences. The "acceptEncodingOld" property allows checking the value
     * of the current "Accept-Encoding" header. If the latest equals to the value of the
     * "acceptEncodingOld" property, then the preferences will be updated.
     *
     * @param userAgentPropertiesUrl The URL of the properties file that describe replacement values
     *     based on the user agent string.
     * @param oldHeaderName The name of the property that gives the value of the header to be
     *     replaced (could be null - in that case, the new value is unconditionally set.
     * @param newHeaderName The name of the property that gives the replacement value.
     * @return The list of new header values.
     */
    private List<HeaderReplacer> getHeaderReplacers(
            final URL userAgentPropertiesUrl, String oldHeaderName, String newHeaderName) {

        if (userAgentPropertiesUrl == null) {
            return new ArrayList<>();
        }

        HeaderReplacerAccumulator headerReplacerAccumulator =
                new HeaderReplacerAccumulator(oldHeaderName, newHeaderName);

        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                userAgentPropertiesUrl.openStream(), CharacterSet.UTF_8.getName()),
                        IoUtils.BUFFER_SIZE)) {

            // Read the entire file, excluding comment lines starting with "#" character.
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.stripLeading().startsWith("#")) {
                    headerReplacerAccumulator.analyze(line);
                }
            }
        } catch (IOException e) {
            getContext()
                    .getLogger()
                    .warning(
                            "Cannot read '"
                                    + userAgentPropertiesUrl
                                    + "' due to: "
                                    + e.getMessage());
        }

        return headerReplacerAccumulator.headerReplacers;
    }

    /**
     * Returns the metadata associated with the given extension using the {@link MetadataService}.
     *
     * @param extension The extension to lookup.
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
     * Updates the client preferences based on file-like extensions. The matched extensions are
     * removed from the last segment.
     *
     * <p>See also section 3.6.1 of JAX-RS specification (<a
     * href="https://jsr311.dev.java.net">https://jsr311.dev.java.net</a>)
     *
     * @param request The request to update.
     */
    private void processExtensions(Request request) {
        final TunnelService tunnelService = getTunnelService();

        // Tunnel the client preferences only for GET or HEAD requests
        final Method method = request.getMethod();
        if (!tunnelService.isPreferencesTunnel()
                || (!Method.GET.equals(method) && !Method.HEAD.equals(method))) {
            return;
        }

        processExtensions(request, request.getResourceRef());
    }

    private void processExtensions(final Request request, final Reference resourceRef) {
        String extensions = resourceRef.getExtensions();

        final ClientInfo clientInfo = request.getClientInfo();
        boolean encodingFound = false;
        boolean characterSetFound = false;
        boolean mediaTypeFound = false;
        boolean languageFound = false;

        // Discover extensions from right to left and stop at the first unknown extension or
        // duplicate type (language, media type, encoding, character set).
        while (!StringUtils.isNullOrEmpty(extensions)) {
            final int lastIndexOfPoint = extensions.lastIndexOf('.');
            final String extension = extensions.substring(lastIndexOfPoint + 1);
            final Metadata metadata = getMetadata(extension);

            if (!mediaTypeFound && (metadata instanceof MediaType mediaType)) {
                updateMetadata(clientInfo, mediaType);
                mediaTypeFound = true;
            } else if (!languageFound && (metadata instanceof Language language)) {
                updateMetadata(clientInfo, language);
                languageFound = true;
            } else if (!characterSetFound && (metadata instanceof CharacterSet characterSet)) {
                updateMetadata(clientInfo, characterSet);
                characterSetFound = true;
            } else if (!encodingFound && (metadata instanceof Encoding encoding)) {
                updateMetadata(clientInfo, encoding);
                encodingFound = true;
            } else {
                // extension does not match -> break loop
                break;
            }
            extensions = (lastIndexOfPoint > 0) ? extensions.substring(0, lastIndexOfPoint) : "";
        }

        // Update the extensions if necessary
        if (encodingFound || characterSetFound || mediaTypeFound || languageFound) {
            resourceRef.setExtensions(extensions);
        }
    }

    /**
     * Updates the request method based on a specific header.
     *
     * @param request The request to update.
     */
    @SuppressWarnings("unchecked")
    private void processHeaders(Request request) {
        final TunnelService tunnelService = getTunnelService();

        if (tunnelService.isMethodTunnel()) {
            // get the headers
            Series<Header> extraHeaders =
                    (Series<Header>) request.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);

            if (extraHeaders != null) {
                // look for the new value of the method
                final String newMethodValue =
                        extraHeaders.getFirstValue(getTunnelService().getMethodHeader(), true);

                if (newMethodValue != null && !newMethodValue.trim().isEmpty()) {
                    // set the current method to the new method
                    request.setMethod(Method.valueOf(newMethodValue));
                }
            }
        }
    }

    /**
     * Updates the request method and client preferences based on query parameters. The matched
     * parameters are removed from the query.
     *
     * @param request The request to update.
     */
    private void processQuery(Request request) {
        Reference resourceRef = request.getResourceRef();

        if (!resourceRef.hasQuery()) {
            return;
        }

        boolean queryModified = false;
        TunnelService tunnelService = getTunnelService();
        Form query = resourceRef.getQueryAsForm(CharacterSet.UTF_8);

        if (tunnelService.isMethodTunnel()) {
            queryModified = tunnelRequestMethod(tunnelService, query, request);
        }

        // Tunnel the client preferences
        if (tunnelService.isPreferencesTunnel()) {
            ClientInfo clientInfo = request.getClientInfo();
            queryModified =
                    queryModified || tunnelCharacterSetPreference(tunnelService, query, clientInfo);
            queryModified =
                    queryModified || tunnelEncodingPreference(tunnelService, query, clientInfo);
            queryModified =
                    queryModified || tunnelLanguagePreference(tunnelService, query, clientInfo);
            queryModified =
                    queryModified || tunnelMediaTypePreference(tunnelService, query, clientInfo);
        }

        // Update the query if it has been modified
        if (queryModified) {
            request.getResourceRef().setQuery(query.getQueryString(CharacterSet.UTF_8));
        }
    }

    private static boolean tunnelRequestMethod(
            final TunnelService tunnelService, final Form query, final Request request) {
        // Tunnel the request method
        Method method = request.getMethod();

        String methodName = query.getFirstValue(tunnelService.getMethodParameter());

        Method tunnelledMethod = Method.valueOf(methodName);
        // The OPTIONS method can be tunneled via GET requests.
        if (tunnelledMethod != null
                && (Method.POST.equals(method) || Method.OPTIONS.equals(tunnelledMethod))) {
            request.setMethod(tunnelledMethod);
            query.removeFirst(tunnelService.getMethodParameter());
            return true;
        }

        return false;
    }

    private boolean tunnelMediaTypePreference(
            final TunnelService tunnelService, final Form query, final ClientInfo clientInfo) {
        // Get the parameter names to look for
        String mediaTypeParameter = tunnelService.getMediaTypeParameter();
        // Get the preferences from the query
        String acceptedMediaType = query.getFirstValue(mediaTypeParameter);
        Metadata metadata;
        metadata = getMetadata(acceptedMediaType);

        if ((metadata == null) && (acceptedMediaType != null)) {
            metadata = MediaType.valueOf(acceptedMediaType);
        }

        if (metadata instanceof MediaType mediaType) {
            // Updates the client preferences
            updateMetadata(clientInfo, mediaType);
            query.removeFirst(mediaTypeParameter);
            return true;
        }
        return false;
    }

    private boolean tunnelLanguagePreference(
            final TunnelService tunnelService, final Form query, final ClientInfo clientInfo) {
        // Get the parameter names to look for
        String languageParameter = tunnelService.getLanguageParameter();
        // Get the preferences from the query
        String acceptedLanguage = query.getFirstValue(languageParameter);
        Metadata metadata = getMetadata(acceptedLanguage);

        if ((metadata == null) && (acceptedLanguage != null)) {
            metadata = Language.valueOf(acceptedLanguage);
        }

        if (metadata instanceof Language language) {
            // Updates the client preferences
            updateMetadata(clientInfo, language);
            query.removeFirst(languageParameter);
            return true;
        }
        return false;
    }

    private boolean tunnelEncodingPreference(
            final TunnelService tunnelService, final Form query, final ClientInfo clientInfo) {
        // Get the parameter names to look for
        String encodingParameter = tunnelService.getEncodingParameter();
        // Get the preferences from the query
        String acceptedEncoding = query.getFirstValue(encodingParameter);

        Metadata metadata = getMetadata(acceptedEncoding);
        if ((metadata == null) && (acceptedEncoding != null)) {
            metadata = Encoding.valueOf(acceptedEncoding);
        }

        if (metadata instanceof Encoding encoding) {
            // Updates the client preferences
            updateMetadata(clientInfo, encoding);
            query.removeFirst(encodingParameter);
            return true;
        }
        return false;
    }

    private boolean tunnelCharacterSetPreference(
            final TunnelService tunnelService, final Form query, final ClientInfo clientInfo) {
        // Get the parameter names to look for
        String charSetParameter = tunnelService.getCharacterSetParameter();
        // Get the preferences from the query
        String acceptedCharSet = query.getFirstValue(charSetParameter);

        Metadata metadata = getMetadata(acceptedCharSet);
        if ((metadata == null) && (acceptedCharSet != null)) {
            metadata = CharacterSet.valueOf(acceptedCharSet);
        }
        if (metadata instanceof CharacterSet characterSet) {
            // Updates the client preferences
            updateMetadata(clientInfo, characterSet);
            query.removeFirst(charSetParameter);
            return true;
        }
        return false;
    }

    /**
     * Updates the client preferences according to the user agent properties (name, version, etc.)
     * taken from the "agent.properties" file located in the classpath. See {@link
     * ClientInfo#getAgentAttributes()} for more details.<br>
     * The list of new media type preferences is loaded from a property file called
     * "accept.properties" located in the classpath in the subdirectory "org/restlet/service". This
     * property file is composed of blocks of properties. One "block" of properties starts either
     * with the beginning of the properties file or with the end of the previous block. One block
     * ends with the "acceptNew" property which contains the value of the new Accept header. Here is
     * a sample block.
     *
     * <pre>
     * agentName: firefox
     * acceptOld: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,\*\/\*;q=0.5
     * acceptNew: application/xhtml+xml,text/html,text/xml;q=0.9,application/xml;q=0.9,text/plain;q=0.8,image/png,\*\/\*;q=0.5
     * </pre>
     *
     * Each declared property is a condition that must be filled to update the client preferences.
     * For example, "agentName: firefox" expresses the fact this block concerns only "firefox"
     * clients.
     *
     * <p>The "acceptOld" property allows checking the value of the current "Accept" header. If the
     * latest equals to the value of the "acceptOld" property, then the preferences will be updated.
     * This is useful for Ajax clients that look like their browser (same agentName, agentVersion,
     * etc.) but can provide their own "Accept" header.
     *
     * @param request the request to update.
     */
    private void processUserAgent(Request request) {
        final Map<String, String> agentAttributes = request.getClientInfo().getAgentAttributes();
        if (agentAttributes == null
                || (this.acceptReplacers.isEmpty() && this.acceptEncodingReplacers.isEmpty())) {
            return;
        }

        // Get the old Accept header value
        @SuppressWarnings("unchecked")
        Series<Header> headers =
                (Series<Header>) request.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);

        // replace "accept" header if necessary
        String acceptOld =
                (headers != null)
                        ? headers.getFirstValue(HeaderConstants.HEADER_ACCEPT, true)
                        : null;
        for (HeaderReplacer headerReplacer : this.acceptReplacers) {
            if (headerReplacer.matchesConditions(agentAttributes, acceptOld)) {
                ClientInfo clientInfo = new ClientInfo();
                PreferenceReader.addMediaTypes(headerReplacer.getHeaderNew(), clientInfo);
                request.getClientInfo().setAcceptedMediaTypes(clientInfo.getAcceptedMediaTypes());
                break;
            }
        }

        // replace "accept-encoding" header if necessary
        String acceptEncodingOld =
                (headers != null)
                        ? headers.getFirstValue(HeaderConstants.HEADER_ACCEPT_ENCODING, true)
                        : null;
        for (HeaderReplacer headerReplacer : this.acceptEncodingReplacers) {
            if (headerReplacer.matchesConditions(agentAttributes, acceptEncodingOld)) {
                ClientInfo clientInfo = new ClientInfo();
                PreferenceReader.addEncodings(headerReplacer.getHeaderNew(), clientInfo);
                request.getClientInfo().setAcceptedEncodings(clientInfo.getAcceptedEncodings());
                break;
            }
        }
    }

    /**
     * Updates the client info with the given metadata. It clears existing preferences for the same
     * type of metadata if necessary.
     *
     * @param clientInfo The client info to update.
     * @param characterSet The characterSet to use.
     */
    private void updateMetadata(ClientInfo clientInfo, CharacterSet characterSet) {
        clientInfo.getAcceptedCharacterSets().clear();
        clientInfo.getAcceptedCharacterSets().add(new Preference<>(characterSet));
    }

    /**
     * Updates the client info with the given metadata. It clears existing preferences for the same
     * type of metadata if necessary.
     *
     * @param clientInfo The client info to update.
     * @param encoding The encoding to use.
     */
    private void updateMetadata(ClientInfo clientInfo, Encoding encoding) {
        clientInfo.getAcceptedEncodings().clear();
        clientInfo.getAcceptedEncodings().add(new Preference<>(encoding));
    }

    /**
     * Updates the client info with the given metadata. It clears existing preferences for the same
     * type of metadata if necessary.
     *
     * @param clientInfo The client info to update.
     * @param language The language to use.
     */
    private void updateMetadata(ClientInfo clientInfo, Language language) {
        clientInfo.getAcceptedLanguages().clear();
        clientInfo.getAcceptedLanguages().add(new Preference<>(language));
    }

    /**
     * Updates the client info with the given metadata. It clears existing preferences for the same
     * type of metadata if necessary.
     *
     * @param clientInfo The client info to update.
     * @param mediaType The mediaType to use.
     */
    private void updateMetadata(ClientInfo clientInfo, MediaType mediaType) {
        clientInfo.getAcceptedMediaTypes().clear();
        clientInfo.getAcceptedMediaTypes().add(new Preference<>(mediaType));
    }

    /**
     * Used to describe the replacement value for an old client preference and for a series of
     * specific agent (i.e., web client) attributes.
     *
     * @author Thierry Boileau
     */
    private static class HeaderReplacer {

        /** Agent attributes that must be checked. */
        private final Map<String, String> agentAttributes;

        /** New header value. */
        private final String headerNew;

        /** Old header value. */
        private final String headerOld;

        HeaderReplacer(String headerOld, String headerNew, Map<String, String> agentAttributes) {
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
         * Indicates if the current header replacer matches the request attributes.
         *
         * @param agentAttributes The user agent attributes to match.
         * @param headerOld The facultative value of the current's request header to match.
         * @return true if the given request's attributes match the current header replacer.
         */
        public boolean matchesConditions(Map<String, String> agentAttributes, String headerOld) {
            // Check the conditions
            boolean checked = true;
            // Check that the agent properties match the properties
            // set by the rule.
            for (Iterator<Entry<String, String>> iterator =
                            getAgentAttributes().entrySet().iterator();
                    checked && iterator.hasNext(); ) {
                Entry<String, String> entry = iterator.next();
                String attribute = agentAttributes.get(entry.getKey());
                checked = (attribute != null && attribute.equalsIgnoreCase(entry.getValue()));
            }
            if (checked && getHeaderOld() != null) {
                // If the rule defines an old header value, check that it is the
                // same as the user agent's header value.
                checked = getHeaderOld().equals(headerOld);
            }
            return checked;
        }
    }

    private static class HeaderReplacerAccumulator {
        final List<HeaderReplacer> headerReplacers = new ArrayList<>();
        private final Map<String, String> agentAttributes = new HashMap<>();
        private final String oldHeaderName;
        private final String newHeaderName;

        private String oldValue;

        private HeaderReplacerAccumulator(final String oldHeaderName, final String newHeaderName) {
            this.oldHeaderName = oldHeaderName;
            this.newHeaderName = newHeaderName;
        }

        void analyze(final String line) {
            final String[] keyValue = line.split(":");

            if (keyValue.length == 2) {
                final String key = keyValue[0].trim();
                final String value = keyValue[1].trim();

                if (oldHeaderName.equalsIgnoreCase(key)) {
                    this.oldValue = (value.isEmpty()) ? null : value;
                } else if (newHeaderName.equalsIgnoreCase(key)) {
                    headerReplacers.add(
                            new HeaderReplacer(oldValue, value, new HashMap<>(agentAttributes)));

                    agentAttributes.clear();
                } else {
                    agentAttributes.put(key, value);
                }
            }
        }
    }
}
