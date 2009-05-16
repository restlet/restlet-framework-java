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

package org.restlet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.Engine;
import org.restlet.engine.RestletHelper;
import org.restlet.engine.component.ComponentHelper;
import org.restlet.engine.component.ComponentXmlParser;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Finder;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.VirtualHost;
import org.restlet.service.LogService;
import org.restlet.service.RealmService;
import org.restlet.service.StatusService;
import org.restlet.util.ClientList;
import org.restlet.util.ServerList;

/**
 * Restlet managing a set of Connectors, VirtualHosts, Services and
 * Applications. Applications are expected to be directly attached to virtual
 * hosts or to the internal router (see RIAP pseudo-protocol for usage).
 * Components also expose several services: access logging and status setting. <br>
 * <br>
 * From an architectural point of view, here is the REST definition: "A
 * component is an abstract unit of software instructions and internal state
 * that provides a transformation of data via its interface." Roy T. Fielding<br>
 * <br>
 * The configuration of a Component can be done programmatically or by using a
 * XML document. There are dedicated constructors that accept either an URI
 * reference to such XML document or a representation of such XML document,
 * allowing easy configuration of the list of supported client and server
 * connectors as well as services. In addition, you can add and configure
 * virtual hosts (including the default one). Finally, you can attach
 * applications either using their fully qualified class name or by pointing to
 * a descriptor document (at this time only WADL description are supported, see
 * the WADL Restlet extension for details).<br>
 * <br>
 * The XML Schema of the configuration files is available both <a
 * href="http://www.restlet.org/schemas/1.1/Component">online</a> and inside the
 * API JAR under the "org.restlet.Component.xsd" name. Here is a sample of XML
 * configuration:
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;component xmlns=&quot;http://www.restlet.org/schemas/1.2/Component&quot;&gt;
 *    &lt;client protocol=&quot;CLAP&quot; /&gt;
 *    &lt;client protocol=&quot;FILE&quot; /&gt;
 *    &lt;client protocols=&quot;HTTP HTTPS&quot; /&gt;
 *    &lt;server protocols=&quot;HTTP HTTPS&quot; /&gt;
 * 
 *    &lt;defaultHost&gt;
 *       &lt;attach uriPattern=&quot;/abcd/{xyz}&quot; 
 *                  targetClass=&quot;org.restlet.test.MyApplication&quot; /&gt;
 *       &lt;attach uriPattern=&quot;/efgh/{xyz}&quot;
 *                  targetDescriptor=&quot;clap://class/org/restlet/test/MyApplication.wadl&quot; /&gt;
 *    &lt;/defaultHost&gt;
 * &lt;/component&gt;
 * </pre>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/software_arch.htm#sec_1_2_1">Source
 *      dissertation</a>
 * 
 * @author Jerome Louvel
 */
public class Component extends Restlet {

    /**
     * Used as bootstrap for configuring and running a component in command
     * line. Just provide as first and unique parameter the path to the XML
     * file.
     * 
     * @param args
     *            The list of in-line parameters.
     */
    public static void main(String[] args) throws Exception {
        try {
            if ((args == null) || (args.length != 1)) {
                // Display program arguments
                System.err
                        .println("Can't launch the component. Requires the path to an XML configuration file.\n");
            } else {
                // Create and start the component
                new Component(LocalReference.createFileReference(args[0]))
                        .start();
            }
        } catch (Exception e) {
            System.err
                    .println("Can't launch the component.\nAn unexpected exception occurred:");
            e.printStackTrace(System.err);
        }
    }

    /** The modifiable list of client connectors. */
    private final ClientList clients;

    /** The default host. */
    private volatile VirtualHost defaultHost;

    /** The helper provided by the implementation. */
    private volatile RestletHelper<Component> helper;

    /** The modifiable list of virtual hosts. */
    private final List<VirtualHost> hosts;

    /**
     * The private internal router that can be addressed via the RIAP client
     * connector.
     */
    private volatile Router internalRouter;

    /** The log service. */
    private volatile LogService logService;

    /** The modifiable list of server connectors. */
    private final ServerList servers;

    /** The security realm service. */
    private volatile RealmService realmService;

    /** The status service. */
    private volatile StatusService statusService;

    /**
     * Constructor.
     */
    public Component() {
        this.hosts = new CopyOnWriteArrayList<VirtualHost>();
        this.clients = new ClientList(null);
        this.servers = new ServerList(null, this);

        if (Engine.getInstance() != null) {
            this.helper = new ComponentHelper(this);

            if (this.helper != null) {
                this.defaultHost = new VirtualHost(getContext()
                        .createChildContext());
                this.internalRouter = new Router(getContext()
                        .createChildContext()) {

                    @Override
                    public Route attach(Restlet target) {
                        if (target.getContext() == null) {
                            target
                                    .setContext(getContext()
                                            .createChildContext());
                        }

                        return super.attach(target);
                    }

                    @Override
                    public Route attach(String uriPattern, Restlet target) {
                        if (target.getContext() == null) {
                            target
                                    .setContext(getContext()
                                            .createChildContext());
                        }

                        return super.attach(uriPattern, target);
                    }

                    @Override
                    public Route attachDefault(Restlet defaultTarget) {
                        if (defaultTarget.getContext() == null) {
                            defaultTarget.setContext(getContext()
                                    .createChildContext());
                        }

                        return super.attachDefault(defaultTarget);
                    }

                    @Override
                    public Finder createFinder(Class<?> targetClass) {
                        Finder result = super.createFinder(targetClass);
                        result.setContext(getContext().createChildContext());
                        return result;
                    }

                };
                this.logService = new LogService(true);
                this.realmService = new RealmService(true);
                this.statusService = new StatusService(true);
                this.clients.setContext(getContext());
                this.servers.setContext(getContext());
            }
        }
    }

    /**
     * Constructor with the reference to the XML configuration file.
     * 
     * @param xmlConfigReference
     *            The reference to the XML configuration file.
     */
    public Component(Reference xmlConfigReference) {
        this();

        // Get the representation of the configuration file.
        Representation xmlConfigRepresentation = null;
        if (xmlConfigReference != null) {
            Protocol protocol = xmlConfigReference.getSchemeProtocol();
            if (Protocol.FILE.equals(protocol)) {
                // Get directly the FileRepresentation.
                xmlConfigRepresentation = new FileRepresentation(
                        new LocalReference(xmlConfigReference).getFile(),
                        MediaType.TEXT_XML);
            } else {
                // e.g. for WAR or CLAP protocols.
                Response response = new Client(protocol)
                        .get(xmlConfigReference);
                if (response.getStatus().isSuccess()
                        && response.isEntityAvailable()) {
                    xmlConfigRepresentation = response.getEntity();
                }
            }
        }

        if (xmlConfigRepresentation != null) {
            new ComponentXmlParser(this, xmlConfigRepresentation).parse();
        } else {
            getLogger().log(
                    Level.WARNING,
                    "Unable to get the Component XML configuration located at this URI: "
                            + xmlConfigReference);
        }
    }

    /**
     * Constructor with the representation of the XML configuration file.
     * 
     * @param xmlConfigRepresentation
     *            The representation of the XML configuration file.
     */
    public Component(Representation xmlConfigRepresentation) {
        this();

        if (xmlConfigRepresentation != null) {
            new ComponentXmlParser(this, xmlConfigRepresentation).parse();
        } else {
            getLogger().log(Level.WARNING,
                    "Unable to parse the Component XML configuration.");
        }
    }

    /**
     * Returns a modifiable list of client connectors.
     * 
     * @return A modifiable list of client connectors.
     */
    public ClientList getClients() {
        return this.clients;
    }

    /**
     * Returns the default virtual host.
     * 
     * @return The default virtual host.
     */
    public VirtualHost getDefaultHost() {
        return this.defaultHost;
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private RestletHelper<Component> getHelper() {
        return this.helper;
    }

    /**
     * Returns the modifiable list of virtual hosts.
     * 
     * @return The modifiable list of virtual hosts.
     */
    public List<VirtualHost> getHosts() {
        return this.hosts;
    }

    /**
     * Returns the private internal router were Restlets like Applications can
     * be attached. Those Restlets can be addressed via the
     * {@link org.restlet.data.Protocol#RIAP} (Restlet Internal Access Protocol)
     * client connector. This is used to manage private, internal and optimized
     * access to local applications.<br>
     * <br>
     * The first use case is the modularisation of a large application into
     * modules or layers. This can also be achieved using the
     * {@link Context#getServerDispatcher()} method, but the internal router is
     * easily addressable via an URI scheme and can be fully private to the
     * current Component.<br>
     * <br>
     * The second use case is the composition/mash-up of several representations
     * via the {@link org.restlet.routing.Transformer} class for example. For
     * this you can leverage the XPath's document() function or the XSLT's
     * include and import elements with RIAP URIs.
     * 
     * @return The private internal router.
     */
    public Router getInternalRouter() {
        return this.internalRouter;
    }

    /**
     * Returns the global log service. On the first call, if no log service was
     * defined via the {@link #setLogService(LogService)} method, then a default
     * logger service is created. This service will be enabled by default and
     * has a logger name composed the "org.restlet." prefix followed by the
     * simple component class name (without packages), followed by the
     * ".LogService" suffix.
     * 
     * @return The global log service.
     */
    public LogService getLogService() {
        return this.logService;
    }

    /**
     * Returns the security service, enabled by default.
     * 
     * @return The security service.
     */
    public RealmService getRealmService() {
        return this.realmService;
    }

    /**
     * Returns the modifiable list of server connectors.
     * 
     * @return The modifiable list of server connectors.
     */
    public ServerList getServers() {
        return this.servers;
    }

    /**
     * Returns the status service, enabled by default.
     * 
     * @return The status service.
     */
    public StatusService getStatusService() {
        return this.statusService;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null) {
            getHelper().handle(request, response);
        }
    }

    /**
     * Sets a modifiable list of client connectors. Method synchronized to make
     * compound action (clear, addAll) atomic, not for visibility.
     * 
     * @param clients
     *            A modifiable list of client connectors.
     */
    public synchronized void setClients(ClientList clients) {
        this.clients.clear();

        if (clients != null) {
            this.clients.addAll(clients);
        }
    }

    /**
     * Sets the default virtual host.
     * 
     * @param defaultHost
     *            The default virtual host.
     */
    public void setDefaultHost(VirtualHost defaultHost) {
        this.defaultHost = defaultHost;
    }

    /**
     * Sets the modifiable list of virtual hosts.
     * 
     * @param hosts
     *            The modifiable list of virtual hosts.
     */
    public synchronized void setHosts(List<VirtualHost> hosts) {
        // Method synchronized to make compound action (clear, addAll) atomic,
        // not for visibility.
        this.hosts.clear();

        if (hosts != null) {
            this.hosts.addAll(hosts);
        }
    }

    /**
     * Sets the private internal router were Restlets like Applications can be
     * attached.
     * 
     * @param internalRouter
     *            The private internal router.
     * @see #getInternalRouter()
     */
    public void setInternalRouter(Router internalRouter) {
        this.internalRouter = internalRouter;
    }

    /**
     * Sets the global log service.
     * 
     * @param logService
     *            The global log service.
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Sets the security service.
     * 
     * @param securityService
     *            The security service.
     */
    public void setRealmService(RealmService securityService) {
        this.realmService = securityService;
    }

    /**
     * Sets a modifiable list of server connectors. Method synchronized to make
     * compound action (clear, addAll) atomic, not for visibility.
     * 
     * @param servers
     *            A modifiable list of server connectors.
     */
    public synchronized void setServers(ServerList servers) {
        this.servers.clear();

        if (servers != null) {
            this.servers.addAll(servers);
        }
    }

    /**
     * Sets the status service.
     * 
     * @param statusService
     *            The status service.
     */
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    /**
     * Starts the component. First it starts all the connectors (clients then
     * servers) and then starts the component's internal helper. Finally it
     * calls the start method of the super class.
     * 
     * @see #startClients()
     * @see #startServers()
     * @see #startHelper()
     */
    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            startClients();
            startServers();
            startHelper();
            startServices();
            super.start();
        }
    }

    /**
     * Starts the client connectors.
     * 
     * @throws Exception
     */
    protected synchronized void startClients() throws Exception {
        if (this.clients != null) {
            for (final Client client : this.clients) {
                client.start();
            }
        }
    }

    /**
     * Starts the internal helper allowing incoming requests to be served.
     * 
     * @throws Exception
     */
    protected synchronized void startHelper() throws Exception {
        if (getHelper() != null) {
            getHelper().start();
        }
    }

    /**
     * Starts the server connectors.
     * 
     * @throws Exception
     */
    protected synchronized void startServers() throws Exception {
        if (this.servers != null) {
            for (final Server server : this.servers) {
                server.start();
            }
        }
    }

    /**
     * Starts the associated services.
     * 
     * @throws Exception
     */
    protected synchronized void startServices() throws Exception {
        if (getLogService() != null) {
            getLogService().start();
        }

        if (getStatusService() != null) {
            getStatusService().start();
        }
    }

    /**
     * Stops the component. First it stops the component's internal helper and
     * then stops all the connectors (servers then clients). Finally it calls
     * the stop method of the super class.
     * 
     * @see #stopHelper()
     * @see #stopServers()
     * @see #stopClients()
     */
    @Override
    public synchronized void stop() throws Exception {
        stopHelper();
        stopServers();
        stopClients();
        stopServices();
        super.stop();
    }

    /**
     * Stops the client connectors.
     * 
     * @throws Exception
     */
    protected synchronized void stopClients() throws Exception {
        if (this.clients != null) {
            for (final Client client : this.clients) {
                client.stop();
            }
        }
    }

    /**
     * Stops the internal helper allowing incoming requests to be served.
     * 
     * @throws Exception
     */
    protected synchronized void stopHelper() throws Exception {
        if (getHelper() != null) {
            getHelper().stop();
        }
    }

    /**
     * Stops the server connectors.
     * 
     * @throws Exception
     */
    protected synchronized void stopServers() throws Exception {
        if (this.servers != null) {
            for (final Server server : this.servers) {
                server.stop();
            }
        }
    }

    /**
     * Stops the associated services.
     * 
     * @throws Exception
     */
    protected synchronized void stopServices() throws Exception {
        if (getLogService() != null) {
            getLogService().stop();
        }

        if (getStatusService() != null) {
            getStatusService().stop();
        }
    }

    /**
     * Updates the component to take into account changes to the virtual hosts.
     * This method doesn't stop the connectors or the applications or Restlets
     * attached to the virtual hosts. It just updates the internal routes
     * between the virtual hosts and the attached Restlets or applications.<br>
     */
    public synchronized void updateHosts() throws Exception {
        getHelper().update();
    }
}
