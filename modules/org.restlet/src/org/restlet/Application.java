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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Filter;
import java.util.logging.Level;

import org.restlet.engine.Engine;
import org.restlet.engine.application.ApplicationHelper;
import org.restlet.engine.resource.AnnotationUtils;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.routing.VirtualHost;
import org.restlet.security.Role;
import org.restlet.service.ConnectorService;
import org.restlet.service.ConnegService;
import org.restlet.service.ConverterService;
import org.restlet.service.DecoderService;
import org.restlet.service.EncoderService;
import org.restlet.service.MetadataService;
import org.restlet.service.RangeService;
import org.restlet.service.StatusService;
import org.restlet.service.TunnelService;
import org.restlet.util.ServiceList;

/**
 * Restlet managing a coherent set of resources and services. Applications are
 * guaranteed to receive calls with their base reference set relatively to the
 * {@link VirtualHost} that served them. This class is both a descriptor able to
 * create the root Restlet and the actual Restlet that can be attached to one or
 * more VirtualHost instances.<br>
 * <br>
 * Applications also have many useful services associated. Most are enabled by
 * default and are available as properties that can be eventually overridden:
 * <ul>
 * <li>"connectorService" to declare necessary client and server connectors.</li>
 * <li>"converterService" to convert between regular objects and
 * representations.</li>
 * <li>"decoderService" to automatically decode or uncompress received entities.
 * </li>
 * <li>"encoderService" to automatically encode or compress sent entities
 * (disabled by default).</li>
 * <li>"metadataService" to provide access to metadata and their associated
 * extension names.</li>
 * <li>"rangeService" to automatically exposes ranges of response entities.</li>
 * <li>"statusService" to provide common representations for exception status.</li>
 * <li>"taskService" to run tasks asynchronously (disabled by default).</li>
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

    /** Indicates if the debugging mode is enabled. */
    private volatile boolean debugging;

    /** The helper provided by the implementation. */
    private volatile ApplicationHelper helper;

    /** The inbound root Restlet. */
    private volatile Restlet inboundRoot;

    /** The outbound root Restlet. */
    private volatile Restlet outboundRoot;

    /** The modifiable list of roles. */
    private final List<Role> roles;

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
            this.helper.setContext(context);
        }

        ConnegService connegService = new ConnegService();
        ConverterService converterService = new ConverterService();
        MetadataService metadataService = new MetadataService();

        this.debugging = false;
        this.outboundRoot = null;
        this.inboundRoot = null;
        this.roles = new CopyOnWriteArrayList<Role>();
        this.services = new ServiceList(context);
        this.services.add(new TunnelService(true, true));
        this.services.add(new StatusService(true, converterService,
                metadataService, connegService));
        this.services.add(new DecoderService());
        this.services.add(new EncoderService(false));
        this.services.add(new RangeService());
        this.services.add(new ConnectorService());
        this.services.add(connegService);
        this.services.add(converterService);
        this.services.add(metadataService);

        // [ifndef gae]
        this.services.add(new org.restlet.service.TaskService(false));
        // [enddef]
    }

    /**
     * Creates a inbound root Restlet that will receive all incoming calls. In
     * general, instances of Router, Filter or Finder classes will be used as
     * initial application Restlet. The default implementation returns null by
     * default. This method is intended to be overridden by subclasses.
     * 
     * @return The inbound root Restlet.
     */
    public Restlet createInboundRoot() {
        return null;
    }

    /**
     * Creates a outbound root Restlet that will receive all outgoing calls from
     * ClientResource. In general, instances of {@link Router} and
     * {@link Filter} classes will be used. The default implementation returns a
     * Restlet giving access to the the outbound service layer and finally to
     * the {@link Context#getClientDispatcher()}.
     * <p>
     * This method is intended to be overridden by subclasses but in order to
     * benefit from the outbound service filtering layer, the original outbound
     * root must be careful attached again at the end of the user filtering
     * layer.
     * 
     * @return The outbound root Restlet.
     */
    public Restlet createOutboundRoot() {
        return getHelper().getFirstOutboundFilter();
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
     * Returns the content negotiation service. The service is enabled by
     * default.
     * 
     * @return The content negotiation service.
     */
    public ConnegService getConnegService() {
        return getServices().get(ConnegService.class);
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
     * Returns the encoder service. The service is disabled by default.
     * 
     * @return The encoder service.
     */
    public EncoderService getEncoderService() {
        return getServices().get(EncoderService.class);
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private ApplicationHelper getHelper() {
        return this.helper;
    }

    /**
     * Returns the inbound root Restlet.
     * 
     * @return The inbound root Restlet.
     */
    public Restlet getInboundRoot() {
        if (this.inboundRoot == null) {
            synchronized (this) {
                if (this.inboundRoot == null) {
                    this.inboundRoot = createInboundRoot();
                }
            }
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
    public Restlet getOutboundRoot() {
        if (this.outboundRoot == null) {
            synchronized (this) {
                if (this.outboundRoot == null) {
                    this.outboundRoot = createOutboundRoot();
                }
            }
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
     * @deprecated
     */
    // [ifndef gae] method
    @Deprecated
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
     * Indicates if the debugging mode is enabled. True by default.
     * 
     * @return True if the debugging mode is enabled.
     */
    public boolean isDebugging() {
        return debugging;
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
     * Sets the content negotiation service.
     * 
     * @param connegService
     *            The content negotiation service.
     */
    public void setConnegService(ConnegService connegService) {
        getServices().set(connegService);
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        getHelper().setContext(context);
        getServices().setContext(context);
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
     * Indicates if the debugging mode is enabled.
     * 
     * @param debugging
     *            True if the debugging mode is enabled.
     */
    public void setDebugging(boolean debugging) {
        this.debugging = debugging;
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
     * Sets the encoder service.
     * 
     * @param encoderService
     *            The encoder service.
     */
    public void setEncoderService(EncoderService encoderService) {
        getServices().set(encoderService);
    }

    /**
     * Sets the inbound root Resource class.
     * 
     * @param inboundRootClass
     *            The inbound root Resource class.
     */
    public synchronized void setInboundRoot(
            Class<? extends ServerResource> inboundRootClass) {
        setInboundRoot(createFinder(inboundRootClass));
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
     * Sets the outbound root Resource class.
     * 
     * @param outboundRootClass
     *            The client root {@link ServerResource} subclass.
     */
    public synchronized void setOutboundRoot(
            Class<? extends ServerResource> outboundRootClass) {
        setOutboundRoot(createFinder(outboundRootClass));
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
     * Sets the modifiable list of roles. This method clears the current list
     * and adds all entries in the parameter list.
     * 
     * @param roles
     *            A list of roles.
     */
    public void setRoles(List<Role> roles) {
        synchronized (getRoles()) {
            if (roles != getRoles()) {
                getRoles().clear();

                if (roles != null) {
                    getRoles().addAll(roles);
                }
            }
        }
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
            if (isDebugging()) {
                getLogger().log(
                        Level.INFO,
                        "Starting " + getClass().getName()
                                + " application in debug mode");
            } else {
                getLogger().log(Level.INFO,
                        "Starting " + getClass().getName() + " application");
            }

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

            // Must be invoked as a last step
            super.start();
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
            // Must be invoked as a first step
            super.stop();

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
            AnnotationUtils.getInstance().clearCache();
        }
    }

}
