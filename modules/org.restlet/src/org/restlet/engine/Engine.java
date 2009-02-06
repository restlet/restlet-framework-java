/**
 * Copyright 2005-2009 Noelios Technologies.
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
import org.restlet.engine.authentication.AuthenticationHelper;
import org.restlet.engine.authentication.HttpAmazonS3Helper;
import org.restlet.engine.authentication.HttpBasicHelper;
import org.restlet.engine.authentication.HttpDigestHelper;
import org.restlet.engine.authentication.SharedKeyLiteMicrosoftHelper;
import org.restlet.engine.authentication.SharedKeyMicrosoftHelper;
import org.restlet.engine.authentication.SmtpPlainHelper;
import org.restlet.engine.http.StreamClientHelper;
import org.restlet.engine.http.StreamServerHelper;
import org.restlet.engine.local.ClapClientHelper;
import org.restlet.engine.local.FileClientHelper;

/**
 * Engine supporting the Restlet API.
 * 
 * @author Jerome Louvel
 */
public class Engine {

    public static final String DESCRIPTOR_AUTHENTICATION = "org.restlet.engine.AuthenticationHelper";

    public static final String DESCRIPTOR_PATH = "META-INF/services";

    public static final String DESCRIPTOR_AUTHENTICATION_PATH = DESCRIPTOR_PATH
            + "/" + DESCRIPTOR_AUTHENTICATION;

    public static final String DESCRIPTOR_CLIENT = "org.restlet.engine.ClientHelper";

    public static final String DESCRIPTOR_CLIENT_PATH = DESCRIPTOR_PATH + "/"
            + DESCRIPTOR_CLIENT;

    public static final String DESCRIPTOR_SERVER = "org.restlet.engine.ServerHelper";

    public static final String DESCRIPTOR_SERVER_PATH = DESCRIPTOR_PATH + "/"
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
            + '.' + RELEASE_NUMBER;

    /** Complete version header. */
    public static final String VERSION_HEADER = "Noelios-Restlet-Engine/"
            + VERSION;

    /**
     * Returns the best class loader, first the engine class loader if available
     * using {@link #getUserClassLoader()}, otherwise the current thread context
     * class loader, or finally the classloader of the current class.
     * 
     * @return The best class loader.
     */
    public static ClassLoader getClassLoader() {
        ClassLoader result = getUserClassLoader();

        if (result == null) {
            result = Thread.currentThread().getContextClassLoader();
        }

        if (result == null) {
            result = Class.class.getClassLoader();
        }

        if (result == null) {
            result = ClassLoader.getSystemClassLoader();
        }

        return result;
    }

    /**
     * Returns the registered Restlet engine.
     * 
     * @return The registered Restlet engine.
     */
    public static Engine getInstance() {
        Engine result = instance;

        if (result == null) {
            result = new Engine();
        }

        return result;
    }

    /**
     * Returns the class loader specified by the user and that should be used in
     * priority.
     * 
     * @return The user class loader
     */
    private static ClassLoader getUserClassLoader() {
        return userClassLoader;
    }

    /**
     * Computes the hash code of a set of objects. Follows the algorithm
     * specified in List.hasCode().
     * 
     * @param objects
     *            the objects to compute the hashCode
     * 
     * @return The hash code of a set of objects.
     */
    public static int hashCode(Object... objects) {
        int result = 1;

        if (objects != null) {
            for (final Object obj : objects) {
                result = 31 * result + (obj == null ? 0 : obj.hashCode());
            }
        }

        return result;
    }

    /**
     * Returns the class object for the given name using the given class loader.
     * 
     * @param classLoader
     *            The class loader to use.
     * @param className
     *            The class name to lookup.
     * @return The class object or null.
     */
    private static Class<?> loadClass(ClassLoader classLoader, String className) {
        Class<?> result = null;

        if (classLoader != null) {
            try {
                result = classLoader.loadClass(className);
            } catch (ClassNotFoundException e) {
                // Do nothing
            }
        }

        return result;
    }

    /**
     * Returns the class object for the given name using the engine class loader
     * fist, then the current thread context class loader, or the classloader of
     * the current class.
     * 
     * @param className
     *            The class name to lookup.
     * @return The class object or null if the class was not found.
     */
    public static Class<?> loadClass(String className)
            throws ClassNotFoundException {
        Class<?> result = null;

        // First, try using the engine class loader
        result = loadClass(getUserClassLoader(), className);

        // Then, try using the current thread context class loader
        if (result == null) {
            result = loadClass(Thread.currentThread().getContextClassLoader(),
                    className);
        }

        // Then, try using the current class's class loader
        if (result == null) {
            result = loadClass(Class.class.getClassLoader(), className);
        }

        // Then, try using the caller's class loader
        if (result == null) {
            result = Class.forName(className);
        }

        // Finally try using the system class loader
        if (result == null) {
            result = loadClass(ClassLoader.getSystemClassLoader(), className);
        }

        if (result == null) {
            throw new ClassNotFoundException(className);
        }

        return result;
    }

    /**
     * Sets the registered Restlet engine.
     * 
     * @param engine
     *            The registered Restlet engine.
     */
    public static void setInstance(Engine engine) {
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

    /** List of available authentication helpers. */
    private volatile List<AuthenticationHelper> registeredAuthentications;

    /** List of available client connectors. */
    private volatile List<ClientHelper> registeredClients;

    /** List of available server connectors. */
    private volatile List<ServerHelper> registeredServers;

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
        org.restlet.engine.Engine.setInstance(result);
        return result;
    }

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
        this.registeredAuthentications = new CopyOnWriteArrayList<AuthenticationHelper>();

        if (discoverHelpers) {
            try {
                discoverConnectors();
                discoverAuthentications();
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
     * Discovers the authentication helpers and register the default helpers.
     * 
     * @throws IOException
     */
    private void discoverAuthentications() throws IOException {
        // Find the factory class name
        final ClassLoader classLoader = org.restlet.engine.Engine
                .getClassLoader();

        registerHelpers(classLoader, classLoader
                .getResources(DESCRIPTOR_AUTHENTICATION_PATH),
                getRegisteredAuthentications(), null);

        // Register the default helpers that will be used if no
        // other helper has been found
        registerDefaultAuthentications();
    }

    /**
     * Discovers client connectors in the classpath.
     * 
     * @param classLoader
     *            Classloader to search.
     * @throws IOException
     */
    private void discoverClientConnectors(ClassLoader classLoader)
            throws IOException {
        registerHelpers(classLoader, classLoader
                .getResources(DESCRIPTOR_CLIENT_PATH), getRegisteredClients(),
                Client.class);
    }

    /**
     * Discovers the server and client connectors and register the default
     * connectors.
     * 
     * @throws IOException
     */
    private void discoverConnectors() throws IOException {
        // Find the factory class name
        final ClassLoader classLoader = org.restlet.engine.Engine
                .getClassLoader();

        // Register the client connector providers
        discoverClientConnectors(classLoader);

        // Register the server connector providers
        discoverServerConnectors(classLoader);

        // Register the default connectors that will be used if no
        // other connector has been found
        registerDefaultConnectors();
    }

    /**
     * Discovers server connectors in the classpath.
     * 
     * @param classLoader
     *            Classloader to search.
     * @throws IOException
     */
    private void discoverServerConnectors(ClassLoader classLoader)
            throws IOException {
        registerHelpers(classLoader, classLoader
                .getResources(DESCRIPTOR_SERVER_PATH), getRegisteredServers(),
                Server.class);
    }

    /**
     * Finds the authentication helper supporting the given scheme.
     * 
     * @param challengeScheme
     *            The challenge scheme to match.
     * @param clientSide
     *            Indicates if client side support is required.
     * @param serverSide
     *            Indicates if server side support is required.
     * @return The authentication helper or null.
     */
    public AuthenticationHelper findHelper(ChallengeScheme challengeScheme,
            boolean clientSide, boolean serverSide) {
        AuthenticationHelper result = null;
        final List<AuthenticationHelper> helpers = getRegisteredAuthentications();
        AuthenticationHelper current;

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
    public List<AuthenticationHelper> getRegisteredAuthentications() {
        return this.registeredAuthentications;
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
        getRegisteredAuthentications().add(new HttpBasicHelper());
        getRegisteredAuthentications().add(new HttpDigestHelper());
        getRegisteredAuthentications().add(new SmtpPlainHelper());
        getRegisteredAuthentications().add(new HttpAmazonS3Helper());
        getRegisteredAuthentications().add(new SharedKeyMicrosoftHelper());
        getRegisteredAuthentications().add(new SharedKeyLiteMicrosoftHelper());
    }

    /**
     * Registers the default client and server connectors.
     */
    public void registerDefaultConnectors() {
        getRegisteredClients().add(new StreamClientHelper(null));
        getRegisteredClients().add(new ClapClientHelper(null));
        getRegisteredClients().add(new FileClientHelper(null));
        getRegisteredServers().add(new StreamServerHelper(null));
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
     * @param classLoader
     *            The classloader to use.
     * @param configUrls
     *            Configuration URLs to parse
     * @param helpers
     *            The list of helpers to update.
     * @param constructorClass
     *            The constructor parameter class to look for.
     */
    @SuppressWarnings("unchecked")
    public void registerHelpers(ClassLoader classLoader,
            Enumeration<URL> configUrls, List helpers, Class constructorClass) {
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

}
