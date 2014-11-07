package org.restlet.ext.apispark.internal.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.module.AnalyticsModule;
import org.restlet.ext.apispark.internal.agent.module.AuthenticationModule;
import org.restlet.ext.apispark.internal.agent.module.AuthorizationModule;
import org.restlet.ext.apispark.internal.agent.module.FirewallModule;
import org.restlet.routing.Filter;

/**
 * Utilities for creating agents modules.
 * 
 * @author Manuel Boillod
 */
public class AgentModulesHelper {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(AgentModulesHelper.class
            .getName());

    /**
     * Creates each agent module enabled by the settings.
     * 
     * @param agentConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     * @param context
     *            The current context.
     */
    public static AgentModulesConfigurer buildFromSettings(
            AgentConfig agentConfig, ModulesSettings modulesSettings,
            Context context) {
        List<Filter> filters = new ArrayList<>();

        if (modulesSettings.isAuthenticationModuleEnabled()) {
            LOGGER.info("Add authentication module");
            filters.add(new AuthenticationModule(agentConfig, modulesSettings,
                    context));
        }
        if (modulesSettings.isAuthorizationModuleEnabled()) {
            if (!modulesSettings.isAuthenticationModuleEnabled()) {
                throw new AgentConfigurationException(
                        "The authorization module requires the authentication module which is not enabled");
            }
            LOGGER.info("Add authorization module");
            filters.add(new AuthorizationModule(agentConfig, modulesSettings,
                    context));
        }
        if (modulesSettings.isFirewallModuleEnabled()) {
            LOGGER.info("Add firewall module");
            filters.add(new FirewallModule(agentConfig, modulesSettings,
                    context));
        }
        if (modulesSettings.isAnalyticsModuleEnabled()) {
            LOGGER.info("Add analytics module");
            filters.add(new AnalyticsModule(agentConfig, modulesSettings,
                    context));
        }

        if (filters.isEmpty()) {
            LOGGER.warning("No modules are enabled.");
        }

        return new AgentModulesConfigurer(filters);
    }
}
