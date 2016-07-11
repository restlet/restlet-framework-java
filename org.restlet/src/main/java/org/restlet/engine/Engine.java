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

package org.restlet.engine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.io.IoUtils;
import org.restlet.engine.log.LoggerFacade;

/**
 * Engine supporting the Restlet API. The engine acts as a registry of various {@link Helper} types:
 * {@link org.restlet.engine.security.AuthenticatorHelper} , {@link org.restlet.engine.connector.ClientHelper},
 * {@link org.restlet.engine.converter.ConverterHelper} and {@link org.restlet.engine.connector.ServerHelper} classes.<br>
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

    public static final String DESCRIPTOR_PROTOCOL = "org.restlet.engine.ProtocolHelper";

    public static final String DESCRIPTOR_PROTOCOL_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_PROTOCOL;

    public static final String DESCRIPTOR_SERVER = "org.restlet.engine.ServerHelper";

    public static final String DESCRIPTOR_SERVER_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_SERVER;

    /** The registered engine. */
    private static volatile Engine instance = null;

    // [ifdef jse,android,osgi] member
    /** The org.restlet log level . */
    private static volatile boolean logConfigured = false;

    // [ifdef jse,android,osgi] member
    /** The general log formatter. */
    private static volatile Class<? extends Formatter> logFormatter = org.restlet.engine.log.SimplestFormatter.class;

    // [ifdef jse,android,osgi] member
    /** The general log level . */
    private static volatile Level logLevel = Level.INFO;

    /** Major version number. */
    public static final String MAJOR_NUMBER = "@major-number@";

    /** Minor version number. */
    public static final String MINOR_NUMBER = "@minor-number@";

    /** Release number. */
    public static final String RELEASE_NUMBER = "@release-type@@release-number@";

    // [ifdef jse,android,osgi] member
    /** The org.restlet log level . */
    private static volatile Level restletLogLevel;

    /** Complete version. */
    public static final String VERSION = MAJOR_NUMBER + '.' + MINOR_NUMBER
            + RELEASE_NUMBER;

    /** Complete version header. */
    public static final String VERSION_HEADER = "Restlet-Framework/" + VERSION;

    /**
     * Clears the current Restlet Engine altogether.
     */
    public static synchronized void clear() {
        instance = null;
    }

    // [ifndef gwt] method
    /**
     * Creates a new standalone thread with local Restlet thread variable
     * properly set.
     * 
     * @param runnable
     *            The runnable task to execute.
     * @param name
     *            The thread name.
     * @return The thread with proper variables ready to run the given runnable
     *         task.
     */
    public static Thread createThreadWithLocalVariables(
            final Runnable runnable, String name) {
        // Save the thread local variables
        final org.restlet.Application currentApplication = org.restlet.Application
                .getCurrent();
        final Context currentContext = Context.getCurrent();
        final Integer currentVirtualHost = org.restlet.routing.VirtualHost
                .getCurrent();
        final Response currentResponse = Response.getCurrent();

        Runnable r = new Runnable() {

            @Override
            public void run() {
                // Copy the thread local variables
                Response.setCurrent(currentResponse);
                Context.setCurrent(currentContext);
                org.restlet.routing.VirtualHost.setCurrent(currentVirtualHost);
                org.restlet.Application.setCurrent(currentApplication);

                try {
                    // Run the user task
                    runnable.run();
                } finally {
                    Engine.clearThreadLocalVariables();
                }
            }

        };

        // [ifndef gae] instruction
        return new Thread(r, name);
        // [ifdef gae] instruction uncomment
        // return
        // com.google.appengine.api.ThreadManager.createThreadForCurrentRequest(r);
    }

    // [ifndef gwt] method
    /**
     * Clears the thread local variables set by the Restlet API and engine.
     */
    public static void clearThreadLocalVariables() {
        Response.setCurrent(null);
        Context.setCurrent(null);
        org.restlet.routing.VirtualHost.setCurrent(null);
        org.restlet.Application.setCurrent(null);
    }

    // [ifdef jse,android,osgi] method
    /**
     * Updates the global log configuration of the JVM programmatically.
     */
    public static void configureLog() {
        if ((System.getProperty("java.util.logging.config.file") == null)
                && (System.getProperty("java.util.logging.config.class") == null)) {
            StringBuilder sb = new StringBuilder();
            sb.append("handlers=")
                    .append(java.util.logging.ConsoleHandler.class.getCanonicalName())
                    .append('\n');

            if (getLogLevel() != null) {
                sb.append(".level=")
                        .append(getLogLevel().getName())
                        .append('\n');
            }

            if (getRestletLogLevel() != null) {
                sb.append("org.restlet.level=")
                        .append(getRestletLogLevel().getName())
                        .append('\n');
            }

            if (getLogFormatter() != null) {
                String handler = java.util.logging.ConsoleHandler.class.getCanonicalName();
                sb.append(handler)
                        .append(".formatter=")
                        .append(getLogFormatter().getCanonicalName())
                        .append("\n");

                if (getLogLevel() != null) {
                    sb.append(handler)
                            .append(".level=")
                            .append(getLogLevel().getName())
                            .append("\n");
                }
            }

            try {
                LogManager.getLogManager().readConfiguration(
                        new ByteArrayInputStream(sb.toString().getBytes()));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        logConfigured = true;
    }

    /**
     * Returns an anonymous logger. By default it calls {@link #getLogger(String)} with a "" name.
     * 
     * @return The logger.
     */
    public static Logger getAnonymousLogger() {
        return getInstance().getLoggerFacade().getAnonymousLogger();
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

    // [ifdef jse,android,osgi] method
    /**
     * Returns the general log formatter.
     * 
     * @return The general log formatter.
     */
    public static Class<? extends Formatter> getLogFormatter() {
        return Engine.logFormatter;
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

    // [ifdef jse,android,osgi] method
    /**
     * Returns the general log level.
     * 
     * @return The general log level.
     */
    public static Level getLogLevel() {
        return Engine.logLevel;
    }

    // [ifndef gwt] method
    /**
     * Returns the classloader resource for a given name/path.
     * 
     * @param name
     *            The name/path to lookup.
     * @return The resource URL.
     */
    public static java.net.URL getResource(String name) {
        return getInstance().getClassLoader().getResource(name);
    }

    // [ifdef jse,android,osgi] method
    /**
     * Returns the Restlet log level. For loggers with a name starting with
     * "org.restlet".
     * 
     * @return The Restlet log level.
     */
    public static Level getRestletLogLevel() {
        return Engine.restletLogLevel;
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
        return getInstance().getClassLoader().loadClass(className);
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
        // [ifdef jse,android,osgi]
        if (!logConfigured) {
            configureLog();
        }
        // [enddef]
        Engine result = new Engine(discoverPlugins);
        instance = result;
        return result;
    }

    // [ifdef jse,android,osgi] method
    /**
     * Sets the general log formatter.
     * 
     * @param logFormatter
     *            The general log formatter.
     */
    public static void setLogFormatter(Class<? extends Formatter> logFormatter) {
        Engine.logFormatter = logFormatter;
        configureLog();
    }

    // [ifdef jse,android,osgi] method
    /**
     * Sets the general log level. Modifies the global JVM's {@link LogManager}.
     * 
     * @param logLevel
     *            The general log level.
     */
    public static void setLogLevel(Level logLevel) {
        Engine.logLevel = logLevel;
        configureLog();
    }

    // [ifdef jse,android,osgi] method
    /**
     * Sets the Restlet log level. For loggers with a name starting with
     * "org.restlet".
     * 
     * @param restletLogLevel
     *            The Restlet log level.
     */
    public static void setRestletLogLevel(Level restletLogLevel) {
        Engine.restletLogLevel = restletLogLevel;
        configureLog();
    }

    // [ifndef gwt] member
    /** Class loader to use for dynamic class loading. */
    private volatile ClassLoader classLoader;

    /** The logger facade to use. */
    private LoggerFacade loggerFacade;

    // [ifndef gwt] member
    /** List of available authenticator helpers. */
    private final List<org.restlet.engine.security.AuthenticatorHelper> registeredAuthenticators;

    /** List of available client connectors. */
    private final List<org.restlet.engine.connector.ConnectorHelper<Client>> registeredClients;

    // [ifndef gwt] member
    /** List of available converter helpers. */
    private final List<org.restlet.engine.converter.ConverterHelper> registeredConverters;

    /** List of available protocol helpers. */
    private final List<org.restlet.engine.connector.ProtocolHelper> registeredProtocols;

    // [ifndef gwt] member
    /** List of available server connectors. */
    private final List<org.restlet.engine.connector.ConnectorHelper<org.restlet.Server>> registeredServers;

    // [ifndef gwt] member
    /** User class loader to use for dynamic class loading. */
    private volatile ClassLoader userClassLoader;

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
        // Prevent engine initialization code from recreating other engines
        instance = this;

        // Instantiate the logger facade
        if (Edition.CURRENT == Edition.GWT) {
            this.loggerFacade = new LoggerFacade();
        } else {
            // [ifndef gwt]
            this.classLoader = createClassLoader();
            this.userClassLoader = null;

            String loggerFacadeClass = System.getProperty(
                    "org.restlet.engine.loggerFacadeClass",
                    "org.restlet.engine.log.LoggerFacade");
            try {
                this.loggerFacade = (LoggerFacade) getClassLoader().loadClass(
                        loggerFacadeClass).newInstance();
            } catch (Exception e) {
                this.loggerFacade = new LoggerFacade();
                this.loggerFacade.getLogger("org.restlet").log(Level.WARNING,
                        "Unable to register the logger facade", e);
            }
            // [enddef]
        }

        this.registeredClients = new CopyOnWriteArrayList<org.restlet.engine.connector.ConnectorHelper<Client>>();
        this.registeredProtocols = new CopyOnWriteArrayList<org.restlet.engine.connector.ProtocolHelper>();

        // [ifndef gwt]
        this.registeredServers = new CopyOnWriteArrayList<org.restlet.engine.connector.ConnectorHelper<org.restlet.Server>>();
        this.registeredAuthenticators = new CopyOnWriteArrayList<org.restlet.engine.security.AuthenticatorHelper>();
        this.registeredConverters = new CopyOnWriteArrayList<org.restlet.engine.converter.ConverterHelper>();
        // [enddef]

        if (discoverHelpers) {
            try {
                discoverConnectors();
                discoverProtocols();

                // [ifndef gwt]
                discoverAuthenticators();
                discoverConverters();
                // [enddef]
            } catch (IOException e) {
                Context.getCurrentLogger()
                        .log(Level.WARNING,
                                "An error occurred while discovering the engine helpers.",
                                e);
            }
        }
    }

    // [ifndef gwt] method
    /**
     * Creates a new class loader. By default, it returns an instance of
     * {@link org.restlet.engine.util.EngineClassLoader}.
     * 
     * @return A new class loader.
     */
    protected ClassLoader createClassLoader() {
        return new org.restlet.engine.util.EngineClassLoader(this);
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
    @SuppressWarnings("unchecked")
    public org.restlet.engine.connector.ConnectorHelper<Client> createHelper(
            Client client, String helperClass) {
        org.restlet.engine.connector.ConnectorHelper<Client> result = null;

        if (!client.getProtocols().isEmpty()) {
            org.restlet.engine.connector.ConnectorHelper<Client> connector = null;
            for (final Iterator<org.restlet.engine.connector.ConnectorHelper<Client>> iter = getRegisteredClients()
                    .iterator(); (result == null) && iter.hasNext();) {
                connector = iter.next();

                if (connector.getProtocols().containsAll(client.getProtocols())) {
                    // [ifndef gwt]
                    if ((helperClass == null)
                            || connector.getClass().getCanonicalName()
                                    .equals(helperClass)) {
                        try {
                            result = connector.getClass()
                                    .getConstructor(Client.class)
                                    .newInstance(client);
                        } catch (Exception e) {
                            Context.getCurrentLogger()
                                    .log(Level.SEVERE,
                                            "Exception during the instantiation of the client connector.",
                                            e);
                        }
                    }
                    // [enddef]
                    // [ifdef gwt] instruction uncomment
                    // result = new
                    // org.restlet.engine.adapter.GwtHttpClientHelper(client);
                }
            }

            if (result == null) {
                // Couldn't find a matching connector
                StringBuilder sb = new StringBuilder();
                sb.append("No available client connector supports the required protocols: ");

                for (Protocol p : client.getProtocols()) {
                    sb.append("'").append(p.getName()).append("' ");
                }

                sb.append(". Please add the JAR of a matching connector to your classpath.");

                if (Edition.CURRENT == Edition.ANDROID) {
                    sb.append(" Then, register this connector helper manually.");
                }

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
    @SuppressWarnings("unchecked")
    public org.restlet.engine.connector.ConnectorHelper<org.restlet.Server> createHelper(
            org.restlet.Server server, String helperClass) {
        org.restlet.engine.connector.ConnectorHelper<org.restlet.Server> result = null;

        if (!server.getProtocols().isEmpty()) {
            org.restlet.engine.connector.ConnectorHelper<org.restlet.Server> connector = null;
            for (final Iterator<org.restlet.engine.connector.ConnectorHelper<org.restlet.Server>> iter = getRegisteredServers()
                    .iterator(); (result == null) && iter.hasNext();) {
                connector = iter.next();

                if ((helperClass == null)
                        || connector.getClass().getCanonicalName()
                                .equals(helperClass)) {
                    if (connector.getProtocols().containsAll(
                            server.getProtocols())) {
                        try {
                            result = connector.getClass()
                                    .getConstructor(org.restlet.Server.class)
                                    .newInstance(server);
                        } catch (Exception e) {
                            Context.getCurrentLogger()
                                    .log(Level.SEVERE,
                                            "Exception while instantiation the server connector.",
                                            e);
                        }
                    }
                }
            }

            if (result == null) {
                // Couldn't find a matching connector
                final StringBuilder sb = new StringBuilder();
                sb.append("No available server connector supports the required protocols: ");

                for (final Protocol p : server.getProtocols()) {
                    sb.append("'").append(p.getName()).append("' ");
                }

                sb.append(". Please add the JAR of a matching connector to your classpath.");

                if (Edition.CURRENT == Edition.ANDROID) {
                    sb.append(" Then, register this connector helper manually.");
                }

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

    /**
     * Discovers the protocol helpers and register the default helpers.
     * 
     * @throws IOException
     */
    private void discoverProtocols() throws IOException {
        // [ifndef gwt] instruction
        registerHelpers(DESCRIPTOR_PROTOCOL_PATH, getRegisteredProtocols(),
                null);
        registerDefaultProtocols();
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
        List<org.restlet.engine.security.AuthenticatorHelper> helpers = getRegisteredAuthenticators();
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

    // [ifndef gwt] method
    /**
     * Returns the class loader. It uses the delegation model with the Engine
     * class's class loader as a parent. If this parent doesn't find a class or
     * resource, it then tries the user class loader (via {@link #getUserClassLoader()} and finally the
     * {@link Thread#getContextClassLoader()}.
     * 
     * @return The engine class loader.
     * @see org.restlet.engine.util.EngineClassLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the logger facade to use.
     * 
     * @return The logger facade to use.
     */
    public LoggerFacade getLoggerFacade() {
        return loggerFacade;
    }

    // [ifndef gwt] method
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
    public List<org.restlet.engine.connector.ConnectorHelper<Client>> getRegisteredClients() {
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

    /**
     * Returns the list of available protocol connectors.
     * 
     * @return The list of available protocol connectors.
     */
    public List<org.restlet.engine.connector.ProtocolHelper> getRegisteredProtocols() {
        return this.registeredProtocols;
    }

    // [ifndef gwt] method
    /**
     * Returns the list of available server connectors.
     * 
     * @return The list of available server connectors.
     */
    public List<org.restlet.engine.connector.ConnectorHelper<org.restlet.Server>> getRegisteredServers() {
        return this.registeredServers;
    }

    // [ifndef gwt] method
    /**
     * Returns the class loader specified by the user and that should be used in
     * priority.
     * 
     * @return The user class loader
     */
    public ClassLoader getUserClassLoader() {
        return userClassLoader;
    }

    // [ifndef gwt] method
    /**
     * Registers the default authentication helpers.
     */
    public void registerDefaultAuthentications() {
        getRegisteredAuthenticators().add(
                new org.restlet.engine.security.HttpBasicHelper());
        getRegisteredAuthenticators().add(
                new org.restlet.engine.security.SmtpPlainHelper());
    }

    /**
     * Registers the default client and server connectors.
     */
    public void registerDefaultConnectors() {
        // [ifndef gae, gwt]
        getRegisteredClients().add(
                new org.restlet.engine.connector.FtpClientHelper(null));
        // [enddef]
        // [ifndef gwt]
        getRegisteredClients().add(
                new org.restlet.engine.connector.HttpClientHelper(null));
        getRegisteredClients().add(
                new org.restlet.engine.local.ClapClientHelper(null));
        getRegisteredClients().add(
                new org.restlet.engine.local.RiapClientHelper(null));
        getRegisteredServers().add(
                new org.restlet.engine.local.RiapServerHelper(null));
        // [enddef]

        // [ifndef android, gae, gwt]
        getRegisteredServers().add(
                new org.restlet.engine.connector.HttpServerHelper(null));
        getRegisteredServers().add(
                new org.restlet.engine.connector.HttpsServerHelper(null));
        // [enddef]

        // [ifndef gae, gwt]
        getRegisteredClients().add(
                new org.restlet.engine.local.FileClientHelper(null));
        getRegisteredClients().add(
                new org.restlet.engine.local.ZipClientHelper(null));
        // [enddef]

        // [ifdef gwt] uncomment
        // getRegisteredClients().add(
        // new org.restlet.engine.adapter.GwtHttpClientHelper(null));
        // [enddef]
    }

    // [ifndef gwt] method
    /**
     * Registers the default converters.
     */
    public void registerDefaultConverters() {
        getRegisteredConverters().add(
                new org.restlet.engine.converter.DefaultConverter());
        getRegisteredConverters().add(
                new org.restlet.engine.converter.StatusInfoHtmlConverter());
    }

    /**
     * Registers the default protocols.
     */
    public void registerDefaultProtocols() {
        getRegisteredProtocols().add(
                new org.restlet.engine.connector.HttpProtocolHelper());
        getRegisteredProtocols().add(
                new org.restlet.engine.connector.WebDavProtocolHelper());
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
    public void registerHelpers(ClassLoader classLoader,
            java.net.URL configUrl, List<?> helpers, Class<?> constructorClass) {
        try {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(
                        configUrl.openStream(), "utf-8"), IoUtils.BUFFER_SIZE);
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
    public void registerHelpers(String descriptorPath, List<?> helpers,
            Class<?> constructorClass) throws IOException {
        ClassLoader classLoader = getClassLoader();
        Enumeration<java.net.URL> configUrls = classLoader
                .getResources(descriptorPath);

        if (configUrls != null) {
            for (Enumeration<java.net.URL> configEnum = configUrls; configEnum
                    .hasMoreElements();) {
                registerHelpers(classLoader, configEnum.nextElement(), helpers,
                        constructorClass);
            }
        }
    }

    // [ifndef gae,gwt] method
    /**
     * Registers a factory that is used by the URL class to create the {@link java.net.URLConnection} instances when the
     * {@link java.net.URL#openConnection()} or {@link java.net.URL#openStream()} methods are invoked.
     * <p>
     * The implementation is based on the client dispatcher of the current context, as provided by
     * {@link Context#getCurrent()} method.
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
                                            Response response = context
                                                    .getClientDispatcher()
                                                    .handle(new Request(
                                                            Method.GET,
                                                            this.url.toString()));

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

    // [ifndef gwt] method
    /**
     * Sets the engine class loader.
     * 
     * @param newClassLoader
     *            The new user class loader to use.
     */
    public void setClassLoader(ClassLoader newClassLoader) {
        this.classLoader = newClassLoader;
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
            if (registeredAuthenticators != this.registeredAuthenticators) {
                this.registeredAuthenticators.clear();

                if (registeredAuthenticators != null) {
                    this.registeredAuthenticators
                            .addAll(registeredAuthenticators);
                }
            }
        }
    }

    /**
     * Sets the list of available client helpers.
     * 
     * @param registeredClients
     *            The list of available client helpers.
     */
    public void setRegisteredClients(
            List<org.restlet.engine.connector.ConnectorHelper<Client>> registeredClients) {
        synchronized (this.registeredClients) {
            if (registeredClients != this.registeredClients) {
                this.registeredClients.clear();

                if (registeredClients != null) {
                    this.registeredClients.addAll(registeredClients);
                }
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
            if (registeredConverters != this.registeredConverters) {
                this.registeredConverters.clear();

                if (registeredConverters != null) {
                    this.registeredConverters.addAll(registeredConverters);
                }
            }
        }
    }

    /**
     * Sets the list of available protocol helpers.
     * 
     * @param registeredProtocols
     *            The list of available protocol helpers.
     */
    public void setRegisteredProtocols(
            List<org.restlet.engine.connector.ProtocolHelper> registeredProtocols) {
        synchronized (this.registeredProtocols) {
            if (registeredProtocols != this.registeredProtocols) {
                this.registeredProtocols.clear();

                if (registeredProtocols != null) {
                    this.registeredProtocols.addAll(registeredProtocols);
                }
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
    public void setRegisteredServers(
            List<org.restlet.engine.connector.ConnectorHelper<org.restlet.Server>> registeredServers) {
        synchronized (this.registeredServers) {
            if (registeredServers != this.registeredServers) {
                this.registeredServers.clear();

                if (registeredServers != null) {
                    this.registeredServers.addAll(registeredServers);
                }
            }
        }
    }

    // [ifndef gwt] method
    /**
     * Sets the user class loader that should used in priority.
     * 
     * @param newClassLoader
     *            The new user class loader to use.
     */
    public void setUserClassLoader(ClassLoader newClassLoader) {
        this.userClassLoader = newClassLoader;
    }

}
