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

package org.restlet.ext.jaas;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.security.User;
import org.restlet.security.Verifier;

/**
 * Verifier that leverages the JAAS pluggable authentication mechanism.
 * 
 * @author Jerome Louvel
 * @see <a
 *      href="http://java.sun.com/j2se/1.5.0/docs/guide/security/jaas/tutorials/index.html">JAAS
 *      Tutorials</a>
 * @see <a
 *      href="http://java.sun.com/j2se/1.5.0/docs/guide/security/jaas/JAASRefGuide.html">JAAS
 *      Reference Guide</a>
 */
public class JaasVerifier implements Verifier {

    /** The optional JAAS login configuration. */
    private volatile Configuration configuration;

    /** The JAAS login context name. */
    private volatile String name;

    /** Optional class from which to build a user principal */
    private volatile String userPrincipalClassName;

    /**
     * Constructor.
     * 
     * @param name
     *            The JAAS login context name.
     */
    public JaasVerifier(String name) {
        this.name = name;
    }

    /**
     * Creates a callback handler for the given parameters. By default it
     * returns one handler that handles name and password JAAS callbacks.
     * 
     * @return The callback handler created.
     */
    protected CallbackHandler createCallbackHandler(Request request,
            Response response) {
        return new ChallengeCallbackHandler(request, response);
    }

    /**
     * Returns the optional JAAS login configuration.
     * 
     * @return The optional JAAS login configuration.
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Returns the JAAS login context name.
     * 
     * @return The JAAS login context name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the user principal class name.
     * 
     * @return the user principal class name.
     */
    public String getUserPrincipalClassName() {
        return this.userPrincipalClassName;
    }

    /**
     * Sets the optional JAAS login configuration.
     * 
     * @param configuration
     *            The optional JAAS login configuration.
     */
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Sets the JAAS login context name.
     * 
     * @param contextName
     *            The JAAS login context name.
     */
    public void setName(String contextName) {
        this.name = contextName;
    }

    /**
     * Sets the user principal class name. If a {@link User} is not associated
     * with the {@link Request}'s {@link ClientInfo} and if one of the
     * principals returned after the JAAS login is of this type, a new
     * {@link User} will be associated with the {@link ClientInfo} using its
     * name.
     * 
     * @param userPrincipalClassName
     *            the user principal class name.
     */
    public void setUserPrincipalClassName(String userPrincipalClassName) {
        this.userPrincipalClassName = userPrincipalClassName;
    }

    /**
     * Verifies that the proposed secret is correct for the specified
     * identifier. By default, it creates a JAAS login context with the callback
     * handler obtained by {@link #createCallbackHandler(Request, Response)} and
     * calls the {@link LoginContext#login()} method on it.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return Result of the verification based on the RESULT_* constants.
     */
    public int verify(Request request, Response response) {
        int result = RESULT_VALID;

        try {
            Subject subject = new Subject();
            if (request.getClientInfo().getUser() != null) {
                subject.getPrincipals().add(request.getClientInfo().getUser());
            }
            if (request.getClientInfo().getRoles() != null) {
                subject.getPrincipals().addAll(
                        request.getClientInfo().getRoles());
            }
            subject.getPrincipals().addAll(
                    request.getClientInfo().getPrincipals());

            LoginContext loginContext = new LoginContext(getName(), subject,
                    createCallbackHandler(request, response),
                    getConfiguration());
            loginContext.login();

            /*
             * If the login operation added new principals to the subject, we
             * add them back to the ClientInfo.
             */
            for (Principal principal : subject.getPrincipals()) {
                if ((!principal.equals(request.getClientInfo().getUser()))
                        && (!request.getClientInfo().getRoles().contains(
                                principal))
                        && (!request.getClientInfo().getPrincipals().contains(
                                principal))) {
                    request.getClientInfo().getPrincipals().add(principal);
                }
                /*
                 * If no user has been set yet and if this principal if of the
                 * type we expect for a user, we create a new User based on the
                 * principal's name.
                 */
                if ((request.getClientInfo().getUser() == null)
                        && (this.userPrincipalClassName != null)
                        && (principal.getClass().getName()
                                .equals(this.userPrincipalClassName))) {
                    request.getClientInfo().setUser(
                            new User(principal.getName()));
                }
            }
        } catch (LoginException le) {
            result = RESULT_INVALID;
        }

        return result;
    }
}
