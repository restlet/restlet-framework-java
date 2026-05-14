/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.restlet.Application;
import org.restlet.engine.util.SystemUtils;

/**
 * Application-specific role. Common examples are "administrator", "user", "anonymous",
 * "supervisor". Note that for reusability purposes, it is recommended that those roles don't
 * reflect an actual organization, but more the functional requirements of your application.
 *
 * <p>Two roles are considered equals if they belong to the same parent application and have the
 * same name and child roles. The description isn't used for equality assessment.
 *
 * <p>Since version 2.2, they don't need to be the same Java objects anymore. To prevent the
 * multiplication of equivalent {@link Role} instances, you should try to call {@link
 * Application#getRole(String)} method.
 *
 * @author Jerome Louvel
 * @author Tim Peierls
 */
public class Role implements Principal {

    /**
     * Finds an existing role or creates a new one if needed. Note that a null description will be
     * set if the role has to be created.
     *
     * @param application The parent application.
     * @param name The role name to find or create.
     * @return The role found or created.
     */
    public static Role get(Application application, String name) {
        return get(application, name, null);
    }

    /**
     * Finds an existing role or creates a new one if needed.
     *
     * @param application The parent application.
     * @param name The role name to find or create.
     * @param description The role description if one needs to be created.
     * @return The role found or created.
     */
    public static Role get(Application application, String name, String description) {
        Role role = (application == null) ? null : application.getRole(name);
        return (role == null) ? new Role(application, name, description) : role;
    }

    /** The parent application. */
    private volatile Application application;

    /** The modifiable list of child roles. */
    private final List<Role> childRoles;

    /** The description. */
    private volatile String description;

    /** The name. */
    private volatile String name;

    /**
     * Default constructor. Note that the parent application is retrieved using the {@link
     * Application#getCurrent()} method if available or is null.
     */
    public Role() {
        this(Application.getCurrent(), null, null);
    }

    /**
     * Constructor.
     *
     * @param application The parent application or null.
     * @param name The name.
     */
    public Role(Application application, String name) {
        this(application, name, null);
    }

    /**
     * Constructor.
     *
     * @param application The parent application or null.
     * @param name The name.
     * @param description The description.
     */
    public Role(Application application, String name, String description) {
        this.application = application;
        this.name = name;
        this.description = description;
        this.childRoles = new CopyOnWriteArrayList<>();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Role that)) {
            return false;
        }

        return Objects.equals(getApplication(), that.getApplication())
                && Objects.equals(getName(), that.getName())
                && Objects.equals(getChildRoles(), that.getChildRoles());
    }

    /**
     * Returns the parent application.
     *
     * @return The parent application.
     */
    public Application getApplication() {
        return application;
    }

    /**
     * Returns the modifiable list of child roles.
     *
     * @return The modifiable list of child roles.
     */
    public List<Role> getChildRoles() {
        return childRoles;
    }

    /**
     * Returns the description.
     *
     * @return The description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getApplication(), getName(), getChildRoles());
    }

    /**
     * Sets the parent application.
     *
     * @param application The parent application.
     */
    public void setApplication(Application application) {
        this.application = application;
    }

    /**
     * Sets the modifiable list of child roles. This method clears the current list and adds all
     * entries in the parameter list.
     *
     * @param childRoles A list of child roles.
     */
    public void setChildRoles(List<Role> childRoles) {
        synchronized (getChildRoles()) {
            if (childRoles != getChildRoles()) {
                getChildRoles().clear();

                if (childRoles != null) {
                    getChildRoles().addAll(childRoles);
                }
            }
        }
    }

    /**
     * Sets the description.
     *
     * @param description The description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the name.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
