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

package org.restlet;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.component.ComponentHelper;
import org.restlet.engine.component.InternalRouter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.routing.Router;
import org.restlet.routing.VirtualHost;
import org.restlet.security.Realm;
import org.restlet.service.LogService;
import org.restlet.service.Service;
import org.restlet.service.StatusService;
import org.restlet.util.ClientList;
import org.restlet.util.ServerList;
import org.restlet.util.ServiceList;

/**
 * Restlet managing a set of {@link Connector}s, {@link VirtualHost}s,
 * {@link Service}s and {@link Application}s. Applications are expected to be
 * directly attached to virtual hosts or to the internal router (see RIAP
 * pseudo-protocol for usage). Components also expose several services: access
 * logging and status setting. <br>
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
 * href="http://restlet.org/schemas/2.0/Component">online</a> and inside the API
 * JAR under the "org.restlet.Component.xsd" name. Here is a sample of XML
 * configuration:
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;component xmlns=&quot;http://restlet.org/schemas/2.0/Component&quot;&gt;
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
 * <br>
 * Components also have useful services associated. They are all enabled by
 * default and are available as properties that can be eventually overridden:
 * <ul>
 * <li>"logService" to configure access logging.</li>
 * <li>"statusService" to provide common representations for exception status.</li>
 * <li>"taskService" to run tasks asynchronously.</li>
 * </ul>
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
     * line. Just provide as first and unique parameter the URI to the XML file.
     * Note that relative paths are accepted.
     * 
     * @param args
     *            The list of in-line parameters.
     * @deprecated Use XML support in the Spring extension instead.
     */
    @Deprecated
    public static void main(String[] args) throws Exception {
        try {
            if ((args == null) || (args.length != 1)) {
                // Display program arguments
                System.err
                        .println("Can't launch the component. Requires the path to an XML configuration file.\n");
            } else {
                // Create and start the component
                URI currentDirURI = (new File(".")).toURI();
                URI confURI = currentDirURI.resolve(args[0]);
                new Component(confURI.toString()).start();
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
    private volatile ComponentHelper helper;

    /** The modifiable list of virtual hosts. */
    private final List<VirtualHost> hosts;

    /**
     * The private internal router that can be addressed via the RIAP client
     * connector.
     */
    private volatile Router internalRouter;

    /** The modifiable list of security realms. */
    private final List<Realm> realms;

    /** The modifiable list of server connectors. */
    private final ServerList servers;

    /** The list of services. */
    private final ServiceList services;

    /**
     * Constructor.
     */
    public Component() {
        super();
        this.hosts = new CopyOnWriteArrayList<VirtualHost>();
        this.clients = new ClientList(null);
        this.servers = new ServerList(null, this);
        this.realms = new CopyOnWriteArrayList<Realm>();
        this.services = new ServiceList(getContext());

        if (Engine.getInstance() != null) {
            // [ifndef gae] instruction
            // To be done before setting the helper...
            this.services.add(new org.restlet.service.TaskService());

            this.helper = new ComponentHelper(this);
            Context childContext = getContext().createChildContext();
            this.defaultHost = new VirtualHost(childContext);
            this.internalRouter = new InternalRouter(childContext);
            this.services.add(new LogService());
            getLogService().setContext(childContext);
            this.services.add(new StatusService());
            getStatusService().setContext(childContext);
            this.clients.setContext(childContext);
            this.servers.setContext(childContext);
        }
    }

    /**
     * Constructor with the reference to the XML configuration file.
     * 
     * @param xmlConfigRef
     *            The URI reference to the XML configuration file.
     * @deprecated Use XML support in the Spring extension instead.
     */
    @Deprecated
    public Component(Reference xmlConfigRef) {
        this();

        // Get the representation of the configuration file.
        Representation xmlConfigRepresentation = null;

        if (xmlConfigRef != null) {
            ClientResource cr = new ClientResource(xmlConfigRef);
            xmlConfigRepresentation = cr.get();

            if (xmlConfigRepresentation != null) {
                new org.restlet.engine.component.ComponentXmlParser(this,
                        xmlConfigRepresentation).parse();
            } else {
                getLogger().log(
                        Level.WARNING,
                        "Unable to get the Component XML configuration located at this URI: "
                                + xmlConfigRef);
            }
        }
    }

    /**
     * Constructor with the representation of the XML configuration file.
     * 
     * @param xmlConfigRepresentation
     *            The representation of the XML configuration file.
     * @deprecated Use XML support in the Spring extension instead.
     */
    @Deprecated
    public Component(Representation xmlConfigRepresentation) {
        this();

        if (xmlConfigRepresentation != null) {
            new org.restlet.engine.component.ComponentXmlParser(this,
                    xmlConfigRepresentation).parse();
        } else {
            getLogger().log(Level.WARNING,
                    "Unable to parse the Component XML configuration.");
        }
    }

    /**
     * Constructor with the URI reference to the XML configuration file.
     * 
     * @param xmlConfigurationRef
     *            The URI reference to the XML configuration file.
     * @deprecated Use XML support in the Spring extension instead.
     */
    @Deprecated
    public Component(String xmlConfigurationRef) {
        this((xmlConfigurationRef == null) ? null : new Reference(
                xmlConfigurationRef));
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
    private ComponentHelper getHelper() {
        return this.helper;
    }

    /**
     * Returns the modifiable list of virtual hosts. Note that the order of
     * virtual hosts in this list will be used to check the first one that
     * matches.
     * 
     * @return The modifiable list of virtual hosts.
     */
    public List<VirtualHost> getHosts() {
        return this.hosts;
    }

    /**
     * Returns the private internal router where Restlets like Applications can
     * be attached. Those Restlets can be addressed via the
     * {@link org.restlet.data.Protocol#RIAP} (Restlet Internal Access Protocol)
     * client connector. This is used to manage private, internal and optimized
     * access to local applications.<br>
     * <br>
     * The first use case is the modularization of a large application into
     * modules or layers. This can also be achieved using the
     * {@link Context#getServerDispatcher()} method, but the internal router is
     * easily addressable via an URI scheme and can be fully private to the
     * current Component.<br>
     * <br>
     * The second use case is the composition/mash-up of several representations
     * via the org.restlet.ext.xml.Transformer class for example. For this you
     * can leverage the XPath's document() function or the XSLT's include and
     * import elements with RIAP URIs.
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
        return getServices().get(LogService.class);
    }

    /**
     * Finds the realm with the given name.
     * 
     * @param name
     *            The name.
     * @return The realm found or null.
     */
    public Realm getRealm(String name) {
        if (name != null) {
            for (Realm realm : getRealms()) {
                if (name.equals(realm.getName())) {
                    return realm;
                }
            }
        }

        return null;
    }

    /**
     * Returns the modifiable list of security realms.
     * 
     * @return The modifiable list of security realms.
     */
    public List<Realm> getRealms() {
        return realms;
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
     * Returns the modifiable list of services.
     * 
     * @return The modifiable list of services.
     */
    public ServiceList getServices() {
        return services;
    }

    /**
     * Returns the status service, enabled by default.
     * 
     * @return The status service.
     * @deprecated Use {@link Application#getStatusService()} instead.
     */
    @Deprecated
    public StatusService getStatusService() {
        return getServices().get(StatusService.class);
    }

    /**
     * Returns a task service to run concurrent tasks. The service is enabled by
     * default.
     * 
     * @return A task service.
     */
    // [ifndef gae] method
    public org.restlet.service.TaskService getTaskService() {
        return getServices().get(org.restlet.service.TaskService.class);
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null) {
            getHelper().handle(request, response);
        }
    }

    /**
     * Sets the modifiable list of client connectors. This method clears the
     * current list and adds all entries in the parameter list.
     * 
     * @param clients
     *            A list of client connectors.
     */
    public void setClients(ClientList clients) {
        synchronized (getClients()) {
            if (clients != getClients()) {
                getClients().clear();

                if (clients != null) {
                    getClients().addAll(clients);
                }
            }
        }
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        getServices().setContext(context);
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
     * Sets the modifiable list of virtual hosts. Note that the order of virtual
     * hosts in this list will be used to check the first one that matches. This
     * method clears the current list and adds all entries in the parameter
     * list.
     * 
     * @param hosts
     *            A list of virtual hosts.
     */
    public void setHosts(List<VirtualHost> hosts) {
        synchronized (getHosts()) {
            if (hosts != getHosts()) {
                getHosts().clear();

                if (hosts != null) {
                    getHosts().addAll(hosts);
                }
            }
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
        getServices().set(logService);
    }

    /**
     * Sets the list of realms. This method clears the current list and adds all
     * entries in the parameter list.
     * 
     * @param realms
     *            A list of realms.
     */
    public void setRealms(List<Realm> realms) {
        synchronized (getRealms()) {
            if (realms != getRealms()) {
                getRealms().clear();

                if (realms != null) {
                    getRealms().addAll(realms);
                }
            }
        }
    }

    /**
     * Sets the modifiable list of server connectors. This method clears the
     * current list and adds all entries in the parameter list.
     * 
     * @param servers
     *            A list of server connectors.
     */
    public void setServers(ServerList servers) {
        synchronized (getServers()) {
            if (servers != getServers()) {
                getServers().clear();

                if (servers != null) {
                    getServers().addAll(servers);
                }
            }
        }
    }

    /**
     * Sets the status service.
     * 
     * @param statusService
     *            The status service.
     * @deprecated Use {@link Application#setStatusService(StatusService)}
     *             instead.
     */
    @Deprecated
    public void setStatusService(StatusService statusService) {
        getServices().set(statusService);
    }

    /**
     * Sets the task service.
     * 
     * @param taskService
     *            The task service.
     */
    // [ifndef gae] method
    public void setTaskService(org.restlet.service.TaskService taskService) {
        getServices().set(taskService);
    }

    /**
     * Starts the component. First it starts all the connectors (clients then
     * servers), the routers, the services, the realms and then the component's
     * internal helper. Finally it calls the start method of the super class.
     * 
     * @see #startClients()
     * @see #startServers()
     * @see #startRouters()
     * @see #startServices()
     * @see #startRealms()
     * @see #startHelper()
     */
    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            startClients();
            startServers();
            startRouters();
            startServices();
            startRealms();
            startHelper();

            // Must be invoked as a last step
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
     * Starts the realms.
     * 
     * @throws Exception
     */
    protected synchronized void startRealms() throws Exception {
        if (this.realms != null) {
            for (Realm realm : this.realms) {
                realm.start();
            }
        }
    }

    /**
     * Starts the virtual hosts and the internal router.
     * 
     * @throws Exception
     */
    protected synchronized void startRouters() throws Exception {
        if (this.internalRouter != null) {
            this.internalRouter.start();
        }

        if (this.defaultHost != null) {
            this.defaultHost.start();
        }

        for (VirtualHost host : getHosts()) {
            host.start();
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
        getServices().start();
    }

    /**
     * Stops the component. First it stops the component's internal helper, the
     * realms, the services, the routers and then stops all the connectors
     * (servers then clients) Finally it calls the stop method of the super
     * class.
     * 
     * @see #stopHelper()
     * @see #stopRealms()
     * @see #stopServices()
     * @see #stopRouters()
     * @see #stopServers()
     * @see #stopClients()
     */
    @Override
    public synchronized void stop() throws Exception {
        stopHelper();
        stopRealms();
        stopServices();
        stopRouters();
        stopServers();
        stopClients();
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
     * Stops the realms.
     * 
     * @throws Exception
     */
    protected synchronized void stopRealms() throws Exception {
        if (this.realms != null) {
            for (Realm realm : this.realms) {
                realm.stop();
            }
        }
    }

    /**
     * Stops the virtual hosts and the internal router.
     * 
     * @throws Exception
     */
    protected synchronized void stopRouters() throws Exception {
        for (VirtualHost host : getHosts()) {
            host.stop();
        }

        if (this.defaultHost != null) {
            this.defaultHost.stop();
        }

        if (this.internalRouter != null) {
            this.internalRouter.stop();
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
        getServices().stop();
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
