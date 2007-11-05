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

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * Contextual data and services provided to a Restlet. The context is the means
 * by which a Restlet may access the software environment within the framework.
 * It is typically provided by the immediate parent Restlet (Component and
 * Application are the most common cases).
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Context {
    /** The modifiable attributes map. */
    private Map<String, Object> attributes;

    /** The modifiable series of parameters. */
    private Series<Parameter> parameters;

    /** The logger instance to use. */
    private Logger logger;

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
     *                The logger instance of use.
     */
    public Context(Logger logger) {
        this.logger = logger;
    }

    /**
     * Constructor.
     * 
     * @param loggerName
     *                The name of the logger to use.
     */
    public Context(String loggerName) {
        this(Logger.getLogger(loggerName));
    }

    /**
     * Returns the parent application if it exists, or null.
     * 
     * @return The parent application if it exists, or null.
     */
    public Application getApplication() {
        return (Application) getAttributes().get(Application.KEY);
    }

    /**
     * Returns a modifiable attributes map that can be used by developers to
     * save information relative to the context. Creates a new instance if no
     * one has been set. This is a convenient mean to provide common objects to
     * all the Restlets and Resources composing an Application.<br/> <br/>
     * 
     * In addition, this map is a shared space between the developer and the
     * Restlet implementation. For this purpose, all attribute names starting
     * with "org.restlet" are reserved. Currently the following attributes are
     * used: <table>
     * <tr>
     * <th>Attribute name</th>
     * <th>Class name</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>org.restlet.application</td>
     * <td>org.restlet.Application</td>
     * <td>The parent application providing this context, if any. </td>
     * </tr>
     * </table></td>
     * 
     * @return The modifiable attributes map.
     */
    public Map<String, Object> getAttributes() {
        if (this.attributes == null) {
            this.attributes = new TreeMap<String, Object>();
        }

        return this.attributes;
    }

    /**
     * Returns a request dispatcher to available client connectors. When you ask
     * the dispatcher to handle a request, it will automatically select the best
     * client connector for your request, based on the request.protocol property
     * or on the resource URI's scheme. This call is blocking and will return an
     * updated response object.
     * 
     * @return A request dispatcher to virtual hosts of the local component.
     * @deprecated Use getClientDispatcher() instead.
     */
    public Uniform getDispatcher() {
        return getClientDispatcher();
    }

    /**
     * Returns a request dispatcher to local virtual hosts. When you ask the
     * dispatcher to handle a request, it will directly route your request to
     * the component's client-side router.
     * 
     * @return A request dispatcher to available client connectors.
     */
    public Uniform getClientDispatcher() {
        return null;
    }

    /**
     * Returns a request dispatcher to available client connectors. When you ask
     * the dispatcher to handle a request, it will automatically select the best
     * client connector for your request, based on the request.protocol property
     * or on the resource URI's scheme. This call is blocking and will return an
     * updated response object.
     * 
     * @return A request dispatcher to virtual hosts of the local component.
     */
    public Uniform getServerDispatcher() {
        return null;
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
     * Returns the modifiable series of parameters. Creates a new instance if no
     * one has been set. A parameter is a pair composed of a name and a value
     * and is typically used for configuration purpose, like Java properties.
     * Note that multiple parameters with the same name can be declared and
     * accessed.
     * 
     * @return The modifiable series of parameters.
     */
    public Series<Parameter> getParameters() {
        if (this.parameters == null)
            this.parameters = new Form();
        return this.parameters;
    }

    /**
     * Sets the modifiable map of attributes.
     * 
     * @param attributes
     *                The modifiable map of attributes.
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * Sets the logger.
     * 
     * @param logger
     *                The logger.
     */
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Sets the modifiable series of parameters.
     * 
     * @param parameters
     *                The modifiable series of parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

}