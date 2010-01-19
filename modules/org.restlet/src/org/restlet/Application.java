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

import org.restlet.engine.Engine;
import org.restlet.engine.RestletHelper;
import org.restlet.engine.application.ApplicationHelper;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.resource.Finder;
import org.restlet.security.Role;
import org.restlet.service.ConnectorService;
import org.restlet.service.ConverterService;
import org.restlet.service.DecoderService;
import org.restlet.service.MetadataService;
import org.restlet.service.RangeService;
import org.restlet.service.StatusService;
import org.restlet.service.TunnelService;
import org.restlet.util.ServiceList;

/**
 * Restlet managing a coherent set of Resources and Services. Applications are
 * guaranteed to receive calls with their base reference set relatively to the
 * VirtualHost that served them. This class is both a descriptor able to create
 * the root Restlet and the actual Restlet that can be attached to one or more
 * VirtualHost instances.<br>
 * <br>
 * Applications also have many useful services associated. They are all enabled
 * by default and are available as properties that can be eventually overridden:
 * <ul>
 * <li>"connectorService" to declare necessary client and server connectors.</li>
 * <li>"converterService" to convert between regular objects and
 * representations.</li>
 * <li>"decoderService" to automatically decode or uncompress request entities.</li>
 * <li>"metadataService" to provide access to metadata and their associated
 * extension names.</li>
 * <li>"rangeService" to automatically exposes ranges of response entities.</li>
 * <li>"statusService" to provide common representations for exception status.</li>
 * <li>"taskService" to run tasks asynchronously.</li>
 * <li>"tunnelService" to tunnel method names or client preferences via query
 * parameters.</li>
 * </ul>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class Application extends Restlet {
    private static final ThreadLocal<Application> CURRENT = new ThreadLocal<Application>();

    /**
     * This variable is stored internally as a thread local variable and updated
     * each time a call enters an application.
     * 
     * Warning: this method should only be used under duress. You should by
     * default prefer obtaining the current application using methods such as
     * {@link org.restlet.resource.Resource#getApplication()}
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
     *            The thread's context.
     */
    public static void setCurrent(Application application) {
        CURRENT.set(application);
    }

    /** Finder class to instantiate. */
    private volatile Class<? extends Finder> finderClass;

    /** The helper provided by the implementation. */
    private volatile RestletHelper<Application> helper;

    /** The modifiable list of roles. */
    private final List<Role> roles;

    /** The inbound root Restlet. */
    private volatile Restlet inboundRoot;

    /** The outbound root Restlet. */
    private volatile Restlet outboundRoot;

    /** The list of services. */
    private final ServiceList services;

    /**
     * Constructor. Note this constructor is convenient because you don't have
     * to provide a context like for {@link #Application(Context)}. Therefore
     * the context will initially be null. It's only when you attach the
     * application to a virtual host via one of its attach*() methods that a
     * proper context will be set.
     */
    public Application() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context to use based on parent component context. This
     *            context should be created using the
     *            {@link Context#createChildContext()} method to ensure a proper
     *            isolation with the other applications.
     */
    public Application(Context context) {
        super(context);

        if (Engine.getInstance() != null) {
            this.helper = new ApplicationHelper(this);
        }

        this.roles = new CopyOnWriteArrayList<Role>();

        this.outboundRoot = null;
        this.inboundRoot = null;
        this.services = new ServiceList();
        this.services.add(new TunnelService(true, true));
        this.services.add(new StatusService());
        this.services.add(new DecoderService());
        this.services.add(new RangeService());
        this.services.add(new ConnectorService());
        this.services.add(new ConverterService());
        this.services.add(new MetadataService());

        // [ifndef gae]
        this.services.add(new org.restlet.service.TaskService());
        // [enddef]
    }

    /**
     * Creates a inbound root Restlet that will receive all incoming calls. In
     * general, instances of Router, Filter or Finder classes will be used as
     * initial application Restlet. The default implementation returns null by
     * default. This method is intended to be overridden by subclasses.
     * 
     * @return The server root Restlet.
     */
    public Restlet createInboundRoot() {
        return null;
    }

    /**
     * Creates a outbound root Restlet that will receive all outgoing calls from
     * ClientResource. In general, instances of Router, Filter or Finder classes
     * will be used as initial application Restlet. The default implementation
     * returns the {@link Context#getClientDispatcher()} by default. This method
     * is intended to be overridden by subclasses.
     * 
     * @return The server root Restlet.
     */
    public Restlet createOutboundRoot() {
        return (getContext() != null) ? getContext().getClientDispatcher()
                : null;
    }

    /**
     * Creates a inbound root Restlet that will receive all incoming calls. In
     * general, instances of Router, Filter or Handler classes will be used as
     * initial application Restlet. The default implementation returns null by
     * default. This method is intended to be overridden by subclasses.
     * 
     * @return The server root Restlet.
     * @deprecated Override the {@link #createInboundRoot()} method instead.
     */
    @Deprecated
    public Restlet createRoot() {
        return createInboundRoot();
    }

    /**
     * Returns the connector service. The service is enabled by default.
     * 
     * @return The connector service.
     */
    public ConnectorService getConnectorService() {
        return getServices().get(ConnectorService.class);
    }

    /**
     * Returns the converter service. The service is enabled by default.
     * 
     * @return The converter service.
     */
    public ConverterService getConverterService() {
        return getServices().get(ConverterService.class);
    }

    /**
     * Returns the decoder service. The service is enabled by default.
     * 
     * @return The decoder service.
     */
    public DecoderService getDecoderService() {
        return getServices().get(DecoderService.class);
    }

    /**
     * Returns the finder class used to instantiate resource classes. By
     * default, it returns the {@link Finder} class.
     * 
     * @return the finder class to instantiate.
     */
    public Class<? extends Finder> getFinderClass() {
        return finderClass;
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private RestletHelper<Application> getHelper() {
        return this.helper;
    }

    /**
     * Returns the inbound root Restlet.
     * 
     * @return The inbound root Restlet.
     */
    public synchronized Restlet getInboundRoot() {
        if (this.inboundRoot == null) {
            this.inboundRoot = createRoot();
        }

        return this.inboundRoot;
    }

    /**
     * Returns the metadata service. The service is enabled by default.
     * 
     * @return The metadata service.
     */
    public MetadataService getMetadataService() {
        return getServices().get(MetadataService.class);
    }

    /**
     * Returns the outbound root Restlet.
     * 
     * @return The outbound root Restlet.
     */
    public synchronized Restlet getOutboundRoot() {
        if (this.outboundRoot == null) {
            this.outboundRoot = createOutboundRoot();
        }

        return this.outboundRoot;
    }

    /**
     * Returns the range service.
     * 
     * @return The range service.
     */
    public RangeService getRangeService() {
        return getServices().get(RangeService.class);
    }

    /**
     * Returns the role associated to the given name.
     * 
     * @param name
     *            The name of the role to find.
     * @return The role matched or null.
     */
    public Role getRole(String name) {
        for (Role role : getRoles()) {
            if (role.getName().equals(name)) {
                return role;
            }
        }

        return null;
    }

    /**
     * Returns the modifiable list of roles.
     * 
     * @return The modifiable list of roles.
     */
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * Returns the root inbound Restlet. Invokes the createRoot() method if no
     * inbound root has been set, and stores the Restlet created for future
     * uses.
     * 
     * @return The root inbound Restlet.
     * @deprecated Use the {@link #getInboundRoot()} method instead.
     */
    @Deprecated
    public synchronized Restlet getRoot() {
        return getInboundRoot();
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
     * Returns the status service. The service is enabled by default.
     * 
     * @return The status service.
     */
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

    /**
     * Returns the tunnel service. The service is enabled by default.
     * 
     * @return The tunnel service.
     */
    public TunnelService getTunnelService() {
        return getServices().get(TunnelService.class);
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        if (getHelper() != null) {
            getHelper().handle(request, response);
        }
    }

    /**
     * Sets the client root Resource class.
     * 
     * @param clientRootClass
     *            The client root Resource class.
     */
    public synchronized void setClientRoot(Class<?> clientRootClass) {
        setOutboundRoot(Finder.createFinder(clientRootClass, getFinderClass(),
                getContext(), getLogger()));
    }

    /**
     * Sets the connector service.
     * 
     * @param connectorService
     *            The connector service.
     */
    public void setConnectorService(ConnectorService connectorService) {
        getServices().set(connectorService);
    }

    /**
     * Sets the converter service.
     * 
     * @param converterService
     *            The converter service.
     */
    public void setConverterService(ConverterService converterService) {
        getServices().set(converterService);
    }

    /**
     * Sets the decoder service.
     * 
     * @param decoderService
     *            The decoder service.
     */
    public void setDecoderService(DecoderService decoderService) {
        getServices().set(decoderService);
    }

    /**
     * Sets the finder class to instantiate.
     * 
     * @param finderClass
     *            The finder class to instantiate.
     */
    public void setFinderClass(Class<? extends Finder> finderClass) {
        this.finderClass = finderClass;
    }

    /**
     * Sets the inbound root Resource class.
     * 
     * @param inboundRootClass
     *            The inbound root Resource class.
     */
    public synchronized void setInboundRoot(Class<?> inboundRootClass) {
        setInboundRoot(Finder.createFinder(inboundRootClass, getFinderClass(),
                getContext(), getLogger()));
    }

    /**
     * Sets the inbound root Restlet.
     * 
     * @param inboundRoot
     *            The inbound root Restlet.
     */
    public synchronized void setInboundRoot(Restlet inboundRoot) {
        this.inboundRoot = inboundRoot;

        if ((inboundRoot != null) && (inboundRoot.getContext() == null)) {
            inboundRoot.setContext(getContext());
        }
    }

    /**
     * Sets the metadata service.
     * 
     * @param metadataService
     *            The metadata service.
     */
    public void setMetadataService(MetadataService metadataService) {
        getServices().set(metadataService);
    }

    /**
     * Sets the outbound root Restlet.
     * 
     * @param outboundRoot
     *            The outbound root Restlet.
     */
    public synchronized void setOutboundRoot(Restlet outboundRoot) {
        this.outboundRoot = outboundRoot;

        if ((outboundRoot != null) && (outboundRoot.getContext() == null)) {
            outboundRoot.setContext(getContext());
        }
    }

    /**
     * Sets the range service.
     * 
     * @param rangeService
     *            The range service.
     */
    public void setRangeService(RangeService rangeService) {
        getServices().set(rangeService);
    }

    /**
     * Sets the list of roles.
     * 
     * @param roles
     *            The list of roles.
     */
    public void setRoles(List<Role> roles) {
        this.roles.clear();

        if (roles != null) {
            this.roles.addAll(roles);
        }
    }

    /**
     * Sets the inbound root Resource class.
     * 
     * @param inboundRootClass
     *            The inbound root Resource class.
     * @deprecated Use the {@link #setInboundRoot(Class)} method instead.
     */
    @Deprecated
    public synchronized void setRoot(Class<?> inboundRootClass) {
        setInboundRoot(inboundRootClass);
    }

    /**
     * Sets the inbound root Restlet.
     * 
     * @param inboundRoot
     *            The inbound root Restlet.
     * @deprecated Use the {@link #setInboundRoot(Restlet)} method instead.
     */
    @Deprecated
    public synchronized void setRoot(Restlet inboundRoot) {
        setInboundRoot(inboundRoot);
    }

    /**
     * Sets the status service.
     * 
     * @param statusService
     *            The status service.
     */
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
     * Sets the tunnel service.
     * 
     * @param tunnelService
     *            The tunnel service.
     */
    public void setTunnelService(TunnelService tunnelService) {
        getServices().set(tunnelService);
    }

    /**
     * Starts the application, all the enabled associated services then the
     * inbound and outbound roots.
     */
    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            super.start();

            if (getHelper() != null) {
                getHelper().start();
            }

            getServices().start();

            if (getInboundRoot() != null) {
                getInboundRoot().start();
            }

            if (getOutboundRoot() != null) {
                getOutboundRoot().start();
            }
        }
    }

    /**
     * Stops the application, the inbound and outbound roots then all the
     * enabled associated services. Finally, it clears the internal cache of
     * annotations.
     */
    @Override
    public synchronized void stop() throws Exception {
        if (isStarted()) {
            if (getOutboundRoot() != null) {
                getOutboundRoot().stop();
            }

            if (getInboundRoot() != null) {
                getInboundRoot().stop();
            }

            getServices().stop();

            if (getHelper() != null) {
                getHelper().stop();
            }

            // Clear the annotations cache
            AnnotationUtils.clearCache();

            super.stop();
        }
    }

}
