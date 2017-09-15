/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark;

import org.restlet.Context;
import org.restlet.engine.application.Encoder;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.ApiSparkFilter;
import org.restlet.ext.apispark.internal.agent.AgentConfigurationTimerTask;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallRule;
import org.restlet.routing.Filter;
import org.restlet.service.EncoderService;
import org.restlet.service.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Configures a proxy for your own application and provides some services hosted
 * by the APISpark platform such as analytics, security.
 * 
 * The service could be configured by a property file with the
 * {@link #loadConfiguration()} method.
 * 
 * @author Cyprien Quilici
 * @author Manuel Boillod
 */
public class ApiSparkService extends Service {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(ApiSparkService.class
            .getName());

    /** The URL of the remote service used by default. */
    public static final String DEFAULT_AGENT_SERVICE_URL = "https://apispark.restlet.com";

    /** The system property key for agent configuration file. */
    public static final String CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY = "apiSparkServiceConfig";

    /** The filter performing the services */
    private ApiSparkFilter apiSparkFilter;

    /** Indicates if the APISpark agent is enabled.*/
    private boolean agentEnabled;

    /** The password used to connect to the APISpark platform. */
    private char[] agentPassword;

    /** The url of the APISpark service. */
    private String agentServiceUrl = DEFAULT_AGENT_SERVICE_URL;

    /**
     * The maximum size of the stored analytics. When this threshold is
     * exceeded, an asynchronous task is triggered to send them to the APISpark
     * service.
     */
    private int agentAnalyticsBufferSize =  100;

    /**
     * The period in seconds of the timer triggering the asynchronous post
     * of analytics to the APISpark service.
     */
    private long agentAnalyticsPostPeriodInSecond = 60;

    /** The timer that triggers agent re-configuration */
    private Timer agentRefreshTimer;

    /** Agent refresh period in seconds */
    private long agentRefreshPeriodInSecond = TimeUnit.MINUTES.toSeconds(15);

    /** The login used to connect to the APISpark platform. */
    private String agentLogin;

    /**
     * The identifier of the cell configured on the APISpark platform for your
     * application.
     */
    private Integer agentCellId;

    /**
     * The identifier of the cell version configured on the APISpark platform
     * for your application.
     */
    private Integer agentCellVersion;

    /**
     * The list of associated
     * {@link org.restlet.ext.apispark.internal.firewall.rule.FirewallRule}.
     */
    private List<FirewallRule> firewallRules = new ArrayList<>();

    /**
     * Indicates if the firewall is enabled. Add firewall rules with
     * {@link #firewallConfig}.
     */
    private boolean firewallEnabled;

    /**
     * Firewall configuration
     */
    private FirewallConfig firewallConfig = new FirewallConfig(firewallRules);

    /**
     * Indicates if the request redirection is enabled. If true, the
     * {@link #reverseProxyTargetUrl} should be set.
     */
    private boolean reverseProxyEnabled;

    /**
     * The redirection URL. Used if {@link #reverseProxyEnabled} is true.
     */
    private String reverseProxyTargetUrl;

    /**
     * Default constructor.
     */
    public ApiSparkService() {
        super(true);
    }

    /**
     * Constructor using the default APISpark service url.
     * 
     * @param agentLogin
     *            The login used to connect to the APISpark platform.
     * @param agentPassword
     *            The password used to connect to the APISpark platform.
     * @param agentCellId
     *            The identifier of the cell configured on the APISpark platform
     *            for your application.
     * @param agentCellVersion
     *            The identifier of the cell version configured on the APISpark
     *            platform for your application.
     * @param reverseProxyEnabled
     *            Indicates if the request redirection is enabled.
     * @param reverseProxyTargetUrl
     *            The redirection URL.
     */
    public ApiSparkService(String agentLogin, char[] agentPassword,
            Integer agentCellId, Integer agentCellVersion,
            boolean reverseProxyEnabled, String reverseProxyTargetUrl) {
        this(DEFAULT_AGENT_SERVICE_URL, agentLogin, agentPassword, agentCellId,
                agentCellVersion, reverseProxyEnabled, reverseProxyTargetUrl);
    }

    /**
     * Constructor.
     * 
     * @param agentServiceUrl
     *            The url of the APISpark service.
     * @param agentLogin
     *            The login used to connect to the APISpark platform.
     * @param agentPassword
     *            The password used to connect to the APISpark platform.
     * @param agentCellId
     *            The identifier of the cell configured on the APISpark platform
     *            for your application.
     * @param agentCellVersion
     *            The identifier of the cell version configured on the APISpark
     *            platform for your application.
     * @param reverseProxyEnabled
     *            Indicates if the request redirection is enabled.
     * @param reverseProxyTargetUrl
     *            The redirection URL.
     */
    public ApiSparkService(String agentServiceUrl, String agentLogin,
            char[] agentPassword, Integer agentCellId,
            Integer agentCellVersion, boolean reverseProxyEnabled,
            String reverseProxyTargetUrl) {
        super(true);
        this.agentPassword = agentPassword;
        this.agentServiceUrl = agentServiceUrl;
        this.agentLogin = agentLogin;
        this.agentCellId = agentCellId;
        this.agentCellVersion = agentCellVersion;
        this.reverseProxyEnabled = reverseProxyEnabled;
        this.reverseProxyTargetUrl = reverseProxyTargetUrl;
    }

    @Override
    public Filter createInboundFilter(Context context) {
        ApiSparkConfig apiSparkConfig = new ApiSparkConfig();
        apiSparkConfig.setAgentCellId(agentCellId);
        apiSparkConfig.setAgentCellVersion(agentCellVersion);
        apiSparkConfig.setAgentServiceUrl(agentServiceUrl);
        apiSparkConfig.setAgentLogin(agentLogin);
        apiSparkConfig.setAgentPassword(agentPassword);
        apiSparkConfig.setReverseProxyEnabled(reverseProxyEnabled);
        apiSparkConfig.setReverseProxyTargetUrl(reverseProxyTargetUrl);
        apiSparkConfig.setAgentAnalyticsBufferSize(agentAnalyticsBufferSize);
        apiSparkConfig.setAgentAnalyticsPostPeriodInSecond(agentAnalyticsPostPeriodInSecond);

        apiSparkFilter = new ApiSparkFilter(context, apiSparkConfig,
                agentEnabled, firewallEnabled, firewallRules);

        if (agentEnabled && agentRefreshPeriodInSecond > 0) {
            TimerTask task = new AgentConfigurationTimerTask(apiSparkFilter);
            agentRefreshTimer = new Timer(true);
            long agentRefreshPeriodInMs = TimeUnit.SECONDS.toMillis(agentRefreshPeriodInSecond);
            agentRefreshTimer.scheduleAtFixedRate(task, agentRefreshPeriodInMs,
                    agentRefreshPeriodInMs);
            LOGGER.info("Setting agent refresh timer every "
                    + TimeUnit.SECONDS.toMinutes(agentRefreshPeriodInSecond)
                    + " minutes");
        }

        return apiSparkFilter;
    }

    @Override
    public Filter createOutboundFilter(Context context) {
        Encoder encoder = new Encoder(context, true, false,
                new EncoderService());
        return encoder;
    }

    /**
     * Returns the agent Analytics module buffer size.
     * 
     * @return The agent Analytics module buffer size.
     */
    public int getAgentAnalyticsBufferSize() {
        return agentAnalyticsBufferSize;
    }

    /**
     * Returns the agent Analytics module post period.
     * 
     * @return The agent Analytics module post period.
     */
    public long getAgentAnalyticsPostPeriodInSecond() {
        return agentAnalyticsPostPeriodInSecond;
    }

    /**
     * Returns the password used to connect to the APISpark platform.
     * 
     * @return The password used to connect to the APISpark platform.
     */
    public String getAgentPassword() {
        return new String(agentPassword);
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
     * Returns the agent refresh period in seconds
     * 
     * @return The agent refresh period in seconds
     */
    public long getAgentRefreshPeriodInSecond() {
        return agentRefreshPeriodInSecond;
    }

    /**
     * Returns the login used to connect to the APISpark platform.
     * 
     * @return The login used to connect to the APISpark platform.
     */
    public String getAgentLogin() {
        return agentLogin;
    }

    /**
     * Returns the identifier of the cell configured on the APISpark platform
     * for your application.
     * 
     * @return The identifier of the cell configured on the APISpark platform
     *         for your application.
     */
    public Integer getAgentCellId() {
        return agentCellId;
    }

    /**
     * Returns the identifier of the cell version configured on the APISpark
     * platform for your application.
     * 
     * @return The identifier of the cell version configured on the APISpark
     *         platform for your application.
     */
    public Integer getAgentCellVersion() {
        return agentCellVersion;
    }

    public FirewallConfig getFirewallConfig() {
        return firewallConfig;
    }

    /**
     * Returns the redirection URL. Used if {@link #isReverseProxyEnabled()}
     * returns true.
     * 
     * @return The redirection URL.
     */
    public String getReverseProxyTargetUrl() {
        return reverseProxyTargetUrl;
    }

    /**
     * Indicates if the APISpark agent is enabled.
     * 
     * @return True if the APISpark agent is enabled.
     */
    public boolean isAgentEnabled() {
        return agentEnabled;
    }

    /**
     * Indicates if the firewall is enabled. Add firewall rules with
     * {@link #firewallConfig}.
     * 
     * @return True if the firewall is enabled.
     */
    public boolean isFirewallEnabled() {
        return firewallEnabled;
    }

    /**
     * Indicates if the request redirection is enabled. If true, the redirection
     * URL should be set with {@link #setReverseProxyTargetUrl(String)}.
     * 
     * @return True if the request redirection is enabled.
     */
    public boolean isReverseProxyEnabled() {
        return reverseProxyEnabled;
    }

    /**
     * Load the agent configuration from the file set by the system property
     * 'apiSparkServiceConfig'.
     * 
     * @see #CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY
     */
    public void loadConfiguration() {
        String configurationFile = System
                .getProperty(CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY);
        if (configurationFile == null) {
            throw new IllegalArgumentException(
                    "Agent configuration file is not set. "
                            + "Use system property '"
                            + CONFIGURATION_FILE_SYSTEM_PROPERTY_KEY
                            + "' to define it.");
        }

        loadConfiguration(new File(configurationFile));
    }

    /**
     * Load the agent configuration from the file.
     * 
     * @param configurationFile
     *            The configuration file.
     */
    public void loadConfiguration(File configurationFile) {
        if (configurationFile == null) {
            throw new IllegalArgumentException(
                    "APISpark configuration file is null.");
        }
        if (!configurationFile.exists()) {
            throw new IllegalArgumentException(
                    "APISpark configuration file does not exist: "
                            + configurationFile.getAbsolutePath());
        }
        try {
            loadConfiguration(new FileInputStream(configurationFile));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                    "APISpark configuration file error. See exception for details.",
                    e);
        }
    }

    /**
     * Load the agent configuration from the input stream.
     * 
     * @param inputStream
     *            The input stream of the configuration file.
     */
    public void loadConfiguration(InputStream inputStream) {
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "APISpark configuration file error. See exception for details.",
                    e);
        }
        this.agentServiceUrl = properties.getProperty("agent.serviceUrl",
                DEFAULT_AGENT_SERVICE_URL);
        this.agentLogin = properties.getProperty("agent.login");
        this.agentPassword = getRequiredProperty(properties, "agent.password")
                .toCharArray();
        this.agentCellId = getRequiredIntegerProperty(properties,
                "agent.cellId");
        this.agentCellVersion = getRequiredIntegerProperty(properties,
                "agent.cellVersion");
        this.reverseProxyEnabled = Boolean.valueOf(getRequiredProperty(
                properties, "reverseProxy.enabled"));
        if (this.reverseProxyEnabled) {
            this.reverseProxyTargetUrl = getRequiredProperty(properties,
                    "reverseProxy.targetUrl");
        }
    }

    private String getRequiredProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (StringUtils.isNullOrEmpty(value)) {
            throw new IllegalArgumentException(
                    "APISpark configuration file error. The property '" + key
                            + "' is required");
        }
        return value;
    }

    private Integer getRequiredIntegerProperty(Properties properties, String key) {
        String value = getRequiredProperty(properties, key);
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "APISpark configuration file error. The property '" + key
                            + "' should be a number", e);
        }
    }

    /**
     * Sets the agent Analytics module buffer size.
     * 
     * @param agentAnalyticsBufferSize
     *            The agent Analytics module buffer size.
     */
    public void setAgentAnalyticsBufferSize(int agentAnalyticsBufferSize) {
        this.agentAnalyticsBufferSize = agentAnalyticsBufferSize;
    }

    /**
     * Sets the agent Analytics module post period.
     * 
     * @param agentAnalyticsPostPeriodInSecond
     *            The agent Analytics module post period.
     */
    public void setAgentAnalyticsPostPeriodInSecond(long agentAnalyticsPostPeriodInSecond) {
        this.agentAnalyticsPostPeriodInSecond = agentAnalyticsPostPeriodInSecond;
    }

    /**
     * Sets the password used to connect to the APISpark platform.
     * 
     * @param agentPassword
     *            The password used to connect to the APISpark platform.
     */
    public void setAgentPassword(String agentPassword) {
        this.agentPassword = agentPassword != null ? agentPassword
                .toCharArray() : null;
    }

    /**
     * Sets the url of the APISpark agent service.
     * 
     * @param agentServiceUrl
     *            The url of the APISpark agent service.
     */
    public void setAgentServiceUrl(String agentServiceUrl) {
        this.agentServiceUrl = agentServiceUrl;
    }

    /**
     * Sets the agent refresh period
     * 
     * @param agentRefreshPeriodInSecond
     *            The agent refresh period in seconds
     * 
     */
    public void setAgentRefreshPeriodInSecond(long agentRefreshPeriodInSecond) {
        this.agentRefreshPeriodInSecond = agentRefreshPeriodInSecond;
    }

    /**
     * Sets the login used to connect to the APISpark platform.
     * 
     * @param agentLogin
     *            The login used to connect to the APISpark platform.
     */
    public void setAgentLogin(String agentLogin) {
        this.agentLogin = agentLogin;
    }

    /**
     * Sets the identifier of the cell configured on the APISpark platform for
     * your application.
     * 
     * @param agentCellId
     *            The identifier of the cell configured on the APISpark platform
     *            for your application.
     */
    public void setAgentCellId(Integer agentCellId) {
        this.agentCellId = agentCellId;
    }

    /**
     * Sets the identifier of the cell version configured on the APISpark
     * platform for your application.
     * 
     * @param agentCellVersion
     *            The version of the cell configured on the APISpark platform
     *            for your application.
     */
    public void setAgentCellVersion(Integer agentCellVersion) {
        this.agentCellVersion = agentCellVersion;
    }

    /**
     * Indicates if the APISpark agent is enabled.
     * 
     * @param agentEnabled
     *            True if the APISpark agent is enabled.
     */
    public void setAgentEnabled(boolean agentEnabled) {
        this.agentEnabled = agentEnabled;
    }

    /**
     * Indicates if the firewall is enabled.
     * 
     * @param firewallEnabled
     *            True if the firewall is enabled.
     */
    public void setFirewallEnabled(boolean firewallEnabled) {
        this.firewallEnabled = firewallEnabled;
    }

    /**
     * Indicates if the reverse proxy is enabled. If true, the target URL should
     * be set with {@link #setReverseProxyTargetUrl(String)}.
     * 
     * @param reverseProxyEnabled
     *            True if the reverse proxy is enabled.
     */
    public void setReverseProxyEnabled(boolean reverseProxyEnabled) {
        this.reverseProxyEnabled = reverseProxyEnabled;
    }

    /**
     * Set the target URL of the reverse proxy. Used if
     * {@link #isReverseProxyEnabled()} is true.
     * 
     * @param reverseProxyTargetUrl
     *            The target URL.
     */
    public void setReverseProxyTargetUrl(String reverseProxyTargetUrl) {
        this.reverseProxyTargetUrl = reverseProxyTargetUrl;
    }

    /** Stops the service and its timer */
    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        if (agentRefreshTimer != null) {
            agentRefreshTimer.cancel();
        }
    }
}
