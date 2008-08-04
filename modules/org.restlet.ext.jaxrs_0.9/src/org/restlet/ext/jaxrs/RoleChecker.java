/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs;

import java.security.Principal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.Guard;

/**
 * <p>
 * This interface provides user role checks.
 * </p>
 * <p>
 * Because the Restlet API does not support its own mechanism for role checks
 * (as e.g. the Servlet API), you must use this inteface if you need role checks
 * in a JAX-RS application.<br>
 * This interface is used to check, if a user is in a role. Implementations must
 * be thread save.
 * </p>
 * <p>
 * This interface is used by {@link SecurityContext#isUserInRole(String)}. The
 * JAX-RS runtime delegates this method call along with the {@link Principal} of
 * the HTTP request to method {@link #isInRole(Principal, String)}, the only
 * method of this interface.
 * </p>
 * <p>
 * If you need user access control, you must give an instance of this inteface
 * to the {@link JaxRsApplication}. If you do not give an instance, every call
 * of {@link SecurityContext#isUserInRole(String)} results in an Internal Server
 * Error (HTTP status 500), which will get returned to the client (see
 * {@link #REJECT_WITH_ERROR}).
 * </p>
 * <p>
 * To check if the user is authenticated, use any Restlet {@link Guard}.
 * </p>
 * <p>
 * <i>The JAX-RS extension as well as the JAX-RS specification are currently
 * under development. You should use this extension only for experimental
 * purpose.</i> <br>
 * For further information see <a href="https://jsr311.dev.java.net/">Java
 * Service Request 311</a>.
 * </p>
 * 
 * @author Stephan Koops
 * @see SecurityContext
 */
public interface RoleChecker {

    /**
     * Access control constant that gives all roles to all principals.
     */
    public static final RoleChecker ALLOW_ALL = new RoleChecker() {
        public boolean isInRole(Principal principal, String role)
                throws WebApplicationException {
            return true;
        }
    };

    /**
     * Access control constant that doesn't give any role to any principal.
     */
    public static final RoleChecker FORBID_ALL = new RoleChecker() {
        public boolean isInRole(Principal principal, String role)
                throws WebApplicationException {
            return false;
        }
    };

    /**
     * An {@link RoleChecker} that throws an WebApplicationExeption with status
     * 500 (Internal Server Error) for every call on it.
     */
    public static final RoleChecker REJECT_WITH_ERROR = new RoleChecker() {
        public boolean isInRole(Principal principal, String role)
                throws WebApplicationException {
            final String message = "No access control defined.";
            final ResponseBuilder rb = Response.serverError();
            rb.entity(message).language("en").type(MediaType.TEXT_HTML_TYPE);
            throw new WebApplicationException(rb.build());
        }
    };

    /**
     * Checks, if the user is in the given role, or false if not.<br>
     * This method is used by the {@link SecurityContext}.
     * 
     * @param principal
     *            The principal to check.
     * @param role
     *            the role.
     * @return true, if the user is in the role, false otherwise.
     * @throws WebApplicationException
     *             The developer may handle exceptions by throw a
     *             {@link WebApplicationException}.
     * @see SecurityContext#isUserInRole(String)
     */
    public boolean isInRole(Principal principal, String role)
            throws WebApplicationException;
}