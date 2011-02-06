/**
 * Copyright 2005-2011 Noelios Technologies.
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
