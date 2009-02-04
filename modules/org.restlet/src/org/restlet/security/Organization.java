/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.security;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Organization that contains users and groups.
 * 
 * @author Jerome Louvel
 */
public class Organization {

    /** The description. */
    private volatile String description;

    /** The domain name (ex: "noelios.com"). */
    private volatile String domainName;

    /** The display name. */
    private volatile String name;

    /** The modifiable list of root groups. */
    private final List<Group> rootGroups;

    /** The modifiable list of users. */
    private final List<User> users;

    /**
     * Constructor.
     */
    public Organization() {
        this.rootGroups = new CopyOnWriteArrayList<Group>();
        this.users = new CopyOnWriteArrayList<User>();
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
     * Returns the domain name (ex: "noelios.com").
     * 
     * @return The domain name.
     */
    public String getDomainName() {
        return domainName;
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
     * Returns the modifiable list of root groups.
     * 
     * @return The modifiable list of root groups.
     */
    public List<Group> getRootGroups() {
        return rootGroups;
    }

    /**
     * Returns the modifiable list of users.
     * 
     * @return The modifiable list of users.
     */
    public List<User> getUsers() {
        return users;
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
     * Sets the domain name (ex: "noelios.com").
     * 
     * @param domainName
     *            The domain name.
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
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
     * Sets the list of root groups.
     * 
     * @param rootGroups
     *            The list of root groups.
     */
    public synchronized void setRootGroups(List<Group> rootGroups) {
        this.rootGroups.clear();

        if (rootGroups != null) {
            this.rootGroups.addAll(rootGroups);
        }
    }

    /**
     * Sets the list of users.
     * 
     * @param users
     *            The list of users.
     */
    public synchronized void setUsers(List<User> users) {
        this.users.clear();

        if (users != null) {
            this.users.addAll(users);
        }
    }

}
