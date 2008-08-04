/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.gwt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.restlet.gwt.data.Form;
import org.restlet.gwt.data.Parameter;
import org.restlet.gwt.util.Series;

/**
 * Contextual data and services provided to a Restlet. The context is the means
 * by which a Restlet may access the software environment within the framework.
 * It is typically provided by the immediate parent Restlet (Component and
 * Application are the most common cases).<br>
 * <br>
 * Note that attributes and parameters of a context are stored in concurrent
 * collections that guarantee thread safe access and modification. If several
 * threads concurrently access objects from these collections, they should be
 * either immutable, thread-safe themselves or guarded by a commonly agreed
 * lock.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Context {

    /** The modifiable attributes map. */
    private volatile Map<String, Object> attributes;

    /** The modifiable series of parameters. */
    private volatile Series<Parameter> parameters;

    /**
     * Constructor. Writes log messages to "org.restlet".
     */
    public Context() {
        this.attributes = new HashMap<String, Object>();
        this.parameters = new Form(new ArrayList<Parameter>());
    }

    /**
     * Returns a modifiable attributes map that can be used by developers to
     * save information relative to the context. Creates a new instance if no
     * one has been set. This is a convenient mean to provide common objects to
     * all the Restlets and Resources composing an Application.<br>
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
    public Map<String, Object> getAttributes() {
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
    public Uniform getClientDispatcher() {
        return null;
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
        return this.parameters;
    }

    /**
     * Returns a request dispatcher to component's virtual hosts. This is mostly
     * useful for application that want to optimize calls to other applications
     * hosted in the same component or to the application itself.<br>
     * <br>
     * The processing is the same as what would have been done if the request
     * came from one of the component's server connectors. It first must match
     * one of the registered virtual hosts. Then it can be routed to one of the
     * attaced Restlets, typically an Application.
     * 
     * @return A request dispatcher to the server connectors' router.
     */
    public Uniform getServerDispatcher() {
        return null;
    }

    /**
     * Sets the modifiable map of attributes.
     * 
     * @param attributes
     *            The modifiable map of attributes.
     */
    public void setAttributes(Map<String, Object> attributes) {
        if (attributes instanceof HashMap) {
            this.attributes = attributes;
        } else {
            this.attributes = new HashMap<String, Object>(attributes);
        }
    }

    /**
     * Sets the modifiable series of parameters.
     * 
     * @param parameters
     *            The modifiable series of parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

}
