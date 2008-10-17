/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gwt.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.restlet.gwt.Client;
import org.restlet.gwt.data.CharacterSet;
import org.restlet.gwt.data.ClientInfo;
import org.restlet.gwt.data.Cookie;
import org.restlet.gwt.data.CookieSetting;
import org.restlet.gwt.data.Dimension;
import org.restlet.gwt.data.Form;
import org.restlet.gwt.data.Language;
import org.restlet.gwt.data.MediaType;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.data.Preference;
import org.restlet.gwt.data.Product;
import org.restlet.gwt.data.Protocol;
import org.restlet.gwt.data.Response;
import org.restlet.gwt.internal.http.ContentType;
import org.restlet.gwt.internal.http.CookieReader;
import org.restlet.gwt.internal.http.CookieUtils;
import org.restlet.gwt.internal.http.GwtHttpClientHelper;
import org.restlet.gwt.internal.http.HttpClientCall;
import org.restlet.gwt.internal.http.HttpClientConverter;
import org.restlet.gwt.internal.http.HttpUtils;
import org.restlet.gwt.internal.util.FormUtils;
import org.restlet.gwt.resource.Representation;
import org.restlet.gwt.resource.Variant;

/**
 * Restlet factory supported by the engine.
 * 
 * @author Jerome Louvel
 */
public class Engine extends org.restlet.gwt.util.Engine {
    /** Complete version. */
    @SuppressWarnings("hiding")
    public static final String VERSION = org.restlet.gwt.util.Engine.VERSION;

    /** Complete version header. */
    public static final String VERSION_HEADER = "Noelios-Restlet-Engine/"
            + VERSION;


    /**
     * Registers a new Noelios Restlet Engine.
     * 
     * @return The registered engine.
     */
    public static Engine register() {
        return register(true);
    }

    /**
     * Registers a new Noelios Restlet Engine.
     * 
     * @param discoverConnectors
     *            True if connectors should be automatically discovered.
     * @return The registered engine.
     */
    public static Engine register(boolean discoverConnectors) {
        final Engine result = new Engine(discoverConnectors);
        org.restlet.gwt.util.Engine.setInstance(result);
        return result;
    }

    /** List of available client connectors. */
    private List<ClientHelper> registeredClients;

    /**
     * Constructor that will automatically attempt to discover connectors.
     */
    public Engine() {
        this(true);
    }

    /**
     * Constructor.
     * 
     * @param discoverHelpers
     *            True if helpers should be automatically discovered.
     */
    public Engine(boolean discoverHelpers) {
        this.registeredClients = new ArrayList<ClientHelper>();

        if (discoverHelpers) {
            getRegisteredClients().add(new GwtHttpClientHelper(null));
        }
    }

    /**
     * Copies the given header parameters into the given {@link Response}.
     * 
     * @param responseHeaders
     *            The headers to copy.
     * @param response
     *            The response to update. Must contain a {@link Representation}
     *            to copy the representation headers in it.
     * @see org.restlet.util.Engine#copyResponseHeaders(java.lang.Iterable,
     *      org.restlet.data.Response)
     */
    @Override
    public void copyResponseHeaders(Iterable<Parameter> responseHeaders,
            Response response) {
        HttpClientConverter.copyResponseTransportHeaders(responseHeaders,
                response);
        HttpClientCall.copyResponseEntityHeaders(responseHeaders, response
                .getEntity());
    }

    @Override
    public ClientHelper createHelper(Client client) {
        ClientHelper result = null;

        if (client.getProtocols().size() > 0) {
            ClientHelper connector = null;
            for (final Iterator<ClientHelper> iter = getRegisteredClients()
                    .iterator(); (result == null) && iter.hasNext();) {
                connector = iter.next();

                if (connector.getProtocols().containsAll(client.getProtocols())) {
                    // Not very dynamic but works as we only have one helper
                    // available currently
                    result = new GwtHttpClientHelper(client);
                }
            }

            if (result == null) {
                // Couldn't find a matching connector
                final StringBuilder sb = new StringBuilder();
                sb
                        .append("No available client connector supports the required protocols: ");

                for (final Protocol p : client.getProtocols()) {
                    sb.append("'").append(p.getName()).append("' ");
                }

                sb
                        .append(". Please add the JAR of a matching connector to your classpath.");

                System.err.println(sb.toString());
            }
        }

        return result;
    }

    @Override
    public String formatCookie(Cookie cookie) throws IllegalArgumentException {
        return CookieUtils.format(cookie);
    }

    @Override
    public String formatCookieSetting(CookieSetting cookieSetting)
            throws IllegalArgumentException {
        return CookieUtils.format(cookieSetting);
    }

    @Override
    public String formatDimensions(Collection<Dimension> dimensions) {
        return HttpUtils.createVaryHeader(dimensions);
    }

    @Override
    public String formatUserAgent(List<Product> products)
            throws IllegalArgumentException {
        final StringBuilder builder = new StringBuilder();

        for (final Iterator<Product> iterator = products.iterator(); iterator
                .hasNext();) {
            final Product product = iterator.next();
            if ((product.getName() == null)
                    || (product.getName().length() == 0)) {
                throw new IllegalArgumentException(
                        "Product name cannot be null.");
            }

            builder.append(product.getName());
            if (product.getVersion() != null) {
                builder.append("/").append(product.getVersion());
            }
            if (product.getComment() != null) {
                builder.append(" (").append(product.getComment()).append(")");
            }

            if (iterator.hasNext()) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    @Override
    public Variant getPreferredVariant(ClientInfo client,
            List<Variant> variants, Language defaultLanguage) {
        if (variants == null) {
            return null;
        }
        List<Language> variantLanguages = null;
        MediaType variantMediaType = null;

        boolean compatibleLanguage = false;
        boolean compatibleMediaType = false;

        Variant currentVariant = null;
        Variant bestVariant = null;

        Preference<Language> currentLanguagePref = null;
        Preference<Language> bestLanguagePref = null;
        Preference<MediaType> currentMediaTypePref = null;
        Preference<MediaType> bestMediaTypePref = null;

        float bestQuality = 0;
        float bestLanguageScore = 0;
        float bestMediaTypeScore = 0;

        // If no language preference is defined or even none matches, we
        // want to make sure that at least a variant can be returned.
        // Based on experience, it appears that browsers are often
        // misconfigured and don't expose all the languages actually
        // understood by end users.
        // Thus, a few other preferences are added to the user's ones:
        // - primary languages inferred from and sorted according to the
        // user's preferences with quality between 0.005 and 0.006
        // - default language (if any) with quality 0.003
        // - primary language of the default language (if available) with
        // quality 0.002
        // - all languages with quality 0.001
        List<Preference<Language>> languagePrefs = client
                .getAcceptedLanguages();
        final List<Preference<Language>> primaryLanguagePrefs = new ArrayList<Preference<Language>>();
        // A default language preference is defined with a better weight
        // than the "All languages" preference
        final Preference<Language> defaultLanguagePref = ((defaultLanguage == null) ? null
                : new Preference<Language>(defaultLanguage, 0.003f));
        final Preference<Language> allLanguagesPref = new Preference<Language>(
                Language.ALL, 0.001f);

        if (languagePrefs.isEmpty()) {
            // All languages accepted.
            languagePrefs.add(new Preference<Language>(Language.ALL));
        } else {
            // Get the primary language preferences that are not currently
            // accepted by the client
            final List<String> list = new ArrayList<String>();
            for (final Preference<Language> preference : languagePrefs) {
                final Language language = preference.getMetadata();
                if (!language.getSubTags().isEmpty()) {
                    if (!list.contains(language.getPrimaryTag())) {
                        list.add(language.getPrimaryTag());
                        primaryLanguagePrefs.add(new Preference<Language>(
                                new Language(language.getPrimaryTag()),
                                0.005f + (0.001f * preference.getQuality())));
                    }
                }
            }
            // If the default language is a "primary" language but is not
            // present in the list of all primary languages, add it.
            if ((defaultLanguage != null)
                    && !defaultLanguage.getSubTags().isEmpty()) {
                if (!list.contains(defaultLanguage.getPrimaryTag())) {
                    primaryLanguagePrefs.add(new Preference<Language>(
                            new Language(defaultLanguage.getPrimaryTag()),
                            0.002f));
                }
            }

        }

        // Client preferences are altered
        languagePrefs.addAll(primaryLanguagePrefs);
        if (defaultLanguagePref != null) {
            languagePrefs.add(defaultLanguagePref);
            // In this case, if the client adds the "all languages"
            // preference, the latter is removed, in order to support the
            // default preference defined by the server
            final List<Preference<Language>> list = new ArrayList<Preference<Language>>();
            for (final Preference<Language> preference : languagePrefs) {
                final Language language = preference.getMetadata();
                if (!language.equals(Language.ALL)) {
                    list.add(preference);
                }
            }
            languagePrefs = list;
        }
        languagePrefs.add(allLanguagesPref);

        // For each available variant, we will compute the negotiation score
        // which depends on both language and media type scores.
        for (final Iterator<Variant> iter1 = variants.iterator(); iter1
                .hasNext();) {
            currentVariant = iter1.next();
            variantLanguages = currentVariant.getLanguages();
            variantMediaType = currentVariant.getMediaType();

            // All languages of the current variant are scored.
            for (final Language variantLanguage : variantLanguages) {
                // For each language preference defined in the call
                // Calculate the score and remember the best scoring
                // preference
                for (final Iterator<Preference<Language>> iter2 = languagePrefs
                        .iterator(); (variantLanguage != null)
                        && iter2.hasNext();) {
                    currentLanguagePref = iter2.next();
                    final float currentScore = getScore(variantLanguage,
                            currentLanguagePref.getMetadata());
                    final boolean compatiblePref = (currentScore != -1.0f);
                    // 3) Do we have a better preference?
                    // currentScore *= currentPref.getQuality();
                    if (compatiblePref
                            && ((bestLanguagePref == null) || (currentScore > bestLanguageScore))) {
                        bestLanguagePref = currentLanguagePref;
                        bestLanguageScore = currentScore;
                    }
                }
            }

            // Are the preferences compatible with the current variant
            // language?
            compatibleLanguage = (variantLanguages.isEmpty())
                    || (bestLanguagePref != null);

            // If no media type preference is defined, assume that all media
            // types are acceptable
            final List<Preference<MediaType>> mediaTypePrefs = client
                    .getAcceptedMediaTypes();
            if (mediaTypePrefs.size() == 0) {
                mediaTypePrefs.add(new Preference<MediaType>(MediaType.ALL));
            }

            // For each media range preference defined in the call
            // Calculate the score and remember the best scoring preference
            for (final Iterator<Preference<MediaType>> iter2 = mediaTypePrefs
                    .iterator(); compatibleLanguage && iter2.hasNext();) {
                currentMediaTypePref = iter2.next();
                final float currentScore = getScore(variantMediaType,
                        currentMediaTypePref.getMetadata());
                final boolean compatiblePref = (currentScore != -1.0f);
                // 3) Do we have a better preference?
                // currentScore *= currentPref.getQuality();
                if (compatiblePref
                        && ((bestMediaTypePref == null) || (currentScore > bestMediaTypeScore))) {
                    bestMediaTypePref = currentMediaTypePref;
                    bestMediaTypeScore = currentScore;
                }

            }

            // Are the preferences compatible with the current media type?
            compatibleMediaType = (variantMediaType == null)
                    || (bestMediaTypePref != null);

            if (compatibleLanguage && compatibleMediaType) {
                // Do we have a compatible media type?
                float currentQuality = 0;
                if (bestLanguagePref != null) {
                    currentQuality += (bestLanguagePref.getQuality() * 10F);
                } else if (!variantLanguages.isEmpty()) {
                    currentQuality += 0.1F * 10F;
                }

                if (bestMediaTypePref != null) {
                    // So, let's conclude on the current variant, its
                    // quality
                    currentQuality += bestMediaTypePref.getQuality();
                }

                if (bestVariant == null) {
                    bestVariant = currentVariant;
                    bestQuality = currentQuality;
                } else if (currentQuality > bestQuality) {
                    bestVariant = currentVariant;
                    bestQuality = currentQuality;
                }
            }

            // Reset the preference variables
            bestLanguagePref = null;
            bestLanguageScore = 0;
            bestMediaTypePref = null;
            bestMediaTypeScore = 0;
        }

        return bestVariant;

    }

    /**
     * Returns the list of available client connectors.
     * 
     * @return The list of available client connectors.
     */
    public List<ClientHelper> getRegisteredClients() {
        return this.registeredClients;
    }

    /**
     * Returns a matching score between 2 Languages
     * 
     * @param variantLanguage
     * @param preferenceLanguage
     * @return the positive matching score or -1 if the languages are not
     *         compatible
     */
    private float getScore(Language variantLanguage, Language preferenceLanguage) {
        float score = 0.0f;
        boolean compatibleLang = true;

        // 1) Compare the main tag
        if (variantLanguage.getPrimaryTag().equalsIgnoreCase(
                preferenceLanguage.getPrimaryTag())) {
            score += 100;
        } else if (!preferenceLanguage.getPrimaryTag().equals("*")) {
            compatibleLang = false;
        } else if (!preferenceLanguage.getSubTags().isEmpty()) {
            // Only "*" is an acceptable language range
            compatibleLang = false;
        } else {
            // The valid "*" range has the lowest valid score
            score++;
        }

        if (compatibleLang) {
            // 2) Compare the sub tags
            if ((preferenceLanguage.getSubTags().isEmpty())
                    || (variantLanguage.getSubTags().isEmpty())) {
                if (variantLanguage.getSubTags().isEmpty()
                        && preferenceLanguage.getSubTags().isEmpty()) {
                    score += 10;
                } else {
                    // Don't change the score
                }
            } else {
                final int maxSize = Math.min(preferenceLanguage.getSubTags()
                        .size(), variantLanguage.getSubTags().size());
                for (int i = 0; (i < maxSize) && compatibleLang; i++) {
                    if (preferenceLanguage.getSubTags().get(i)
                            .equalsIgnoreCase(
                                    variantLanguage.getSubTags().get(i))) {
                        // Each subtag contribution to the score
                        // is getting less and less important
                        score += Math.pow(10, 1 - i);
                    } else {
                        // SubTags are different
                        compatibleLang = false;
                    }
                }
            }
        }

        return (compatibleLang ? score : -1.0f);
    }

    /**
     * Returns a matching score between 2 Media types
     * 
     * @param variantMediaType
     * @param preferenceMediaType
     * @return the positive matching score or -1 if the media types are not
     *         compatible
     */
    private float getScore(MediaType variantMediaType,
            MediaType preferenceMediaType) {
        float score = 0.0f;
        boolean comptabibleMediaType = true;

        // 1) Compare the main types
        if (preferenceMediaType.getMainType().equals(
                variantMediaType.getMainType())) {
            score += 1000;
        } else if (!preferenceMediaType.getMainType().equals("*")) {
            comptabibleMediaType = false;
        } else if (!preferenceMediaType.getSubType().equals("*")) {
            // Ranges such as "*/html" are not supported
            // Only "*/*" is acceptable in this case
            comptabibleMediaType = false;
        }

        if (comptabibleMediaType) {
            // 2) Compare the sub types
            if (variantMediaType.getSubType().equals(
                    preferenceMediaType.getSubType())) {
                score += 100;
            } else if (!preferenceMediaType.getSubType().equals("*")) {
                // Sub-type are different
                comptabibleMediaType = false;
            }

            if (comptabibleMediaType
                    && (variantMediaType.getParameters() != null)) {
                // 3) Compare the parameters
                // If current media type is compatible with the
                // current media range then the parameters need to
                // be checked too
                for (final Parameter currentParam : variantMediaType
                        .getParameters()) {
                    if (isParameterFound(currentParam, preferenceMediaType)) {
                        score++;
                    }
                }
            }

        }
        return (comptabibleMediaType ? score : -1.0f);
    }

    /**
     * Indicates if the searched parameter is specified in the given media
     * range.
     * 
     * @param searchedParam
     *            The searched parameter.
     * @param mediaRange
     *            The media range to inspect.
     * @return True if the searched parameter is specified in the given media
     *         range.
     */
    private boolean isParameterFound(Parameter searchedParam,
            MediaType mediaRange) {
        boolean result = false;

        for (final Iterator<Parameter> iter = mediaRange.getParameters()
                .iterator(); !result && iter.hasNext();) {
            result = searchedParam.equals(iter.next());
        }

        return result;
    }

    @Override
    public void parse(Form form, Representation webForm) {
        if (webForm != null) {
            FormUtils.parse(form, webForm);
        }
    }

    @Override
    public void parse(Form form, String queryString, CharacterSet characterSet,
            boolean decode, char separator) {
        if ((queryString != null) && !queryString.equals("")) {
            FormUtils.parse(form, queryString, characterSet, decode, separator);
        }
    }

    @Override
    public MediaType parseContentType(String contentType)
            throws IllegalArgumentException {
        try {
            return ContentType.parseContentType(contentType);
        } catch (Exception e) {
            throw new IllegalArgumentException("The content type string \""
                    + contentType + "\" can not be parsed: " + e.getMessage());
        }
    }

    @Override
    public Cookie parseCookie(String cookie) throws IllegalArgumentException {
        final CookieReader cr = new CookieReader(cookie);
        try {
            return cr.readCookie();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read the cookie");
        }
    }

    @Override
    public CookieSetting parseCookieSetting(String cookieSetting)
            throws IllegalArgumentException {
        final CookieReader cr = new CookieReader(cookieSetting);
        try {
            return cr.readCookieSetting();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not read the cookie setting");
        }
    }

    @Override
    public List<Product> parseUserAgent(String userAgent)
            throws IllegalArgumentException {
        final List<Product> result = new ArrayList<Product>();

        if (userAgent != null) {
            String token = null;
            String version = null;
            String comment = null;
            final char[] tab = userAgent.trim().toCharArray();
            StringBuilder tokenBuilder = new StringBuilder();
            StringBuilder versionBuilder = null;
            StringBuilder commentBuilder = null;
            int index = 0;
            boolean insideToken = true;
            boolean insideVersion = false;
            boolean insideComment = false;

            for (index = 0; index < tab.length; index++) {
                final char c = tab[index];
                if (insideToken) {
                    if (((c >= 'a') && (c <= 'z'))
                            || ((c >= 'A') && (c <= 'Z')) || (c == ' ')) {
                        tokenBuilder.append(c);
                    } else {
                        token = tokenBuilder.toString().trim();
                        insideToken = false;
                        if (c == '/') {
                            insideVersion = true;
                            versionBuilder = new StringBuilder();
                        } else if (c == '(') {
                            insideComment = true;
                            commentBuilder = new StringBuilder();
                        }
                    }
                } else {
                    if (insideVersion) {
                        if (c != ' ') {
                            versionBuilder.append(c);
                        } else {
                            insideVersion = false;
                            version = versionBuilder.toString();
                        }
                    } else {
                        if (c == '(') {
                            insideComment = true;
                            commentBuilder = new StringBuilder();
                        } else {
                            if (insideComment) {
                                if (c == ')') {
                                    insideComment = false;
                                    comment = commentBuilder.toString();
                                    result.add(new Product(token, version,
                                            comment));
                                    insideToken = true;
                                    tokenBuilder = new StringBuilder();
                                } else {
                                    commentBuilder.append(c);
                                }
                            } else {
                                result.add(new Product(token, version, null));
                                insideToken = true;
                                tokenBuilder = new StringBuilder();
                                tokenBuilder.append(c);
                            }
                        }
                    }
                }
            }

            if (insideComment) {
                comment = commentBuilder.toString();
                result.add(new Product(token, version, comment));
            } else {
                if (insideVersion) {
                    version = versionBuilder.toString();
                    result.add(new Product(token, version, null));
                } else {
                    if (insideToken && (tokenBuilder.length() > 0)) {
                        token = tokenBuilder.toString();
                        result.add(new Product(token, null, null));
                    }
                }
            }
        }

        return result;

    }

}
