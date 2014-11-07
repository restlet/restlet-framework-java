package org.restlet.ext.apispark.internal.agent;

/**
 * @author Manuel Boillod
 */
public class AgentConfig {

    public static final String AGENT_VERSION = "1.0";

    public static final String DEFAULT_AGENT_SERVICE_PATH = "https://apispark.restlet.com";

    private Integer cellId;
    private Integer cellVersion;
    private String agentServicePath = DEFAULT_AGENT_SERVICE_PATH;
    private String agentUsername;
    private char[] agentSecretKey;


    public Integer getCellId() {
        return cellId;
    }

    public AgentConfig setCellId(Integer cellId) {
        this.cellId = cellId;
        return this;
    }

    public Integer getCellVersion() {
        return cellVersion;
    }

    public AgentConfig setCellVersion(Integer cellVersion) {
        this.cellVersion = cellVersion;
        return this;
    }

    public String getAgentServicePath() {
        return agentServicePath;
    }

    public AgentConfig setAgentServicePath(String agentServicePath) {
        this.agentServicePath = agentServicePath;
        return this;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public AgentConfig setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
        return this;
    }

    public String getAgentSecretKey() {
        return agentSecretKey != null ? new String(agentSecretKey) : null;
    }

    public AgentConfig setAgentSecretKey(char[] agentSecretKey) {
        this.agentSecretKey = agentSecretKey;
        return this;
    }

    public AgentConfig setAgentSecretKey(String agentSecretKey) {
        this.agentSecretKey = agentSecretKey != null ? agentSecretKey.toCharArray() : null;
        return this;
    }

    public void validate() {
        if (cellId == null) {
            throw new IllegalArgumentException("Cell id is required");
        }
        if (cellVersion == null) {
            throw new IllegalArgumentException("Cell version is required");
        }
        if (agentServicePath == null) {
            throw new IllegalArgumentException("Agent service path is required");
        }
        if (agentUsername == null) {
            throw new IllegalArgumentException("Agent username is required");
        }
        if (agentSecretKey == null) {
            throw new IllegalArgumentException("Agent secret key is required");
        }
    }


}
