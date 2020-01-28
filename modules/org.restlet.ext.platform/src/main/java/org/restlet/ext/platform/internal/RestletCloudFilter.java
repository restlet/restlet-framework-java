/**
 * Copyright 2005-2020 Talend
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
 * https://restlet.com/open-source/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.platform.internal;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.platform.FirewallConfig;
import org.restlet.ext.platform.internal.agent.AgentConfigurationException;
import org.restlet.ext.platform.internal.agent.bean.ModulesSettings;
import org.restlet.ext.platform.internal.agent.module.AnalyticsModule;
import org.restlet.ext.platform.internal.agent.module.AuthenticationModule;
import org.restlet.ext.platform.internal.agent.module.AuthorizationModule;
import org.restlet.ext.platform.internal.agent.module.FirewallModule;
import org.restlet.ext.platform.internal.agent.module.ModulesSettingsModule;
import org.restlet.ext.platform.internal.agent.module.ReverseProxyModule;
import org.restlet.ext.platform.internal.firewall.FirewallFilter;
import org.restlet.ext.platform.internal.firewall.rule.FirewallRule;
import org.restlet.ext.platform.internal.utils.RestletChain;
import org.restlet.routing.Filter;
import org.restlet.routing.Redirector;

public class RestletCloudFilter extends Filter {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(RestletCloudFilter.class
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

    /** Restlet Cloud connection configuration */
    private RestletCloudConfig restletCloudConfig;

    /** Restlet Cloud connection enabled */
    private boolean agentEnabled;

    /** Restlet context */
    private Context context;

    /**
     * Create a new RestletCloudFilter with the specified configuration.
     * 
     * @param context
     *            The current context.
     * @param restletCloudConfig
     *            The RestletCloudService configuration.
     */
    public RestletCloudFilter(Context context, RestletCloudConfig restletCloudConfig,
                              boolean agentEnabled, boolean firewallEnabled,
                              List<FirewallRule> firewallRules) {
        super(context);
        this.firewallRules = firewallRules;
        this.firewallEnabled = firewallEnabled;
        this.restletCloudConfig = restletCloudConfig;
        this.agentEnabled = agentEnabled;
        this.context = context;

        validateRedirection(restletCloudConfig);

        if (agentEnabled) {
            validateAgentConfiguration(restletCloudConfig);
            modulesSettings = ModulesSettingsModule.getModulesSettings(
                    restletCloudConfig, null);
        }

        refreshRestletCloudFilter();

    }

    /**
     * Retrieves the modules settings from the service and updates the filter.
     */
    public void refreshRestletCloudFilterIfRevisionChanged() {

        try {
            ModulesSettings newModulesSettings = ModulesSettingsModule
                    .getModulesSettings(restletCloudConfig, modulesSettings);

            if (newModulesSettings != null) {
                modulesSettings = newModulesSettings;

                LOGGER.info("Updating modules settings");
                refreshRestletCloudFilter();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING,
                    "Unable to retrieve agent settings from platform", e);
        }
    }

    /**
     * Updates the filter with its modules settings.
     */
    public void refreshRestletCloudFilter() {

        boolean authenticationEnabled = false;
        RestletChain restletChain = new RestletChain();

        if (agentEnabled) {

            if (modulesSettings.isAuthenticationModuleEnabled()) {
                LOGGER.info("Add authentication module");
                authenticationEnabled = true;
                restletChain.add(new AuthenticationModule(restletCloudConfig,
                        modulesSettings, context));
            }
            if (modulesSettings.isAuthorizationModuleEnabled()) {
                if (!modulesSettings.isAuthenticationModuleEnabled()) {
                    throw new AgentConfigurationException(
                            "The authorization module requires the authentication module which is not enabled");
                }
                LOGGER.info("Add authorization module");
                restletChain.add(new AuthorizationModule(restletCloudConfig,
                        modulesSettings, context));
            }

            if (modulesSettings.isFirewallModuleEnabled()) {
                firewallEnabled = true;
                FirewallModule firewallModule = new FirewallModule(
                        restletCloudConfig, modulesSettings);
                FirewallConfig firewallConfig = new FirewallConfig(
                        firewallRules);
                firewallModule.updateFirewallConfig(firewallConfig);
            }

            if (modulesSettings.isAnalyticsModuleEnabled()) {
                LOGGER.info("Add analytics module");
                restletChain.add(new AnalyticsModule(restletCloudConfig,
                        modulesSettings, context));
            }
        }

        if (firewallEnabled) {
            LOGGER.info("Add firewall module");
            restletChain.add(new FirewallFilter(context, firewallRules));
        }

        if (restletCloudConfig.isReverseProxyEnabled()) {
            LOGGER.info("Add redirection module");
            String redirectorUrl = restletCloudConfig.getReverseProxyTargetUrl()
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
    public void validateAgentConfiguration(RestletCloudConfig config) {
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
    public void validateRedirection(RestletCloudConfig config) {
        if (config.isReverseProxyEnabled()
                && StringUtils.isNullOrEmpty(config.getReverseProxyTargetUrl())) {
            throw new IllegalArgumentException(
                    "The redirection url is mandatory when redirection is enabled");
        }
    }
}
