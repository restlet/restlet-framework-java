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

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.restlet.engine.authentication.JaasCallbackHandler;

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

    private volatile Configuration configuration;

    /** The JAAS login context name. */
    private volatile String name;

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
     * 
     * @param subject
     * @param identifier
     * @param secret
     * @return
     */
    protected CallbackHandler createCallbackHandler(Subject subject,
            String identifier, char[] secret) {
        return new JaasCallbackHandler(subject, identifier, secret);
    }

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
     * 
     */
    public boolean verify(Subject subject, String identifier, char[] secret) {
        boolean result = true;

        try {
            LoginContext loginContext = new LoginContext(getName(), subject,
                    createCallbackHandler(subject, identifier, secret),
                    getConfiguration());

            // TODO
            loginContext.login();
        } catch (LoginException le) {
            result = false;
        }

        return result;
    }

}
