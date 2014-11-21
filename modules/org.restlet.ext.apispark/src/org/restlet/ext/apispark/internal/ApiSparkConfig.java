package org.restlet.ext.apispark.internal;

import org.restlet.ext.apispark.ApiSparkService;

/**
 * TODO I'm not convinced by the presence of such configuration bean. The
 * service really hosts the configuration.
 * 
 * @author Manuel Boillod
 */
public class ApiSparkConfig {

    private char[] agentPassword;

    private String agentServiceUrl = ApiSparkService.DEFAULT_AGENT_SERVICE_URL;

    private String agentLogin;

    private Integer agentCellId;

    private Integer agentCellVersion;

    private boolean reverseProxyEnabled;

    private String reverseProxyTargetUrl;

    public String getAgentPassword() {
        return agentPassword != null ? new String(agentPassword) : null;
    }

    public String getAgentServiceUrl() {
        return agentServiceUrl;
    }

    public String getAgentLogin() {
        return agentLogin;
    }

    public Integer getAgentCellId() {
        return agentCellId;
    }

    public Integer getAgentCellVersion() {
        return agentCellVersion;
    }

    public String getReverseProxyTargetUrl() {
        return reverseProxyTargetUrl;
    }

    public boolean isReverserProxyEnabled() {
        return reverseProxyEnabled;
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

    public void setAgentCellId(Integer cell) {
        this.agentCellId = cell;
    }

    public void setAgentCellVersion(Integer cellVersion) {
        this.agentCellVersion = cellVersion;
    }

    public void setReverseProxyEnabled(boolean reverseProxyEnabled) {
        this.reverseProxyEnabled = reverseProxyEnabled;
    }

    public void setReverseProxyTargetUrl(String reverseProxyTargetUrl) {
        this.reverseProxyTargetUrl = reverseProxyTargetUrl;
    }

}
