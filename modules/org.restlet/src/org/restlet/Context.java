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

package org.restlet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.engine.security.RoleMapping;
import org.restlet.security.Group;
import org.restlet.security.LocalVerifier;
import org.restlet.security.Organization;
import org.restlet.security.Role;
import org.restlet.security.RolePrincipal;
import org.restlet.security.User;
import org.restlet.security.UserPrincipal;
import org.restlet.util.Series;

/**
 * Contextual data and services provided to a Restlet. The context is the means
 * by which a Restlet may access the software environment within the framework.
 * It is typically provided by the immediate parent Restlet (Application is the
 * most common case).<br>
 * <br>
 * Concurrency note: attributes and parameters of a context are stored in
 * concurrent collections that guarantee thread safe access and modification. If
 * several threads concurrently access objects and modify these collections,
 * they should synchronize on the lock of the Context instance.
 * 
 * @author Jerome Louvel
 */
public class Context {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<Context>();

    /**
     * Returns the context associated to the current Restlet. The context can be
     * the one of a Component, an Application, a Filter or any other Restlet
     * subclass.
     * 
     * Warning: this method should only be used under duress. You should by
     * default prefer obtaining the current context using methods such as
     * {@link org.restlet.Restlet#getContext()} or
     * {@link org.restlet.resource.Resource#getContext()}.
     * 
     * This variable is stored internally as a thread local variable and updated
     * each time a request is handled by a Restlet via the
     * {@link Restlet#handle(org.restlet.data.Request, org.restlet.data.Response)}
     * method.
     * 
     * @return The current context.
     */
    public static Context getCurrent() {
        return CURRENT.get();
    }

    /**
     * Returns the current context's logger.
     * 
     * @return The current context's logger.
     */
    public static Logger getCurrentLogger() {
        return (Context.getCurrent() != null) ? Context.getCurrent()
                .getLogger() : Logger.getLogger(Context.class
                .getCanonicalName());
    }

    /**
     * Sets the context to associated with the current thread.
     * 
     * @param context
     *            The thread's context.
     */
    public static void setCurrent(Context context) {
        CURRENT.set(context);
    }

    /** The modifiable attributes map. */
    private final ConcurrentMap<String, Object> attributes;

    /** The logger instance to use. */
    private volatile Logger logger;

    /** The modifiable map of bounded organizations. */
    private Map<String, Organization> organizations;

    /** The modifiable series of parameters. */
    private final Series<Parameter> parameters;

    /** The modifiable list of role mappings. */
    private List<RoleMapping> roleMappings;

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
        this.parameters = new Form(new CopyOnWriteArrayList<Parameter>());
        this.organizations = new ConcurrentHashMap<String, Organization>();
        this.roleMappings = new CopyOnWriteArrayList<RoleMapping>();
    }

    /**
     * Constructor.
     * 
     * @param loggerName
     *            The name of the logger to use.
     */
    public Context(String loggerName) {
        this(Logger.getLogger(loggerName));
    }

    /**
     * Bind an organization and all its users to this context to support the
     * authentication of users. Note that if you bind several organizations, the
     * users must authenticate themselves using a fully qualified identifier
     * (ex: mylogin@mycompany.com).
     * 
     * @param organization
     *            The organization to bind.
     */
    public void bind(Organization organization) {
        getOrganizations().put(organization.getDomainName(), organization);
    }

    /**
     * Creates a protected child context. This is especially useful for new
     * application attached to their parent component, to ensure their isolation
     * from the other applications. By default it just creates a new context
     * instance.
     * 
     * @return The child context.
     */
    public Context createChildContext() {
        return new Context();
    }

    /**
     * Finds the roles mapped to a specific user/groups/organization triple.
     * 
     * @param userOrganization
     *            The user organization.
     * @param userGroups
     *            The user groups.
     * @param user
     *            The user.
     * @return
     */
    private Set<Role> findRoles(Organization userOrganization,
            Set<Group> userGroups, User user) {
        Set<Role> result = new HashSet<Role>();

        Object source;
        for (RoleMapping mapping : getRoleMappings()) {
            source = mapping.getSource();

            if (userOrganization.equals(source) || user.equals(source)
                    || userGroups.contains(source)) {
                result.add(mapping.getTarget());
            }
        }

        return result;
    }

    /**
     * Returns a modifiable attributes map that can be used by developers to
     * save information relative to the context. This is a convenient mean to
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
    public Uniform getClientDispatcher() {
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
     * Returns the modifiable map of bounded organizations.
     * 
     * @return The modifiable map of bounded organizations.
     */
    private Map<String, Organization> getOrganizations() {
        return organizations;
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
     * Returns the modifiable list of role mappings.
     * 
     * @return The modifiable list of role mappings.
     */
    private List<RoleMapping> getRoleMappings() {
        return roleMappings;
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
     * Returns a local verifier that can check the validity of user/secret
     * couples based on Restlet default authorization model.
     * 
     * @return A local verifier.
     */
    public LocalVerifier getVerifier() {
        return new LocalVerifier() {

            @Override
            protected char[] getSecret(String identifier) {
                char[] result = null;

                // Parse qualified identifiers
                int at = identifier.indexOf('@');
                String domainName = (at == -1) ? null : identifier
                        .substring(at + 1);
                String userIdentifier = (at == -1) ? identifier : identifier
                        .substring(0, at);

                // Lookup the organization
                Organization orga = null;

                if (domainName == null) {
                    if (getOrganizations().size() == 1) {
                        orga = getOrganizations().entrySet().iterator().next()
                                .getValue();
                    } else {
                        getLogger()
                                .info(
                                        "Unable to identify an unqualified user. Multiple organizations were bounded.");
                    }
                } else {
                    orga = getOrganizations().get(domainName);
                }

                // Lookup the user
                if (orga != null) {
                    User user = orga.findUser(userIdentifier);

                    if (user != null) {
                        result = user.getSecret();
                    }
                }

                return result;
            }

            @Override
            protected void updateSubject(Subject subject, String identifier,
                    char[] inputSecret) {
                // Parse qualified identifiers
                int at = identifier.indexOf('@');
                String domainName = (at == -1) ? null : identifier
                        .substring(at + 1);
                String userIdentifier = (at == -1) ? identifier : identifier
                        .substring(0, at);

                // Lookup the organization
                Organization orga = null;

                if (domainName == null) {
                    if (getOrganizations().size() == 1) {
                        orga = getOrganizations().entrySet().iterator().next()
                                .getValue();
                    } else {
                        getLogger()
                                .info(
                                        "Unable to identify an unqualified user. Multiple organizations were bounded.");
                    }
                } else {
                    orga = getOrganizations().get(domainName);
                }

                // Lookup the user
                if (orga != null) {
                    User user = orga.findUser(userIdentifier);

                    if (user != null) {
                        // Add a principal for this identifier
                        subject.getPrincipals().add(new UserPrincipal(user));

                        // Add a principal for the user roles
                        Set<Group> userGroups = orga.findGroups(user);
                        Set<Role> userRoles = findRoles(orga, userGroups, user);

                        for (Role role : userRoles) {
                            subject.getPrincipals()
                                    .add(new RolePrincipal(role));
                        }
                    }
                }
            }

        };
    }

    /**
     * Maps a group defined in a component to a role defined in the application.
     * 
     * @param group
     *            The source group.
     * @param role
     *            The target role.
     */
    public void map(Group group, Role role) {
        getRoleMappings().add(new RoleMapping(group, role));
    }

    /**
     * Maps an organization defined in a component to a role defined in the
     * application.
     * 
     * @param organization
     *            The source organization.
     * @param role
     *            The target role.
     */
    public void map(Organization organization, Role role) {
        getRoleMappings().add(new RoleMapping(organization, role));
    }

    /**
     * Maps a user defined in a component to a role defined in the application.
     * 
     * @param user
     *            The source user.
     * @param role
     *            The target role.
     */
    public void map(User user, Role role) {
        getRoleMappings().add(new RoleMapping(user, role));
    }

    /**
     * Sets the modifiable map of attributes.
     * 
     * @param attributes
     *            The modifiable map of attributes.
     */
    public synchronized void setAttributes(Map<String, Object> attributes) {
        this.attributes.clear();
        this.attributes.putAll(attributes);
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
        setLogger(Logger.getLogger(loggerName));
    }

    /**
     * Sets the modifiable series of parameters.
     * 
     * @param parameters
     *            The modifiable series of parameters.
     */
    public synchronized void setParameters(Series<Parameter> parameters) {
        this.parameters.clear();

        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
    }

    /**
     * Sets the modifiable list of role mappings.
     * 
     * @param roleMappings
     *            The modifiable list of role mappings.
     */
    public void setRoleMappings(List<RoleMapping> roleMappings) {
        this.roleMappings.clear();

        if (roleMappings != null) {
            this.roleMappings.addAll(roleMappings);
        }
    }

    /**
     * Unbind an organization and all its users from this context.
     * 
     * @param organization
     *            The organization to unbind.
     */
    public void unbind(Organization organization) {
        getOrganizations().remove(organization);
    }

    /**
     * Unmaps a group defined in a component from a role defined in the
     * application.
     * 
     * @param group
     *            The source group.
     * @param role
     *            The target role.
     */
    public void unmap(Group group, Role role) {
        unmap((Object) group, role);
    }

    /**
     * Unmaps an element (user, group or organization) defined in a component
     * from a role defined in the application.
     * 
     * @param group
     *            The source group.
     * @param role
     *            The target role.
     */
    private void unmap(Object source, Role role) {
        RoleMapping mapping;
        for (int i = getRoleMappings().size(); i >= 0; i--) {
            mapping = getRoleMappings().get(i);

            if (mapping.getSource().equals(source)
                    && mapping.getTarget().equals(role)) {
                getRoleMappings().remove(i);
            }
        }
    }

    /**
     * Unmaps an organization defined in a component from a role defined in the
     * application.
     * 
     * @param organization
     *            The source organization.
     * @param role
     *            The target role.
     */
    public void unmap(Organization organization, Role role) {
        unmap((Object) organization, role);
    }

    /**
     * Unmaps a user defined in a component from a role defined in the
     * application.
     * 
     * @param user
     *            The source user.
     * @param role
     *            The target role.
     */
    public void unmap(User user, Role role) {
        unmap((Object) user, role);
    }

}
