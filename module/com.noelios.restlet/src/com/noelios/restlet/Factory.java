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

package com.noelios.restlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Server;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.restlet.util.Helper;

import com.noelios.restlet.application.ApplicationHelper;
import com.noelios.restlet.component.ComponentHelper;
import com.noelios.restlet.local.DirectoryResource;
import com.noelios.restlet.util.FormUtils;

/**
 * Restlet factory supported by the engine.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Factory extends org.restlet.util.Factory {
    /** Obtain a suitable logger. */
    private static Logger logger = Logger.getLogger(Factory.class
            .getCanonicalName());

    public static final String VERSION_LONG = org.restlet.util.Factory.VERSION_LONG;

    public static final String VERSION_SHORT = org.restlet.util.Factory.VERSION_SHORT;

    public static final String VERSION_HEADER = "Noelios-Restlet-Engine/"
            + VERSION_SHORT;

    /**
     * Registers a new Noelios Restlet Engine.
     */
    public static void register() {
        Factory.setInstance(new Factory());
    }

    /** List of available client connectors. */
    private List<ConnectorHelper> registeredClients;

    /** List of available server connectors. */
    private List<ConnectorHelper> registeredServers;

    /**
     * Constructor that will automatically attempt to discover connectors.
     */
    @SuppressWarnings("unchecked")
    public Factory() {
        this(true);
    }

    /**
     * Constructor.
     * 
     * @param discoverConnectors
     *            True if connectors should be automatically discovered.
     */
    @SuppressWarnings("unchecked")
    public Factory(boolean discoverConnectors) {
        if (discoverConnectors) {
            // Find the factory class name
            String line = null;
            String provider = null;

            // Find the factory class name
            ClassLoader cl = org.restlet.util.Factory.getClassLoader();
            URL configURL;

            // Register the client connector providers
            try {
                for (Enumeration<URL> configUrls = cl
                        .getResources("META-INF/services/com.noelios.restlet.ClientHelper"); configUrls
                        .hasMoreElements();) {
                    configURL = configUrls.nextElement();

                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(
                                configURL.openStream(), "utf-8"));
                        line = reader.readLine();

                        while (line != null) {
                            provider = getProviderClassName(line);

                            if ((provider != null) && (!provider.equals(""))) {
                                // Instantiate the factory
                                try {
                                    Class<? extends ConnectorHelper> providerClass = (Class<? extends ConnectorHelper>) Class
                                            .forName(provider);
                                    getRegisteredClients().add(
                                            providerClass.getConstructor(
                                                    Client.class).newInstance(
                                                    (Client) null));
                                } catch (Exception e) {
                                    logger.log(Level.SEVERE,
                                            "Unable to register the client connector "
                                                    + provider, e);
                                }
                            }

                            line = reader.readLine();
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE,
                                "Unable to read the provider descriptor: "
                                        + configURL.toString());
                    } finally {
                        if (reader != null)
                            reader.close();
                    }
                }
            } catch (IOException ioe) {
                logger
                        .log(
                                Level.SEVERE,
                                "Exception while detecting the client connectors.",
                                ioe);
            }

            // Register the server connector providers
            try {
                for (Enumeration<URL> configUrls = cl
                        .getResources("META-INF/services/com.noelios.restlet.ServerHelper"); configUrls
                        .hasMoreElements();) {
                    configURL = configUrls.nextElement();

                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(
                                configURL.openStream(), "utf-8"));
                        line = reader.readLine();

                        while (line != null) {
                            provider = getProviderClassName(line);

                            if ((provider != null) && (!provider.equals(""))) {
                                // Instantiate the factory
                                try {
                                    Class<? extends ConnectorHelper> providerClass = (Class<? extends ConnectorHelper>) Class
                                            .forName(provider);
                                    getRegisteredServers().add(
                                            providerClass.getConstructor(
                                                    Server.class).newInstance(
                                                    (Server) null));
                                } catch (Exception e) {
                                    logger.log(Level.SEVERE,
                                            "Unable to register the server connector "
                                                    + provider, e);
                                }
                            }

                            line = reader.readLine();
                        }
                    } catch (IOException e) {
                        logger.log(Level.SEVERE,
                                "Unable to read the provider descriptor: "
                                        + configURL.toString());
                    } finally {
                        if (reader != null)
                            reader.close();
                    }
                }
            } catch (IOException ioe) {
                logger
                        .log(
                                Level.SEVERE,
                                "Exception while detecting the client connectors.",
                                ioe);
            }
        }
    }

    /**
     * Returns the list of available client connectors.
     * 
     * @return The list of available client connectors.
     */
    public List<ConnectorHelper> getRegisteredClients() {
        if (this.registeredClients == null)
            this.registeredClients = new ArrayList<ConnectorHelper>();
        return this.registeredClients;
    }

    /**
     * Returns the list of available server connectors.
     * 
     * @return The list of available server connectors.
     */
    public List<ConnectorHelper> getRegisteredServers() {
        if (this.registeredServers == null)
            this.registeredServers = new ArrayList<ConnectorHelper>();
        return this.registeredServers;
    }

    /**
     * Creates a directory resource.
     * 
     * @param handler
     *            The parent directory handler.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to return.
     * @return A new directory resource.
     * @throws IOException
     */
    public Resource createDirectoryResource(Directory handler, Request request,
            Response response) throws IOException {
        return new DirectoryResource(handler, request, response);
    }

    /**
     * Creates a new helper for a given component.
     * 
     * @param application
     *            The application to help.
     * @param parentContext
     *            The parent context, typically the component's context.
     * @return The new helper.
     */
    public Helper createHelper(Application application, Context parentContext) {
        return new ApplicationHelper(application, parentContext);
    }

    /**
     * Creates a new helper for a given client connector.
     * 
     * @param client
     *            The client to help.
     * @return The new helper.
     */
    public Helper createHelper(Client client) {
        Helper result = null;

        if (client.getProtocols().size() > 0) {
            for (ConnectorHelper connector : getRegisteredClients()) {
                if (connector.getProtocols().containsAll(client.getProtocols())) {
                    try {
                        return connector.getClass()
                                .getConstructor(Client.class).newInstance(
                                        client);
                    } catch (Exception e) {
                        logger
                                .log(
                                        Level.SEVERE,
                                        "Exception while instantiation the client connector.",
                                        e);
                    }

                    result = connector;
                }
            }

            if (result == null) {
                // Couldn't find a matching connector
                StringBuilder sb = new StringBuilder();
                sb
                        .append("No available client connector supports the required protocols: ");

                for (Protocol p : client.getProtocols()) {
                    sb.append(p.getName()).append(" ");
                }

                sb
                        .append(". Please add the JAR of a matching connector to your classpath.");

                logger.log(Level.WARNING, sb.toString());
            }
        }

        return result;
    }

    /**
     * Creates a new helper for a given component.
     * 
     * @param component
     *            The component to help.
     * @return The new helper.
     */
    public Helper createHelper(Component component) {
        return new ComponentHelper(component);
    }

    /**
     * Creates a new helper for a given server connector.
     * 
     * @param server
     *            The server to help.
     * @return The new helper.
     */
    public Helper createHelper(Server server) {
        Helper result = null;

        if (server.getProtocols().size() > 0) {
            for (ConnectorHelper connector : getRegisteredServers()) {
                if (connector.getProtocols().containsAll(server.getProtocols())) {
                    try {
                        result = connector.getClass().getConstructor(
                                Server.class).newInstance(server);
                    } catch (Exception e) {
                        logger
                                .log(
                                        Level.SEVERE,
                                        "Exception while instantiation the server connector.",
                                        e);
                    }
                }
            }

            if (result == null) {
                // Couldn't find a matching connector
                StringBuilder sb = new StringBuilder();
                sb
                        .append("No available server connector supports the required protocols: ");

                for (Protocol p : server.getProtocols()) {
                    sb.append(p.getName()).append(" ");
                }

                sb
                        .append(". Please add the JAR of a matching connector to your classpath.");

                logger.log(Level.WARNING, sb.toString());
            }
        }

        return result;
    }

    /**
     * Returns the preferred variant representation for a given resource
     * according the the client preferences.
     * 
     * @param client
     *            The client preferences.
     * @param variants
     *            The list of variants to compare.
     * @return The preferred variant.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public Variant getPreferredVariant(ClientInfo client,
            List<Variant> variants, Language defaultLanguage) {
        if (variants == null) {
            return null;
        } else {
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
            List<Preference<Language>> languagePrefs = client
                    .getAcceptedLanguages();
            List<Preference<Language>> primaryLanguagePrefs = new ArrayList<Preference<Language>>();
            Preference<Language> defaultLanguagePref = ((defaultLanguage == null) ? null
                    : new Preference<Language>(defaultLanguage, 0.001f));
            Preference<Language> allLanguagesPref = new Preference<Language>(
                    Language.ALL, 0.004f);

            if (languagePrefs.isEmpty()) {
                // All languages accepted.
                languagePrefs.add(new Preference<Language>(Language.ALL));
            } else {
                // Get the primary language preferences that are not currently
                // accepted by the client
                List<String> list = new ArrayList<String>();
                for (Preference<Language> preference : languagePrefs) {
                    Language language = preference.getMetadata();
                    if (!language.getSubTags().isEmpty()) {
                        if (!list.contains(language.getPrimaryTag())) {
                            list.add(language.getPrimaryTag());
                            primaryLanguagePrefs.add(new Preference<Language>(
                                    new Language(language.getPrimaryTag()),
                                    0.002f));
                        }
                    }
                }
            }

            // Client preferences are altered
            languagePrefs.addAll(primaryLanguagePrefs);
            languagePrefs.add(defaultLanguagePref);
            languagePrefs.add(allLanguagesPref);

            // For each available variant, we will compute the negotiation score
            // which is dependant on the language score and on the media type
            // score
            for (Iterator<Variant> iter1 = variants.iterator(); iter1.hasNext();) {
                currentVariant = iter1.next();
                variantLanguages = currentVariant.getLanguages();
                variantMediaType = currentVariant.getMediaType();

                // All languages of the current variant are scored.
                for (Language variantLanguage : variantLanguages) {
                    // For each language preference defined in the call
                    // Calculate the score and remember the best scoring
                    // preference
                    for (Iterator<Preference<Language>> iter2 = languagePrefs
                            .iterator(); (variantLanguage != null)
                            && iter2.hasNext();) {
                        currentLanguagePref = iter2.next();
                        float currentScore = getScore(variantLanguage,
                                currentLanguagePref.getMetadata());
                        boolean compatiblePref = (currentScore != -1.0f);
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
                List<Preference<MediaType>> mediaTypePrefs = client
                        .getAcceptedMediaTypes();
                if (mediaTypePrefs.size() == 0)
                    mediaTypePrefs
                            .add(new Preference<MediaType>(MediaType.ALL));

                // For each media range preference defined in the call
                // Calculate the score and remember the best scoring preference
                for (Iterator<Preference<MediaType>> iter2 = mediaTypePrefs
                        .iterator(); compatibleLanguage && iter2.hasNext();) {
                    currentMediaTypePref = iter2.next();
                    float currentScore = getScore(variantMediaType,
                            currentMediaTypePref.getMetadata());
                    boolean compatiblePref = (currentScore != -1.0f);
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
                int maxSize = Math.min(preferenceLanguage.getSubTags().size(),
                        variantLanguage.getSubTags().size());
                for (int i = 0; i < maxSize && compatibleLang; i++) {
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
                // Subtype are different
                comptabibleMediaType = false;
            }

            if (comptabibleMediaType
                    && (variantMediaType.getParameters() != null)) {
                // 3) Compare the parameters
                // If current media type is compatible with the
                // current media range then the parameters need to
                // be checked too
                for (Iterator iter3 = variantMediaType.getParameters()
                        .iterator(); iter3.hasNext();) {
                    Parameter currentParam = (Parameter) iter3.next();

                    if (isParameterFound(currentParam, preferenceMediaType)) {
                        score++;
                    }
                }
            }

        }
        return (comptabibleMediaType ? score : -1.0f);
    }

    /**
     * Parses a line to extract the provider class name.
     * 
     * @param line
     *            The line to parse.
     * @return The provider's class name or an empty string.
     */
    private String getProviderClassName(String line) {
        int index = line.indexOf('#');
        if (index != -1)
            line = line.substring(0, index);
        return line.trim();
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

        for (Iterator iter = mediaRange.getParameters().iterator(); !result
                && iter.hasNext();) {
            result = searchedParam.equals((Parameter) iter.next());
        }

        return result;
    }

    /**
     * Parses an URL encoded Web form.
     * 
     * @param logger
     *            The logger to use.
     * @param form
     *            The target form.
     * @param webForm
     *            The posted form.
     */
    public void parse(Logger logger, Form form, Representation webForm) {
        if (webForm != null) {
            FormUtils.parsePost(logger, form, webForm);
        }
    }

    /**
     * Parses an URL encoded query string into a given form.
     * 
     * @param logger
     *            The logger to use.
     * @param form
     *            The target form.
     * @param queryString
     *            Query string.
     * @param characterSet
     *            The supported character encoding.
     */
    public void parse(Logger logger, Form form, String queryString,
            CharacterSet characterSet) {
        if ((queryString != null) && !queryString.equals("")) {
            FormUtils.parseQuery(logger, form, queryString, characterSet);
        }
    }

}
