package org.restlet.ext.apispark.internal.agent;

import org.restlet.Context;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.module.AnalyticsModule;
import org.restlet.ext.apispark.internal.agent.module.AuthenticationModule;
import org.restlet.ext.apispark.internal.agent.module.AuthorizationModule;
import org.restlet.ext.apispark.internal.agent.module.FirewallModule;
import org.restlet.ext.apispark.internal.utils.RestletChain;
import org.restlet.routing.Redirector;

import java.net.URI;
import java.util.logging.Logger;

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
     *  @param agentConfig
     *            The agent configuration.
     * @param modulesSettings
     *            The modules settings.
     * @param context
     *            The context.
     */
    public static RestletChain buildFromSettings(
            AgentConfig agentConfig, ModulesSettings modulesSettings,
            Context context) {
        RestletChain restletChain = new RestletChain();

        if (modulesSettings.isAuthenticationModuleEnabled()) {
            LOGGER.info("Add authentication module");
            restletChain.add(new AuthenticationModule(agentConfig, modulesSettings,
                    context));
        }
        if (modulesSettings.isAuthorizationModuleEnabled()) {
            if (!modulesSettings.isAuthenticationModuleEnabled()) {
                throw new AgentConfigurationException(
                        "The authorization module requires the authentication module which is not enabled");
            }
            LOGGER.info("Add authorization module");
            restletChain.add(new AuthorizationModule(agentConfig, modulesSettings,
                    context));
        }
        if (modulesSettings.isFirewallModuleEnabled()) {
            LOGGER.info("Add firewall module");
            restletChain.add(new FirewallModule(agentConfig, modulesSettings,
                    context));
        }
        if (modulesSettings.isAnalyticsModuleEnabled()) {
            LOGGER.info("Add analytics module");
            restletChain.add(new AnalyticsModule(agentConfig, modulesSettings,
                    context));
        }

        if (restletChain.getFirst() == null) {
            LOGGER.warning("No modules are enabled.");
        }

        if (agentConfig.isRedirectionEnabled()) {
            LOGGER.info("Add redirection module");
            String redirectorUrl = agentConfig.getRedirectionUrl() + "{rr}";
            Redirector redirector = new Redirector(context, redirectorUrl, Redirector.MODE_SERVER_OUTBOUND);
            restletChain.add(redirector);
        }

        return restletChain;
    }
}
