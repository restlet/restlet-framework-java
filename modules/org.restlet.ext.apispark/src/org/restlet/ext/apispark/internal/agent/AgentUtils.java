package org.restlet.ext.apispark.internal.agent;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Tool class for the agent service.
 * 
 * @author Manuel Boillod
 */
public abstract class AgentUtils {

    /**
     * Returns a client resource configured to communicate with the apispark
     * connector cell.
     * 
     * @param agentConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The optional modules settings.
     * @param resourceClass
     *            The resource class.
     * @param resourcePath
     *            The resource path.
     * 
     * @return A client resource configured to communicate with the apispark
     *         connector cell.
     */
    public static <T> T getClientResource(AgentConfig agentConfig,
            ModulesSettings modulesSettings, Class<T> resourceClass,
            String resourcePath) {

        StringBuilder sb = new StringBuilder(agentConfig.getAgentServiceUrl());
        if (!agentConfig.getAgentServiceUrl().endsWith("/")) {
            sb.append("/");
        }
        sb.append("agent");
        sb.append("/cells/");
        sb.append(agentConfig.getCell());
        sb.append("/versions/");
        sb.append(agentConfig.getCellVersion());
        if (resourcePath != null) {
            if (!resourcePath.startsWith("/")) {
                sb.append("/");
            }
            sb.append(resourcePath);
        }

        ClientResource clientResource = new ClientResource(sb.toString());
        clientResource.accept(MediaType.APPLICATION_JSON);
        
        // add authentication scheme
        clientResource.setChallengeResponse(ChallengeScheme.HTTP_BASIC,
                agentConfig.getAgentLogin(), agentConfig.getAgentPassword());

        // send agent version to apispark in headers
        Series<Header> headers = clientResource.getRequest().getHeaders();
        headers.add(AgentConstants.REQUEST_HEADER_CONNECTOR_AGENT_VERSION,
                AgentConfig.AGENT_VERSION);

        // send connector cell revision to apispark in headers
        if (modulesSettings != null) {
            headers.add(AgentConstants.REQUEST_HEADER_CONNECTOR_CELL_REVISION,
                    modulesSettings.getCellRevision());
        }

        return clientResource.wrap(resourceClass);
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private AgentUtils() {
    }

}
