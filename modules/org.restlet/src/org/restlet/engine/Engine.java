/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.engine.log.LoggerFacade;

/**
 * Engine supporting the Restlet API. The engine acts as a registry of various
 * {@link Helper} types: {@link org.restlet.engine.security.AuthenticatorHelper}
 * , {@link ClientHelper}, {@link org.restlet.engine.converter.ConverterHelper}
 * and {@link ServerHelper} classes.<br>
 * <br>
 * Note that by default the JULI logging mechanism is used but it is possible to
 * replace it by providing an alternate {@link LoggerFacade} implementation. For
 * this, just pass a system property named
 * "org.restlet.engine.loggerFacadeClass" with the qualified class name as a
 * value.
 * 
 * @author Jerome Louvel
 */
public class Engine {

    // [ifndef gwt]
    /** Engine class loader to use for dynamic class loading. */
    private static volatile ClassLoader classLoader = new org.restlet.engine.util.EngineClassLoader();

    public static final String DESCRIPTOR = "META-INF/services";

    public static final String DESCRIPTOR_AUTHENTICATOR = "org.restlet.engine.security.AuthenticatorHelper";

    public static final String DESCRIPTOR_AUTHENTICATOR_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_AUTHENTICATOR;

    public static final String DESCRIPTOR_CLIENT = "org.restlet.engine.ClientHelper";

    public static final String DESCRIPTOR_CLIENT_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_CLIENT;

    public static final String DESCRIPTOR_CONVERTER = "org.restlet.engine.converter.ConverterHelper";

    public static final String DESCRIPTOR_CONVERTER_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_CONVERTER;

    public static final String DESCRIPTOR_SERVER = "org.restlet.engine.ServerHelper";

    public static final String DESCRIPTOR_SERVER_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_SERVER;

    // [enddef]

    /** The registered engine. */
    private static volatile Engine instance = null;

    /** Major version number. */
    public static final String MAJOR_NUMBER = "@major-number@";

    /** Minor version number. */
    public static final String MINOR_NUMBER = "@minor-number@";

    /** Release number. */
    public static final String RELEASE_NUMBER = "@release-type@@release-number@";

    // [ifndef gwt] member
    /** User class loader to use for dynamic class loading. */
    private static volatile ClassLoader userClassLoader;

    /** Complete version. */
    public static final String VERSION = MAJOR_NUMBER + '.' + MINOR_NUMBER
            + RELEASE_NUMBER;

    /** Complete version header. */
    public static final String VERSION_HEADER = "Restlet-Framework/" + VERSION;

    /**
     * Returns an anonymous logger. By default it calls
     * {@link #getLogger(String)} with a "" name.
     * 
     * @return The logger.
     */
    public static Logger getAnonymousLogger() {
        return getInstance().getLoggerFacade().getAnonymousLogger();
    }

    // [ifndef gwt] method
    /**
     * Returns the engine class loader. It uses the delegation model with the
     * Engine class's class loader as a parent. If this parent doesn't find a
     * class or resource, it then tries the user class loader (via
     * {@link #getUserClassLoader()} and finally the
     * {@link Thread#getContextClassLoader()}.
     * 
     * @return The engine class loader.
     * @see org.restlet.engine.util.EngineClassLoader
     */
    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the registered Restlet engine.
     * 
     * @return The registered Restlet engine.
     */
    public static synchronized Engine getInstance() {
        Engine result = instance;

        if (result == null) {
            result = register();
        }

        return result;
    }

    /**
     * Returns a logger based on the class name of the given object.
     * 
     * @param clazz
     *            The parent class.
     * @return The logger.
     */
    public static Logger getLogger(Class<?> clazz) {
        return getInstance().getLoggerFacade().getLogger(clazz);
    }

    /**
     * Returns a logger based on the class name of the given object.
     * 
     * @param clazz
     *            The parent class.
     * @param defaultLoggerName
     *            The default logger name to use if no one can be inferred from
     *            the class.
     * @return The logger.
     */
    public static Logger getLogger(Class<?> clazz, String defaultLoggerName) {
        return getInstance().getLoggerFacade().getLogger(clazz,
                defaultLoggerName);
    }

    /**
     * Returns a logger based on the class name of the given object.
     * 
     * @param object
     *            The parent object.
     * @param defaultLoggerName
     *            The default logger name to use if no one can be inferred from
     *            the object class.
     * @return The logger.
     */
    public static Logger getLogger(Object object, String defaultLoggerName) {
        return getInstance().getLoggerFacade().getLogger(object,
                defaultLoggerName);
    }

    /**
     * Returns a logger based on the given logger name.
     * 
     * @param loggerName
     *            The logger name.
     * @return The logger.
     */
    public static Logger getLogger(String loggerName) {
        return getInstance().getLoggerFacade().getLogger(loggerName);
    }

    // [ifndef gwt] method
    /**
     * Returns the class loader specified by the user and that should be used in
     * priority.
     * 
     * @return The user class loader
     */
    public static ClassLoader getUserClassLoader() {
        return userClassLoader;
    }

    // [ifndef gwt] method
    /**
     * Returns the class object for the given name using the engine classloader.
     * 
     * @param className
     *            The class name to lookup.
     * @return The class object or null if the class was not found.
     * @see #getClassLoader()
     */
    public static Class<?> loadClass(String className)
            throws ClassNotFoundException {
        return getClassLoader().loadClass(className);
    }

    /**
     * Registers a new Restlet Engine.
     * 
     * @return The registered engine.
     */
    public static synchronized Engine register() {
        return register(true);
    }

    /**
     * Registers a new Restlet Engine.
     * 
     * @param discoverPlugins
     *            True if plug-ins should be automatically discovered.
     * @return The registered engine.
     */
    public static synchronized Engine register(boolean discoverPlugins) {
        final Engine result = new Engine(discoverPlugins);
        org.restlet.engine.Engine.setInstance(result);
        return result;
    }

    /**
     * Sets the registered Restlet engine.
     * 
     * @param engine
     *            The registered Restlet engine.
     */
    public static synchronized void setInstance(Engine engine) {
        instance = engine;
    }

    // [ifndef gwt] method
    /**
     * Sets the user class loader that should used in priority.
     * 
     * @param newClassLoader
     *            The new user class loader to use.
     */
    public static void setUserClassLoader(ClassLoader newClassLoader) {
        userClassLoader = newClassLoader;
    }

    /** The logger facade to use. */
    private LoggerFacade loggerFacade;

    // [ifndef gwt] member
    /** List of available authenticator helpers. */
    private final List<org.restlet.engine.security.AuthenticatorHelper> registeredAuthenticators;

    /** List of available client connectors. */
    private final List<ClientHelper> registeredClients;

    // [ifndef gwt] member
    /** List of available converter helpers. */
    private final List<org.restlet.engine.converter.ConverterHelper> registeredConverters;

    // [ifndef gwt] member
    /** List of available server connectors. */
    private final List<ServerHelper> registeredServers;

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
        // Instantiate the logger facade
        if (Edition.CURRENT == Edition.GWT) {
            this.loggerFacade = new LoggerFacade();
        } else {
            // [ifndef gwt]
            String loggerFacadeClass = System.getProperty(
                    "org.restlet.engine.loggerFacadeClass",
                    "org.restlet.engine.log.LoggerFacade");
            try {
                this.loggerFacade = (LoggerFacade) loadClass(loggerFacadeClass)
                        .newInstance();
            } catch (Exception e) {
                this.loggerFacade = new LoggerFacade();
                this.loggerFacade.getLogger("org.restlet").log(Level.WARNING,
                        "Unable to register the logger facade", e);
            }
            // [enddef]
        }

        this.registeredClients = new CopyOnWriteArrayList<ClientHelper>();

        // [ifndef gwt]
        this.registeredServers = new CopyOnWriteArrayList<ServerHelper>();
        this.registeredAuthenticators = new CopyOnWriteArrayList<org.restlet.engine.security.AuthenticatorHelper>();
        this.registeredConverters = new CopyOnWriteArrayList<org.restlet.engine.converter.ConverterHelper>();
        // [enddef]

        if (discoverHelpers) {
            try {
                discoverConnectors();

                // [ifndef gwt]
                discoverAuthenticators();
                discoverConverters();
                // [enddef]
            } catch (IOException e) {
                Context
                        .getCurrentLogger()
                        .log(
                                Level.WARNING,
                                "An error occured while discovering the engine helpers.",
                                e);
            }
        }
    }

    /**
     * Creates a new helper for a given client connector.
     * 
     * @param client
     *            The client to help.
     * @param helperClass
     *            Optional helper class name.
     * @return The new helper.
     */
    public ClientHelper createHelper(Client client, String helperClass) {
        ClientHelper result = null;

        if (client.getProtocols().size() > 0) {
            ClientHelper connector = null;
            for (final Iterator<ClientHelper> iter = getRegisteredClients()
                    .iterator(); (result == null) && iter.hasNext();) {
                connector = iter.next();

                if (connector.getProtocols().containsAll(client.getProtocols())) {
                    // [ifndef gwt]
                    if ((helperClass == null)
                            || connector.getClass().getCanonicalName().equals(
                                    helperClass)) {
                        try {
                            result = connector.getClass().getConstructor(
                                    Client.class).newInstance(client);
                        } catch (Exception e) {
                            Context
                                    .getCurrentLogger()
                                    .log(
                                            Level.SEVERE,
                                            "Exception while instantiation the client connector.",
                                            e);
                        }
                    }
                    // [enddef]
                    // [ifdef gwt] instruction uncomment
                    // result = new
                    // org.restlet.engine.http.GwtHttpClientHelper(client);
                }
            }

            if (result == null) {
                // Couldn't find a matching connector
                StringBuilder sb = new StringBuilder();
                sb
                        .append("No available client connector supports the required protocols: ");

                for (Protocol p : client.getProtocols()) {
                    sb.append("'").append(p.getName()).append("' ");
                }

                sb
                        .append(". Please add the JAR of a matching connector to your classpath.");

                Context.getCurrentLogger().log(Level.WARNING, sb.toString());
            }
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Creates a new helper for a given server connector.
     * 
     * @param server
     *            The server to help.
     * @param helperClass
     *            Optional helper class name.
     * @return The new helper.
     */
    public ServerHelper createHelper(org.restlet.Server server,
            String helperClass) {
        ServerHelper result = null;

        if (server.getProtocols().size() > 0) {
            ServerHelper connector = null;
            for (final Iterator<ServerHelper> iter = getRegisteredServers()
                    .iterator(); (result == null) && iter.hasNext();) {
                connector = iter.next();

                if ((helperClass == null)
                        || connector.getClass().getCanonicalName().equals(
                                helperClass)) {
                    if (connector.getProtocols().containsAll(
                            server.getProtocols())) {
                        try {
                            result = connector.getClass().getConstructor(
                                    org.restlet.Server.class).newInstance(
                                    server);
                        } catch (Exception e) {
                            Context
                                    .getCurrentLogger()
                                    .log(
                                            Level.SEVERE,
                                            "Exception while instantiation the server connector.",
                                            e);
                        }
                    }
                }
            }

            if (result == null) {
                // Couldn't find a matching connector
                final StringBuilder sb = new StringBuilder();
                sb
                        .append("No available server connector supports the required protocols: ");

                for (final Protocol p : server.getProtocols()) {
                    sb.append("'").append(p.getName()).append("' ");
                }

                sb
                        .append(". Please add the JAR of a matching connector to your classpath.");

                Context.getCurrentLogger().log(Level.WARNING, sb.toString());
            }
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Discovers the authenticator helpers and register the default helpers.
     * 
     * @throws IOException
     */
    private void discoverAuthenticators() throws IOException {
        registerHelpers(DESCRIPTOR_AUTHENTICATOR_PATH,
                getRegisteredAuthenticators(), null);
        registerDefaultAuthentications();
    }

    /**
     * Discovers the server and client connectors and register the default
     * connectors.
     * 
     * @throws IOException
     */
    private void discoverConnectors() throws IOException {
        // [ifndef gwt]
        registerHelpers(DESCRIPTOR_CLIENT_PATH, getRegisteredClients(),
                Client.class);
        registerHelpers(DESCRIPTOR_SERVER_PATH, getRegisteredServers(),
                org.restlet.Server.class);
        // [enddef]
        registerDefaultConnectors();
    }

    // [ifndef gwt] method
    /**
     * Discovers the converter helpers and register the default helpers.
     * 
     * @throws IOException
     */
    private void discoverConverters() throws IOException {
        registerHelpers(DESCRIPTOR_CONVERTER_PATH, getRegisteredConverters(),
                null);
        registerDefaultConverters();
    }

    // [ifndef gwt] method
    /**
     * Finds the converter helper supporting the given conversion.
     * 
     * @return The converter helper or null.
     */
    public org.restlet.engine.converter.ConverterHelper findHelper() {

        return null;
    }

    // [ifndef gwt] method
    /**
     * Finds the authenticator helper supporting the given scheme.
     * 
     * @param challengeScheme
     *            The challenge scheme to match.
     * @param clientSide
     *            Indicates if client side support is required.
     * @param serverSide
     *            Indicates if server side support is required.
     * @return The authenticator helper or null.
     */
    public org.restlet.engine.security.AuthenticatorHelper findHelper(
            ChallengeScheme challengeScheme, boolean clientSide,
            boolean serverSide) {
        org.restlet.engine.security.AuthenticatorHelper result = null;
        final List<org.restlet.engine.security.AuthenticatorHelper> helpers = getRegisteredAuthenticators();
        org.restlet.engine.security.AuthenticatorHelper current;

        for (int i = 0; (result == null) && (i < helpers.size()); i++) {
            current = helpers.get(i);

            if (current.getChallengeScheme().equals(challengeScheme)
                    && ((clientSide && current.isClientSide()) || !clientSide)
                    && ((serverSide && current.isServerSide()) || !serverSide)) {
                result = helpers.get(i);
            }
        }

        return result;
    }

    /**
     * Returns the logger facade to use.
     * 
     * @return The logger facade to use.
     */
    public LoggerFacade getLoggerFacade() {
        return loggerFacade;
    }

    /**
     * Parses a line to extract the provider class name.
     * 
     * @param line
     *            The line to parse.
     * @return The provider's class name or an empty string.
     */
    private String getProviderClassName(String line) {
        final int index = line.indexOf('#');
        if (index != -1) {
            line = line.substring(0, index);
        }
        return line.trim();
    }

    // [ifndef gwt] method
    /**
     * Returns the list of available authentication helpers.
     * 
     * @return The list of available authentication helpers.
     */
    public List<org.restlet.engine.security.AuthenticatorHelper> getRegisteredAuthenticators() {
        return this.registeredAuthenticators;
    }

    /**
     * Returns the list of available client connectors.
     * 
     * @return The list of available client connectors.
     */
    public List<ClientHelper> getRegisteredClients() {
        return this.registeredClients;
    }

    // [ifndef gwt] method
    /**
     * Returns the list of available converters.
     * 
     * @return The list of available converters.
     */
    public List<org.restlet.engine.converter.ConverterHelper> getRegisteredConverters() {
        return registeredConverters;
    }

    // [ifndef gwt] method
    /**
     * Returns the list of available server connectors.
     * 
     * @return The list of available server connectors.
     */
    public List<ServerHelper> getRegisteredServers() {
        return this.registeredServers;
    }

    // [ifndef gwt] method
    /**
     * Registers the default authentication helpers.
     */
    public void registerDefaultAuthentications() {
        getRegisteredAuthenticators().add(
                new org.restlet.engine.http.security.HttpBasicHelper());
        getRegisteredAuthenticators().add(
                new org.restlet.engine.security.SmtpPlainHelper());
    }

    /**
     * Registers the default client and server connectors.
     */
    public void registerDefaultConnectors() {
        // [ifndef gae, gwt]
        getRegisteredClients().add(
                new org.restlet.engine.http.StreamClientHelper(null));
        // [enddef]
        // [ifndef gwt]
        getRegisteredClients().add(
                new org.restlet.engine.local.ClapClientHelper(null));
        getRegisteredClients().add(
                new org.restlet.engine.local.FileClientHelper(null));
        getRegisteredClients().add(
                new org.restlet.engine.local.ZipClientHelper(null));
        getRegisteredClients().add(
                new org.restlet.engine.riap.RiapClientHelper(null));
        getRegisteredServers().add(
                new org.restlet.engine.riap.RiapServerHelper(null));
        // [enddef]
        // [ifndef gae, gwt]
        getRegisteredServers().add(
                new org.restlet.engine.http.StreamServerHelper(null));
        // [enddef]
        // [ifdef gwt] uncomment
        // getRegisteredClients().add(
        // new org.restlet.engine.http.GwtHttpClientHelper(null));
        // [enddef]
    }

    // [ifndef gwt] method
    /**
     * Registers the default converters.
     */
    public void registerDefaultConverters() {
        getRegisteredConverters().add(
                new org.restlet.engine.converter.DefaultConverter());
    }

    // [ifndef gwt] method
    /**
     * Registers a helper.
     * 
     * @param classLoader
     *            The classloader to use.
     * @param provider
     *            Bynary name of the helper's class.
     * @param helpers
     *            The list of helpers to update.
     * @param constructorClass
     *            The constructor parameter class to look for.
     */
    @SuppressWarnings("unchecked")
    public void registerHelper(ClassLoader classLoader, String provider,
            List helpers, Class constructorClass) {
        if ((provider != null) && (!provider.equals(""))) {
            // Instantiate the factory
            try {
                Class providerClass = classLoader.loadClass(provider);

                if (constructorClass == null) {
                    helpers.add(providerClass.newInstance());
                } else {
                    helpers.add(providerClass.getConstructor(constructorClass)
                            .newInstance(constructorClass.cast(null)));
                }
            } catch (Throwable t) {
                Context.getCurrentLogger().log(Level.INFO,
                        "Unable to register the helper " + provider, t);
            }
        }
    }

    // [ifndef gwt] method
    /**
     * Registers a helper.
     * 
     * @param classLoader
     *            The classloader to use.
     * @param configUrl
     *            Configuration URL to parse
     * @param helpers
     *            The list of helpers to update.
     * @param constructorClass
     *            The constructor parameter class to look for.
     */
    @SuppressWarnings("unchecked")
    public void registerHelpers(ClassLoader classLoader,
            java.net.URL configUrl, List helpers, Class constructorClass) {
        try {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(configUrl
                        .openStream(), "utf-8"));
                String line = reader.readLine();

                while (line != null) {
                    registerHelper(classLoader, getProviderClassName(line),
                            helpers, constructorClass);
                    line = reader.readLine();
                }
            } catch (IOException e) {
                Context.getCurrentLogger().log(
                        Level.SEVERE,
                        "Unable to read the provider descriptor: "
                                + configUrl.toString());
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        } catch (IOException ioe) {
            Context.getCurrentLogger().log(Level.SEVERE,
                    "Exception while detecting the helpers.", ioe);
        }
    }

    // [ifndef gwt] method
    /**
     * Registers a list of helpers.
     * 
     * @param descriptorPath
     *            Classpath to the descriptor file.
     * @param helpers
     *            The list of helpers to update.
     * @param constructorClass
     *            The constructor parameter class to look for.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void registerHelpers(String descriptorPath, List helpers,
            Class constructorClass) throws IOException {
        final ClassLoader classLoader = org.restlet.engine.Engine
                .getClassLoader();
        Enumeration<java.net.URL> configUrls = classLoader
                .getResources(descriptorPath);

        if (configUrls != null) {
            for (final Enumeration<java.net.URL> configEnum = configUrls; configEnum
                    .hasMoreElements();) {
                registerHelpers(classLoader, configEnum.nextElement(), helpers,
                        constructorClass);
            }
        }
    }

    // [ifndef gwt] method
    /**
     * Registers a factory that is used by the URL class to create the
     * {@link java.net.URLConnection} instances when the
     * {@link java.net.URL#openConnection()} or
     * {@link java.net.URL#openStream()} methods are invoked.
     * <p>
     * The implementation is based on the client dispatcher of the current
     * context, as provided by {@link Context#getCurrent()} method.
     */
    public void registerUrlFactory() {
        // Set up an java.net.URLStreamHandlerFactory for
        // proper creation of java.net.URL instances
        java.net.URL
                .setURLStreamHandlerFactory(new java.net.URLStreamHandlerFactory() {
                    public java.net.URLStreamHandler createURLStreamHandler(
                            String protocol) {
                        final java.net.URLStreamHandler result = new java.net.URLStreamHandler() {

                            @Override
                            protected java.net.URLConnection openConnection(
                                    java.net.URL url) throws IOException {
                                return new java.net.URLConnection(url) {

                                    @Override
                                    public void connect() throws IOException {
                                    }

                                    @Override
                                    public InputStream getInputStream()
                                            throws IOException {
                                        InputStream result = null;

                                        // Retrieve the current context
                                        final Context context = Context
                                                .getCurrent();

                                        if (context != null) {
                                            final Response response = context
                                                    .getClientDispatcher()
                                                    .get(this.url.toString());

                                            if (response.getStatus()
                                                    .isSuccess()) {
                                                result = response.getEntity()
                                                        .getStream();
                                            }
                                        }

                                        return result;
                                    }
                                };
                            }

                        };

                        return result;
                    }

                });
    }

    /**
     * Sets the logger facade to use.
     * 
     * @param loggerFacade
     *            The logger facade to use.
     */
    public void setLoggerFacade(LoggerFacade loggerFacade) {
        this.loggerFacade = loggerFacade;
    }

    // [ifndef gwt] method
    /**
     * Sets the list of available authentication helpers.
     * 
     * @param registeredAuthenticators
     *            The list of available authentication helpers.
     */
    public void setRegisteredAuthenticators(
            List<org.restlet.engine.security.AuthenticatorHelper> registeredAuthenticators) {
        synchronized (this.registeredAuthenticators) {
            this.registeredAuthenticators.clear();

            if (registeredAuthenticators != null) {
                this.registeredAuthenticators.addAll(registeredAuthenticators);
            }
        }
    }

    /**
     * Sets the list of available client helpers.
     * 
     * @param registeredClients
     *            The list of available client helpers.
     */
    public void setRegisteredClients(List<ClientHelper> registeredClients) {
        synchronized (this.registeredClients) {
            this.registeredClients.clear();

            if (registeredClients != null) {
                this.registeredClients.addAll(registeredClients);
            }
        }
    }

    // [ifndef gwt] method
    /**
     * Sets the list of available converter helpers.
     * 
     * @param registeredConverters
     *            The list of available converter helpers.
     */
    public void setRegisteredConverters(
            List<org.restlet.engine.converter.ConverterHelper> registeredConverters) {
        synchronized (this.registeredConverters) {
            this.registeredConverters.clear();

            if (registeredConverters != null) {
                this.registeredConverters.addAll(registeredConverters);
            }
        }
    }

    // [ifndef gwt] method
    /**
     * Sets the list of available server helpers.
     * 
     * @param registeredServers
     *            The list of available server helpers.
     */
    public void setRegisteredServers(List<ServerHelper> registeredServers) {
        synchronized (this.registeredServers) {
            this.registeredServers.clear();

            if (registeredServers != null) {
                this.registeredServers.addAll(registeredServers);
            }
        }
    }

}
