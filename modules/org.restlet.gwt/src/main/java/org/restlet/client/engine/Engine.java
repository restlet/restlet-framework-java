/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.engine;

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

import org.restlet.client.Client;
import org.restlet.client.Context;
import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.data.ChallengeScheme;
import org.restlet.client.data.Method;
import org.restlet.client.data.Protocol;
import org.restlet.client.engine.io.IoUtils;
import org.restlet.client.engine.log.LoggerFacade;

/**
 * Engine supporting the Restlet API. The engine acts as a registry of various {@link Helper} types:
 * {@link org.restlet.client.engine.security.AuthenticatorHelper} , {@link org.restlet.client.engine.connector.ClientHelper},
 * {@link org.restlet.client.engine.converter.ConverterHelper} and {@link org.restlet.client.engine.connector.ServerHelper} classes.<br>
 * <br>
 * Note that by default the JULI logging mechanism is used but it is possible to
 * replace it by providing an alternate {@link LoggerFacade} implementation. For
 * this, just pass a system property named
 * "org.restlet.client.engine.loggerFacadeClass" with the qualified class name as a
 * value.
 * 
 * @author Jerome Louvel
 */
public class Engine {

    public static final String DESCRIPTOR = "META-INF/services";

    public static final String DESCRIPTOR_AUTHENTICATOR = "org.restlet.client.engine.security.AuthenticatorHelper";

    public static final String DESCRIPTOR_AUTHENTICATOR_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_AUTHENTICATOR;

    public static final String DESCRIPTOR_CLIENT = "org.restlet.client.engine.ClientHelper";

    public static final String DESCRIPTOR_CLIENT_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_CLIENT;

    public static final String DESCRIPTOR_CONVERTER = "org.restlet.client.engine.converter.ConverterHelper";

    public static final String DESCRIPTOR_CONVERTER_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_CONVERTER;

    public static final String DESCRIPTOR_PROTOCOL = "org.restlet.client.engine.ProtocolHelper";

    public static final String DESCRIPTOR_PROTOCOL_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_PROTOCOL;

    public static final String DESCRIPTOR_SERVER = "org.restlet.client.engine.ServerHelper";

    public static final String DESCRIPTOR_SERVER_PATH = DESCRIPTOR + "/"
            + DESCRIPTOR_SERVER;

    /** The registered engine. */
    private static volatile Engine instance = null;




    /** Major version number. */
    public static final String MAJOR_NUMBER = "2";

    /** Minor version number. */
    public static final String MINOR_NUMBER = "4";

    /** Release number. */
    public static final String RELEASE_NUMBER = ".4";


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
        Engine result = new Engine(discoverPlugins);
        instance = result;
        return result;
    }





    /** The logger facade to use. */
    private LoggerFacade loggerFacade;


    /** List of available client connectors. */
    private final List<org.restlet.client.engine.connector.ConnectorHelper<Client>> registeredClients;


    /** List of available protocol helpers. */
    private final List<org.restlet.client.engine.connector.ProtocolHelper> registeredProtocols;



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
        }

        this.registeredClients = new CopyOnWriteArrayList<org.restlet.client.engine.connector.ConnectorHelper<Client>>();
        this.registeredProtocols = new CopyOnWriteArrayList<org.restlet.client.engine.connector.ProtocolHelper>();


        if (discoverHelpers) {
            try {
                discoverConnectors();
                discoverProtocols();

            } catch (IOException e) {
                Context.getCurrentLogger()
                        .log(Level.WARNING,
                                "An error occurred while discovering the engine helpers.",
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
    @SuppressWarnings("unchecked")
    public org.restlet.client.engine.connector.ConnectorHelper<Client> createHelper(
            Client client, String helperClass) {
        org.restlet.client.engine.connector.ConnectorHelper<Client> result = null;

        if (!client.getProtocols().isEmpty()) {
            org.restlet.client.engine.connector.ConnectorHelper<Client> connector = null;
            for (final Iterator<org.restlet.client.engine.connector.ConnectorHelper<Client>> iter = getRegisteredClients()
                    .iterator(); (result == null) && iter.hasNext();) {
                connector = iter.next();

                if (connector.getProtocols().containsAll(client.getProtocols())) {
                     result = new
                     org.restlet.client.engine.adapter.GwtHttpClientHelper(client);
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



    /**
     * Discovers the server and client connectors and register the default
     * connectors.
     * 
     * @throws IOException
     */
    private void discoverConnectors() throws IOException {
        registerDefaultConnectors();
    }


    /**
     * Discovers the protocol helpers and register the default helpers.
     * 
     * @throws IOException
     */
    private void discoverProtocols() throws IOException {
        registerDefaultProtocols();
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
     * Returns the list of available client connectors.
     * 
     * @return The list of available client connectors.
     */
    public List<org.restlet.client.engine.connector.ConnectorHelper<Client>> getRegisteredClients() {
        return this.registeredClients;
    }


    /**
     * Returns the list of available protocol connectors.
     * 
     * @return The list of available protocol connectors.
     */
    public List<org.restlet.client.engine.connector.ProtocolHelper> getRegisteredProtocols() {
        return this.registeredProtocols;
    }




    /**
     * Registers the default client and server connectors.
     */
    public void registerDefaultConnectors() {



         getRegisteredClients().add(
         new org.restlet.client.engine.adapter.GwtHttpClientHelper(null));
    }


    /**
     * Registers the default protocols.
     */
    public void registerDefaultProtocols() {
        getRegisteredProtocols().add(
                new org.restlet.client.engine.connector.HttpProtocolHelper());
        getRegisteredProtocols().add(
                new org.restlet.client.engine.connector.WebDavProtocolHelper());
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


    /**
     * Sets the list of available client helpers.
     * 
     * @param registeredClients
     *            The list of available client helpers.
     */
    public void setRegisteredClients(
            List<org.restlet.client.engine.connector.ConnectorHelper<Client>> registeredClients) {
        synchronized (this.registeredClients) {
            if (registeredClients != this.registeredClients) {
                this.registeredClients.clear();

                if (registeredClients != null) {
                    this.registeredClients.addAll(registeredClients);
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
            List<org.restlet.client.engine.connector.ProtocolHelper> registeredProtocols) {
        synchronized (this.registeredProtocols) {
            if (registeredProtocols != this.registeredProtocols) {
                this.registeredProtocols.clear();

                if (registeredProtocols != null) {
                    this.registeredProtocols.addAll(registeredProtocols);
                }
            }
        }
    }



}
