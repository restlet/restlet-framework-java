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

package org.restlet.ext.apispark.internal.agent.module;

import java.util.List;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.agent.AgentConfigurationException;
import org.restlet.ext.apispark.internal.agent.AgentUtils;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.bean.OperationAuthorization;
import org.restlet.ext.apispark.internal.agent.resource.AuthorizationOperationsResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.security.Role;

/**
 * Authorization module for the agent.
 * 
 * @author Manuel Boillod
 */
public class AuthorizationModule extends Filter {

    /**
     * Wrap an {@link OperationAuthorization} in a {@link Restlet} class for
     * reuse of {@link Router#getNext(org.restlet.Request, org.restlet.Response)} logic.
     */
    private static class RestletOperationAuthorization extends Restlet {

        private OperationAuthorization operationAuthorization;

        private RestletOperationAuthorization(
                OperationAuthorization operationAuthorization) {
            this.operationAuthorization = operationAuthorization;
        }

        public OperationAuthorization getOperationAuthorization() {
            return operationAuthorization;
        }

        @SuppressWarnings("unused")
        public void setOperationAuthorization(
                OperationAuthorization operationAuthorization) {
            this.operationAuthorization = operationAuthorization;
        }
    }

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(AuthorizationModule.class
            .getName());

    public static final String MODULE_PATH = "/authorization";

    public static final String OPERATIONS_AUTHORIZATIONS_PATH = MODULE_PATH
            + "/operations";

    /**
     * Router is used for finding the Operation corresponding to a incoming
     * request.
     */
    private Router router;

    /**
     * Create a new Authorization module with the specified settings.
     * 
     * @param apiSparkConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     */
    public AuthorizationModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings) {
        this(apiSparkConfig, modulesSettings, null);
    }

    /**
     * Create a new Authorization module with the specified settings.
     * 
     * @param apiSparkConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     * @param context
     *            The context
     */
    public AuthorizationModule(ApiSparkConfig apiSparkConfig,
            ModulesSettings modulesSettings, Context context) {
        super(context);

        AuthorizationOperationsResource authorizationOperationsClientResource = AgentUtils
                .getClientResource(apiSparkConfig, modulesSettings,
                        AuthorizationOperationsResource.class,
                        OPERATIONS_AUTHORIZATIONS_PATH);

        List<OperationAuthorization> operationAuthorizations;
        try {
            operationAuthorizations = authorizationOperationsClientResource
                    .getAuthorizations();
        } catch (Exception e) {
            throw new AgentConfigurationException(
                    "Could not get authorization module configuration from APISpark connector service",
                    e);
        }

        // Initialize the router
        router = new Router();
        for (OperationAuthorization operationAuthorization : operationAuthorizations) {
            router.attach(operationAuthorization.getPathTemplate(),
                    new RestletOperationAuthorization(operationAuthorization));
        }
    }

    /**
     * Find the best {@link OperationAuthorization} for the incoming request and
     * check user authorization.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return {@link org.restlet.routing.Filter#CONTINUE} if the user is
     *         authorized.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {

        // find the corresponding Operation
        TemplateRoute templateRoute = (TemplateRoute) router.getNext(request,
                response);

        // check route exists
        if (templateRoute == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return STOP;
        }
        RestletOperationAuthorization restletOperationAuthorization = (RestletOperationAuthorization) templateRoute
                .getNext();

        List<Role> userRoles = request.getClientInfo().getRoles();

        // check that user has at least one authorized role (named group in
        // apispark)
        boolean authorized = false;
        OperationAuthorization operationAuthorization = restletOperationAuthorization
                .getOperationAuthorization();
        List<String> groupsAllowed = operationAuthorization.getGroupsAllowed();
        if (groupsAllowed == null) {
            LOGGER.warning("No group is allowed for method "
                    + operationAuthorization.getMethod()
                    + " on this resource: "
                    + operationAuthorization.getPathTemplate());
        } else {
            for (String groupAllowed : groupsAllowed) {
                if (hasRole(userRoles, groupAllowed)
                        || "anyone".equals(groupAllowed)) {
                    authorized = true;
                    break;
                }
            }
        }

        if (authorized) {
            return CONTINUE;
        } else {
            response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return STOP;
        }
    }

    /**
     * Indicates if the given role is in the list of roles.
     * 
     * @param roles
     *            The list of roles.
     * @param roleName
     *            The name of the role to look for.
     * @return True if the list of roles contains the given role.
     */
    protected boolean hasRole(List<Role> roles, String roleName) {
        for (Role role : roles) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
}
