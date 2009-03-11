/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.engine.Engine;
import org.restlet.engine.Helper;
import org.restlet.engine.component.ComponentHelper;
import org.restlet.engine.util.DefaultSaxHandler;
import org.restlet.representation.DomRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Resource;
import org.restlet.service.LogService;
import org.restlet.service.RealmService;
import org.restlet.service.StatusService;
import org.restlet.util.ClientList;
import org.restlet.util.ServerList;
import org.restlet.util.Template;
import org.restlet.util.Variable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Restlet managing a set of Connectors, VirtualHosts and Applications.
 * Applications are expected to be directly attached to VirtualHosts. Components
 * also expose several services: access logging and status setting. <br>
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
     * Indicates if the DOM node is a "parameter" element.
     * 
     * @param domNode
     *            The DOM node to test.
     * @return True if the DOM node is a "parameter" element.
     */
    private static boolean isParameter(Node domNode) {
        return domNode != null && "parameter".equals(domNode.getNodeName());
    }

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

    /**
     * Parses the DOM node into a {@link Parameter} instance.
     * 
     * @param domNode
     *            The DOM node to parse.
     * @return The {@link Parameter} instance.
     */
    private static Parameter parseParameter(Node domNode) {
        Parameter result = null;

        if (!isParameter(domNode)) {
            return null;
        }

        Node nameNode = domNode.getAttributes().getNamedItem("name");
        Node valueNode = domNode.getAttributes().getNamedItem("value");

        if ((nameNode != null) && (valueNode != null)) {
            result = new Parameter(nameNode.getNodeValue(), valueNode
                    .getNodeValue());
        }

        return result;
    }

    /** The modifiable list of client connectors. */
    private final ClientList clients;

    /** The default host. */
    private volatile VirtualHost defaultHost;

    /** The helper provided by the implementation. */
    private volatile Helper<Component> helper;

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
                    public Finder createFinder(
                            Class<? extends Resource> targetClass) {
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
            parseXmlConfiguration(xmlConfigRepresentation);
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
            parseXmlConfiguration(xmlConfigRepresentation);
        } else {
            getLogger().log(Level.WARNING,
                    "Unable to parse the Component XML configuration.");
        }
    }

    /**
     * Creates a new route on a router according to a target class name and a
     * URI pattern.
     * 
     * @param router
     *            the router.
     * @param targetClassName
     *            the target class name.
     * @param uriPattern
     *            the URI pattern.
     * @param defaultRoute
     *            Is this route the default one?
     * @return the created route, or null.
     */
    @SuppressWarnings("unchecked")
    private Route attach(Router router, String targetClassName,
            String uriPattern, boolean defaultRoute) {
        Route route = null;
        // Load the application class using the given class name
        if (targetClassName != null) {
            try {
                final Class<?> targetClass = Engine.loadClass(targetClassName);

                // First, check if we have a Resource class that should be
                // attached directly to the router.
                if (Resource.class.isAssignableFrom(targetClass)) {
                    final Class<? extends Resource> resourceClass = (Class<? extends Resource>) targetClass;

                    if ((uriPattern != null) && !defaultRoute) {
                        route = router.attach(uriPattern, resourceClass);
                    } else {
                        route = router.attachDefault(resourceClass);
                    }
                } else {
                    Restlet target = null;

                    try {
                        // Create a new instance of the application class by
                        // invoking the constructor with the Context parameter.
                        target = (Restlet) targetClass.getConstructor(
                                Context.class).newInstance(
                                getContext().createChildContext());
                    } catch (NoSuchMethodException e) {
                        getLogger()
                                .log(
                                        Level.FINE,
                                        "Couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of type Context. The empty constructor and the context setter will be used instead: "
                                                + targetClassName, e);

                        // The constructor with the Context parameter does not
                        // exist. Instantiate an application with the default
                        // constructor then invoke the setContext method.
                        target = (Restlet) targetClass.getConstructor()
                                .newInstance();
                        target.setContext(getContext().createChildContext());
                    }

                    if (target != null) {
                        if ((uriPattern != null) && !defaultRoute) {
                            route = router.attach(uriPattern, target);
                        } else {
                            route = router.attachDefault(target);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                getLogger().log(
                        Level.WARNING,
                        "Couldn't find the target class. Please check that your classpath includes "
                                + targetClassName, e);
            } catch (InstantiationException e) {
                getLogger()
                        .log(
                                Level.WARNING,
                                "Couldn't instantiate the target class. Please check this class has an empty constructor "
                                        + targetClassName, e);
            } catch (IllegalAccessException e) {
                getLogger()
                        .log(
                                Level.WARNING,
                                "Couldn't instantiate the target class. Please check that you have to proper access rights to "
                                        + targetClassName, e);
            } catch (NoSuchMethodException e) {
                getLogger()
                        .log(
                                Level.WARNING,
                                "Couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of Context "
                                        + targetClassName, e);
            } catch (InvocationTargetException e) {
                getLogger()
                        .log(
                                Level.WARNING,
                                "Couldn't instantiate the target class. An exception was thrown while creating "
                                        + targetClassName, e);
            }
        }
        return route;
    }

    /**
     * Creates a new route on a router according to a target descriptor and a
     * URI pattern.
     * 
     * @param router
     *            the router.
     * @param targetDescriptor
     *            the target descriptor.
     * @param uriPattern
     *            the URI pattern.
     * @param defaultRoute
     *            Is this route the default one?
     * @return the created route, or null.
     */
    private Route attachWithDescriptor(Router router, String targetDescriptor,
            String uriPattern, boolean defaultRoute) {
        Route route = null;
        String targetClassName = null;
        try {
            // Only WADL descriptors are supported at this moment.
            targetClassName = "org.restlet.ext.wadl.WadlApplication";
            final Class<?> targetClass = Engine.loadClass(targetClassName);

            // Get the WADL document
            final Response response = getContext().getClientDispatcher().get(
                    targetDescriptor);
            if (response.getStatus().isSuccess()
                    && response.isEntityAvailable()) {
                final Representation representation = response.getEntity();
                // Create a new instance of the application class by
                // invoking the constructor with the Context parameter.
                final Application target = (Application) targetClass
                        .getConstructor(Context.class, Representation.class)
                        .newInstance(getContext().createChildContext(),
                                representation);
                if (target != null) {
                    if ((uriPattern != null) && !defaultRoute) {
                        route = router.attach(uriPattern, target);
                    } else {
                        route = router.attachDefault(target);
                    }
                }
            } else {
                getLogger()
                        .log(
                                Level.WARNING,
                                "The target descriptor has not been found or is not available, or no client supporting the URI's protocol has been defined on this component. "
                                        + targetDescriptor);
            }
        } catch (ClassNotFoundException e) {
            getLogger().log(
                    Level.WARNING,
                    "Couldn't find the target class. Please check that your classpath includes "
                            + targetClassName, e);
        } catch (InstantiationException e) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Couldn't instantiate the target class. Please check this class has an empty constructor "
                                    + targetClassName, e);
        } catch (IllegalAccessException e) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Couldn't instantiate the target class. Please check that you have to proper access rights to "
                                    + targetClassName, e);
        } catch (NoSuchMethodException e) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of Context "
                                    + targetClassName, e);
        } catch (InvocationTargetException e) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Couldn't instantiate the target class. An exception was thrown while creating "
                                    + targetClassName, e);
        }

        return route;
    }

    /**
     * Parses a node and returns its boolean value.
     * 
     * @param Node
     *            the node to parse.
     * @param defaultValue
     *            the default value;
     * @return The boolean value of the node.
     */
    private boolean getBoolean(Node node, boolean defaultValue) {
        boolean value = defaultValue;
        if (node != null) {
            try {
                value = Boolean.parseBoolean(node.getNodeValue());
            } catch (Exception e) {
                value = defaultValue;
            }
        }
        return value;
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
     * Parses a node and returns the float value.
     * 
     * @param node
     *            the node to parse.
     * @param defaultValue
     *            the default value;
     * @return the float value of the node.
     */
    private float getFloat(Node node, float defaultValue) {
        float value = defaultValue;
        if (node != null) {
            try {
                value = Float.parseFloat(node.getNodeValue());
            } catch (Exception e) {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private Helper<Component> getHelper() {
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
     * Parses a node and returns the int value.
     * 
     * @param node
     *            the node to parse.
     * @param defaultValue
     *            the default value;
     * @return the int value of the node.
     */
    private int getInt(Node node, int defaultValue) {
        int value = defaultValue;
        if (node != null) {
            try {
                value = Integer.parseInt(node.getNodeValue());
            } catch (Exception e) {
                value = defaultValue;
            }
        }
        return value;
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
     * via the {@link org.restlet.Transformer} class for example. For this you
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
        return this.logService;
    }

    /**
     * Parses a node and returns the long value.
     * 
     * @param node
     *            the node to parse.
     * @param defaultValue
     *            the default value;
     * @return the long value of the node.
     */
    private long getLong(Node node, long defaultValue) {
        long value = defaultValue;
        if (node != null) {
            try {
                value = Long.parseLong(node.getNodeValue());
            } catch (Exception e) {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * Returns a protocol by its scheme. If the latter is unknown, instantiate a
     * new protocol object.
     * 
     * @param scheme
     *            the scheme of the desired protocol.
     * @return a known protocol or a new instance.
     */
    private Protocol getProtocol(String scheme) {
        Protocol protocol = Protocol.valueOf(scheme);
        if (protocol == null) {
            protocol = new Protocol(scheme);
        }
        return protocol;
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
     * Parse the attributes of a DOM node and update the given host.
     * 
     * @param host
     *            the host to update.
     * @param hostNode
     *            the DOM node.
     */
    private void parseHost(VirtualHost host, Node hostNode) {
        // Parse the "RouterType" attributes and elements.
        parseRouter(host, hostNode);

        Node item = hostNode.getAttributes().getNamedItem("hostDomain");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setHostDomain(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("hostPort");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setHostPort(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("hostScheme");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setHostScheme(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("name");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setName(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("resourceDomain");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setResourceDomain(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("resourcePort");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setResourcePort(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("resourceScheme");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setResourceScheme(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("serverAddress");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setServerAddress(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("serverPort");
        if ((item != null) && (item.getNodeValue() != null)) {
            host.setServerPort(item.getNodeValue());
        }
    }

    /**
     * Parse the attributes of a DOM node and update the given router.
     * 
     * @param router
     *            the router to update.
     * @param routerNode
     *            the DOM node.
     */
    private void parseRouter(Router router, Node routerNode) {
        Node item = routerNode.getAttributes().getNamedItem(
                "defaultMatchingMode");
        if (item != null) {
            router.setDefaultMatchingMode(getInt(item, getInternalRouter()
                    .getDefaultMatchingMode()));
        }

        item = routerNode.getAttributes().getNamedItem("defaultMatchingQuery");
        if (item != null) {
            router.setDefaultMatchQuery(getBoolean(item, getInternalRouter()
                    .getDefaultMatchQuery()));
        }

        item = routerNode.getAttributes().getNamedItem("maxAttempts");
        if (item != null) {
            router.setMaxAttempts(getInt(item, getInternalRouter()
                    .getMaxAttempts()));
        }

        item = routerNode.getAttributes().getNamedItem("routingMode");
        if (item != null) {
            router.setRoutingMode(getInt(item, getInternalRouter()
                    .getRoutingMode()));
        }

        item = routerNode.getAttributes().getNamedItem("requiredScore");
        if (item != null) {
            router.setRequiredScore(getFloat(item, getInternalRouter()
                    .getRequiredScore()));
        }

        item = routerNode.getAttributes().getNamedItem("retryDelay");
        if (item != null) {
            router.setRetryDelay(getLong(item, getInternalRouter()
                    .getRetryDelay()));
        }

        // Loops the list of "parameter" and "attach" elements
        setAttach(router, routerNode);
    }

    /**
     * Parse a configuration file and update the component's configuration.
     * 
     * @param xmlConfigRepresentation
     *            The representation of the XML config file.
     */
    private void parseXmlConfiguration(Representation xmlConfigRepresentation) {
        try {
            // Parse and validate the XML configuration
            DomRepresentation dom = new DomRepresentation(
                    xmlConfigRepresentation);
            DefaultSaxHandler handler = new DefaultSaxHandler();
            dom.setErrorHandler(handler);
            dom.setEntityResolver(handler);
            dom.setNamespaceAware(true);
            dom.setValidating(true);
            dom.setXIncludeAware(true);

            try {
                Client client = new Client(Protocol.CLAP);
                Representation xsd = client.get(
                        "clap://class/org/restlet/Component.xsd").getEntity();
                dom.setSchema(xsd);
            } catch (Exception x) {
                Context
                        .getCurrentLogger()
                        .log(
                                Level.CONFIG,
                                "Unable to acquire a compiled instance of Component.xsd "
                                        + "to check the given restlet.xml. Ignore and continue");
            }

            final Document document = dom.getDocument();

            // Check root node
            if ("component".equals(document.getFirstChild().getNodeName())) {
                // Look for clients
                final NodeList childNodes = document.getFirstChild()
                        .getChildNodes();
                Node childNode;

                for (int i = 0; i < childNodes.getLength(); i++) {
                    childNode = childNodes.item(i);
                    if ("client".equals(childNode.getNodeName())) {
                        Node item = childNode.getAttributes().getNamedItem(
                                "protocol");
                        Client client = null;

                        if (item == null) {
                            item = childNode.getAttributes().getNamedItem(
                                    "protocols");

                            if (item != null) {
                                final String[] protocols = item.getNodeValue()
                                        .split(" ");
                                final List<Protocol> protocolsList = new ArrayList<Protocol>();

                                for (final String protocol : protocols) {
                                    protocolsList.add(getProtocol(protocol));
                                }

                                client = new Client(new Context(),
                                        protocolsList);
                            }
                        } else {
                            client = new Client(new Context(), getProtocol(item
                                    .getNodeValue()));
                        }

                        if (client != null) {
                            getClients().add(client);

                            // Look for parameters
                            for (int j = 0; j < childNode.getChildNodes()
                                    .getLength(); j++) {
                                final Node childNode2 = childNode
                                        .getChildNodes().item(j);

                                if (isParameter(childNode2)) {
                                    Parameter p = parseParameter(childNode2);
                                    if (p != null) {
                                        client.getContext().getParameters()
                                                .add(p);
                                    }
                                }
                            }
                        }
                    } else if ("server".equals(childNode.getNodeName())) {
                        Node item = childNode.getAttributes().getNamedItem(
                                "protocol");
                        final Node portNode = childNode.getAttributes()
                                .getNamedItem("port");
                        final Node addressNode = childNode.getAttributes()
                                .getNamedItem("address");
                        Server server = null;

                        if (item == null) {
                            item = childNode.getAttributes().getNamedItem(
                                    "protocols");

                            if (item != null) {
                                final String[] protocols = item.getNodeValue()
                                        .split(" ");
                                final List<Protocol> protocolsList = new ArrayList<Protocol>();

                                for (final String protocol : protocols) {
                                    protocolsList.add(getProtocol(protocol));
                                }

                                final int port = getInt(portNode,
                                        Protocol.UNKNOWN_PORT);

                                if (port == Protocol.UNKNOWN_PORT) {
                                    getLogger()
                                            .warning(
                                                    "Please specify a port when defining a list of protocols.");
                                } else {
                                    server = new Server(new Context(),
                                            protocolsList, getInt(portNode,
                                                    Protocol.UNKNOWN_PORT),
                                            getServers().getTarget());
                                }
                            }
                        } else {
                            final Protocol protocol = getProtocol(item
                                    .getNodeValue());
                            server = new Server(
                                    new Context(),
                                    protocol,
                                    getInt(portNode, protocol.getDefaultPort()),
                                    getServers().getTarget());
                        }

                        if (server != null) {
                            if (addressNode != null) {
                                final String address = addressNode
                                        .getNodeValue();
                                if (address != null) {
                                    server.setAddress(address);
                                }
                            }

                            // Look for parameters
                            for (int j = 0; j < childNode.getChildNodes()
                                    .getLength(); j++) {
                                final Node childNode2 = childNode
                                        .getChildNodes().item(j);

                                if (isParameter(childNode2)) {
                                    Parameter p = parseParameter(childNode2);
                                    if (p != null) {
                                        server.getContext().getParameters()
                                                .add(p);
                                    }
                                }
                            }

                            getServers().add(server);
                        }
                    } else if (isParameter(childNode)) {
                        Parameter p = parseParameter(childNode);
                        if (p != null) {
                            getContext().getParameters().add(p);
                        }
                    } else if ("defaultHost".equals(childNode.getNodeName())) {
                        parseHost(getDefaultHost(), childNode);
                    } else if ("host".equals(childNode.getNodeName())) {
                        final VirtualHost host = new VirtualHost(getContext());
                        parseHost(host, childNode);
                        getHosts().add(host);
                    } else if ("internalRouter".equals(childNode.getNodeName())) {
                        parseRouter(getInternalRouter(), childNode);
                    } else if ("logService".equals(childNode.getNodeName())) {
                        Node item = childNode.getAttributes().getNamedItem(
                                "logFormat");

                        if (item != null) {
                            getLogService().setLogFormat(item.getNodeValue());
                        }

                        item = childNode.getAttributes().getNamedItem(
                                "loggerName");

                        if (item != null) {
                            getLogService().setLoggerName(item.getNodeValue());
                        }

                        item = childNode.getAttributes()
                                .getNamedItem("enabled");

                        if (item != null) {
                            getLogService().setEnabled(getBoolean(item, true));
                        }

                        item = childNode.getAttributes().getNamedItem(
                                "identityCheck");

                        if (item != null) {
                            getLogService().setIdentityCheck(
                                    getBoolean(item, true));
                        }
                    } else if ("statusService".equals(childNode.getNodeName())) {
                        Node item = childNode.getAttributes().getNamedItem(
                                "contactEmail");

                        if (item != null) {
                            getStatusService().setContactEmail(
                                    item.getNodeValue());
                        }

                        item = childNode.getAttributes()
                                .getNamedItem("enabled");

                        if (item != null) {
                            getStatusService().setEnabled(
                                    getBoolean(item, true));
                        }

                        item = childNode.getAttributes()
                                .getNamedItem("homeRef");

                        if (item != null) {
                            getStatusService().setHomeRef(
                                    new Reference(item.getNodeValue()));
                        }

                        item = childNode.getAttributes().getNamedItem(
                                "overwrite");

                        if (item != null) {
                            getStatusService().setOverwrite(
                                    getBoolean(item, true));
                        }
                    }
                }
            } else {
                getLogger()
                        .log(Level.WARNING,
                                "Unable to find the root \"component\" node in the XML configuration.");
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Unable to parse the Component XML configuration.", e);
        }
    }

    /**
     * Attaches Restlet to a router.
     * 
     * @param router
     *            The router to attach to.
     * @param node
     *            The node describing the Restlets to attach.
     */
    private void setAttach(Router router, Node node) {
        final NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);
            if (isParameter(childNode)) {
                Parameter p = parseParameter(childNode);
                if (p != null) {
                    router.getContext().getParameters().add(p);
                }
            } else if ("attach".equals(childNode.getNodeName())) {
                String uriPattern = null;
                Node item = childNode.getAttributes()
                        .getNamedItem("uriPattern");
                if (item != null) {
                    uriPattern = item.getNodeValue();
                } else {
                    uriPattern = "";
                }

                item = childNode.getAttributes().getNamedItem("default");
                final boolean bDefault = getBoolean(item, false);

                // Attaches a new route.
                // save the old router context so new routes do not inherit it
                final Context oldContext = router.getContext();
                router.setContext(new Context());

                Route route = null;
                item = childNode.getAttributes().getNamedItem("targetClass");
                if (item != null) {
                    route = attach(router, item.getNodeValue(), uriPattern,
                            bDefault);
                } else {
                    item = childNode.getAttributes().getNamedItem(
                            "targetDescriptor");
                    if (item != null) {
                        route = attachWithDescriptor(router, item
                                .getNodeValue(), uriPattern, bDefault);
                    } else {
                        getLogger()
                                .log(
                                        Level.WARNING,
                                        "Both targetClass name and targetDescriptor are missing. Couldn't attach a new route.");
                    }
                }

                if (route != null) {
                    final Template template = route.getTemplate();
                    item = childNode.getAttributes().getNamedItem(
                            "matchingMode");
                    template.setMatchingMode(getInt(item,
                            Template.MODE_STARTS_WITH));
                    item = childNode.getAttributes().getNamedItem(
                            "defaultVariableType");
                    template.getDefaultVariable().setType(
                            getInt(item, Variable.TYPE_URI_SEGMENT));

                    // Parse possible parameters specific to this AttachType
                    final NodeList childNodes2 = childNode.getChildNodes();
                    for (int j = 0; j < childNodes2.getLength(); j++) {
                        Node aNode = childNodes2.item(j);
                        if (isParameter(aNode)) {
                            Parameter p = parseParameter(aNode);
                            if (p != null) {
                                route.getContext().getParameters().add(p);
                            }
                        }
                    }
                }

                // Restore the router's old context
                router.setContext(oldContext);
            }
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
     * Sets the modifiable list of virtual hosts. Method synchronized to make
     * compound action (clear, addAll) atomic, not for visibility.
     * 
     * @param hosts
     *            The modifiable list of virtual hosts.
     */
    public synchronized void setHosts(List<VirtualHost> hosts) {
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
