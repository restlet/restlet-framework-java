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

/**
 * Configures a proxy for your own application and provides some services hosted
 * by the APISpark platform such as analytics, security.
 * 
 * @author Cyprien Quilici
 * @author Manuel Boillod
 */
public class AgentService extends Service {
    /** The URL of the remote service used by default. */
    public static final String DEFAULT_AGENT_SERVICE_URL = "https://apispark.restlet.com";

    /** The password used to connect to the APISpark platform. */
    private char[] agentSecret;

    /** The url of the APISpark service. */
    private String agentServiceUrl = DEFAULT_AGENT_SERVICE_URL;

    /** The login used to connect to the APISpark platform. */
    private String agentUsername;

    /**
     * The identifier of the cell configured on the APISpark platform for your
     * application.
     */
    private Integer cell;

    /**
     * The identifier of the cell version configured on the APISpark platform
     * for your application.
     */
    private Integer cellVersion;

    /**
     * Constructor using the default APISpark service url.
     * 
     * @param agentUsername
     *            The login used to connect to the APISpark platform.
     * @param agentSecret
     *            The password used to connect to the APISpark platform.
     * @param cell
     *            The identifier of the cell configured on the APISpark platform
     *            for your application.
     * @param cellVersion
     *            The identifier of the cell version configured on the APISpark
     *            platform for your application.
     */
    public AgentService(String agentUsername, char[] agentSecret, Integer cell,
            Integer cellVersion) {
        this(DEFAULT_AGENT_SERVICE_URL, agentUsername, agentSecret, cell,
                cellVersion);
    }

    /**
     * Constructor.
     * 
     * @param agentServiceUrl
     *            The url of the APISpark service.
     * @param agentUsername
     *            The login used to connect to the APISpark platform.
     * @param agentSecret
     *            The password used to connect to the APISpark platform.
     * @param cell
     *            The identifier of the cell configured on the APISpark platform
     *            for your application.
     * @param cellVersion
     *            The identifier of the cell version configured on the APISpark
     *            platform for your application.
     */
    public AgentService(String agentServiceUrl, String agentUsername,
            char[] agentSecret, Integer cell, Integer cellVersion) {
        super(true);
        this.agentSecret = agentSecret;
        this.agentServiceUrl = agentServiceUrl;
        this.agentUsername = agentUsername;
        this.cell = cell;
        this.cellVersion = cellVersion;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setCell(cell);
        agentConfig.setCellVersion(cellVersion);
        agentConfig.setAgentServiceUrl(agentServiceUrl);
        agentConfig.setAgentUsername(agentUsername);
        agentConfig.setAgentSecret(agentSecret);

        return new AgentFilter(agentConfig, context);
    }

    /**
     * Returns the password used to connect to the APISpark platform.
     * 
     * @return The password used to connect to the APISpark platform.
     */
    public String getAgentSecret() {
        return new String(agentSecret);
    }

    /**
     * Returns the url of the APISpark service.
     * 
     * @return The url of the APISpark service.
     */
    public String getAgentServiceUrl() {
        return agentServiceUrl;
    }

    /**
     * Returns the login used to connect to the APISpark platform.
     * 
     * @return The login used to connect to the APISpark platform.
     */
    public String getAgentUsername() {
        return agentUsername;
    }

    /**
     * Returns the identifier of the cell configured on the APISpark platform
     * for your application.
     * 
     * @return The identifier of the cell configured on the APISpark platform
     *         for your application.
     */
    public Integer getCell() {
        return cell;
    }

    /**
     * Returns the identifier of the cell version configured on the APISpark
     * platform for your application.
     * 
     * @return The identifier of the cell version configured on the APISpark
     *         platform for your application.
     */
    public Integer getCellVersion() {
        return cellVersion;
    }

    /**
     * Sets the password used to connect to the APISpark platform.
     * 
     * @param agentSecret
     *            The password used to connect to the APISpark platform.
     */
    public void setAgentSecret(String agentSecret) {
        this.agentSecret = agentSecret != null ? agentSecret.toCharArray()
                : null;
    }

    /**
     * Sets the url of the APISpark service.
     * 
     * @param agentServiceUrl
     *            The url of the APISpark service.
     */
    public void setAgentServiceUrl(String agentServiceUrl) {
        this.agentServiceUrl = agentServiceUrl;
    }

    /**
     * Sets the login used to connect to the APISpark platform.
     * 
     * @param agentUsername
     *            The login used to connect to the APISpark platform.
     */
    public void setAgentUsername(String agentUsername) {
        this.agentUsername = agentUsername;
    }

    /**
     * Sets the identifier of the cell configured on the APISpark platform for
     * your application.
     * 
     * @param cell
     *            The identifier of the cell configured on the APISpark platform
     *            for your application.
     */
    public void setCell(Integer cell) {
        this.cell = cell;
    }

    /**
     * Sets the identifier of the cell version configured on the APISpark
     * platform for your application.
     * 
     * @param cellVersion
     *            The identifier of the cell version configured on the APISpark
     *            platform for your application.
     */
    public void setCellVersion(Integer cellVersion) {
        this.cellVersion = cellVersion;
    }
}
