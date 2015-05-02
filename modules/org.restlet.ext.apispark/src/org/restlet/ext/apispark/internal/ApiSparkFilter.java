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

package org.restlet.ext.apispark.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.FirewallConfig;
import org.restlet.ext.apispark.internal.agent.AgentConfigurationException;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.module.AnalyticsModule;
import org.restlet.ext.apispark.internal.agent.module.AuthenticationModule;
import org.restlet.ext.apispark.internal.agent.module.AuthorizationModule;
import org.restlet.ext.apispark.internal.agent.module.FirewallModule;
import org.restlet.ext.apispark.internal.agent.module.ModulesSettingsModule;
import org.restlet.ext.apispark.internal.agent.module.ReverseProxyModule;
import org.restlet.ext.apispark.internal.firewall.FirewallFilter;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallRule;
import org.restlet.ext.apispark.internal.utils.RestletChain;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;

public class ApiSparkFilter extends Filter {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(ApiSparkFilter.class
            .getName());

    /** Default next restlet of filter. */
    private Restlet agentFirstRestlet;

    /** First restlet of the agent filter. */
    private Restlet filterNext;

    /** Last restlet of the agent filter. */
    private Restlet agentLastRestlet;

    /** Modules settings of the agent */
    private ModulesSettings modulesSettings;

    /** List of firewall rules set by user at creation */
    private List<FirewallRule> firewallRules;

    /** Is user set firewall enabled */
    private boolean firewallEnabled;

    /** APISpark connection configuration */
    private ApiSparkConfig apiSparkConfig;

    /** APISpark connection enabled */
    private boolean agentEnabled;

    /** Restlet context */
    private Context context;

    /**
     * Create a new ApiSparkFilter with the specified configuration.
     * 
     * @param context
     *            The current context.
     * @param apiSparkConfig
     *            The ApiSparkService configuration.
     */
    public ApiSparkFilter(Context context, ApiSparkConfig apiSparkConfig,
            boolean agentEnabled, boolean firewallEnabled,
            List<FirewallRule> firewallRules) {
        super(context);
        this.firewallRules = firewallRules;
        this.firewallEnabled = firewallEnabled;
        this.apiSparkConfig = apiSparkConfig;
        this.agentEnabled = agentEnabled;
        this.context = context;

        validateRedirection(apiSparkConfig);

        if (agentEnabled) {
            validateAgentConfiguration(apiSparkConfig);
            modulesSettings = ModulesSettingsModule.getModulesSettings(
                    apiSparkConfig, null);
        }

        refreshApiSparkFilter();

    }

    /**
     * Retrieves the modules settings from the service and updates the filter.
     */
    public void refreshApiSparkFilterIfRevisionChanged() {

        try {
            ModulesSettings newModulesSettings = ModulesSettingsModule
                    .getModulesSettings(apiSparkConfig, modulesSettings);

            if (newModulesSettings != null) {
                modulesSettings = newModulesSettings;

                LOGGER.info("Updating modules settings");
                refreshApiSparkFilter();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Unable to retrieve agent settings from apispark", e);
        }
    }

    /**
     * Updates the filter with its modules settings.
     */
    public void refreshApiSparkFilter() {

        boolean authenticationEnabled = false;
        RestletChain restletChain = new RestletChain();

        if (agentEnabled) {

            if (modulesSettings.isAuthenticationModuleEnabled()) {
                LOGGER.info("Add authentication module");
                authenticationEnabled = true;
                restletChain.add(new AuthenticationModule(apiSparkConfig,
                        modulesSettings, context));
            }
            if (modulesSettings.isAuthorizationModuleEnabled()) {
                if (!modulesSettings.isAuthenticationModuleEnabled()) {
                    throw new AgentConfigurationException(
                            "The authorization module requires the authentication module which is not enabled");
                }
                LOGGER.info("Add authorization module");
                restletChain.add(new AuthorizationModule(apiSparkConfig,
                        modulesSettings, context));
            }

            if (modulesSettings.isFirewallModuleEnabled()) {
                firewallEnabled = true;
                FirewallModule firewallModule = new FirewallModule(
                        apiSparkConfig, modulesSettings);
                FirewallConfig firewallConfig = new FirewallConfig(
                        firewallRules);
                firewallModule.updateFirewallConfig(firewallConfig);
            }

            if (modulesSettings.isAnalyticsModuleEnabled()) {
                LOGGER.info("Add analytics module");
                restletChain.add(new AnalyticsModule(apiSparkConfig,
                        modulesSettings, context));
            }
        }

        if (firewallEnabled) {
            LOGGER.info("Add firewall module");
            restletChain.add(new FirewallFilter(context, firewallRules));
        }

        if (apiSparkConfig.isReverseProxyEnabled()) {
            LOGGER.info("Add redirection module");
            String redirectorUrl = apiSparkConfig.getReverseProxyTargetUrl()
                    + "{rr}";
            Redirector redirector = new ReverseProxyModule(context,
                    redirectorUrl, authenticationEnabled);
            restletChain.add(redirector);
        }

        if (restletChain.getFirst() == null) {
            LOGGER.warning("No modules are enabled.");
        }

        agentFirstRestlet = restletChain.getFirst();
        agentLastRestlet = restletChain.getLast();
    }

    @Override
    public Restlet getNext() {
        return agentFirstRestlet != null ? agentFirstRestlet : filterNext;
    }

    @Override
    public void setNext(Restlet next) {
        filterNext = next;
        // If the agent has any restlet components, set the next on the last
        // one.
        if (agentLastRestlet != null) {
            if (agentLastRestlet instanceof Filter) {
                Filter filter = (Filter) agentLastRestlet;
                filter.setNext(next);
            }
        }
    }

    /**
     * Terminates the agent if the service's configuration is not correctly set.
     * 
     * @param config
     *            The service's configuration
     */
    public void validateAgentConfiguration(ApiSparkConfig config) {
        if (config.getAgentCellId() == null) {
            throw new IllegalArgumentException(
                    "The cell identifier is mandatory");
        }
        if (config.getAgentCellId() == null) {
            throw new IllegalArgumentException(
                    "The cell version identifier is mandatory");
        }
        if (StringUtils.isNullOrEmpty(config.getAgentServiceUrl())) {
            throw new IllegalArgumentException(
                    "The agent service url is mandatory");
        }
        if (StringUtils.isNullOrEmpty(config.getAgentLogin())) {
            throw new IllegalArgumentException("The agent login is mandatory");
        }
        if (StringUtils.isNullOrEmpty(config.getAgentPassword())) {
            throw new IllegalArgumentException(
                    "The agent password key is mandatory");
        }
    }

    /**
     * Validates the redirection URL
     * 
     * @param config
     *            The service's configuration
     */
    public void validateRedirection(ApiSparkConfig config) {
        if (config.isReverseProxyEnabled()
                && StringUtils.isNullOrEmpty(config.getReverseProxyTargetUrl())) {
            throw new IllegalArgumentException(
                    "The redirection url is mandatory when redirection is enabled");
        }
    }
}
