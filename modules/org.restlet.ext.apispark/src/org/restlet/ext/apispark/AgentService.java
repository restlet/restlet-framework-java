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
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.agent.AgentConfig;
import org.restlet.ext.apispark.internal.agent.AgentFilter;
import org.restlet.routing.Filter;
import org.restlet.service.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configures a proxy for your own application and provides some services hosted
 * by the APISpark platform such as analytics, security.
 *
 * The service could be configured by a property file with the {@link #loadConfiguration()}
 * method.
 * 
 * @author Cyprien Quilici
 * @author Manuel Boillod
 */
public class AgentService extends Service {
    /** The URL of the remote service used by default. */
    public static final String DEFAULT_AGENT_SERVICE_URL = "https://apispark.restlet.com";

    /** The system property key for agent configuration file. */
    public static final String CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY = "agentConfiguration";

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
     * Indicates if the request redirection is enabled.
     * If true, the {@link #redirectionUrl} should be set.
     */
    private boolean redirectionEnabled;

    /**
     * The redirection URL. Used if {@link #redirectionEnabled}
     * is true.
     */
    private String redirectionUrl;

    /**
     * Default constructor.
     */
    public AgentService() {
        super(true);
    }

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
     * @param redirectionEnabled
     *            Indicates if the request redirection is enabled.
     * @param redirectionUrl
     *            The redirection URL.
     */
    public AgentService(String agentUsername, char[] agentSecret, Integer cell,
            Integer cellVersion, boolean redirectionEnabled, String redirectionUrl) {
        this(DEFAULT_AGENT_SERVICE_URL, agentUsername, agentSecret, cell,
                cellVersion, redirectionEnabled, redirectionUrl);
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
     * @param redirectionEnabled
     *            Indicates if the request redirection is enabled.
     * @param redirectionUrl
     *            The redirection URL.
     */
    public AgentService(String agentServiceUrl, String agentUsername,
            char[] agentSecret, Integer cell, Integer cellVersion,
            boolean redirectionEnabled, String redirectionUrl) {
        super(true);
        this.agentSecret = agentSecret;
        this.agentServiceUrl = agentServiceUrl;
        this.agentUsername = agentUsername;
        this.cell = cell;
        this.cellVersion = cellVersion;
        this.redirectionEnabled = redirectionEnabled;
        this.redirectionUrl = redirectionUrl;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setCell(cell);
        agentConfig.setCellVersion(cellVersion);
        agentConfig.setAgentServiceUrl(agentServiceUrl);
        agentConfig.setAgentUsername(agentUsername);
        agentConfig.setAgentSecret(agentSecret);
        agentConfig.setRedirectionEnabled(redirectionEnabled);
        agentConfig.setRedirectionUrl(redirectionUrl);
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
     * Returns the redirection URL. Used if {@link #isRedirectionEnabled()}
     * returns true.
     *
     * @return The redirection URL.
     */
    public String getRedirectionUrl() {
        return redirectionUrl;
    }

    /**
     * Load the agent configuration from the file set by the
     * system property 'agentConfiguration'.
     *
     * @see #CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY
     */
    public void loadConfiguration() {
        String configurationFile = System.getProperty(CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY);
        if (configurationFile == null) {
            throw new IllegalArgumentException("Agent configuration file is not set. " +
                    "Use system property '" + CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY + "' to define it.");
        }

        loadConfiguration(new File(configurationFile));
    }

    /**
     * Load the agent configuration from the file.
     *
     * @param configurationFile
     *          The configuration file.
     */
    public void loadConfiguration(File configurationFile) {
        if (configurationFile == null) {
            throw new IllegalArgumentException("Agent configuration file is null.");
        }
        if (!configurationFile.exists()) {
            throw new IllegalArgumentException("Agent configuration file does not exist: " + configurationFile.getAbsolutePath());
        }
        try {
            loadConfiguration(new FileInputStream(configurationFile));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Agent configuration file error. See exception for details.", e);
        }
    }

    /**
     * Load the agent configuration from the input stream.
     *
     * @param inputStream
     *          The input stream of the configuration file.
     */
    public void loadConfiguration(InputStream inputStream) {
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Agent configuration file error. See exception for details.", e);
        }
        this.agentSecret = getRequiredProperty(properties, "agent.secret").toCharArray();
        this.agentServiceUrl = properties.getProperty("agent.serviceUrl", DEFAULT_AGENT_SERVICE_URL);
        this.agentUsername = properties.getProperty("agent.usernane");
        this.cell = getRequiredIntegerProperty(properties, "agent.cell.id");
        this.cellVersion = getRequiredIntegerProperty(properties, "agent.cell.version");
        this.redirectionEnabled = Boolean.valueOf(getRequiredProperty(properties, "agent.redirection.enabled"));
        if (this.redirectionEnabled) {
            this.redirectionUrl = getRequiredProperty(properties, "agent.redirection.redirectionUrl");
        }
    }

    private String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (StringUtils.isNullOrEmpty(value)) {
            throw new IllegalArgumentException("Agent configuration file error. The property '" + key + "' is required");
        }
        return value;
    }

    private Integer getRequiredIntegerProperty(Properties properties, String key) {
        String value = getRequiredProperty(properties, key);
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Agent configuration file error. The property '" + key + "' should be a number", e);
        }
    }

    /**
     * Indicates if the request redirection is enabled.
     * If true, the redirection URL should be set with
     * {@link #setRedirectionUrl(String)}.
     *
     * @return True if the request redirection is enabled.
     */
    public boolean isRedirectionEnabled() {
        return redirectionEnabled;
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

    /**
     * Indicates if the request redirection is enabled.
     * If true, the redirection URL should be set with
     * {@link #setRedirectionUrl(String)}.
     *
     * @param redirectionEnabled
     *          True if the redirection is enabled.
     */
    public void setRedirectionEnabled(boolean redirectionEnabled) {
        this.redirectionEnabled = redirectionEnabled;
    }

    /**
     * Set the redirection URL. Used if {@link #isRedirectionEnabled()}
     * returns true.
     *
     * @param redirectionUrl
     *          The redirection URL.
     */
    public void setRedirectionUrl(String redirectionUrl) {
        this.redirectionUrl = redirectionUrl;
    }
}
