/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.sip;

import java.util.HashMap;
import java.util.Map;

/**
 * Describes level of SIP message importance. Used by the SIP "Priority" header.
 * 
 * @author Thierry Boileau
 */
public class Priority {

    /**
     * The known priorities registered with {@link #register(String)},
     * retrievable using {@link #valueOf(String)}.<br>
     * Keep the underscore for the ordering.
     */
    private static volatile Map<String, Priority> _priorities = null;

    /**
     * Highest level of priority.
     * 
     * @see <a http://tools.ietf.org/html/rfc3261#section-20.26">Priority
     *      header</a>
     */
    public static final Priority EMERGENCY = register("emergency");

    /**
     * Lowest level of priority.
     * 
     * @see <a http://tools.ietf.org/html/rfc3261#section-20.26">Priority
     *      header</a>
     */
    public static final Priority NON_URGENT = register("non-urgent");

    /**
     * Normal level of priority.
     * 
     * @see <a http://tools.ietf.org/html/rfc3261#section-20.26">Priority
     *      header</a>
     */
    public static final Priority NORMAL = register("normal");

    /**
     * Urgent level of priority.
     * 
     * @see <a http://tools.ietf.org/html/rfc3261#section-20.26">Priority
     *      header</a>
     */
    public static final Priority URGENT = register("urgent");

    /**
     * Returns the known priorities map.
     * 
     * @return the known priorities map.
     */
    private static Map<String, Priority> getPriorities() {
        if (_priorities == null) {
            _priorities = new HashMap<String, Priority>();
        }
        return _priorities;
    }

    /**
     * Register an option tag that can later be retrieved using
     * {@link #valueOf(String)}. If the option tag already exists, the existing
     * tag is returned, otherwise a new instance is created.
     * 
     * @param name
     *            The name.
     * @return The registered option tag.
     */
    public static synchronized Priority register(String name) {
        if (!getPriorities().containsKey(name)) {
            final Priority tag = new Priority(name);
            getPriorities().put(name, tag);
        }

        return getPriorities().get(name);
    }

    /**
     * Returns the priority associated to a value. If an existing constant
     * exists then it is returned, otherwise a new instance is created.
     * 
     * @param value
     *            The value.
     * @return The associated priority.
     */
    public static Priority valueOf(String value) {
        Priority result = null;

        if ((value != null) && !value.equals("")) {
            result = getPriorities().get(value);
            if (result == null) {
                result = new Priority(value);
            }
        }

        return result;
    }

    /** The priority value. */
    private String value;

    /**
     * Constructor.
     * 
     * @param value
     *            The priority value.
     */
    public Priority(String value) {
        super();
        this.value = value;
    }

    /**
     * Returns the priority value.
     * 
     * @return The priority value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the priority value.
     * 
     * @param value
     *            The priority value.
     */
    public void setValue(String value) {
        this.value = value;
    }

}
