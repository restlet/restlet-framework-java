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

package org.restlet.engine.component;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.util.DefaultSaxHandler;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.Variable;
import org.restlet.routing.VirtualHost;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Parser for component XML configuration.
 * 
 * @author Jerome Louvel
 * @see Component
 * @deprecated Use XML support in the Spring extension instead.
 */
@Deprecated
public class ComponentXmlParser {

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
            result = new Parameter(nameNode.getNodeValue(),
                    valueNode.getNodeValue());
        }

        return result;
    }

    /** The component to update. */
    private volatile Component component;

    /** The XML configuration to parse. */
    private volatile Representation xmlConfiguration;

    /**
     * Constructor.
     * 
     * @param component
     *            The component to update.
     * @param xmlConfiguration
     *            The XML configuration to parse.
     */
    public ComponentXmlParser(Component component,
            Representation xmlConfiguration) {
        this.component = component;
        this.xmlConfiguration = xmlConfiguration;
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
    private TemplateRoute attach(Router router, String targetClassName,
            String uriPattern, boolean defaultRoute) {
        TemplateRoute route = null;

        // Load the application class using the given class name
        if (targetClassName != null) {
            try {
                final Class<?> targetClass = Engine.loadClass(targetClassName);

                // First, check if we have a Resource class that should be
                // attached directly to the router.
                if (ServerResource.class.isAssignableFrom(targetClass)) {
                    final Class<? extends ServerResource> resourceClass = (Class<? extends ServerResource>) targetClass;

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
                                getComponent().getContext()
                                        .createChildContext());
                    } catch (NoSuchMethodException e) {
                        getLogger()
                                .log(Level.FINE,
                                        "Couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of type Context. The empty constructor and the context setter will be used instead: "
                                                + targetClassName, e);

                        // The constructor with the Context parameter does not
                        // exist. Instantiate an application with the default
                        // constructor then invoke the setContext method.
                        target = (Restlet) targetClass.getConstructor()
                                .newInstance();
                        target.setContext(getComponent().getContext()
                                .createChildContext());
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
                        .log(Level.WARNING,
                                "Couldn't instantiate the target class. Please check this class has an empty constructor "
                                        + targetClassName, e);
            } catch (IllegalAccessException e) {
                getLogger()
                        .log(Level.WARNING,
                                "Couldn't instantiate the target class. Please check that you have to proper access rights to "
                                        + targetClassName, e);
            } catch (NoSuchMethodException e) {
                getLogger()
                        .log(Level.WARNING,
                                "Couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of Context "
                                        + targetClassName, e);
            } catch (InvocationTargetException e) {
                getLogger()
                        .log(Level.WARNING,
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
    private TemplateRoute attachWithDescriptor(Router router,
            String targetDescriptor, String uriPattern, boolean defaultRoute) {
        TemplateRoute route = null;
        String targetClassName = null;

        try {
            // Only WADL descriptors are supported at this moment.
            targetClassName = "org.restlet.ext.wadl.WadlApplication";
            final Class<?> targetClass = Engine.loadClass(targetClassName);

            // Get the WADL document
            final Response response = getComponent().getContext()
                    .getClientDispatcher()
                    .handle(new Request(Method.GET, targetDescriptor));
            if (response.getStatus().isSuccess()
                    && response.isEntityAvailable()) {
                final Representation representation = response.getEntity();
                // Create a new instance of the application class by
                // invoking the constructor with the Context parameter.
                final Application target = (Application) targetClass
                        .getConstructor(Context.class, Representation.class)
                        .newInstance(
                                getComponent().getContext()
                                        .createChildContext(), representation);
                if (target != null) {
                    if ((uriPattern != null) && !defaultRoute) {
                        route = router.attach(uriPattern, target);
                    } else {
                        route = router.attachDefault(target);
                    }
                }
            } else {
                getLogger()
                        .log(Level.WARNING,
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
                    .log(Level.WARNING,
                            "Couldn't instantiate the target class. Please check this class has an empty constructor "
                                    + targetClassName, e);
        } catch (IllegalAccessException e) {
            getLogger()
                    .log(Level.WARNING,
                            "Couldn't instantiate the target class. Please check that you have to proper access rights to "
                                    + targetClassName, e);
        } catch (NoSuchMethodException e) {
            getLogger()
                    .log(Level.WARNING,
                            "Couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of Context "
                                    + targetClassName, e);
        } catch (InvocationTargetException e) {
            getLogger()
                    .log(Level.WARNING,
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
     * Returns the component to update.
     * 
     * @return The component to update.
     */
    private Component getComponent() {
        return component;
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
     * Returns the component's logger.
     * 
     * @return The component's logger.
     */
    private Logger getLogger() {
        return getComponent().getLogger();
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
     * Returns the XML configuration to parse.
     * 
     * @return The XML configuration to parse.
     */
    private Representation getXmlConfiguration() {
        return xmlConfiguration;
    }

    /**
     * Parse a configuration file and update the component's configuration.
     */
    public void parse() {
        try {
            // Parse and validate the XML configuration
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setValidating(true);
            dbf.setXIncludeAware(true);

            DefaultSaxHandler handler = new DefaultSaxHandler();
            DocumentBuilder db = dbf.newDocumentBuilder();
            db.setErrorHandler(handler);
            db.setEntityResolver(handler);

            // try {
            // Client client = new Client(Protocol.CLAP);
            // Representation xsd = client.get(
            // "clap://class/org/restlet/Component.xsd").getEntity();
            // db.dom.setSchema(xsd);
            // } catch (Exception x) {
            // Context
            // .getCurrentLogger()
            // .log(
            // Level.CONFIG,
            // "Unable to acquire a compiled instance of Component.xsd "
            // + "to check the given restlet.xml. Ignore and continue");
            // }

            final Document document = db.parse(new InputSource(
                    getXmlConfiguration().getReader()));

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
                            client = new Client(new Context(),
                                    getProtocol(item.getNodeValue()));
                        }

                        if (client != null) {
                            getComponent().getClients().add(client);

                            // Look for Restlet's attributes
                            parseRestlet(client, childNode);

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
                                            getComponent().getServers()
                                                    .getNext());
                                }
                            }
                        } else {
                            final Protocol protocol = getProtocol(item
                                    .getNodeValue());
                            server = new Server(
                                    new Context(),
                                    protocol,
                                    getInt(portNode, protocol.getDefaultPort()),
                                    getComponent().getServers().getNext());
                        }

                        if (server != null) {
                            // Look for Restlet's attributes
                            parseRestlet(server, childNode);

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

                            getComponent().getServers().add(server);
                        }
                    } else if (isParameter(childNode)) {
                        Parameter p = parseParameter(childNode);

                        if (p != null) {
                            getComponent().getContext().getParameters().add(p);
                        }
                    } else if ("defaultHost".equals(childNode.getNodeName())) {
                        parseHost(getComponent().getDefaultHost(), childNode);
                    } else if ("host".equals(childNode.getNodeName())) {
                        final VirtualHost host = new VirtualHost(getComponent()
                                .getContext());
                        parseHost(host, childNode);
                        getComponent().getHosts().add(host);
                    } else if ("internalRouter".equals(childNode.getNodeName())) {
                        parseRouter(getComponent().getInternalRouter(),
                                childNode);
                    } else if ("logService".equals(childNode.getNodeName())) {
                        Node item = childNode.getAttributes().getNamedItem(
                                "logFormat");

                        if (item != null) {
                            getComponent().getLogService()
                                    .setResponseLogFormat(item.getNodeValue());
                        }

                        item = childNode.getAttributes().getNamedItem(
                                "loggerName");

                        if (item != null) {
                            getComponent().getLogService().setLoggerName(
                                    item.getNodeValue());
                        }

                        item = childNode.getAttributes()
                                .getNamedItem("enabled");

                        if (item != null) {
                            getComponent().getLogService().setEnabled(
                                    getBoolean(item, true));
                        }

                        item = childNode.getAttributes().getNamedItem(
                                "identityCheck");

                        if (item != null) {
                            getComponent().getLogService().setIdentityCheck(
                                    getBoolean(item, true));
                        }
                    } else if ("statusService".equals(childNode.getNodeName())) {
                        Node item = childNode.getAttributes().getNamedItem(
                                "contactEmail");

                        if (item != null) {
                            getComponent().getStatusService().setContactEmail(
                                    item.getNodeValue());
                        }

                        item = childNode.getAttributes()
                                .getNamedItem("enabled");

                        if (item != null) {
                            getComponent().getStatusService().setEnabled(
                                    getBoolean(item, true));
                        }

                        item = childNode.getAttributes()
                                .getNamedItem("homeRef");

                        if (item != null) {
                            getComponent().getStatusService().setHomeRef(
                                    new Reference(item.getNodeValue()));
                        }

                        item = childNode.getAttributes().getNamedItem(
                                "overwrite");

                        if (item != null) {
                            getComponent().getStatusService().setOverwriting(
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
     * Parse the attributes of a DOM node and update the given restlet.
     * 
     * @param restlet
     *            the restlet to update.
     * @param restletNode
     *            the DOM node.
     */
    private void parseRestlet(Restlet restlet, Node restletNode) {
        // Parse the "RestletType" attributes and elements.
        Node item = restletNode.getAttributes().getNamedItem("name");
        if ((item != null) && (item.getNodeValue() != null)) {
            restlet.setName(item.getNodeValue());
        }
        item = restletNode.getAttributes().getNamedItem("description");
        if ((item != null) && (item.getNodeValue() != null)) {
            restlet.setDescription(item.getNodeValue());
        }
        item = restletNode.getAttributes().getNamedItem("owner");
        if ((item != null) && (item.getNodeValue() != null)) {
            restlet.setOwner(item.getNodeValue());
        }
        item = restletNode.getAttributes().getNamedItem("author");
        if ((item != null) && (item.getNodeValue() != null)) {
            restlet.setOwner(item.getNodeValue());
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
        parseRestlet(router, routerNode);

        Node item = routerNode.getAttributes().getNamedItem(
                "defaultMatchingMode");
        if (item != null) {
            router.setDefaultMatchingMode(getInt(item, getComponent()
                    .getInternalRouter().getDefaultMatchingMode()));
        }

        item = routerNode.getAttributes().getNamedItem("defaultMatchingQuery");
        if (item != null) {
            router.setDefaultMatchingQuery(getBoolean(item, getComponent()
                    .getInternalRouter().getDefaultMatchingQuery()));
        }

        item = routerNode.getAttributes().getNamedItem("maxAttempts");
        if (item != null) {
            router.setMaxAttempts(getInt(item, getComponent()
                    .getInternalRouter().getMaxAttempts()));
        }

        item = routerNode.getAttributes().getNamedItem("routingMode");
        if (item != null) {
            router.setRoutingMode(getInt(item, getComponent()
                    .getInternalRouter().getRoutingMode()));
        }

        item = routerNode.getAttributes().getNamedItem("requiredScore");
        if (item != null) {
            router.setRequiredScore(getFloat(item, getComponent()
                    .getInternalRouter().getRequiredScore()));
        }

        item = routerNode.getAttributes().getNamedItem("retryDelay");
        if (item != null) {
            router.setRetryDelay(getLong(item, getComponent()
                    .getInternalRouter().getRetryDelay()));
        }

        // Loops the list of "parameter" and "attach" elements
        setAttach(router, routerNode);
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
        NodeList childNodes = node.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (isParameter(childNode)) {
                Parameter p = parseParameter(childNode);

                if (p != null) {
                    router.getContext().getParameters().add(p);
                }
            } else if ("attach".equals(childNode.getNodeName())
                    || "attachDefault".equals(childNode.getNodeName())) {
                String uriPattern = null;
                Node item = childNode.getAttributes()
                        .getNamedItem("uriPattern");

                if (item != null) {
                    uriPattern = item.getNodeValue();
                } else {
                    uriPattern = "";
                }

                item = childNode.getAttributes().getNamedItem("default");
                boolean bDefault = getBoolean(item, false)
                        || "attachDefault".equals(childNode.getNodeName());

                // Attaches a new route.
                TemplateRoute route = null;
                item = childNode.getAttributes().getNamedItem("targetClass");

                if (item != null) {
                    route = attach(router, item.getNodeValue(), uriPattern,
                            bDefault);
                } else {
                    item = childNode.getAttributes().getNamedItem(
                            "targetDescriptor");
                    if (item != null) {
                        route = attachWithDescriptor(router,
                                item.getNodeValue(), uriPattern, bDefault);
                    } else {
                        getLogger()
                                .log(Level.WARNING,
                                        "Both targetClass name and targetDescriptor are missing. Couldn't attach a new route.");
                    }
                }

                if (route != null) {
                    Template template = route.getTemplate();
                    item = childNode.getAttributes().getNamedItem(
                            "matchingMode");
                    template.setMatchingMode(getInt(item,
                            router.getDefaultMatchingMode()));
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
                                route.getNext().getContext().getParameters()
                                        .add(p);
                            }
                        }
                    }
                }
            }
        }
    }

}
