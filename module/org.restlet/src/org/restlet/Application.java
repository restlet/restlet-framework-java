/*
 * Copyright 2005-2007 Noelios Consulting.
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
import org.restlet.service.ConverterService;
import org.restlet.service.DecoderService;
import org.restlet.service.MetadataService;
import org.restlet.service.StatusService;
import org.restlet.service.TunnelService;
import org.restlet.util.Factory;
import org.restlet.util.Helper;

/**
 * Restlet that can be attached to one or more VirtualHosts. Applications are
 * guaranteed to receive calls with their base reference set relatively to the
 * VirtualHost that served them. This class is both a descriptor able to create
 * the root Restlet and the actual Restlet that can be attached to one or more
 * VirtualHost instances.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class Application extends Restlet {
    /** The display name. */
    private String name;

    /** The description. */
    private String description;

    /** The author(s). */
    private String author;

    /** The owner(s). */
    private String owner;

    /** The root Restlet. */
    private Restlet root;

    /** The connector service. */
    private ConnectorService connectorService;

    /** The converter service. */
    private ConverterService converterService;

    /** The decoder service. */
    private DecoderService decoderService;

    /** The local service. */
    private MetadataService metadataService;

    /** The status service. */
    private StatusService statusService;

    /** The tunnel service. */
    private TunnelService tunnelService;

    /** The helper provided by the implementation. */
    private Helper helper;

    /**
     * Constructor.
     */
    public Application() {
        this((Context) null);
    }

    /**
     * Constructor.
     * 
     * @param parentContext
     *            The parent context. Typically the component's context.
     */
    public Application(Context parentContext) {
        super(null);

        if (Factory.getInstance() != null) {
            this.helper = Factory.getInstance().createHelper(this,
                    parentContext);

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
        this.connectorService = null;
        this.decoderService = null;
        this.metadataService = null;
        this.statusService = null;
        this.tunnelService = null;
    }

    /**
     * Creates a root Restlet that will receive all incoming calls. In general,
     * instances of Router, Filter or Handler classes will be used as initial
     * application Restlet. The default implementation returns null by default.
     * This method is intended to be overriden by subclasses.
     * 
     * @return The root Restlet.
     */
    public abstract Restlet createRoot();

    /**
     * Returns the author(s).
     * 
     * @return The author(s).
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Returns the connector service.
     * 
     * @return The connector service.
     */
    public ConnectorService getConnectorService() {
        if (this.connectorService == null)
            this.connectorService = new ConnectorService();
        return this.connectorService;
    }

    /**
     * Returns the converter service.
     * 
     * @return The converter service.
     */
    public ConverterService getConverterService() {
        if (this.converterService == null)
            this.converterService = new ConverterService();
        return this.converterService;
    }

    /**
     * Returns the decoder service. This service is enabled by default.
     * 
     * @return The decoderservice.
     */
    public DecoderService getDecoderService() {
        if (this.decoderService == null)
            this.decoderService = new DecoderService(true);
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
    private Helper getHelper() {
        return this.helper;
    }

    /**
     * Returns the metadata service.
     * 
     * @return The metadata service.
     */
    public MetadataService getMetadataService() {
        if (this.metadataService == null)
            this.metadataService = new MetadataService();
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
     * Returns the root Restlet. Invokes the createRoot() method if no Restlet
     * exists.
     * 
     * @return The root Restlet.
     */
    public Restlet getRoot() {
        if (this.root == null) {
            this.root = createRoot();
        }

        return this.root;
    }

    /**
     * Returns the status service. This service is enabled by default.
     * 
     * @return The status service.
     */
    public StatusService getStatusService() {
        if (this.statusService == null)
            this.statusService = new StatusService(true);
        return this.statusService;
    }

    /**
     * Returns the tunnel service. This service is enabled by default.
     * 
     * @return The tunnel service.
     */
    public TunnelService getTunnelService() {
        if (this.tunnelService == null)
            this.tunnelService = new TunnelService(true, true, true);
        return this.tunnelService;
    }

    /**
     * Handles a call.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        init(request, response);
        if (getHelper() != null)
            getHelper().handle(request, response);
    }

    /**
     * Sets the author(s).
     * 
     * @param author
     *            The author(s).
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            The description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the display name.
     * 
     * @param name
     *            The display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the owner(s).
     * 
     * @param owner
     *            The owner(s).
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /** Start callback. */
    public void start() throws Exception {
        super.start();
        if (getHelper() != null)
            getHelper().start();
    }

    /** Stop callback. */
    public void stop() throws Exception {
        if (getHelper() != null)
            getHelper().stop();
        super.stop();
    }

}
