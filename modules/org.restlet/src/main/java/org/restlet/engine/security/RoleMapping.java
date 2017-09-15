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

package org.restlet.engine.security;

import org.restlet.security.Group;
import org.restlet.security.Role;
import org.restlet.security.User;

/**
 * Mapping from an organization or a user or a group to a role.
 * 
 * @author Jerome Louvel
 */
public class RoleMapping {

    /**
     * The source of the mapping. It must be an instance of one of these
     * classes: {@link User} or {@link Group}.
     */
    private volatile Object source;

    /** The target role of the mapping. */
    private volatile Role target;

    /**
     * Default constructor.
     */
    public RoleMapping() {
        this(null, null);
    }

    /**
     * Constructor.
     * 
     * @param source
     * @param target
     */
    public RoleMapping(Object source, Role target) {
        super();
        this.source = source;
        this.target = target;
    }

    public Object getSource() {
        return source;
    }

    public Role getTarget() {
        return target;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public void setTarget(Role target) {
        this.target = target;
    }

}
