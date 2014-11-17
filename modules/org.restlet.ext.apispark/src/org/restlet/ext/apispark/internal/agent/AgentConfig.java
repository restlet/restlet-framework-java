package org.restlet.ext.apispark.internal.agent;

import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.AgentService;

/**
 * TODO I'm not convinced by the presence of such configuration bean. The
 * service really hosts the configuration.
 * 
 * @author Manuel Boillod
 */
public class AgentConfig {

    public static final String AGENT_VERSION = "1.0.0";

    private char[] agentPassword;

    private String agentServiceUrl = AgentService.DEFAULT_AGENT_SERVICE_URL;

    private String agentLogin;

    private Integer cell;

    private Integer cellVersion;

    private boolean redirectionEnabled;

    private String redirectionUrl;

    public String getAgentPassword() {
        return agentPassword != null ? new String(agentPassword) : null;
    }

    public String getAgentServiceUrl() {
        return agentServiceUrl;
    }

    public String getAgentLogin() {
        return agentLogin;
    }

    public Integer getCell() {
        return cell;
    }

    public Integer getCellVersion() {
        return cellVersion;
    }

    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    public boolean isRedirectionEnabled() {
        return redirectionEnabled;
    }

    public void setAgentPassword(char[] agentPassword) {
        this.agentPassword = agentPassword;
    }

    public void setAgentSecret(String agentSecret) {
        this.agentPassword = agentSecret != null ? agentSecret.toCharArray()
                : null;
    }

    public void setAgentServiceUrl(String agentServiceUrl) {
        this.agentServiceUrl = agentServiceUrl;
    }

    public void setAgentLogin(String agentLogin) {
        this.agentLogin = agentLogin;
    }

    public void setCell(Integer cell) {
        this.cell = cell;
    }

    public void setCellVersion(Integer cellVersion) {
        this.cellVersion = cellVersion;
    }

    public void setRedirectionEnabled(boolean redirectionEnabled) {
        this.redirectionEnabled = redirectionEnabled;
    }

    public void setRedirectionUrl(String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
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
        if (StringUtils.isNullOrEmpty(agentServiceUrl)) {
            throw new IllegalArgumentException(
                    "The agent service url is mandatory");
        }
        if (StringUtils.isNullOrEmpty(agentLogin)) {
            throw new IllegalArgumentException(
                    "The agent login is mandatory");
        }
        if (agentPassword == null || agentPassword.length == 0) {
            throw new IllegalArgumentException(
                    "The agent password key is mandatory");
        }

        if (redirectionEnabled && StringUtils.isNullOrEmpty(redirectionUrl)) {
            throw new IllegalArgumentException(
                    "The redirection url is mandatory when redirection is enabled");
        }
    }

}
