package org.restlet.ext.apispark.internal.agent;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Header;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * @author Manuel Boillod
 */
public abstract class AgentUtils {

    /**
     * Returns a client resource configured to communicate with the apispark connector cell.
     *
     * @param agentConfig
     *          The agent configuration
     * @param modulesSettings
     *          The modules settings. Could be null.
     * @param resourceClass
     *          The resource class
     * @param resourcePath
     *          The resource path
     *
     * @return a client resource configured to communicate with the apispark connector cell.
     */
    public static <T> T getConfiguredClientResource(AgentConfig agentConfig,
                                                    ModulesSettings modulesSettings,
                                                             Class<T> resourceClass,
                                                             String resourcePath) {

        String path = agentConfig.getAgentServicePath() +
                "/agent" +
                "/cells/" + agentConfig.getCellId() +
                "/versions/" + agentConfig.getCellVersion() +
                resourcePath;

        ClientResource clientResource = new ClientResource(path);

        //add authentication scheme
        clientResource.setChallengeResponse(ChallengeScheme.HTTP_BASIC,
                agentConfig.getAgentUsername(),
                agentConfig.getAgentSecretKey());

        //send agent version to apispark in headers
        Series<Header> headers = clientResource.getRequest().getHeaders();
        headers.add(AgentConstants.REQUEST_HEADER_CONNECTOR_AGENT_VERSION, AgentConfig.AGENT_VERSION);

        //send connector cell revision to apispark in headers
        if (modulesSettings != null) {
            headers.add(AgentConstants.REQUEST_HEADER_CONNECTOR_CELL_REVISION, modulesSettings.getCellRevision());
        }

        return clientResource.wrap(resourceClass);
    }

}
