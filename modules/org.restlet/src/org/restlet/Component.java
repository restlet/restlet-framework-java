/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.restlet.data.LocalReference;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.service.LogService;
import org.restlet.service.StatusService;
import org.restlet.util.ClientList;
import org.restlet.util.Engine;
import org.restlet.util.Helper;
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
 * XML document. There is a dedicated constructor that accepts an URI reference
 * to such XML document, allowing easy configuration of the list of supported
 * client and server connectors as well as services. In addition, you can add
 * and configure virtual hosts (including the default one). Finally, you can
 * attach applications either using their fully qualified class name or by
 * pointing to a descriptor document (at this time only WADL description are
 * supported, see the WADL Restlet extension for details).<br>
 * <br>
 * The XML Schema of the configuration files is available both <a
 * href="http://www.restlet.org/schemas/1.1/Component">online</a> and inside
 * the API JAR under the "org.restlet.Component.xsd" name.<br>
 * <br>
 * Here is a sample of XML configuration:
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;component xmlns=&quot;http://www.restlet.org/schemas/1.1/Component&quot;
 *               xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;
 *               xsi:schemaLocation=&quot;http://www.restlet.org/schemas/1.1/Component&quot;&gt;
 *    &lt;client protocol=&quot;FILE&quot; /&gt;
 *    &lt;client protocols=&quot;HTTP HTTPS&quot; /&gt;
 *    &lt;server protocols=&quot;HTTP HTTPS&quot; /&gt;
 * 
 *    &lt;defaultHost&gt;
 *       &lt;attach uriPattern=&quot;/abcd/{xyz}&quot; 
 *                  targetClass=&quot;org.restlet.test.MyApplication&quot; /&gt;
 *       &lt;attach uriPattern=&quot;/efgh/{xyz}&quot;
 *                  targetDescriptor=&quot;clap://class/org.restlet.test.MyApplication.wadl&quot; /&gt;
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
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Component extends Restlet {
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

    /** The status service. */
    private volatile StatusService statusService;

    /**
     * Constructor.
     */
    public Component() {
        super(null);
        this.hosts = new CopyOnWriteArrayList<VirtualHost>();
        this.clients = new ClientList(null);
        this.servers = new ServerList(null, this);

        if (Engine.getInstance() != null) {
            this.helper = Engine.getInstance().createHelper(this);

            if (this.helper != null) {
                setContext(this.helper.createContext(getClass()
                        .getCanonicalName()));
                this.defaultHost = new VirtualHost(getContext());
                this.internalRouter = new Router(getContext());
                this.logService = new LogService(true);
                this.logService.setLoggerName(getClass().getCanonicalName()
                        + " (" + hashCode() + ")");
                this.statusService = new StatusService(true);
                this.clients.setContext(getContext());
                this.servers.setContext(getContext());
            }
        }
    }

    /**
     * Parse a configuration file and update the component's configuration.
     * 
     * @param xmlConfigReference
     *                The reference to the XML config file.
     */
    public Component(Reference xmlConfigReference) {
        this();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        dbf.setValidating(false);

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(new FileInputStream(
                    new LocalReference(xmlConfigReference).getFile()));

            // Look for clients
            NodeList clientNodes = document.getElementsByTagName("client");

            for (int i = 0; i < clientNodes.getLength(); i++) {
                Node clientNode = clientNodes.item(i);
                Node item = clientNode.getAttributes().getNamedItem("protocol");
                Client client = null;

                if (item == null) {
                    item = clientNode.getAttributes().getNamedItem("protocols");

                    if (item != null) {
                        String[] protocols = item.getNodeValue().split(" ");
                        List<Protocol> protocolsList = new ArrayList<Protocol>();

                        for (int j = 0; j < protocols.length; j++) {
                            protocolsList.add(getProtocol(protocols[j]));
                        }

                        client = new Client(getContext(), protocolsList);
                    }
                } else {
                    client = new Client(getContext(), getProtocol(item
                            .getNodeValue()));
                }

                if (client != null) {
                    this.getClients().add(client);
                }
            }

            // Look for servers
            NodeList serverNodes = document.getElementsByTagName("server");

            for (int i = 0; i < serverNodes.getLength(); i++) {
                Node serverNode = serverNodes.item(i);
                Node node = serverNode.getAttributes().getNamedItem("protocol");
                Node portNode = serverNode.getAttributes().getNamedItem("port");
                Server server = null;

                if (node == null) {
                    node = serverNode.getAttributes().getNamedItem("protocols");

                    if (node != null) {
                        String[] protocols = node.getNodeValue().split(" ");
                        List<Protocol> protocolsList = new ArrayList<Protocol>();

                        for (int j = 0; j < protocols.length; j++) {
                            protocolsList.add(getProtocol(protocols[j]));
                        }

                        int port = getInt(portNode, Protocol.UNKNOWN_PORT);

                        if (port == Protocol.UNKNOWN_PORT) {
                            getLogger()
                                    .warning(
                                            "Please specify a port when defining a list of protocols.");
                        } else {
                            server = new Server(getContext(), protocolsList,
                                    getInt(portNode, Protocol.UNKNOWN_PORT),
                                    this.getServers().getTarget());
                        }
                    }
                } else {
                    Protocol protocol = getProtocol(node.getNodeValue());
                    server = new Server(getContext(), protocol, getInt(
                            portNode, protocol.getDefaultPort()), this
                            .getServers().getTarget());
                }

                if (server != null) {
                    this.getServers().add(server);
                }

                // Look for default host
                NodeList defaultHostNodes = document
                        .getElementsByTagName("defaultHost");

                if (defaultHostNodes.getLength() > 0) {
                    parseHost(this.getDefaultHost(), defaultHostNodes.item(0));
                }

                // Look for other virtual hosts
                NodeList hostNodes = document.getElementsByTagName("host");

                for (int j = 0; j < hostNodes.getLength(); j++) {
                    VirtualHost host = new VirtualHost();
                    parseHost(host, hostNodes.item(j));
                    this.getHosts().add(host);
                }
            }

            // Look for internal router
            NodeList internalRouterNodes = document
                    .getElementsByTagName("internalRouter");

            if (internalRouterNodes.getLength() > 0) {
                Node node = internalRouterNodes.item(0);
                Node item = node.getAttributes().getNamedItem(
                        "defaultMatchingMode");
                if (item != null) {
                    this.getInternalRouter().setDefaultMatchingMode(
                            getInt(item, getInternalRouter()
                                    .getDefaultMatchingMode()));
                }

                item = node.getAttributes().getNamedItem("maxAttempts");
                if (item != null) {
                    this.getInternalRouter().setMaxAttempts(
                            getInt(item, this.getInternalRouter()
                                    .getMaxAttempts()));
                }

                item = node.getAttributes().getNamedItem("routingMode");
                if (item != null) {
                    this.getInternalRouter().setRoutingMode(
                            getInt(item, this.getInternalRouter()
                                    .getRoutingMode()));
                }

                item = node.getAttributes().getNamedItem("requiredScore");
                if (item != null) {
                    this.getInternalRouter().setRequiredScore(
                            getFloat(item, this.getInternalRouter()
                                    .getRequiredScore()));
                }

                item = node.getAttributes().getNamedItem("retryDelay");
                if (item != null) {
                    this.getInternalRouter().setRetryDelay(
                            getLong(item, this.getInternalRouter()
                                    .getRetryDelay()));
                }

                // Loops the list of "attach" instructions
                setAttach(getInternalRouter(), node);
            }

            // Look for logService
            NodeList logServiceNodes = document
                    .getElementsByTagName("logService");

            if (logServiceNodes.getLength() > 0) {
                Node node = logServiceNodes.item(0);
                Node item = node.getAttributes().getNamedItem("logFormat");

                if (item != null) {
                    this.getLogService().setLogFormat(item.getNodeValue());
                }

                item = node.getAttributes().getNamedItem("loggerName");

                if (item != null) {
                    this.getLogService().setLoggerName(item.getNodeValue());
                }

                item = node.getAttributes().getNamedItem("enabled");

                if (item != null) {
                    this.getLogService().setEnabled(getBoolean(item, true));
                }

                item = node.getAttributes().getNamedItem("identityCheck");

                if (item != null) {
                    this.getLogService().setIdentityCheck(
                            getBoolean(item, true));
                }
            }

            // Look for statusService
            NodeList statusServiceNodes = document
                    .getElementsByTagName("statusService");

            if (statusServiceNodes.getLength() > 0) {
                Node node = statusServiceNodes.item(0);
                Node item = node.getAttributes().getNamedItem("contactEmail");

                if (item != null) {
                    this.getStatusService()
                            .setContactEmail(item.getNodeValue());
                }

                item = node.getAttributes().getNamedItem("enabled");

                if (item != null) {
                    this.getStatusService().setEnabled(getBoolean(item, true));
                }

                item = node.getAttributes().getNamedItem("homeRef");

                if (item != null) {
                    this.getStatusService().setHomeRef(
                            new Reference(item.getNodeValue()));
                }

                item = node.getAttributes().getNamedItem("overwrite");

                if (item != null) {
                    this.getStatusService()
                            .setOverwrite(getBoolean(item, true));
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Unable to parse the Component XML configuration.", e);
        }
    }

    private void setAttach(Router router, Node node) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if ("attach".equals(childNode.getNodeName())) {
                String uriPattern = null;
                Node item = childNode.getAttributes()
                        .getNamedItem("uriPattern");
                if (item != null) {
                    uriPattern = item.getNodeValue();
                } else {
                    uriPattern = "";
                }

                item = childNode.getAttributes().getNamedItem("default");
                boolean bDefault = getBoolean(item, false);

                // Attaches a new route.
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
                    Template template = route.getTemplate();
                    item = childNode.getAttributes().getNamedItem(
                            "matchingMode");
                    template.setMatchingMode(getInt(item,
                            Template.MODE_STARTS_WITH));
                    item = childNode.getAttributes().getNamedItem(
                            "defaultVariableType");
                    template.getDefaultVariable().setType(
                            getInt(item, Variable.TYPE_URI_SEGMENT));
                }
            }
        }
    }

    /**
     * Creates a new route on a router according to a target class name and a
     * URI pattern.
     * 
     * @param router
     *                the router.
     * @param targetClassName
     *                the target class name.
     * @param uriPattern
     *                the URI pattern.
     * @param defaultRoute
     *                Is this route the default one?
     * @return the created route, or null.
     */
    @SuppressWarnings("unchecked")
    private Route attach(Router router, String targetClassName,
            String uriPattern, boolean defaultRoute) {
        Route route = null;
        // Load the application class using the given class name
        if (targetClassName != null) {
            try {
                Class<?> targetClass = Engine.classForName(targetClassName);

                // First, check if we have a Resource class that should be
                // attached directly to the router.
                if (Resource.class.isAssignableFrom(targetClass)) {
                    Class<? extends Resource> resourceClass = (Class<? extends Resource>) targetClass;

                    if (uriPattern != null && !defaultRoute) {
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
                                Context.class).newInstance(getContext());
                    } catch (NoSuchMethodException e) {
                        getLogger()
                                .log(
                                        Level.WARNING,
                                        "Couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of type Context. The empty constructor and the context setter will be used instead.",
                                        e);

                        // The constructor with the Context parameter does not
                        // exist. Instantiate an application with the default
                        // constructor then invoke the setContext method.
                        target = (Restlet) targetClass.getConstructor()
                                .newInstance();

                        // Set the context based on the component's context
                        target.setContext(getContext());
                    }

                    if (target != null) {
                        if (uriPattern != null && !defaultRoute) {
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
     *                the router.
     * @param targetDescriptor
     *                the target descriptor.
     * @param uriPattern
     *                the URI pattern.
     * @param defaultRoute
     *                Is this route the default one?
     * @return the created route, or null.
     */
    @SuppressWarnings("unchecked")
    private Route attachWithDescriptor(Router router, String targetDescriptor,
            String uriPattern, boolean defaultRoute) {
        Route route = null;
        String targetClassName = null;
        try {
            // Only WADL descriptors are supported at this moment.
            targetClassName = "org.restlet.ext.wadl.WadlApplication";
            Class<?> targetClass = Engine.classForName(targetClassName);

            // Get the WADL document
            Response response = getContext().getClientDispatcher().get(
                    targetDescriptor);
            if (response.getStatus().isSuccess()
                    && response.isEntityAvailable()) {
                Representation representation = response.getEntity();
                // Create a new instance of the application class by
                // invoking the constructor with the Context parameter.
                Application target = (Application) targetClass.getConstructor(
                        Context.class, Representation.class).newInstance(
                        getContext(), representation);
                if (target != null) {
                    if (uriPattern != null && !defaultRoute) {
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
     * Parses a port node and returns the port value.
     * 
     * @param portNode
     *                the node to parse.
     * @param defaultPort
     *                the default value;
     * @return the port number.
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
     * Returns a modifiable list of client connectors. Creates a new instance if
     * no one has been set.
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
     *                the node to parse.
     * @param defaultValue
     *                the default value;
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
     * Returns the modifiable list of virtual hosts. Creates a new instance if
     * no one has been set.
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
     *                the node to parse.
     * @param defaultValue
     *                the default value;
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
        return internalRouter;
    }

    /**
     * Returns the global log service. On the first call, if no log service was
     * defined via the {@link #setLogService(LogService)} method, then a default
     * logger service is created. This default service is enabled by default and
     * has a logger name composed of the canonical name of the current
     * component's class or subclass, appended with the instance hash code
     * between parenthesis (eg. "com.mycompany.MyComponent(1439)").
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
     *                the node to parse.
     * @param defaultValue
     *                the default value;
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
     *                the scheme of the desired protocol.
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
     * Returns the modifiable list of server connectors. Creates a new instance
     * if no one has been set.
     * 
     * @return The modifiable list of server connectors.
     */
    public ServerList getServers() {
        return this.servers;
    }

    /**
     * Returns the status service, enabled by default. Creates a new instance if
     * no one has been set.
     * 
     * @return The status service.
     */
    public StatusService getStatusService() {
        return this.statusService;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null)
            getHelper().handle(request, response);
    }

    /**
     * Parse the attributes of a DOM node and update the given host.
     * 
     * @param host
     *                the host to update.
     * @param hostNode
     *                the DOM node.
     */
    private void parseHost(VirtualHost host, Node hostNode) {
        Node item = hostNode.getAttributes().getNamedItem("hostDomain");
        if (item != null && item.getNodeValue() != null) {
            host.setHostDomain(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("hostPort");
        if (item != null && item.getNodeValue() != null) {
            host.setHostPort(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("hostScheme");
        if (item != null && item.getNodeValue() != null) {
            host.setHostScheme(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("name");
        if (item != null && item.getNodeValue() != null) {
            host.setName(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("resourceDomain");
        if (item != null && item.getNodeValue() != null) {
            host.setResourceDomain(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("resourcePort");
        if (item != null && item.getNodeValue() != null) {
            host.setResourcePort(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("resourceScheme");
        if (item != null && item.getNodeValue() != null) {
            host.setResourceScheme(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("serverAddress");
        if (item != null && item.getNodeValue() != null) {
            host.setServerAddress(item.getNodeValue());
        }
        item = hostNode.getAttributes().getNamedItem("serverPort");
        if (item != null && item.getNodeValue() != null) {
            host.setServerPort(item.getNodeValue());
        }
        // Loops the list of "attach" instructions
        setAttach(host, hostNode);
    }

    /**
     * Sets a modifiable list of client connectors. Method synchronized to make
     * compound action (clear, addAll) atomic, not for visibility.
     * 
     * @param clients
     *                A modifiable list of client connectors.
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
     *                The default virtual host.
     */
    public void setDefaultHost(VirtualHost defaultHost) {
        this.defaultHost = defaultHost;
    }

    /**
     * Sets the modifiable list of virtual hosts. Method synchronized to make
     * compound action (clear, addAll) atomic, not for visibility.
     * 
     * @param hosts
     *                The modifiable list of virtual hosts.
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
     *                The private internal router.
     * @see #getInternalRouter()
     */
    public void setInternalRouter(Router internalRouter) {
        this.internalRouter = internalRouter;
    }

    /**
     * Sets the global log service.
     * 
     * @param logService
     *                The global log service.
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Sets a modifiable list of server connectors. Method synchronized to make
     * compound action (clear, addAll) atomic, not for visibility.
     * 
     * @param servers
     *                A modifiable list of server connectors.
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
     *                The status service.
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
            for (Client client : this.clients) {
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
        if (getHelper() != null)
            getHelper().start();
    }

    /**
     * Starts the server connectors.
     * 
     * @throws Exception
     */
    protected synchronized void startServers() throws Exception {
        if (this.servers != null) {
            for (Server server : this.servers) {
                server.start();
            }
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
        super.stop();
    }

    /**
     * Stops the client connectors.
     * 
     * @throws Exception
     */
    protected synchronized void stopClients() throws Exception {
        if (this.clients != null) {
            for (Client client : this.clients) {
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
        if (getHelper() != null)
            getHelper().stop();
    }

    /**
     * Stops the server connectors.
     * 
     * @throws Exception
     */
    protected synchronized void stopServers() throws Exception {
        if (this.servers != null) {
            for (Server server : this.servers) {
                server.stop();
            }
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

    /**
     * Used as bootstrap for configuring and running a component in command
     * line. Just provide as first and unique parameter the path to the XML
     * file.
     * 
     * @param args
     *                The list of in-line parameters.
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
}
