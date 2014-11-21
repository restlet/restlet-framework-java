package org.restlet.ext.apispark.internal.agent.module;

import org.restlet.ext.apispark.internal.ApiSparkConfig;
import org.restlet.ext.apispark.internal.agent.AgentConfigurationException;
import org.restlet.ext.apispark.internal.agent.AgentUtils;
import org.restlet.ext.apispark.internal.agent.bean.ModulesSettings;
import org.restlet.ext.apispark.internal.agent.resource.ModulesSettingsResource;

/**
 * Get modules settings from connector service
 * 
 * @author Manuel Boillod
 */
public class ModulesSettingsModule {

    public static final String MODULE_PATH = "/settings";

    private ModulesSettings modulesSettings;

    public ModulesSettingsModule(ApiSparkConfig apiSparkConfig) {
        ModulesSettingsResource modulesSettingsResource = AgentUtils
                .getClientResource(apiSparkConfig, null,
                        ModulesSettingsResource.class, MODULE_PATH);
        try {
            modulesSettings = modulesSettingsResource.getSettings();
        } catch (Exception e) {
            throw new AgentConfigurationException("Unable to retrieve agent settings from apispark", e);
        }
    }

    public ModulesSettings getModulesSettings() {
        return modulesSettings;
    }
}
