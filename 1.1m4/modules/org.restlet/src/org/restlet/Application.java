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

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.service.ConnectorService;
import org.restlet.service.DecoderService;
import org.restlet.service.MetadataService;
import org.restlet.service.StatusService;
import org.restlet.service.TunnelService;
import org.restlet.util.Engine;
import org.restlet.util.Helper;

/**
 * Restlet that can be attached to one or more VirtualHosts. Applications are
 * guaranteed to receive calls with their base reference set relatively to the
 * VirtualHost that served them. This class is both a descriptor able to create
 * the root Restlet and the actual Restlet that can be attached to one or more
 * VirtualHost instances.<br>
 * <br>
 * Applications also have many useful Services associated. They are available as
 * properties that can be eventually overriden:
 * <ul>
 * <li>"connectorService" to declare necessary client and server connectors.</li>
 * <li>"decoderService" to automatically decode or decompress request entities.</li>
 * <li>"metadataService" to provide access to metadata and their associated
 * extension names.</li>
 * <li>"statusService" to provide common representations for exception status.</li>
 * <li>"tunnelService" to tunnel method names or client preferences via query
 * parameters.</li>
 * </ul>
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Application extends Restlet {
    private static final ThreadLocal<Application> CURRENT = new ThreadLocal<Application>();

    /**
     * This variable is stored internally as a thread local variable and updated
     * each time a call enters an application.
     * 
     * @return The current context.
     */
    public static Application getCurrent() {
        return CURRENT.get();
    }

    /**
     * Sets the context to associated with the current thread.
     * 
     * @param application
     *                The thread's context.
     */
    public static void setCurrent(Application application) {
        CURRENT.set(application);
    }

    /** The author(s). */
    private volatile String author;

    /** The connector service. */
    private volatile ConnectorService connectorService;

    /**
     * The converter service.
     * 
     * @deprecated Since 1.1 with no replacement as it doesn't fit well with
     *             content negotiation. Most users prefer to handle those
     *             conversion in Resource subclasses.
     */
    @Deprecated
    private volatile org.restlet.service.ConverterService converterService;

    /** The decoder service. */
    private volatile DecoderService decoderService;

    /** The description. */
    private volatile String description;

    /** The helper provided by the implementation. */
    private volatile Helper<Application> helper;

    /** The local service. */
    private volatile MetadataService metadataService;

    /** The display name. */
    private volatile String name;

    /** The owner(s). */
    private volatile String owner;

    /** The root Restlet. */
    private volatile Restlet root;

    /** The status service. */
    private volatile StatusService statusService;

    /** The tunnel service. */
    private volatile TunnelService tunnelService;

    /**
     * Constructor. Note that usage of this constructor is not recommended as
     * your application won't have access to the parent component context. For
     * example, no dispatching will be possible as it requires access to the
     * component's client connectors.
     */
    public Application() {
        this((Context) null);
    }

    /**
     * Constructor.
     * 
     * @param parentContext
     *                The parent context. Typically the component's context.
     */
    @SuppressWarnings("deprecation")
    public Application(Context parentContext) {
        super(null);

        if (Engine.getInstance() != null) {
            this.helper = Engine.getInstance()
                    .createHelper(this, parentContext);

            // Compose the logger name
            String applicationName = (getName() == null) ? Integer
                    .toString(hashCode()) : getName();
            String loggerName = Application.class.getCanonicalName() + "."
                    + applicationName;

            // Create the application context
            setContext(this.helper.createContext(loggerName));
        }

        this.name = null;
        this.description = null;
        this.author = null;
        this.owner = null;
        this.root = null;
        this.connectorService = new ConnectorService();
        this.converterService = new org.restlet.service.ConverterService();
        this.decoderService = new DecoderService(true);
        this.metadataService = new MetadataService();
        this.statusService = new StatusService(true);
        this.tunnelService = new TunnelService(true, true, true);
    }

    /**
     * Creates a root Restlet that will receive all incoming calls. In general,
     * instances of Router, Filter or Handler classes will be used as initial
     * application Restlet. The default implementation returns null by default.
     * This method is intended to be overriden by subclasses.
     * 
     * @return The root Restlet.
     */
    public Restlet createRoot() {
        return null;
    }

    /**
     * Returns the author(s).
     * 
     * @return The author(s).
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Returns the connector service. Creates a new instance if no one has been
     * set.
     * 
     * @return The connector service.
     */
    public ConnectorService getConnectorService() {
        return this.connectorService;
    }

    /**
     * Returns the converter service. Creates a new instance if no one has been
     * set.
     * 
     * @return The converter service.
     * @deprecated Since 1.1 with no replacement as it doesn't fit well with
     *             content negotiation. Most users prefer to handle those
     *             conversion in Resource subclasses.
     */
    @Deprecated
    public org.restlet.service.ConverterService getConverterService() {
        return this.converterService;
    }

    /**
     * Returns the decoder service, enabled by default. Creates a new instance
     * if no one has been set.
     * 
     * @return The decoderservice.
     */
    public DecoderService getDecoderService() {
        return this.decoderService;
    }

    /**
     * Returns the description.
     * 
     * @return The description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private Helper<Application> getHelper() {
        return this.helper;
    }

    /**
     * Returns the metadata service. Creates a new instance if no one has been
     * set.
     * 
     * @return The metadata service.
     */
    public MetadataService getMetadataService() {
        return this.metadataService;
    }

    /**
     * Returns the display name.
     * 
     * @return The display name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the owner(s).
     * 
     * @return The owner(s).
     */
    public String getOwner() {
        return this.owner;
    }

    /**
     * Returns the root Restlet. Invokes the createRoot() method if no root has
     * been set, and stores the Restlet created for future uses.
     * 
     * @return The root Restlet.
     */
    public synchronized Restlet getRoot() {
        if (this.root == null) {
            this.root = createRoot();
        }

        return this.root;
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

    /**
     * Returns the tunnel service, enabled by default. Creates a new instance if
     * no one has been set.
     * 
     * @return The tunnel service.
     */
    public TunnelService getTunnelService() {
        return this.tunnelService;
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null)
            getHelper().handle(request, response);
    }

    /**
     * Sets the author(s).
     * 
     * @param author
     *                The author(s).
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Sets the connector service.
     * 
     * @param connectorService
     *                The connector service.
     */
    public void setConnectorService(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    /**
     * Sets the converter service.
     * 
     * @param converterService
     *                The converter service.
     * @deprecated Since 1.1 with no replacement as it doesn't fit well with
     *             content negotiation. Most users prefer to handle those
     *             conversion in Resource subclasses.
     */
    @Deprecated
    public void setConverterService(
            org.restlet.service.ConverterService converterService) {
        this.converterService = converterService;
    }

    /**
     * Sets the decoder service.
     * 
     * @param decoderService
     *                The decoder service.
     */
    public void setDecoderService(DecoderService decoderService) {
        this.decoderService = decoderService;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *                The description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the metadata service.
     * 
     * @param metadataService
     *                The metadata service.
     */
    public void setMetadataService(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    /**
     * Sets the display name.
     * 
     * @param name
     *                The display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the owner(s).
     * 
     * @param owner
     *                The owner(s).
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * Sets the root Restlet.
     * 
     * @param root
     *                The root Restlet.
     */
    public synchronized void setRoot(Restlet root) {
        this.root = root;
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
     * Sets the tunnel service.
     * 
     * @param tunnelService
     *                The tunnel service.
     */
    public void setTunnelService(TunnelService tunnelService) {
        this.tunnelService = tunnelService;
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            super.start();
            if (getHelper() != null)
                getHelper().start();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            if (getHelper() != null)
                getHelper().stop();
            super.stop();
        }
    }

}
