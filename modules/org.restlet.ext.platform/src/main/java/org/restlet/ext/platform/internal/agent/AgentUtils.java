/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.com/open-source/
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.platform.internal.agent;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.ext.platform.internal.RestletCloudConfig;
import org.restlet.ext.platform.internal.agent.bean.ModulesSettings;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Tool class for the agent service.
 * 
 * @author Manuel Boillod
 */
public abstract class AgentUtils {

    /**
     * Returns a client resource configured to communicate with the Restlet Cloud
     * connector cell.
     * 
     * @param restletCloudConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The optional modules settings.
     * @param resourceClass
     *            The resource class.
     * @param resourcePath
     *            The resource path.
     * 
     * @return A client resource configured to communicate with the platform
     *         connector cell.
     */
    public static <T> T getClientResource(RestletCloudConfig restletCloudConfig,
            ModulesSettings modulesSettings, Class<T> resourceClass,
            String resourcePath) {

        ClientResource clientResource = new ClientResource(buildResourcePath(
                restletCloudConfig, resourcePath));
        clientResource.accept(MediaType.APPLICATION_JSON);

        // add authentication scheme
        clientResource.setChallengeResponse(ChallengeScheme.HTTP_BASIC,
                restletCloudConfig.getAgentLogin(),
                restletCloudConfig.getAgentPassword());

        // send agent version to platform in headers
        Series<Header> headers = clientResource.getRequest().getHeaders();
        headers.add(AgentConstants.REQUEST_HEADER_CONNECTOR_AGENT_VERSION,
                AgentConstants.AGENT_VERSION);

        // send connector cell revision to platform in headers
        if (modulesSettings != null) {
            headers.add(AgentConstants.REQUEST_HEADER_CONNECTOR_CELL_REVISION,
                    modulesSettings.getCellRevision());
        }

        return clientResource.wrap(resourceClass, AgentUtils.class.getClassLoader());
    }

    /**
     * Builds the path of the client resource to communicate with the Restlet Cloud
     * connector cell.
     * 
     * @param restletCloudConfig
     *            The agent configuration.
     * @param resourcePath
     *            The resource path.
     * @return The path of the client resource to communicate with the Restlet Cloud
     *         connector cell.
     */
    private static String buildResourcePath(RestletCloudConfig restletCloudConfig,
            String resourcePath) {
        StringBuilder sb = new StringBuilder(
                restletCloudConfig.getAgentServiceUrl());
        if (!restletCloudConfig.getAgentServiceUrl().endsWith("/")) {
            sb.append("/");
        }
        sb.append("agent");
        sb.append("/cells/");
        sb.append(restletCloudConfig.getAgentCellId());
        sb.append("/versions/");
        sb.append(restletCloudConfig.getAgentCellVersion());
        if (resourcePath != null) {
            if (!resourcePath.startsWith("/")) {
                sb.append("/");
            }
            sb.append(resourcePath);
        }

        return sb.toString();
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private AgentUtils() {
    }

}
