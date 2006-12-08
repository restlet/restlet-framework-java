/*
 * Copyright 2005-2006 Noelios Consulting.
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
import org.restlet.Container;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.Scorer;
import org.restlet.Server;
import org.restlet.data.ClientInfo;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.util.Helper;

import com.noelios.restlet.application.ApplicationHelper;
import com.noelios.restlet.container.ContainerHelper;
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
     *            The handled call.
     * @return A new directory resource.
     * @throws IOException
     */
    public Resource createDirectoryResource(Directory handler, Request request)
            throws IOException {
        return new DirectoryResource(handler, request);
    }

    /**
     * Creates a new helper for a given container.
     * 
     * @param application
     *            The application to help.
     * @param parentContext
     *            The parent context, typically the container's context.
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
        for (ConnectorHelper registeredClient : getRegisteredClients()) {
            if (registeredClient.getProtocols().containsAll(
                    client.getProtocols())) {
                try {
                    return registeredClient.getClass().getConstructor(
                            Client.class).newInstance(client);
                } catch (Exception e) {
                    logger
                            .log(
                                    Level.SEVERE,
                                    "Exception while instantiation the client connector.",
                                    e);
                }

                return registeredClient;
            }
        }

        logger.log(Level.WARNING,
                "No available client connector supports the required protocols: "
                        + client.getProtocols());
        return null;
    }

    /**
     * Creates a new helper for a given container.
     * 
     * @param container
     *            The container to help.
     * @return The new helper.
     */
    public Helper createHelper(Container container) {
        return new ContainerHelper(container);
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
            for (ConnectorHelper registeredServer : getRegisteredServers()) {
                if (registeredServer.getProtocols().containsAll(
                        server.getProtocols())) {
                    try {
                        result = registeredServer.getClass().getConstructor(
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

                logger.log(Level.WARNING, sb.toString());
            }
        }

        return result;
    }

    /**
     * Creates a URI-based Restlet attachment that will score target instance
     * shared by all calls. The score will be proportional to the number of
     * chararacters matched by the pattern, from the start of the context
     * resource path.
     * 
     * @param router
     *            The parent router.
     * @param pattern
     *            The URI pattern used to map calls (see
     *            {@link java.util.regex.Pattern} for the syntax).
     * @param target
     *            The target Restlet to attach.
     * @see java.util.regex.Pattern
     */
    public Scorer createScorer(Router router, String pattern, Restlet target) {
        return new PatternScorer(router, pattern, target);
    }

    /**
     * Returns the preferred variant representation for a given resource
     * according the the client preferences.
     * 
     * @param client
     *            The client preferences.
     * @param variants
     *            The list of variants to compare.
     * @return The best variant representation.
     * @see <a
     *      href="http://httpd.apache.org/docs/2.2/en/content-negotiation.html#algorithm">Apache
     *      content negotiation algorithm</a>
     */
    public Representation getPreferredVariant(ClientInfo client,
            List<Representation> variants) {
        if (variants == null) {
            return null;
        } else {
            Parameter currentParam = null;
            Language currentLanguage = null;
            Language variantLanguage = null;
            MediaType currentMediaType = null;
            MediaType variantMediaType = null;

            boolean compatiblePref = false;
            boolean compatibleLanguage = false;
            boolean compatibleMediaType = false;

            Representation currentVariant = null;
            Representation bestVariant = null;

            Preference<Language> currentLanguagePref = null;
            Preference<Language> bestLanguagePref = null;
            Preference<MediaType> currentMediaTypePref = null;
            Preference<MediaType> bestMediaTypePref = null;

            float bestQuality = 0;
            float currentScore = 0;
            float bestLanguageScore = 0;
            float bestMediaTypeScore = 0;

            // For each available variant, we will compute the negotiation score
            // which is dependant on the language score and on the media type
            // score
            for (Iterator iter1 = variants.iterator(); iter1.hasNext();) {
                currentVariant = (Representation) iter1.next();
                variantLanguage = currentVariant.getLanguage();
                variantMediaType = currentVariant.getMediaType();

                // If no language preference is defined, assume that all
                // languages are acceptable
                List<Preference<Language>> languagePrefs = client
                        .getAcceptedLanguages();
                if (languagePrefs.size() == 0)
                    languagePrefs.add(new Preference<Language>(Language.ALL));

                // For each language preference defined in the call
                // Calculate the score and remember the best scoring preference
                for (Iterator<Preference<Language>> iter2 = languagePrefs
                        .iterator(); (variantLanguage != null)
                        && iter2.hasNext();) {
                    currentLanguagePref = iter2.next();
                    currentLanguage = currentLanguagePref.getMetadata();
                    compatiblePref = true;
                    currentScore = 0;

                    // 1) Compare the main tag
                    if (variantLanguage.getMainTag().equalsIgnoreCase(
                            currentLanguage.getMainTag())) {
                        currentScore += 100;
                    } else if (!currentLanguage.getMainTag().equals("*")) {
                        compatiblePref = false;
                    } else if (currentLanguage.getSubTag() != null) {
                        // Only "*" is an acceptable language range
                        compatiblePref = false;
                    } else {
                        // The valid "*" range has the lowest valid score
                        currentScore++;
                    }

                    if (compatiblePref) {
                        // 2) Compare the sub tags
                        if ((currentLanguage.getSubTag() == null)
                                || (variantLanguage.getSubTag() == null)) {
                            if (variantLanguage.getSubTag() == currentLanguage
                                    .getSubTag()) {
                                currentScore += 10;
                            } else {
                                // Don't change the score
                            }
                        } else if (currentLanguage.getSubTag().equalsIgnoreCase(
                                variantLanguage.getSubTag())) {
                            currentScore += 10;
                        } else {
                            // SubTags are different
                            compatiblePref = false;
                        }

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
                compatibleLanguage = (variantLanguage == null)
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
                    currentMediaType = currentMediaTypePref.getMetadata();
                    compatiblePref = true;
                    currentScore = 0;

                    // 1) Compare the main types
                    if (currentMediaType.getMainType().equals(
                            variantMediaType.getMainType())) {
                        currentScore += 1000;
                    } else if (!currentMediaType.getMainType().equals("*")) {
                        compatiblePref = false;
                    } else if (!currentMediaType.getSubType().equals("*")) {
                        // Ranges such as "*/html" are not supported
                        // Only "*/*" is acceptable in this case
                        compatiblePref = false;
                    }

                    if (compatiblePref) {
                        // 2) Compare the sub types
                        if (variantMediaType.getSubType().equals(
                                currentMediaType.getSubType())) {
                            currentScore += 100;
                        } else if (!currentMediaType.getSubType().equals("*")) {
                            // Subtype are different
                            compatiblePref = false;
                        }

                        if (compatiblePref
                                && (variantMediaType.getParameters() != null)) {
                            // 3) Compare the parameters
                            // If current media type is compatible with the
                            // current
                            // media range then the parameters need to be
                            // checked too
                            for (Iterator iter3 = variantMediaType
                                    .getParameters().iterator(); iter3
                                    .hasNext();) {
                                currentParam = (Parameter) iter3.next();

                                if (isParameterFound(currentParam,
                                        currentMediaType)) {
                                    currentScore++;
                                }
                            }
                        }

                        // 3) Do we have a better preference?
                        // currentScore *= currentPref.getQuality();
                        if (compatiblePref
                                && ((bestMediaTypePref == null) || (currentScore > bestMediaTypeScore))) {
                            bestMediaTypePref = currentMediaTypePref;
                            bestMediaTypeScore = currentScore;
                        }
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
                    } else if (variantLanguage != null) {
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
     */
    public void parse(Logger logger, Form form, String queryString) {
        if ((queryString != null) && !queryString.equals("")) {
            FormUtils.parseQuery(logger, form, queryString);
        }
    }

}
