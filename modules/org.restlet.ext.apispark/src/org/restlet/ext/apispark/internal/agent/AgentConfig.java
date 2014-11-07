package org.restlet.ext.apispark.internal.agent;

import org.restlet.ext.apispark.AgentService;

/**
 * TODO I'm not convinced by the presence of such configuration bean. The
 * service really hosts the configuration.
 * 
 * @author Manuel Boillod
 */
public class AgentConfig {

    public static final String AGENT_VERSION = "1.0";

    private char[] agentSecret;

    private String agentServiceUrl = AgentService.DEFAULT_AGENT_SERVICE_URL;

    private String agentUsername;

    private Integer cell;

    private Integer cellVersion;

    public String getAgentSecret() {
        return agentSecret != null ? new String(agentSecret) : null;
    }

    public String getAgentServiceUrl() {
        return agentServiceUrl;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public Integer getCell() {
        return cell;
    }

    public Integer getCellVersion() {
        return cellVersion;
    }

    public AgentConfig setAgentSecret(char[] agentSecret) {
        this.agentSecret = agentSecret;
        return this;
    }

    public AgentConfig setAgentSecret(String agentSecret) {
        this.agentSecret = agentSecret != null ? agentSecret.toCharArray()
                : null;
        return this;
    }

    public AgentConfig setAgentServiceUrl(String agentServiceUrl) {
        this.agentServiceUrl = agentServiceUrl;
        return this;
    }

    public AgentConfig setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
        return this;
    }

    public AgentConfig setCell(Integer cell) {
        this.cell = cell;
        return this;
    }

    public AgentConfig setCellVersion(Integer cellVersion) {
        this.cellVersion = cellVersion;
        return this;
    }

    public void validate() {
        if (cell == null) {
            throw new IllegalArgumentException(
                    "The cell identifier is mandatory");
        }
        if (cellVersion == null) {
            throw new IllegalArgumentException(
                    "The cell version identifier is mandatory");
        }
        if (agentServiceUrl == null) {
            throw new IllegalArgumentException(
                    "The agent service url is mandatory");
        }
        if (agentUsername == null) {
            throw new IllegalArgumentException(
                    "The agent username is mandatory");
        }
        if (agentSecret == null) {
            throw new IllegalArgumentException(
                    "The agent secret key is mandatory");
        }
    }

}
