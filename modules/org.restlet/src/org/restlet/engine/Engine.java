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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.data.Response;
import org.restlet.engine.component.RiapClientHelper;
import org.restlet.engine.component.RiapServerHelper;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.converter.DefaultConverter;
import org.restlet.engine.local.ClapClientHelper;
import org.restlet.engine.local.FileClientHelper;
import org.restlet.engine.local.ZipClientHelper;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.engine.security.HttpAwsS3Helper;
import org.restlet.engine.security.HttpBasicHelper;
import org.restlet.engine.security.HttpDigestHelper;
import org.restlet.engine.security.HttpMsSharedKeyHelper;
import org.restlet.engine.security.HttpMsSharedKeyLiteHelper;
import org.restlet.engine.security.SmtpPlainHelper;
import org.restlet.engine.util.EngineClassLoader;

/**
 * Engine supporting the Restlet API.
 * 
 * @author Jerome Louvel
 */
public class Engine {

    /** Engine class loader to use for dynamic class loading. */
    private static volatile ClassLoader classLoader = new EngineClassLoader();

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

    /** The registered engine. */
    private static volatile Engine instance = null;

    /** Major version number. */
    public static final String MAJOR_NUMBER = "@major-number@";

    /** Minor version number. */
    public static final String MINOR_NUMBER = "@minor-number@";

    /** Release number. */
    public static final String RELEASE_NUMBER = "@release-type@@release-number@";

    /** User class loader to use for dynamic class loading. */
    private static volatile ClassLoader userClassLoader;

    /** Complete version. */
    public static final String VERSION = MAJOR_NUMBER + '.' + MINOR_NUMBER
            + RELEASE_NUMBER;

    /** Complete version header. */
    public static final String VERSION_HEADER = "Noelios-Restlet/" + VERSION;

    /**
     * Returns the engine class loader. It uses the delegation model with the
     * Engine class's class loader as a parent. If this parent doesn't find a
     * class or resource, it then tries the user class loader (via
     * {@link #getUserClassLoader()} and finally the
     * {@link Thread#getContextClassLoader()}.
     * 
     * @return The engine class loader.
     * @see EngineClassLoader
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
     * Returns the class loader specified by the user and that should be used in
     * priority.
     * 
     * @return The user class loader
     */
    public static ClassLoader getUserClassLoader() {
        return userClassLoader;
    }

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

    /**
     * Sets the user class loader that should used in priority.
     * 
     * @param newClassLoader
     *            The new user class loader to use.
     */
    public static void setUserClassLoader(ClassLoader newClassLoader) {
        userClassLoader = newClassLoader;
    }

    /** List of available authenticator helpers. */
    private final List<AuthenticatorHelper> registeredAuthenticators;

    /** List of available client connectors. */
    private final List<ClientHelper> registeredClients;

    /** List of available converter helpers. */
    private final List<ConverterHelper> registeredConverters;

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
        this.registeredClients = new CopyOnWriteArrayList<ClientHelper>();
        this.registeredServers = new CopyOnWriteArrayList<ServerHelper>();
        this.registeredAuthenticators = new CopyOnWriteArrayList<AuthenticatorHelper>();
        this.registeredConverters = new CopyOnWriteArrayList<ConverterHelper>();

        if (discoverHelpers) {
            try {
                discoverConnectors();
                discoverAuthenticators();
                discoverConverters();
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

                Context.getCurrentLogger().log(Level.WARNING, sb.toString());
            }
        }

        return result;
    }

    /**
     * Creates a new helper for a given server connector.
     * 
     * @param server
     *            The server to help.
     * @param helperClass
     *            Optional helper class name.
     * @return The new helper.
     */
    public ServerHelper createHelper(Server server, String helperClass) {
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
                                    Server.class).newInstance(server);
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
        registerHelpers(DESCRIPTOR_CLIENT_PATH, getRegisteredClients(),
                Client.class);
        registerHelpers(DESCRIPTOR_SERVER_PATH, getRegisteredServers(),
                Server.class);
        registerDefaultConnectors();
    }

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

    /**
     * Finds the converter helper supporting the given conversion.
     * 
     * @return The authenticator helper or null.
     */
    public ConverterHelper findHelper() {

        return null;
    }

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
    public AuthenticatorHelper findHelper(ChallengeScheme challengeScheme,
            boolean clientSide, boolean serverSide) {
        AuthenticatorHelper result = null;
        final List<AuthenticatorHelper> helpers = getRegisteredAuthenticators();
        AuthenticatorHelper current;

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

    /**
     * Returns the list of available authentication helpers.
     * 
     * @return The list of available authentication helpers.
     */
    public List<AuthenticatorHelper> getRegisteredAuthenticators() {
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

    /**
     * Returns the list of available converters.
     * 
     * @return The list of available converters.
     */
    public List<ConverterHelper> getRegisteredConverters() {
        return registeredConverters;
    }

    /**
     * Returns the list of available server connectors.
     * 
     * @return The list of available server connectors.
     */
    public List<ServerHelper> getRegisteredServers() {
        return this.registeredServers;
    }

    /**
     * Registers the default authentication helpers.
     */
    public void registerDefaultAuthentications() {
        getRegisteredAuthenticators().add(new HttpBasicHelper());
        getRegisteredAuthenticators().add(new HttpDigestHelper());
        getRegisteredAuthenticators().add(new SmtpPlainHelper());
        getRegisteredAuthenticators().add(new HttpAwsS3Helper());
        getRegisteredAuthenticators().add(new HttpMsSharedKeyHelper());
        getRegisteredAuthenticators().add(new HttpMsSharedKeyLiteHelper());
    }

    /**
     * Registers the default client and server connectors.
     */
    public void registerDefaultConnectors() {
        // [ifndef gae]
        getRegisteredClients().add(
                new org.restlet.engine.http.StreamClientHelper(null));
        // [enddef]
        getRegisteredClients().add(new ClapClientHelper(null));
        getRegisteredClients().add(new FileClientHelper(null));
        getRegisteredClients().add(new ZipClientHelper(null));
        getRegisteredClients().add(new RiapClientHelper(null));
        // [ifndef gae]
        getRegisteredServers().add(
                new org.restlet.engine.http.StreamServerHelper(null));
        // [enddef]
        getRegisteredServers().add(new RiapServerHelper(null));
    }

    /**
     * Registers the default converters.
     */
    public void registerDefaultConverters() {
        getRegisteredConverters().add(new DefaultConverter());
    }

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
    public void registerHelper(ClassLoader classLoader, URL configUrl,
            List helpers, Class constructorClass) {
        try {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(configUrl
                        .openStream(), "utf-8"));
                String line = reader.readLine();

                while (line != null) {
                    final String provider = getProviderClassName(line);

                    if ((provider != null) && (!provider.equals(""))) {
                        // Instantiate the factory
                        try {
                            final Class providerClass = classLoader
                                    .loadClass(provider);

                            if (constructorClass == null) {
                                helpers.add(providerClass.newInstance());
                            } else {
                                helpers.add(providerClass.getConstructor(
                                        constructorClass).newInstance(
                                        constructorClass.cast(null)));
                            }
                        } catch (Exception e) {
                            Context.getCurrentLogger()
                                    .log(
                                            Level.SEVERE,
                                            "Unable to register the helper "
                                                    + provider, e);
                        }
                    }

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
        Enumeration<URL> configUrls = classLoader.getResources(descriptorPath);

        if (configUrls != null) {
            for (final Enumeration<URL> configEnum = configUrls; configEnum
                    .hasMoreElements();) {
                registerHelper(classLoader, configEnum.nextElement(), helpers,
                        constructorClass);
            }
        }
    }

    /**
     * Registers a factory that is used by the URL class to create the
     * {@link URLConnection} instances when the {@link URL#openConnection()} or
     * {@link URL#openStream()} methods are invoked.
     * <p>
     * The implementation is based on the client dispatcher of the current
     * context, as provided by {@link Context#getCurrent()} method.
     */
    public void registerUrlFactory() {
        // Set up an URLStreamHandlerFactory for
        // proper creation of java.net.URL instances
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            public URLStreamHandler createURLStreamHandler(String protocol) {
                final URLStreamHandler result = new URLStreamHandler() {

                    @Override
                    protected URLConnection openConnection(URL url)
                            throws IOException {
                        return new URLConnection(url) {

                            @Override
                            public void connect() throws IOException {
                            }

                            @Override
                            public InputStream getInputStream()
                                    throws IOException {
                                InputStream result = null;

                                // Retrieve the current context
                                final Context context = Context.getCurrent();

                                if (context != null) {
                                    final Response response = context
                                            .getClientDispatcher().get(
                                                    this.url.toString());

                                    if (response.getStatus().isSuccess()) {
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
     * Sets the list of available authentication helpers.
     * 
     * @param registeredAuthenticators
     *            The list of available authentication helpers.
     */
    public void setRegisteredAuthenticators(
            List<AuthenticatorHelper> registeredAuthenticators) {
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

    /**
     * Sets the list of available converter helpers.
     * 
     * @param registeredConverters
     *            The list of available converter helpers.
     */
    public void setRegisteredConverters(
            List<ConverterHelper> registeredConverters) {
        synchronized (this.registeredConverters) {
            this.registeredConverters.clear();

            if (registeredConverters != null) {
                this.registeredConverters.addAll(registeredConverters);
            }
        }
    }

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
