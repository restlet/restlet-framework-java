/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark;

import org.restlet.Context;
import org.restlet.ext.apispark.internal.agent.AgentConfig;
import org.restlet.ext.apispark.internal.agent.AgentFilter;
import org.restlet.routing.Filter;
import org.restlet.service.Service;

public class AgentService extends Service {

    public static final String DEFAULT_AGENT_SERVICE_PATH = "https://apispark.restlet.com";

    private Integer cellId;
    private Integer cellVersion;
    private String agentServicePath = DEFAULT_AGENT_SERVICE_PATH;
    private String agentUsername;
    private char[] agentSecretKey;

    public AgentService() {
    }

    @Override
    public Filter createInboundFilter(Context context) {
        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setCellId(cellId);
        agentConfig.setCellVersion(cellVersion);
        agentConfig.setAgentServicePath(agentServicePath);
        agentConfig.setAgentUsername(agentUsername);
        agentConfig.setAgentSecretKey(agentSecretKey);
        return new AgentFilter(agentConfig, context);
    }

    public Integer getCellId() {
        return cellId;
    }

    public void setCellId(Integer cellId) {
        this.cellId = cellId;
    }

    public Integer getCellVersion() {
        return cellVersion;
    }

    public void setCellVersion(Integer cellVersion) {
        this.cellVersion = cellVersion;
    }

    public String getAgentServicePath() {
        return agentServicePath;
    }

    public void setAgentServicePath(String agentServicePath) {
        this.agentServicePath = agentServicePath;
    }

    public String getAgentUsername() {
        return agentUsername;
    }

    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    public String getAgentSecretKey() {
        return new String(agentSecretKey);
    }

    public void setAgentSecretKey(String agentSecretKey) {
        this.agentSecretKey = agentSecretKey != null ? agentSecretKey.toCharArray() : null;
    }
}
