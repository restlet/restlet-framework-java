/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

import org.restlet.client.data.Parameter;
import org.restlet.client.engine.Engine;
import org.restlet.client.util.Series;

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


    /**
     * Returns the context associated to the current {@link Restlet}. The
     * context can be the one of a {@link Component}, an {@link Application}, a
     * {@link org.restlet.client.routing.Filter} or any other {@link Restlet} subclass.<br>
     * <br>
     * Warning: this method should only be used under duress. You should by
     * default prefer obtaining the current context using methods such as
     * {@link org.restlet.client.Restlet#getContext()} or
     * {@link org.restlet.client.resource.Resource#getContext()}.<br>
     * <br>
     * This variable is stored internally as a thread local variable and updated
     * each time a request is handled by a {@link Restlet} via the
     * {@link Restlet#handle(org.restlet.client.Request, org.restlet.client.Response)} method.
     * 
     * @return The current context.
     */
    public static Context getCurrent() {
         return new Context();
    }

    /**
     * Returns the current context's logger.
     * 
     * @return The current context's logger.
     */
    public static Logger getCurrentLogger() {

         return Engine.getLogger("org.restlet.client");
    }


    /** The client dispatcher. */
    private volatile Restlet clientDispatcher;


    /** The modifiable attributes map. */
    private final ConcurrentMap<String, Object> attributes;

    /** The logger instance to use. */
    private volatile Logger logger;

    /** The modifiable series of parameters. */
    private final Series<Parameter> parameters;




    /**
     * Constructor. Writes log messages to "org.restlet.client".
     */
    public Context() {
        this("org.restlet.client");
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
         this.parameters = new org.restlet.client.engine.util.ParameterSeries(new
         CopyOnWriteArrayList<Parameter>());
        this.clientDispatcher = null;

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
         return new Context();
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
     * with "org.restlet.client" are reserved. Currently the following attributes are
     * used:
     * <table>
     * <tr>
     * <th>Attribute name</th>
     * <th>Class name</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>org.restlet.client.application</td>
     * <td>org.restlet.client.Application</td>
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


}
