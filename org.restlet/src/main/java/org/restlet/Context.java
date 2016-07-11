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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import org.restlet.data.Parameter;
import org.restlet.engine.Engine;
import org.restlet.util.Series;

/**
 * Contextual data and services provided to a set of Restlets. The context is
 * the means by which a Restlet may access the software environment within the
 * framework. It is typically provided by the immediate parent Restlet
 * (Application is the most common case).<br>
 * <br>
 * Concurrency note: attributes and parameters of a context are stored in
 * concurrent collections that guarantee thread safe access and modification. If
 * several threads concurrently access objects and modify these collections,
 * they should synchronize on the lock of the Context instance.
 * 
 * @author Jerome Louvel
 */
public class Context {

    // [ifndef gwt] member
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<Context>();

    /**
     * Returns the context associated to the current {@link Restlet}. The
     * context can be the one of a {@link Component}, an {@link Application}, a
     * {@link org.restlet.routing.Filter} or any other {@link Restlet} subclass.<br>
     * <br>
     * Warning: this method should only be used under duress. You should by
     * default prefer obtaining the current context using methods such as
     * {@link org.restlet.Restlet#getContext()} or
     * {@link org.restlet.resource.Resource#getContext()}.<br>
     * <br>
     * This variable is stored internally as a thread local variable and updated
     * each time a request is handled by a {@link Restlet} via the
     * {@link Restlet#handle(org.restlet.Request, org.restlet.Response)} method.
     * 
     * @return The current context.
     */
    public static Context getCurrent() {
        // [ifndef gwt] line
        return CURRENT.get();
        // [ifdef gwt] line uncomment
        // return new Context();
    }

    /**
     * Returns the current context's logger.
     * 
     * @return The current context's logger.
     */
    public static Logger getCurrentLogger() {
        // [ifndef gwt] instruction
        return (Context.getCurrent() != null) ? Context.getCurrent()
                .getLogger() : Engine.getLogger("org.restlet");

        // [ifdef gwt] instruction uncomment
        // return Engine.getLogger("org.restlet");
    }

    // [ifndef gwt] method
    /**
     * Sets the context to associated with the current thread.
     * 
     * @param context
     *            The thread's context.
     */
    public static void setCurrent(Context context) {
        CURRENT.set(context);
    }

    /** The client dispatcher. */
    private volatile Restlet clientDispatcher;

    // [ifndef gwt] member
    /** The server dispatcher. */
    private volatile Restlet serverDispatcher;

    /** The modifiable attributes map. */
    private final ConcurrentMap<String, Object> attributes;

    /** The logger instance to use. */
    private volatile Logger logger;

    /** The modifiable series of parameters. */
    private final Series<Parameter> parameters;

    // [ifndef gwt] member
    /**
     * The enroler that can add the user roles based on Restlet default
     * authorization model.
     */
    private volatile org.restlet.security.Enroler defaultEnroler;

    // [ifndef gwt] member
    /**
     * The verifier that can check the validity of user/secret couples based on
     * Restlet default authorization model.
     */
    private volatile org.restlet.security.Verifier defaultVerifier;

    // [ifndef gwt] member
    /** The executor service. */
    private volatile ScheduledExecutorService executorService;

    /**
     * Constructor. Writes log messages to "org.restlet".
     */
    public Context() {
        this("org.restlet");
    }

    /**
     * Constructor.
     * 
     * @param logger
     *            The logger instance of use.
     */
    public Context(Logger logger) {
        this.attributes = new ConcurrentHashMap<String, Object>();
        this.logger = logger;
        // [ifndef gwt] instruction
        this.parameters = new Series<Parameter>(Parameter.class,
                new CopyOnWriteArrayList<Parameter>());
        // [ifdef gwt] instruction uncomment
        // this.parameters = new org.restlet.engine.util.ParameterSeries(new
        // CopyOnWriteArrayList<Parameter>());
        this.clientDispatcher = null;

        // [ifndef gwt]
        this.defaultEnroler = null;
        this.serverDispatcher = null;
        this.defaultVerifier = null;
        // [enddef]
    }

    /**
     * Constructor.
     * 
     * @param loggerName
     *            The name of the logger to use.
     */
    public Context(String loggerName) {
        this(Engine.getLogger(loggerName));
    }

    /**
     * Creates a protected child context. This is especially useful for new
     * application attached to their parent component, to ensure their isolation
     * from the other applications. By default it creates a new context instance
     * with empty or null properties, except the client and server dispatchers
     * that are wrapped for isolation purpose.
     * 
     * @return The child context.
     */
    public Context createChildContext() {
        // [ifndef gwt] instruction
        return new org.restlet.engine.util.ChildContext(this);
        // [ifdef gwt] instruction uncomment
        // return new Context();
    }

    /**
     * Returns a modifiable attributes map that can be used by developers to
     * save information relative to the context. This is a convenient means to
     * provide common objects to all the Restlets and Resources composing an
     * Application.<br>
     * <br>
     * 
     * In addition, this map is a shared space between the developer and the
     * Restlet implementation. For this purpose, all attribute names starting
     * with "org.restlet" are reserved. Currently the following attributes are
     * used:
     * <table>
     * <tr>
     * <th>Attribute name</th>
     * <th>Class name</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>org.restlet.application</td>
     * <td>org.restlet.Application</td>
     * <td>The parent application providing this context, if any.</td>
     * </tr>
     * </table>
     * </td>
     * 
     * @return The modifiable attributes map.
     */
    public ConcurrentMap<String, Object> getAttributes() {
        return this.attributes;
    }

    /**
     * Returns a request dispatcher to available client connectors. When you ask
     * the dispatcher to handle a request, it will automatically select the
     * appropriate client connector for your request, based on the
     * request.protocol property or on the resource URI's scheme. This call is
     * blocking and will return an updated response object.
     * 
     * @return A request dispatcher to available client connectors.
     */
    public Restlet getClientDispatcher() {
        return this.clientDispatcher;
    }

    // [ifndef gwt] method
    /**
     * Returns an enroler that can add the user roles based on authenticated
     * user principals.
     * 
     * @return An enroler.
     */
    public org.restlet.security.Enroler getDefaultEnroler() {
        return defaultEnroler;
    }

    // [ifndef gwt] method
    /**
     * Returns a verifier that can check the validity of the credentials
     * associated to a request.
     * 
     * @return A verifier.
     */
    public org.restlet.security.Verifier getDefaultVerifier() {
        return this.defaultVerifier;
    }

    // [ifndef gwt] method
    /**
     * Returns the executor service.
     * 
     * @return The executor service.
     */
    public ScheduledExecutorService getExecutorService() {
        return this.executorService;
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        return this.logger;
    }

    /**
     * Returns the modifiable series of parameters. A parameter is a pair
     * composed of a name and a value and is typically used for configuration
     * purpose, like Java properties. Note that multiple parameters with the
     * same name can be declared and accessed.
     * 
     * @return The modifiable series of parameters.
     */
    public Series<Parameter> getParameters() {
        return this.parameters;
    }

    // [ifndef gwt] method
    /**
     * Returns a request dispatcher to component's virtual hosts. This is useful
     * for application that want to optimize calls to other applications hosted
     * in the same component or to the application itself.<br>
     * <br>
     * The processing is the same as what would have been done if the request
     * came from one of the component's server connectors. It first must match
     * one of the registered virtual hosts. Then it can be routed to one of the
     * attached Restlets, typically an Application.<br>
     * <br>
     * Note that the RIAP pseudo protocol isn't supported by this dispatcher,
     * you should instead rely on the {@link #getClientDispatcher()} method.
     * 
     * @return A request dispatcher to the server connectors' router.
     */
    public Restlet getServerDispatcher() {
        return this.serverDispatcher;
    }

    /**
     * Sets the modifiable map of attributes. This method clears the current map
     * and puts all entries in the parameter map.
     * 
     * @param attributes
     *            A map of attributes.
     */
    public void setAttributes(Map<String, Object> attributes) {
        synchronized (getAttributes()) {
            if (attributes != getAttributes()) {
                getAttributes().clear();

                if (attributes != null) {
                    getAttributes().putAll(attributes);
                }
            }
        }
    }

    /**
     * Sets the client dispatcher.
     * 
     * @param clientDispatcher
     *            The new client dispatcher.
     */
    public void setClientDispatcher(Restlet clientDispatcher) {
        this.clientDispatcher = clientDispatcher;
    }

    // [ifndef gwt] method
    /**
     * Sets an enroler that can add the user roles based on authenticated user
     * principals.
     * 
     * @param enroler
     *            An enroler.
     */
    public void setDefaultEnroler(org.restlet.security.Enroler enroler) {
        this.defaultEnroler = enroler;
    }

    // [ifndef gwt] method
    /**
     * Sets a local verifier that can check the validity of user/secret couples
     * based on Restlet default authorization model.
     * 
     * @param verifier
     *            A local verifier.
     */
    public void setDefaultVerifier(org.restlet.security.Verifier verifier) {
        this.defaultVerifier = verifier;
    }

    // [ifndef gwt] method
    /**
     * Sets the executor service.
     * 
     * @param executorService
     *            The executor service.
     */
    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Sets the logger.
     * 
     * @param logger
     *            The logger.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets the logger.
     * 
     * @param loggerName
     *            The name of the logger to use.
     */
    public void setLogger(String loggerName) {
        setLogger(Engine.getLogger(loggerName));
    }

    /**
     * Sets the modifiable series of parameters. This method clears the current
     * series and adds all entries in the parameter series.
     * 
     * @param parameters
     *            A series of parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        synchronized (getParameters()) {
            if (parameters != getParameters()) {
                getParameters().clear();

                if (parameters != null) {
                    getParameters().addAll(parameters);
                }
            }
        }
    }

    // [ifndef gwt] method
    /**
     * Sets the server dispatcher.
     * 
     * @param serverDispatcher
     *            The new server dispatcher.
     */
    public void setServerDispatcher(Restlet serverDispatcher) {
        this.serverDispatcher = serverDispatcher;
    }

}
